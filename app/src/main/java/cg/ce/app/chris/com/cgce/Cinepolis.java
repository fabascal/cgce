package cg.ce.app.chris.com.cgce;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

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


public class Cinepolis extends AsyncTask<Void, Void, String> {
    URL url = null;
    HttpURLConnection conn;
    public static final int CONNECTION_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 30000;
    ProgressDialog pdialog;
    cgticket cg = new cgticket();
    public CinepolisAsyncResponse delegate = null;
    Context  context;
    Activity activity;
    String correo=null;
    JSONObject jsonrespuesta = new JSONObject();
    JSONObject res ;

    public Cinepolis(Context context, String correo, Activity activity) {
        pdialog = new ProgressDialog(context);
        this.context=context;
        this.correo=correo;
        this.activity=activity;
    }

    @Override
    protected void onPreExecute() {
        pdialog.setIndeterminate(true);
        pdialog.setCancelable(false);
        pdialog.setTitle("Combu-Express");
        pdialog.setMessage("Cinepolis...");pdialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            url = new URL("http://factura.combuexpress.mx/cinece/wscombugo.php");
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
                    .appendQueryParameter("cveest", cg.get_cveest(context))
                    .appendQueryParameter("nip",cg.nip_desp(context))
                    .appendQueryParameter("correo",correo);
            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();

        } catch (IOException | ClassNotFoundException | SQLException | InstantiationException | JSONException | IllegalAccessException e1) {
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
        if(pdialog.isShowing())
            pdialog.dismiss();

        super.onPostExecute(result);
        delegate.processFinish(result);

        toJson tojson = new toJson();
        jsonrespuesta=tojson.strtojson(result,"|");
        try {
            res=new JSONObject(String.valueOf(jsonrespuesta.getString("0")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ClassImpresionCinepolis classImpresionCinepolis = new ClassImpresionCinepolis(activity,context,res);
        classImpresionCinepolis.execute();


    }
}
