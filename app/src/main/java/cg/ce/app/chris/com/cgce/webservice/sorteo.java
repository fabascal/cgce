package cg.ce.app.chris.com.cgce.webservice;


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

import cg.ce.app.chris.com.cgce.SorteoListener;

public class sorteo extends AsyncTask<JSONObject, String, String> {
    ProgressDialog pdLoading ;
    HttpURLConnection conn;
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 7000;
    JSONObject js_envio ;
    URL url = null;
    public SorteoListener delegate=null;


    public sorteo(Context context, JSONObject js_origen) throws JSONException {
        pdLoading = new ProgressDialog(context);

        this.js_envio=new JSONObject(String.valueOf(js_origen));
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //this method will be running on UI thread
        pdLoading.setIndeterminate(true);
        pdLoading.setCancelable(false);
        pdLoading.setTitle("Repsol");
        pdLoading.setMessage("Generando ticket de sorteo!!!");
        pdLoading.show();

    }


    @Override
    protected String doInBackground(JSONObject... jsonObjects) {
        try {
            url = new URL("http://factura.combuexpress.mx/repsolsorteo/combugosorteows.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
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
                    .appendQueryParameter("cveest", js_envio.getString("cveest"))
                    .appendQueryParameter("ticket",js_envio.getString("nrotrn"))
                    .appendQueryParameter("fecha_ticket",js_envio.getString("fecha"))
                    .appendQueryParameter("id_producto",js_envio.getString("id_producto"))
                    .appendQueryParameter("bomba",js_envio.getString("bomba"))
                    .appendQueryParameter("preunitario",js_envio.getString("precio"))
                    .appendQueryParameter("importe",js_envio.getString("total"))
                    .appendQueryParameter("nip",js_envio.getString("nip"));
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
        } catch (JSONException e) {
            e.printStackTrace();
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
        //if(pdLoading.isShowing())
        //   pdLoading.dismiss();

        Log.w("Sorteo",result);
        super.onPostExecute(result);
        delegate.processFinish3(result);
    }
}
