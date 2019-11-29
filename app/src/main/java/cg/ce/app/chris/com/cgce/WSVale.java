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
 * Created by chris on 27/06/17.
 */

public class WSVale extends AsyncTask<String, String, String> {
    ProgressDialog pdLoading ;
    HttpURLConnection conn;
    URL url = null;
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 7000;
    String codigo,user,serie,cveest,id_despachador,despachador;
    public ValeResultListener delegate = null;

    public WSVale (Context context , JSONObject jsonObject){
        pdLoading = new ProgressDialog(context);
        try {
            this.codigo=jsonObject.getString("codigo");
            this.user=jsonObject.getString("user");
            this.serie=jsonObject.getString("serie");
            this.cveest=jsonObject.getString("cveest");
            this.id_despachador=jsonObject.getString("id_despachador");
            this.despachador=jsonObject.getString("despachador");
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
        pdLoading.setMessage("Consultando Vale ...");
        pdLoading.show();

    }
    @Override
    protected String doInBackground(String... strings) {
        try {

            // Web Service al equipo
            url = new URL("http://combuexpress.mx/cews/consumirva-ws.php");

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
                    .appendQueryParameter("codigo", codigo)
                    .appendQueryParameter("user",user)
                    .appendQueryParameter("serie",serie)
                    .appendQueryParameter("cveest",cveest)
                    .appendQueryParameter("id_despachador",id_despachador)
                    .appendQueryParameter("despachador",despachador);

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
        delegate.processFinishVale(result);
    }
}
