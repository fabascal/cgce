package cg.ce.app.chris.com.cgce;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ClienteBusqueda extends AppCompatActivity  {

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
            JSONObject query = new JSONObject();
            try {
                query.put("nombre", intent.getStringExtra(SearchManager.QUERY));
                query.put("bandera",Bandera);
            } catch (JSONException e) {
                new AlertDialog.Builder(ClienteBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"ContadoActivity_onNewIntent - " + e);
                e.printStackTrace();
            }
            if (searchView != null) {
                searchView.clearFocus();
            }
            new AsyncFetch(query).execute();

        }
    }

    // Create class AsyncFetch
    private class AsyncFetch extends AsyncTask<JSONObject, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(ClienteBusqueda.this);
        HttpURLConnection conn;
        URL url = null;
        String searchQuery, searchBandera;

        public AsyncFetch(JSONObject searchQuery1){
            try {
                this.searchQuery=searchQuery1.getString("nombre");
                this.searchBandera=searchQuery1.getString("bandera");
            } catch (JSONException e) {
                new AlertDialog.Builder(ClienteBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"ContadoActivity_AsyncFetch - " + e);
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected String doInBackground(JSONObject... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://factura.combuexpress.mx/kioscoce/cliente-search_fa2.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                new AlertDialog.Builder(ClienteBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"ContadoActivity_AsyncFetch - " + e);
                e.printStackTrace();
                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // add parameter to our above url
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("searchQuery", searchQuery)
                        .appendQueryParameter("searchBandera", searchBandera);
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                new AlertDialog.Builder(ClienteBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e1))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"ContadoActivity_AsyncFetch - " + e1);
                e1.printStackTrace();
                return e1.toString();
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    return("Connection error");
                }

            } catch (IOException e) {
                new AlertDialog.Builder(ClienteBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"ContadoActivity_AsyncFetch - " + e);
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            pdLoading.dismiss();
            List<DataCliente> data=new ArrayList<>();

            pdLoading.dismiss();
            if(result.equals("no rows")) {

                new AlertDialog.Builder(ClienteBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(NO_DATA)
                        .setPositiveButton(R.string.btn_ok,null).show();
            }else{

                try {
                    Log.w("Error",result);

                    JSONArray jArray = new JSONArray(result);

                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        DataCliente clienteData = new DataCliente();
                        clienteData.rfc = json_data.getString("rfc");
                        clienteData.nombre = json_data.getString("nombre");
                        clienteData.correo = json_data.getString("correo");
                        clienteData.id_cliente = json_data.getString("id_cliente");
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
                break;
            case "Repsol":
                setTheme(R.style.ContentMainSearchRepsol);
                setContentView(R.layout.activity_cliente_busqueda);
                Bandera = "Repsol";
                break;
            case "Ener":
                setTheme(R.style.ContentMainSearchEner);
                setContentView(R.layout.activity_cliente_busqueda);
                Bandera = "Ener";
                break;
            case "Total":
                setTheme(R.style.ContentMainSearchTotal);
                setContentView(R.layout.activity_cliente_busqueda);
                Bandera = "Total";
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
