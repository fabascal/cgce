package cg.ce.app.chris.com.cgce;


import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.sql.SQLException;

import cg.ce.app.chris.com.cgce.Fragments.JarreoFullScreenFragment;
import cg.ce.app.chris.com.cgce.dialogos.Fragment1;


public class VentaActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

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

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_venta);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_venta_repsol);
                break;
            case "Ener":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_venta);
                break;
            case "Total":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_venta);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_venta);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_venta);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent = null;
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_venta);
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
        nipManager.setTransformationMethod(PasswordTransformationMethod.getInstance());
        new AlertDialog.Builder(VentaActivity.this)
                .setTitle(R.string.jarreo)
                .setMessage(R.string.jarreo_msj)
                .setView(nipManager)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cgticket ticket = new cgticket();
                        String bdnipmanager = null;
                        /*obtenemos y comparamos el nip del gerente almacenado previamente en la base de datos cecg_app*/
                        try {
                            bdnipmanager = ticket.getNipManager(getApplicationContext());
                            Log.w("nip",bdnipmanager);
                        } catch (SQLException | ClassNotFoundException | InstantiationException | JSONException | IllegalAccessException e) {
                            new AlertDialog.Builder(VentaActivity.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            e.printStackTrace();
                        }
                        if(bdnipmanager != null) {
                            if (nipManager.getText().toString().equals(bdnipmanager)){
                                JarreoFullScreenFragment dialogFragment = new JarreoFullScreenFragment();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                dialogFragment.show(transaction, String.valueOf(R.string.jarreo));
                            }else {
                                nipManager.setFocusable(true);
                                Toast.makeText(getApplicationContext(),R.string.nipWrong,Toast.LENGTH_LONG).show();
                            }

                        }else{
                            Toast.makeText(getApplicationContext(),R.string.nonipmanager,Toast.LENGTH_LONG).show();
                        }




                    }
                })
                .setNegativeButton(R.string.cancelar,null).show();

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
                intent = new Intent(this,ActivityCreditoDual.class);
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

}
