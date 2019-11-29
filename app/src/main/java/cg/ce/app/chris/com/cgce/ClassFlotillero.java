package cg.ce.app.chris.com.cgce;

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

/**
 * Created by chris on 12/05/17.
 */

public class ClassFlotillero extends AsyncTask<JSONObject, String, Boolean> {
    //funcion que termina asynctask
    protected boolean onPostExecute(boolean result) {

        pdialog.dismiss();
        return result;
    }
    //declaramos variables
    ProgressDialog pdialog;
    int nrotrn,nrobom,codprd,codcli,nroveh,nrocte,mtogto,logusu;
    String fchtrn,hratrn,fchcor,codgas,nrotur,odm;
    double pre,can,mto;
    Context context1;
    URL url = null;
    HttpURLConnection conn;
    public static final int CONNECTION_TIMEOUT = 3000; // 3seg
    public static final int READ_TIMEOUT = 5000; //5seg
    //se crea constructor
    public ClassFlotillero (Context context, JSONObject jsonObject){
        pdialog = new ProgressDialog(context);
        context1=context;
        try {
            this.nrotrn=jsonObject.getInt("nrotrn");
            this.codgas=jsonObject.getString("cveest");
            this.nrotur=jsonObject.getString("nrotur");
            this.codprd=jsonObject.getInt("codprd");
            this.can=jsonObject.getDouble("cantidad");
            this.mto=jsonObject.getDouble("total");
            this.codcli=jsonObject.getInt("codcli");
            this.nroveh=jsonObject.getInt("nroveh");
            this.odm=jsonObject.getString("odm");
            this.nrocte=jsonObject.getInt("nrocte");
            this.mtogto=jsonObject.getInt("mtogto");
            this.pre=jsonObject.getDouble("precio");
            this.logusu=jsonObject.getInt("logusu");
            this.fchtrn=jsonObject.getString("fecha");
            this.hratrn=jsonObject.getString("hora");
            this.fchcor=jsonObject.getString("fchcor");
            this.nrobom=jsonObject.getInt("bomba");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //antes de que inicie el proceso se bloquea la pantalla
    @Override
    protected void onPreExecute() {
        pdialog.setIndeterminate(true);
        pdialog.setCancelable(false);
        pdialog.setTitle("Combu-Express");
        pdialog.setMessage("Actualizando");
        pdialog.show();
    }
    //inicia el proceso de segundo plano
    @Override
    protected Boolean doInBackground(JSONObject... args) {
        try {
            // Enter URL address where your php file resides
            url = new URL("http://187.210.108.135/combufleet/despacho-ws.php");

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        try {
            // Setup HttpURLConnection class to send and receive data from php and mysql
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");

            // setDoInput and setDoOutput method depict handling of both send and receive
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Append parameters to URL
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("nrotrn", String.valueOf(nrotrn))
                    .appendQueryParameter("codgas", String.valueOf(codgas))
                    .appendQueryParameter("nrobom", String.valueOf(nrobom))
                    .appendQueryParameter("fchtrn", String.valueOf(fchtrn))
                    .appendQueryParameter("hratrn", String.valueOf(hratrn))
                    .appendQueryParameter("fchcor", String.valueOf(fchcor))
                    .appendQueryParameter("nrotur", String.valueOf(nrotur))
                    .appendQueryParameter("codprd", String.valueOf(codprd))
                    .appendQueryParameter("can", String.valueOf(can))
                    .appendQueryParameter("mto", String.valueOf(mto))
                    .appendQueryParameter("codcli", String.valueOf(codcli))
                    .appendQueryParameter("nroveh", String.valueOf(nroveh))
                    .appendQueryParameter("odm", String.valueOf(odm))
                    .appendQueryParameter("nrocte", String.valueOf(nrocte))
                    .appendQueryParameter("mtogto", String.valueOf(mtogto))
                    .appendQueryParameter("pre", String.valueOf(pre))
                    .appendQueryParameter("logusu", String.valueOf(logusu));
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

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
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
                cgticket ticket = new cgticket();
                ticket.update_flotillero(context1, String.valueOf(nrotrn));
                pdialog.dismiss();
                return true;
            } else {
                pdialog.dismiss();
                return false;
            }

        } catch (IOException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            pdialog.dismiss();
            return false;
        } finally {
            conn.disconnect();
        }

    }
}
