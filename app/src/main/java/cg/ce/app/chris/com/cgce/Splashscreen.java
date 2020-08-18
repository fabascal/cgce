package cg.ce.app.chris.com.cgce;

/**
 * Created by chris on 17/01/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Splashscreen extends Activity {
    JSONObject cursor=null;
    String msg=null;
    private VersionChecker mVC = new VersionChecker();
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    /** Called when the activity is first created. */
    Thread splashTread;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensores.bluetooth();
        sensores.wifi(this,true);
        SharedPreferences sharedPreferences = getSharedPreferences("Brand",Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setContentView(R.layout.activity_splashscreen);
                break;
            case "Repsol":
                setContentView(R.layout.activity_splashscreen_repsol);
                break;
            case "Ener":
                setContentView(R.layout.activity_splashscreen_ener);
                break;
            case "Total":
                setContentView(R.layout.activity_splashscreen_total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        StartAnimations();
    }
    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        final LinearLayout l=(LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.splash);
        iv.clearAnimation();
        iv.startAnimation(anim);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    /*ValidacionFlotillero vf = new ValidacionFlotillero();
                    Boolean flot_activo = vf.isServerReachable("http://189.206.183.110:1390", getApplicationContext());
                    if (flot_activo==true) {

                    }else{
                        mVC.getDataFalse();
                    }*/
                    mVC.getData(Splashscreen.this);

                    Log.w("mandatory", mVC.getMandatory());
                    Log.w("lates", String.valueOf(mVC.getLatestVersionCode()));
                    Log.w("current", String.valueOf(mVC.getCurrentVersionCode()));
                    Log.w("mvc", String.valueOf(mVC.isNewVersionAvailable()));
                    while (waited < 1000) {
                        sleep(100);
                        waited += 100;
                    }
                    if (mVC.isNewVersionAvailable()){

                    }
                    DataBaseManager manager = new DataBaseManager(getApplicationContext());
                    cursor = manager.cargarcursorodbc2();
                    if (!cursor.has("ip")) {
                        msg="Falta ODBC";
                        Intent intento_odbc = new Intent().setClass(Splashscreen.this, LoginSistemasActivity.class);
                        intento_odbc.putExtra("msg",msg);
                        startActivity(intento_odbc);
                    }else{

                        //ValidarDispositivo valdisp = new ValidarDispositivo();
                        Integer validar = validardisp(getApplicationContext());
                        Log.w("Connection", String.valueOf(validar));
                        if (validar==0){
                            String msg ="Dispositivo sin permiso";
                            Intent intento_mac = new Intent().setClass(Splashscreen.this, LoginSistemasActivity.class);
                            intento_mac.putExtra("msg",msg);
                            startActivity(intento_mac);
                        }else if (validar==1) {
                            if (mVC.isNewVersionAvailable()) {
                                if(mVC.getMandatory().equals("0")) {
                                    ValidarCorte vc = new ValidarCorte();
                                    try {
                                        Integer corte = vc.corte(getApplicationContext());
                                        if (corte == 0) {
                                            Intent intento_venta = new Intent().setClass(Splashscreen.this, VentaActivity.class);
                                            String msj="Actualizacion disponible ("+mVC.getLatestVersionName()+")";
                                            intento_venta.putExtra("msj",msj);
                                            startActivity(intento_venta);
                                        } else if (corte == 1) {
                                            Intent login_despachador = new Intent().setClass(Splashscreen.this, Login_Despachador.class);
                                            msg = "Registrar Despachador";
                                            login_despachador.putExtra("msg", msg);
                                            startActivity(login_despachador);
                                        }
                                    } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                                        new AlertDialog.Builder(Splashscreen.this)
                                                .setTitle(R.string.error)
                                                .setMessage(String.valueOf(e))
                                                .setPositiveButton(R.string.btn_ok,null).show();
                                        e.printStackTrace();
                                    }
                                }else{
                                    Intent update = new Intent().setClass(Splashscreen.this, AutoUpdate.class);
                                    startActivity(update);
                                }
                            }else{
                                ValidarCorte vc = new ValidarCorte();
                                try {
                                    Integer corte = vc.corte(getApplicationContext());
                                    if (corte == 0) {
                                        Intent intento_venta = new Intent().setClass(Splashscreen.this, VentaActivity.class);
                                        startActivity(intento_venta);
                                    } else if (corte == 1) {
                                        Intent login_despachador = new Intent().setClass(Splashscreen.this, Login_Despachador.class);
                                        msg = "Registrar Despachador";
                                        login_despachador.putExtra("msg", msg);
                                        startActivity(login_despachador);
                                    }
                                } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                                    new AlertDialog.Builder(Splashscreen.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
                                    e.printStackTrace();
                                }
                            }
                        }
                    }//termina
                    Splashscreen.this.finish();
                } catch (InterruptedException | ClassNotFoundException | SQLException |
                        InstantiationException | JSONException | IllegalAccessException |
                        IOException | PackageManager.NameNotFoundException e) {

                    // do nothing
                }
                finally {
                    Splashscreen.this.finish();
                }

            }
        };
        splashTread.start();

    }
    public int validardisp(Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        ValidarDispositivo  mac_add=new ValidarDispositivo();
        //valor de res 0-sin autorizacion, 1-autorizado
        int res=0;
        String mac = mac_add.getMacAddress();
        Log.w("Mac","Mac: "+mac);
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(getApplication());
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            new AlertDialog.Builder(Splashscreen.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            try {
                conn.close();
            } catch (SQLException e1) {
                new AlertDialog.Builder(Splashscreen.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e1))
                        .setPositiveButton(R.string.btn_ok,null).show();
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        try {
            ResultSet r = stmt.executeQuery("SELECT disp.activo FROM  [cecg_app].[dbo].[dispositivos] as disp where disp.mac_adr = '"+String.valueOf(mac)+"';");
            if (!r.next()){
                res=0;
            }else {
                res=r.getInt(1);
            }
            r.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            new AlertDialog.Builder(Splashscreen.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
        try {
            stmt.close();
        } catch (SQLException e) {
            new AlertDialog.Builder(Splashscreen.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();

            e.printStackTrace();
        }
        return res;
    }

}