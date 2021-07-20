package cg.ce.app.chris.com.cgce.ValesWS;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cg.ce.app.chris.com.cgce.Facturacion.Listeners.GetCustomerFacturacionListener;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.ValesWS.Listeners.ValesWSListeners;
import cg.ce.app.chris.com.cgce.common.Variables;

public class ValesWS extends AsyncTask<String, String, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    HttpURLConnection conn;
    URL url = null;
    Context mContext;
    public ValesWSListeners delegate = null;
    public static final int CONNECTION_TIMEOUT = 60000;
    public static final int READ_TIMEOUT = 60000;
    private final static String NETWORK_ERROR = "Error de conexion.";
    JSONObject peticion;

    public ValesWS(Activity activity, Context context, JSONObject vales){
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = context;
        this.peticion= vales;
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("CombuGo");
        // Progress dialog message
        mProgressDialog.setMessage("Favor de esperar, estamos obteniendo informacion de los vales...");
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        Log.w("peticion",String.valueOf(peticion));
        JSONObject res = new JSONObject();
        try {
            url = new URL(Get_Url());
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
            // setDoInput and setDoOutput method depict handling of both send and receive
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // Append parameters to URL
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("trama", "["+String.valueOf(peticion)+"]");
            String query = builder.build().getEncodedQuery();
            // Open connection for sending data
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
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
                Log.w("result",result.toString());
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
                e.printStackTrace();
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        return res;
    }
    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
        }
        delegate.ValesWSFinish(jsonObject);
        super.onPostExecute(jsonObject);
    }
    public String Get_Url(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("Vale", mContext.MODE_PRIVATE);
        Log.w("ruta", sharedPreferences.getString(mContext.getResources().getString(R.string.ValeFile),""));
        return sharedPreferences.getString(mContext.getResources().getString(R.string.ValeFile),"");
    }
}
