package cg.ce.app.chris.com.cgce.ValesWS;

import android.app.Activity;
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
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import cg.ce.app.chris.com.cgce.LogCE;
import cg.ce.app.chris.com.cgce.ValesWS.Listeners.NominativaListener;
import cg.ce.app.chris.com.cgce.common.Variables;

public class Nominativa  extends AsyncTask<String, String, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    HttpURLConnection conn;
    URL url = null;
    Context mContext;
    public NominativaListener delegate = null;
    public static final int CONNECTION_TIMEOUT = 60000;
    public static final int READ_TIMEOUT = 60000;
    private final static String NETWORK_ERROR = "Error de conexion.";
    private final static String URL_NOMINATIVA = "http://70.35.195.68/integra/anticipos/timbrarconsumo";
    LogCE logCE = new LogCE();
    JSONObject peticion;

    public Nominativa(Activity activity, Context context, JSONObject vale){
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = context;
        this.peticion = vale;
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("CombuGo");
        // Progress dialog message
        mProgressDialog.setMessage("Favor de esperar, estamos emitiendo el CFDi de los vales...");
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        Log.w("Peticion" , "["+String.valueOf(peticion)+"]");
        JSONObject res = new JSONObject();
        try {
            url = new URL(URL_NOMINATIVA);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
            // setDoInput and setDoOutput method depict handling of both send and receive
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // Append parameters to URL
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("trama",  "["+String.valueOf(peticion)+"]" );
            String query = builder.build().getEncodedQuery();
            // Open connection for sending data
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
            int response_code = conn.getResponseCode();
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
                res.put(Variables.CODE_ERROR,0);
                res.put(Variables.RESULT, result.toString());
            } else {
                res.put(Variables.CODE_ERROR,1);
                res.put(Variables.MESSAGE_ERROR, NETWORK_ERROR);
            }
        } catch (IOException | JSONException e) {
            try {
                res.put(Variables.CODE_ERROR,1);
                res.put(Variables.MESSAGE_ERROR, e);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
            e.printStackTrace();
        }
        return res;
    }
    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
        }
        delegate.NominativaFinish(jsonObject);
        super.onPostExecute(jsonObject);
    }
}
