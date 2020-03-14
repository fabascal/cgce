package cg.ce.app.chris.com.cgce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetodoPagoBusqueda extends AppCompatActivity implements View.OnClickListener {
    TextView tv_cliente_cfdi,tv_rfc_cfdi,tv_correo_cfdi,tv_calle_cfdi,tv_colonia_cfdi,tv_estado_cfdi,tv_municipio_cfdi,tv_cp_cfdi;
    EditText et_correo2,numcuenta,comentario;
    Spinner spn_metodo,spn_id,spn_uso;
    Button btn_cfdi;
    JSONObject cfdi_data;
    String id_metodo2,usocfdi;
    ArrayAdapter<String> adapter_id=null;
    ArrayAdapter<String> adapter_usocfdi_id=null;

    String bomba;
    public static final int CONNECTION_TIMEOUT = 20000;
    public static final int READ_TIMEOUT = 25000;
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metodo_pago_busqueda);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        sensores.bluetooth();
        sensores.wifi(this,true);
        et_correo2 = (EditText)findViewById(R.id.et_correo2);
        numcuenta = (EditText)findViewById(R.id.numcuenta);
        spn_metodo = (Spinner)findViewById(R.id.spn_metodo);
        spn_uso = (Spinner)findViewById(R.id.spn_uso);
        comentario = (EditText)findViewById(R.id.comentario);
        spn_metodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (spn_metodo.getSelectedItem().equals("01-Efectivo") || spn_metodo.getSelectedItem().equals("99-Otros")
                        || spn_metodo.getSelectedItem().equals("08-Vales de despensa") || spn_metodo.getSelectedItem().equals("98-NA")  ){
                    numcuenta.setVisibility(View.GONE);
                }else {
                    numcuenta.setVisibility(View.GONE);
                }
                Integer position_id = spn_metodo.getSelectedItemPosition();
                id_metodo2=String.valueOf(adapter_id.getItem(position_id));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        spn_uso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //usocfdi=String.valueOf(spn_uso.getSelectedItem());
                Integer position_id = spn_uso.getSelectedItemPosition();
                usocfdi=String.valueOf(adapter_usocfdi_id.getItem(position_id));
                //Toast.makeText(MetodoPagoBusqueda.this,usocfdi,Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        btn_cfdi =(Button)findViewById(R.id.btn_cfdi) ;
        btn_cfdi.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            try {
                cfdi_data = new JSONObject(getIntent().getStringExtra("cliente"));
                tv_cliente_cfdi = (TextView)findViewById(R.id.tv_cliente_cfdi);
                tv_rfc_cfdi = (TextView)findViewById(R.id.tv_rfc_cfdi);
                tv_correo_cfdi = (TextView)findViewById(R.id.tv_correo_cfdi);
                tv_calle_cfdi = (TextView)findViewById(R.id.tv_calle_cfdi);
                tv_colonia_cfdi = (TextView)findViewById(R.id.tv_colonia_cfdi);
                tv_estado_cfdi = (TextView)findViewById(R.id.tv_estado_cfdi);
                tv_cp_cfdi = (TextView)findViewById(R.id.tv_cp_cfdi);
                tv_municipio_cfdi = (TextView)findViewById(R.id.tv_municipio_cfdi);
                tv_cliente_cfdi.setText(cfdi_data.getString("nombre"));
                tv_rfc_cfdi.setText("R.F.C. :"+cfdi_data.getString("rfc"));
                tv_correo_cfdi.setText("Correo :"+cfdi_data.getString("correo"));
                tv_calle_cfdi.setText("Domicilio :"+cfdi_data.getString("calle")+" "+cfdi_data.getString("exterior")+" "+cfdi_data.getString("interior")+" ");
                tv_colonia_cfdi.setText("Colonia :"+cfdi_data.getString("colonia"));
                tv_estado_cfdi.setText("Estado :"+cfdi_data.getString("estado"));
                tv_municipio_cfdi.setText("Municipio :"+cfdi_data.getString("municipio"));
                tv_cp_cfdi.setText("CP :"+cfdi_data.getString("cp"));
                bomba=cfdi_data.getString("bomba");
                new AsyncUsoCFDi().execute();
                new AsyncFetch(cfdi_data).execute();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {

        Intent intent=null;
        switch (view.getId()) {
            case R.id.btn_cfdi:
                JSONObject cfdi_envio= null;
                try {
                    cfdi_envio = getJson(cfdi_data);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
                //String res = timbre.getCFDi(cfdi_envio);
                //Log.w("ws_res",res);
                intent=new Intent(getApplicationContext(),EmisionCfdi.class);
                intent.putExtra("json",cfdi_envio.toString());
                break;
        }
        if (intent!=null){
            startActivity(intent);
        }

    }
    public JSONObject getJson (JSONObject jsonObject) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        cgticket cgticket_obj = new cgticket();

        JSONObject ticket=null;
        String nip = null;

        try {

            ticket = cgticket_obj.consulta_servicio(getApplicationContext(),bomba);
            nip = cgticket_obj.nip_desp(getApplicationContext());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JSONObject jsoncfdi = new JSONObject();
        try {
            String calle = cfdi_data.getString("calle")+" "+cfdi_data.getString("exterior")+" "+cfdi_data.getString("interior");
            jsoncfdi.put("nip",nip);
            jsoncfdi.put("categoria","cfdi");
            jsoncfdi.put("id_cliente",jsonObject.getInt("id_cliente"));
            jsoncfdi.put("cliente",jsonObject.getString("nombre"));
            jsoncfdi.put("rfc",jsonObject.getString("rfc"));
            jsoncfdi.put("id_domicilio",jsonObject.getInt("id_domicilio"));
            jsoncfdi.put("domicilio",calle);
            jsoncfdi.put("colonia",jsonObject.getString("colonia"));
            jsoncfdi.put("estado",jsonObject.getString("estado"));
            jsoncfdi.put("municipio",jsonObject.getString("municipio"));
            jsoncfdi.put("cp",jsonObject.getString("cp"));
            jsoncfdi.put("id_estacion","12");
            jsoncfdi.put("id_formpago",id_metodo2);
            jsoncfdi.put("usocfdi",usocfdi);
            jsoncfdi.put("formapago",String.valueOf(spn_metodo.getSelectedItem()));
            jsoncfdi.put("numcuenta",numcuenta.getText());
            jsoncfdi.put("cveest",ticket.getString("cveest"));
            jsoncfdi.put("ticket",ticket.getInt("nrotrn"));
            jsoncfdi.put("cg_cliente",ticket.getInt("codcli"));
            jsoncfdi.put("fecha_ticket",ticket.getString("fecha"));
            jsoncfdi.put("id_producto",ticket.getInt("id_producto"));
            jsoncfdi.put("producto",ticket.getString("producto").replace(" ",""));
            jsoncfdi.put("bomba",ticket.getInt("bomba"));
            jsoncfdi.put("preunitario",ticket.getDouble("precio"));
            jsoncfdi.put("importe",ticket.getDouble("total"));
            jsoncfdi.put("mtogto",ticket.getDouble("mtogto"));
            jsoncfdi.put("cantidad",ticket.getDouble("cantidad"));
            jsoncfdi.put("despachador",ticket.getString("despachador"));
            jsoncfdi.put("copia",et_correo2.getText());
            jsoncfdi.put("comentario",comentario.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsoncfdi;
    }

    // Clase asyncrona para obtener el metodo de pago
    private class AsyncFetch extends AsyncTask<JSONObject, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(MetodoPagoBusqueda.this);
        HttpURLConnection conn;
        URL url = null;
        String searchQuery;

        public AsyncFetch(JSONObject searchQuery){
            try {
                this.searchQuery=searchQuery.getString("id_cliente");
            } catch (JSONException e) {
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
                url = new URL("http://factura.combuexpress.mx/kioscoce/metodopago-search_fa2.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
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
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("searchQuery", searchQuery);
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
            List<String> metodos = new ArrayList<String>();
            List<String> id_metodos = new ArrayList<String>();

            pdLoading.dismiss();
            if(result.equals("no rows")) {
                Toast.makeText(MetodoPagoBusqueda.this, "No Results found for entered query", Toast.LENGTH_LONG).show();
            }else{

                try {

                    JSONArray jArray = new JSONArray(result);
                    String metodo = null;
                    String id_metodo = null;


                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        metodo = json_data.getString("clave") +"-"+ json_data.getString("descripcion");
                        id_metodo = json_data.getString("id");
                        metodos.add(i,metodo);
                        id_metodos.add(i,id_metodo);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MetodoPagoBusqueda.this,android.R.layout.simple_dropdown_item_1line, (List<String>) metodos);
                    adapter_id = new ArrayAdapter<String>(MetodoPagoBusqueda.this,android.R.layout.simple_dropdown_item_1line, (List<String>) id_metodos);
                    spn_metodo.setAdapter(adapter);


                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Log.w("ERR",e.toString());
                    Log.w("ERR",result.toString());

                }

            }

        }

    }
    // Clase asyncrona para obtener el uso de cfdi
    private class AsyncUsoCFDi extends AsyncTask<JSONObject, String, String> {

        //ProgressDialog pdLoading1 = new ProgressDialog(MetodoPagoBusqueda.this);
        HttpURLConnection conn;
        URL url = null;
        String searchQuery;

        public AsyncUsoCFDi(){

        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            //pdLoading1.setMessage("\tLoading...");
            //pdLoading1.setCancelable(false);
            //pdLoading1.show();

        }

        @Override
        protected String doInBackground(JSONObject... jsonObjects) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://factura.combuexpress.mx/kioscoce/usocfdi-search_fa2.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
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
                        .appendQueryParameter("searchQuery", "1")
                        .appendQueryParameter("searchBandera","Repsol");
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
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            //pdLoading1.dismiss();
            List<String> UsoCFDi = new ArrayList<String>();
            List<String> id_usocfdis = new ArrayList<String>();

            //pdLoading1.dismiss();
            if(result.equals("no rows")) {
                Toast.makeText(MetodoPagoBusqueda.this, "No Results found for entered query", Toast.LENGTH_LONG).show();
            }else{

                try {

                    JSONArray jArray = new JSONArray(result);
                    String usocfdi = null;
                    String id_usocfdi = null;


                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        usocfdi = json_data.getString("clave") +"-"+ json_data.getString("descripcion");
                        id_usocfdi = json_data.getString("clave");
                        UsoCFDi.add(i,usocfdi);
                        id_usocfdis.add(i,id_usocfdi);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MetodoPagoBusqueda.this,android.R.layout.simple_dropdown_item_1line, (List<String>) UsoCFDi);
                    adapter_usocfdi_id = new ArrayAdapter<String>(MetodoPagoBusqueda.this,android.R.layout.simple_dropdown_item_1line, (List<String>) id_usocfdis);
                    spn_uso.setAdapter(adapter);


                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Log.w("ERR",e.toString());
                    Log.w("ERR",result.toString());

                }

            }

        }

    }
}
