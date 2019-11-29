package cg.ce.app.chris.com.cgce;

/**
 * Created by chris on 17/01/17.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONObject;

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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensores.bluetooth();
        sensores.wifi(this,true);
        setContentView(R.layout.activity_splashscreen);
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
                    ValidacionFlotillero vf = new ValidacionFlotillero();
                    Boolean flot_activo = vf.isServerReachable("http://189.206.183.110:1390", getApplicationContext());
                    if (flot_activo==true) {
                        mVC.getData(Splashscreen.this);
                    }else{
                        mVC.getDataFalse();
                    }
                    Log.w("mandatory", mVC.getMandatory());
                    Log.w("lates", String.valueOf(mVC.getLatestVersionCode()));
                    Log.w("current", String.valueOf(mVC.getCurrentVersionCode()));
                    Log.w("mvc", String.valueOf(mVC.isNewVersionAvailable()));
                    while (waited < 1500) {
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

                                    e.printStackTrace();
                                }
                            }
                        }
                    }//termina
                    Splashscreen.this.finish();
                } catch (InterruptedException e) {

                    // do nothing
                } finally {
                    Splashscreen.this.finish();
                }

            }
        };
        splashTread.start();

    }
    public int validardisp(Context con){
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
            try {
                conn.close();
            } catch (SQLException e1) {
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

            e.printStackTrace();
        }
        try {
            stmt.close();
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return res;
    }

}