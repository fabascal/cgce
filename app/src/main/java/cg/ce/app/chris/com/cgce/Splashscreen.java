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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

import cg.ce.app.chris.com.cgce.ControlGas.GetDevicePermissions;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetCorte;
import cg.ce.app.chris.com.cgce.common.Variables;

public class Splashscreen extends Activity {
    JSONObject cursor=null;
    String msg=null;
    private VersionChecker mVC = new VersionChecker();
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    ValidarDispositivo  mac_add=new ValidarDispositivo();
    LogCE logCE = new LogCE();

    Drawable icon,background;
    ConstraintLayout main;
    ImageView logo;
    Animation fromtop;
    TextView version,proceso;

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
        BrandSharedPreferences();


        sensores.bluetooth();
        sensores.wifi(this,true);


        logo = (ImageView) findViewById(R.id.logo);
        main = (ConstraintLayout) findViewById(R.id.main);
        version = (TextView) findViewById(R.id.version);
        proceso = (TextView) findViewById(R.id.proceso);
        logo.setImageDrawable(icon);
        main.setBackground(background);
        fromtop = AnimationUtils.loadAnimation(this, R.anim.fromtop);
        logo.setAnimation(fromtop);
        fromtop.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                SetCurrentVersionName();
                Validations();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        /*Intent intento_venta = new Intent().setClass(Splashscreen.this, VentaActivity.class);
        startActivity(intento_venta);*/
        /*StartAnimations();*/
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
                    /*while (waited < 1000) {
                        sleep(100);
                        waited += 100;
                    }*/
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
                } catch ( ClassNotFoundException | SQLException |
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

    /*Funciones nuevas android 9*/
    public void SetCurrentVersionName(){

        try {
            PackageInfo pckginfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            version.setText("V "+pckginfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void SetCurrentProces(String proces){
        proceso.setText(proces);
    }
    public void Validations(){
        Intent intent;
        if (!HasCursor()){
            intent = new Intent(Splashscreen.this, LoginSistemasActivity.class);
            intent.putExtra("msg", msg);
            startActivity(intent);
        }
        else if (!HasDevicePermissions()){
            intent = new Intent(Splashscreen.this, LoginSistemasActivity.class);
            intent.putExtra("msg", msg);
            startActivity(intent);
        }
        else if (!HasCorte()){
            intent = new Intent(Splashscreen.this, Login_Despachador.class);
            intent.putExtra("msg", msg);
            startActivity(intent);
        }else if(mVC.isNewVersionAvailable()){
            if(mVC.getMandatory().equals("0")){
                intent = new Intent(Splashscreen.this, VentaActivity.class);
                String msj="Actualizacion disponible ("+mVC.getLatestVersionName()+")";
                intent.putExtra("msj",msj);
                startActivity(intent);
            }else{
                intent = new Intent(Splashscreen.this, AutoUpdate.class);
                startActivity(intent);
            }
        }else{
            intent = new Intent(Splashscreen.this, VentaActivity.class);
            startActivity(intent);
        }
    }
    public boolean HasCursor(){
        SetCurrentProces("Validando datos de ODBC...");
        DataBaseManager manager = new DataBaseManager(getApplicationContext());
        cursor = manager.cargarcursorodbc2();
        if (!cursor.has("ip")) {
            msg="Falta ODBC";
            return false;
        }else{
            return true;
        }
    }
    public boolean HasDevicePermissions(){
        SetCurrentProces("Validando el dispositivo ...");
        GetDevicePermissions getDevicePermissions  = new GetDevicePermissions(this);
        try {
            Log.w("MAC",mac_add.getMacAddress());
            Log.w("MAC", String.valueOf(mac_add.getMacAddress().length()));
            JSONObject js = getDevicePermissions.execute(mac_add.getMacAddress()).get();
            if (js.has(Variables.CODE_ERROR)){
                Log.w(Variables.CODE_ERROR, String.valueOf(js.getInt(Variables.CODE_ERROR)));
                Log.w(Variables.DEVICE, String.valueOf(js.getInt(Variables.DEVICE)));
                if (js.getInt(Variables.CODE_ERROR)==0){
                    if (js.getInt(Variables.DEVICE)==1){
                        return true;
                    }else if(js.getInt(Variables.DEVICE)==0){
                        msg = "Dispositivo sin permiso";
                        return false;
                    }
                }else if(js.getInt(Variables.CODE_ERROR)==1){
                    msg = "Error en la configuracion";
                    return false;
                }
            }
        } catch (ExecutionException | InterruptedException | JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Splashscreen.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
            return false;
        }
        return false;
    }
    public boolean HasCorte(){
        SetCurrentProces("Validando corte ...");
        GetCorte getCorte = new GetCorte(this);
        try {
            JSONObject js = getCorte.execute(mac_add.getMacAddress()).get();
            Log.w("Json-Corte", String.valueOf(js));
            if (js.getInt(Variables.CODE_ERROR)==0){
                if (js.getInt(Variables.CORTE)==0){
                    return true;
                }
            }else{
                new AlertDialog.Builder(Splashscreen.this)
                        .setTitle(R.string.error)
                        .setMessage(js.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (ExecutionException | InterruptedException | JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Splashscreen.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
        return false;
    }


    /*Funcion para multimarca*/
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_splashscreen);
                icon = getDrawable(R.drawable.logobienvenida);
                background = getDrawable(R.drawable.fondoazulbienvenida);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_splashscreen);
                icon = getDrawable(R.drawable.repsol);
                background = getDrawable(R.drawable.fondorepsolazul);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_splashscreen);
                icon = getDrawable(R.drawable.logo_impresion_ener);
                background = getDrawable(R.drawable.fondorepsolazul);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_splashscreen);
                icon = getDrawable(R.drawable.total);
                background = getDrawable(R.drawable.fondorepsolazul);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

}