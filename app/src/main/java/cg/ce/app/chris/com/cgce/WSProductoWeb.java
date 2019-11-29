package cg.ce.app.chris.com.cgce;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by chris on 20/06/17.
 */

public class WSProductoWeb extends AsyncTask<String, String, String> {
    ProgressDialog pdLoading ;
    HttpURLConnection conn;
    URL url = null;
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 7000;
    String config_id,cveest,codigo,producto,precio,cantidad,id_despachador,despachador,rfc,tipo_venta,corte,nrotrn,isla,fecha;
    public ProductoWebResultListener delegate = null;
    cgticket cia = new cgticket();

    public WSProductoWeb(Context context, JSONObject jsonObject) {
        pdLoading = new ProgressDialog(context);
        Log.w("jsonantes",jsonObject.toString());
        try {
            this.config_id=jsonObject.getString("config_id");
            this.cveest=jsonObject.getString("cveest");
            this.codigo=jsonObject.getString("codigo");
            this.producto=jsonObject.getString("producto");
            this.precio=jsonObject.getString("precio");
            this.cantidad=jsonObject.getString("cantidad");
            this.id_despachador=jsonObject.getString("id_despachador");
            this.despachador=jsonObject.getString("despachador");
            this.rfc=jsonObject.getString("rfc");
            this.tipo_venta=jsonObject.getString("tipo_venta");
            this.corte=jsonObject.getString("corte");
            this.nrotrn=jsonObject.getString("nota");
            this.isla= String.valueOf(jsonObject.getInt("isla"));
            this.fecha = cia.fechaaceite(context,jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //this method will be running on UI thread
        pdLoading.setIndeterminate(true);
        pdLoading.setCancelable(false);
        pdLoading.setTitle("Combu-Express");
        pdLoading.setMessage("Actualizando Portal");
        pdLoading.show();

    }
    @Override
    protected String doInBackground(String... strings) {
        try {

            // Web Service al equipo
            url = new URL("http://189.206.183.110:1390/consumir_aceite.php");

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
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
            //Log.w("ean13",ean13);
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("cveest", cveest)
                    .appendQueryParameter("config_id",config_id)
                    .appendQueryParameter("nrotrn",nrotrn)
                    .appendQueryParameter("codigo",codigo)
                    .appendQueryParameter("producto",producto)
                    .appendQueryParameter("precio",precio)
                    .appendQueryParameter("cantidad",cantidad)
                    .appendQueryParameter("id_despachador",id_despachador)
                    .appendQueryParameter("despachador",despachador)
                    .appendQueryParameter("rfc",rfc)
                    .appendQueryParameter("tipo_venta",tipo_venta)
                    .appendQueryParameter("corte",corte)
                    .appendQueryParameter("isla",isla)
                    .appendQueryParameter("fecha",fecha);
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
            return null;
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
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            conn.disconnect();
        }
    }
    @Override
    protected void onPostExecute(String result) {
        if(pdLoading.isShowing())
            pdLoading.dismiss();
        Log.w("Producto",result);
        super.onPostExecute(result);
        delegate.processFinish2(result);
    }
}
