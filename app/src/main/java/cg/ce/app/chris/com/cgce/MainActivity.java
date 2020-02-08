package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    JSONObject cursor;

    String msg=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Thread timerThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    DataBaseManager manager = new DataBaseManager(getApplicationContext());
                    cursor = manager.cargarcursorodbc2();
                    if (!cursor.has("ip")) {
                        msg="Falta ODBC";
                        Intent intento_odbc = new Intent().setClass(MainActivity.this, LoginSistemasActivity.class);
                        intento_odbc.putExtra("msg",msg);
                        startActivity(intento_odbc);
                    }else{
                        ValidarDispositivo valdisp = new ValidarDispositivo();
                        Integer validar = valdisp.validardisp(getApplicationContext());
                        Log.w("Connection", String.valueOf(validar));
                        if (validar == 0) {
                            String msg = "Dispositivo sin permiso";
                            Intent intento_mac = new Intent().setClass(MainActivity.this, LoginSistemasActivity.class);
                            intento_mac.putExtra("msg", msg);
                            startActivity(intento_mac);
                        } else if (validar == 1) {
                            Intent intento_venta = new Intent().setClass(MainActivity.this, VentaActivity.class);
                            startActivity(intento_venta);
                        }
                    }


                }
            }
        };
        timerThread.start();
    }
}
