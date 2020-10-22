package cg.ce.app.chris.com.cgce;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cg.ce.app.chris.com.cgce.ControlGas.GetNipManager;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetNipManagerListener;
import cg.ce.app.chris.com.cgce.Fragments.JarreoFullScreenFragment;
import cg.ce.app.chris.com.cgce.common.RequestPermission;
import cg.ce.app.chris.com.cgce.common.Variables;
import cg.ce.app.chris.com.cgce.dialogos.Fragment1;


public class VentaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, GetNipManagerListener {

    SharedPreferences sharedPreferences;
    CardView cardViewContado,cardViewCredito,CardViewServicios,CardViewTPV,cardViewAceite;
    Cursor c;
    EditText contrasena_input;
    TextView msj,error_status;
    ImageView warning;
    private static final boolean DEVELOPER_MODE = true;
    public static final String MSJ="msj";
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    LogCE logCE = new LogCE();
    RequestPermission requestPermission = new RequestPermission();
    Drawable icon;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BrandSharedPreferences();
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        requestPermission.requestRuntimePermission(this);
        sensores.bluetooth();
        sensores.wifi(this,true);
        cardViewContado = findViewById(R.id.CardViewContado);
        cardViewCredito =  findViewById(R.id.CardViewCredito);
        CardViewServicios =  findViewById(R.id.CardViewServicios);
        CardViewTPV =  findViewById(R.id.CardViewTPV);
        cardViewAceite =  findViewById(R.id.CardViewAceite);
        error_status = findViewById(R.id.error_status);
        cardViewContado.setOnClickListener(this);
        cardViewCredito.setOnClickListener(this);
        CardViewServicios.setOnClickListener(this);
        CardViewTPV.setOnClickListener(this);
        cardViewAceite.setOnClickListener(this);
        msj=findViewById(R.id.tvmsj);
        Intent intent= getIntent();
        msj.setText(intent.getStringExtra(MSJ));
        mContext = this;
        if (intent.hasExtra("Error_Status")) {
                error_status.setVisibility(View.VISIBLE);
                //warning.setVisibility(View.VISIBLE);
                error_status.setText(intent.getStringExtra("Error_Status"));
        }

        if (DEVELOPER_MODE) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        DrawerLayout drawer = findViewById(R.id.activity_venta);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.activity_venta);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_sistemas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings2) {
            return true;
        }
        else if (id == R.id.nav_conf_sis) {
            Intent intent_conf = new Intent(VentaActivity.this, LoginSistemasActivity.class);
            String msg="Login Sistemas";
            intent_conf.putExtra("msg",msg);
            startActivity(intent_conf);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent ;
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_printer) {
            intent = new Intent(this, DiscoveryPrinterActivity.class);
            startActivityForResult(intent, 0);
        } else if (id == R.id.nav_conf_sis) {
            Intent intent_conf = new Intent(VentaActivity.this, MainConfiguracionActivity.class);
            startActivity(intent_conf);

        } else if (id == R.id.nav_sesion) {
            Intent sesion = new Intent(VentaActivity.this,Datos_corte.class);
            startActivity(sesion);

        } else if (id == R.id.nav_finalizar) {
            Fragment1 dialogFragment = Fragment1
                    .newInstance("Cierre de Turno");
            dialogFragment.show(getFragmentManager(), "dialog");

        } else if (id==R.id.nav_salir){
           quit();
        } else if(id==R.id.version){
            Intent update = new Intent(VentaActivity.this,AutoUpdate.class);
            startActivity(update);
        }

        DrawerLayout drawer = findViewById(R.id.activity_venta);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void quit() {
        int p = android.os.Process.myPid();
        android.os.Process.killProcess(p);
    }

    public void CallJarreo(MenuItem menuItem){
        /*esta accion muestra un dialogo para corroborar datos*/
        final EditText nipManager = new EditText(this);
        nipManager.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
        nipManager.setTransformationMethod(PasswordTransformationMethod.getInstance());
        nipManager.setFocusable(true);
        new AlertDialog.Builder(VentaActivity.this)
                .setTitle(R.string.jarreo)
                .setMessage(R.string.jarreo_msj)
                .setIcon(icon)
                .setView(nipManager)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*obtenemos y comparamos el nip del gerente almacenado previamente en la base de datos cecg_app*/
                        GetNipManager getNipManager = new GetNipManager(VentaActivity.this,getApplicationContext());
                        getNipManager.delegate = VentaActivity.this;
                        getNipManager.execute(nipManager.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancelar,null).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != requestPermission.REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.CAMERA)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.BLUETOOTH)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), requestPermission.REQUEST_PERMISSION);
        }
    }


    @Override
    public void onClick(View view) {
        Intent intent=null;
        switch (view.getId()) {
            case R.id.CardViewContado:
                intent = new Intent(this,ContadoActivity.class);
                break;
            case R.id.CardViewCredito:
                /*intent = new Intent(this,ActivityCreditoDual.class);*/
                intent = new Intent(this,Credito.class);
                break;
            case R.id.CardViewServicios:
                intent = new Intent(this,ActivityPrePagos.class);
                break;
            case R.id.CardViewTPV:
                intent = new Intent(this,VentasTPV.class);
                break;
            case R.id.CardViewAceite:
                intent = new Intent(this,AceiteVenta.class);
                break;
        }
        if (intent!=null){
        startActivity(intent);
        }
    }


    public void doNegativeClick(){
        Toast.makeText(this, "Ha pulsado Cancelar", Toast.LENGTH_SHORT).show();
    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_venta);
                icon = getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_venta_repsol);
                icon = getDrawable(R.drawable.isologo_repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_venta_ener);
                icon = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_venta_total);
                icon = getDrawable(R.drawable.total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void GetNipManagerFinish(JSONObject output) {
        System.out.println(output);
        try {
            if (output.getInt(Variables.CODE_ERROR)==0) {
                if(output.getString(Variables.NIP_MANAGER) != null) {
                    if (output.getString(Variables.NIP_MANAGER_WRITE).equals(output.getString(Variables.NIP_MANAGER))){
                        JarreoFullScreenFragment dialogFragment = new JarreoFullScreenFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        dialogFragment.show(transaction, String.valueOf(R.string.jarreo));
                    }else {
                        new AlertDialog.Builder(VentaActivity.this)
                                .setTitle(R.string.error)
                                .setIcon(icon)
                                .setMessage(R.string.nipWrong)
                                .setPositiveButton(R.string.btn_ok,null).show();
                    }
                }else{
                    new AlertDialog.Builder(VentaActivity.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(R.string.nonipmanager)
                            .setPositiveButton(R.string.btn_ok,null).show();
                }
            }else{
                logCE.EscirbirLog2(getApplicationContext(),
                        "VentaActivity_CallJarreo - " + output.get(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(VentaActivity.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(String.valueOf(output.get(Variables.MESSAGE_ERROR)))
                        .setPositiveButton(R.string.btn_ok,null).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
