package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class DomicilioBusqueda extends AppCompatActivity {

    TextView tv_cliente_cfdi,tv_rfc_cfdi,tv_correo_cfdi;


    public static final int CONNECTION_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 35000;
    private RecyclerView mRVClienteDomicilio;
    private AdapterClienteDomicilio mAdapter;
    public static Bundle mMyAppsBundle = new Bundle();
    String bomba;
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    String Bandera;
    LogCE logCE = new LogCE();

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        sensores.bluetooth();
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            //cliente_domicilio-search_fa.php
            try {
                JSONObject cfdi_data = new JSONObject(getIntent().getStringExtra("cliente"));
                tv_cliente_cfdi = (TextView)findViewById(R.id.tv_cliente_cfdi);
                tv_rfc_cfdi = (TextView)findViewById(R.id.tv_rfc_cfdi);
                tv_correo_cfdi = (TextView)findViewById(R.id.tv_correo_cfdi);
                tv_cliente_cfdi.setText(cfdi_data.getString("nombre"));
                tv_rfc_cfdi.setText(cfdi_data.getString("rfc"));
                tv_correo_cfdi.setText(cfdi_data.getString("correo"));
                DomicilioBusqueda.mMyAppsBundle.putString("key",cfdi_data.getString("id_cliente"));
                bomba = cfdi_data.getString("bomba");
                cfdi_data.put("bandera",Bandera);
                //new AsyncFetch(query).execute();
                new AsyncFetch(cfdi_data).execute();
            } catch (JSONException e) {
                new AlertDialog.Builder(DomicilioBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"DomicilioBusqueda_onCreate - " + e);
                e.printStackTrace();
            }
        }

    }
    // Create class AsyncFetch
    private class AsyncFetch extends AsyncTask<JSONObject, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(DomicilioBusqueda.this);
        HttpURLConnection conn;
        URL url = null;
        String searchQuery,searchBandera;

        public AsyncFetch(JSONObject searchQuery){
            try {
                this.searchQuery=searchQuery.getString("id_cliente");
                this.searchBandera=searchQuery.getString("bandera");
            } catch (JSONException e) {
                new AlertDialog.Builder(DomicilioBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"DomicilioBusqueda_AsyncFetch - " + e);
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
        protected String doInBackground(JSONObject... jsonObjects) {
            try {
                // Enter URL address where your php file resides
                url = new URL("http://factura.combuexpress.mx/kioscoce/cliente_domicilio-search_fa2.php");
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                new AlertDialog.Builder(DomicilioBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"DomicilioBusqueda_AsyncFetch - " + e);
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
                new AlertDialog.Builder(DomicilioBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e1))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"DomicilioBusqueda_AsyncFetch - " + e1);
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
                new AlertDialog.Builder(DomicilioBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"DomicilioBusqueda_AsyncFetch - " + e);
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
            List<DataClienteDomicilio> data=new ArrayList<>();

            pdLoading.dismiss();
            if(result.equals("no rows")) {
                Toast.makeText(DomicilioBusqueda.this, "No Results found for entered query", Toast.LENGTH_LONG).show();
            }else{

                try {

                    JSONArray jArray = new JSONArray(result);

                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        DataClienteDomicilio clienteDataDomicilio = new DataClienteDomicilio();
                        clienteDataDomicilio.id_domicilio = json_data.getString("id_domicilio");
                        clienteDataDomicilio.calle = json_data.getString("calle");
                        clienteDataDomicilio.exterior = json_data.getString("exterior");
                        clienteDataDomicilio.interior = json_data.getString("interior");
                        clienteDataDomicilio.cp = json_data.getString("cp");
                        clienteDataDomicilio.colonia = json_data.getString("colonia");
                        clienteDataDomicilio.localidad = json_data.getString("localidad");
                        clienteDataDomicilio.municipio = json_data.getString("municipio");
                        clienteDataDomicilio.pais = json_data.getString("pais");
                        clienteDataDomicilio.estado = json_data.getString("estado");
                        clienteDataDomicilio.bomba = bomba;
                        data.add(clienteDataDomicilio);
                    }



                    // Setup and Handover data to recyclerview
                    mRVClienteDomicilio = (RecyclerView) findViewById(R.id.cliente_domicilio);
                    mAdapter = new AdapterClienteDomicilio(DomicilioBusqueda.this, data);
                    mRVClienteDomicilio.setAdapter(mAdapter);
                    mRVClienteDomicilio.setLayoutManager(new LinearLayoutManager(DomicilioBusqueda.this));

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    new AlertDialog.Builder(DomicilioBusqueda.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    logCE.EscirbirLog2(getApplicationContext(),"DomicilioBusqueda_AsyncFetch - " + e);
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
                setContentView(R.layout.activity_domicilio_busqueda);
                Bandera="Combu-Express";
                break;
            case "Repsol":
                setTheme(R.style.ContentMainSearchRepsol);
                setContentView(R.layout.activity_domicilio_busqueda);
                Bandera = "Repsol";
                break;
            case "Ener":
                setTheme(R.style.ContentMainSearchEner);
                setContentView(R.layout.activity_domicilio_busqueda);
                Bandera = "Ener";
                break;
            case "Total":
                setTheme(R.style.ContentMainSearchTotal);
                setContentView(R.layout.activity_domicilio_busqueda);
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
