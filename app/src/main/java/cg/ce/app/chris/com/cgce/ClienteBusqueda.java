package cg.ce.app.chris.com.cgce;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cg.ce.app.chris.com.cgce.Facturacion.GetCustomerFacturacion;
import cg.ce.app.chris.com.cgce.Facturacion.Listeners.GetCustomerFacturacionListener;
import cg.ce.app.chris.com.cgce.common.Variables;


public class ClienteBusqueda extends AppCompatActivity implements GetCustomerFacturacionListener {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 20000;
    public static final int READ_TIMEOUT = 25000;
    private RecyclerView mRVCliente;
    private AdapterCliente mAdapter;
    String bomba;
    Sensores sensores=new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    SearchView searchView = null;
    private final static String NO_DATA = "No existen clientes con el criterio establecido.";
    private String Bandera;
    LogCE logCE = new LogCE();
    Drawable icon;
    JSONObject cursor=null;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        sensores.bluetooth();
        sensores.wifi(this,true);
        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("bomba")!= null)
        {
            bomba=bundle.getString("bomba");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // adds item to action bar
        getMenuInflater().inflate(R.menu.cliente_busqueda, menu);

        // Get Search item from action bar and Get Search service
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) ClienteBusqueda.this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(ClienteBusqueda.this.getComponentName()));
            searchView.setIconified(false);
        }

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
    // Every time when you press search button on keypad an Activity is recreated which in turn calls this function
    @Override
    protected void onNewIntent(Intent intent) {
        // Get search query and create object of class AsyncFetch
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            GetCustomerFacturacion getCustomerFacturacion = new GetCustomerFacturacion(this, getApplicationContext());
            getCustomerFacturacion.delegate=this;
            getCustomerFacturacion.execute(intent.getStringExtra(SearchManager.QUERY),Bandera,getIntegra());
            if (searchView != null) {
                searchView.clearFocus();
            }
        }
    }
    public String getIntegra(){
        DataBaseManager manager = new DataBaseManager(getApplicationContext());
        cursor = manager.cargarcursorodbc2();
        String integra=null;
        try {
            integra = cursor.getString("integra");
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(ClienteBusqueda.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
        return integra;
    }
    @Override
    public void GetCustomerNameFinish(JSONObject result) {
        try {
            if (result.getInt(Variables.CODE_ERROR)==0){
                Log.w("json-getcustomer", String.valueOf(result.getString(Variables.KEY_CLIENTE)));
                List<DataCliente> data=new ArrayList<>();
                if(result.getString(Variables.KEY_CLIENTE).equals("no rows")) {
                    new AlertDialog.Builder(ClienteBusqueda.this)
                            .setTitle(R.string.error)
                            .setMessage(NO_DATA)
                            .setPositiveButton(R.string.btn_ok,null).show();
                }else{
                    try {
                        JSONArray jArray = new JSONArray(result.getString(Variables.KEY_CLIENTE));

                        // Extract data from json and store into ArrayList as class objects
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject json_data = jArray.getJSONObject(i);
                            DataCliente clienteData = new DataCliente();
                            clienteData.rfc = json_data.getString("rfc");
                            clienteData.nombre = json_data.getString("nombre");
                            clienteData.correo = json_data.getString("correo");
                            clienteData.id_cliente = json_data.getString("id");
                            clienteData.bomba = bomba;
                            data.add(clienteData);
                        }


                        // Setup and Handover data to recyclerview
                        mRVCliente = (RecyclerView) findViewById(R.id.fishPriceList);
                        mAdapter = new AdapterCliente(ClienteBusqueda.this, data);
                        mRVCliente.setAdapter(mAdapter);
                        mRVCliente.setLayoutManager(new LinearLayoutManager(ClienteBusqueda.this));

                    } catch (JSONException e) {
                        // You to understand what actually error is and handle it appropriately
                        new AlertDialog.Builder(ClienteBusqueda.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        logCE.EscirbirLog2(getApplicationContext(),"ContadoActivity_AsyncFetch - " + e);
                    }
                }
            }else{
                /*Convertimos el error y lo mostramos en pantalla*/
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + result.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(ClienteBusqueda.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(result.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(ClienteBusqueda.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.ContentMainSearch);
                setContentView(R.layout.activity_cliente_busqueda);
                Bandera="Combu-Express";
                icon = getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainSearchRepsol);
                setContentView(R.layout.activity_cliente_busqueda);
                Bandera = "Repsol";
                icon = getDrawable(R.drawable.repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainSearchEner);
                setContentView(R.layout.activity_cliente_busqueda);
                Bandera = "Ener";
                icon = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainSearchTotal);
                setContentView(R.layout.activity_cliente_busqueda);
                Bandera = "Total";
                icon = getDrawable(R.drawable.total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
