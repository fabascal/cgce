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

import cg.ce.app.chris.com.cgce.listeners.ValesListener;

public class ValesWS extends AsyncTask<JSONObject, String, String> {

    ProgressDialog pdLoading ;
    HttpURLConnection conn;
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 7000;
    JSONObject js_envio, error;
    URL url = null;
    public ValesListener delegate = null;
    Context context;

    public ValesWS (JSONObject js, Context context) throws JSONException {
        this.js_envio = new JSONObject(js.toString());
        this.context = context;
        pdLoading = new ProgressDialog(context);
        error = new JSONObject();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //this method will be running on UI thread
        pdLoading.setIndeterminate(true);
        pdLoading.setCancelable(false);
        pdLoading.setTitle("Combu-Express");
        pdLoading.setMessage("Consultando Combu-Vale");
        pdLoading.show();
    }

    @Override
    protected String doInBackground(JSONObject... jsonObjects) {
        try {
            url = new URL("http://combuexpress.mx");
        } catch (MalformedURLException e) {
            try {
                error.put("mensaje",e);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
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
                    .appendQueryParameter("folio",js_envio.getString("folio"))
                    .appendQueryParameter("nip",js_envio.getString("nip"))
                    .appendQueryParameter("despachador",js_envio.getString("despachador"));
            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();

        } catch (IOException e1) {
            try {
                error.put("mensaje",e1);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return null;
        } catch (JSONException e) {
            try {
                error.put("mensaje",e);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
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
            try {
                error.put("mensaje",e);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
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

        if (result != null) {

            Log.w("Combu-Vale", result);
            super.onPostExecute(result);
            delegate.processFinish(result);
        }else {
            try {
                error.put("error","1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            delegate.processFinish(error.toString());
        }

    }
}
