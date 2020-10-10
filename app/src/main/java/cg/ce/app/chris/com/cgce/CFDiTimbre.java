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
import java.net.SocketException;
import java.net.URL;
import java.sql.SQLException;

/**
 * Created by chris on 29/06/17.
 */

public class CFDiTimbre extends AsyncTask<String, Void, String> {
    ProgressDialog pdLoading;
    HttpURLConnection conn;
    URL url = null;
    public static final int CONNECTION_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 30000;
    cgticket cg = new cgticket();
    String bandera,usocfdi,categoria,id_cliente,id_domicilio,id_formpago,numcuenta,id_producto,cveest,ticket,fecha_ticket,producto,bomba,preunitario,mtogto,importe,copia,comentarios,nip;
    public CfdiResultListener delegate = null;
    Context  context;

    public CFDiTimbre (Context context, JSONObject jsonObject){
        this.context=context;
        pdLoading = new ProgressDialog(context);
        Log.w("json_envio",jsonObject.toString());
        try {
            this.id_producto=jsonObject.getString("id_producto");
            this.usocfdi=jsonObject.getString("usocfdi");
            this.categoria=jsonObject.getString("categoria");
            this.id_cliente=String.valueOf(jsonObject.getInt("id_cliente"));
            this.id_domicilio=String.valueOf(jsonObject.getInt("id_domicilio"));
            this.id_formpago=jsonObject.getString("id_formpago");
            this.numcuenta=jsonObject.getString("numcuenta");
            this.cveest=jsonObject.getString("cveest");
            this.ticket=String .valueOf(jsonObject.getInt("ticket"))+"0";
            this.fecha_ticket=jsonObject.getString("fecha_ticket");
            this.producto=String.valueOf(jsonObject.getInt("id_producto"));
            this.bomba=String.valueOf(jsonObject.getInt("bomba"));
            this.preunitario=String.valueOf(jsonObject.getDouble("preunitario"));
            /*this.mtogto=String.valueOf(jsonObject.getDouble("mtogto"));*/
            this.importe=String.valueOf(jsonObject.getDouble("importe"));
            this.copia=String.valueOf(jsonObject.getString("copia"));
            this.comentarios=String.valueOf(jsonObject.getString("comentario"));
            this.nip=cg.nip_desp(context);
            this.bandera=String.valueOf(jsonObject.getString("bandera"));
        } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException | SocketException e) {
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
        pdLoading.setMessage("Timbrando ...");
        pdLoading.show();

    }
    @Override
    protected String doInBackground(String... strings) {
        try {

            // Web Service al equipo
            //url = new URL (cg.urltimbre(context));
            /*Log.w("Url Timbre", String.valueOf(cg.urltimbre(context)));*/

            switch (bandera){
                case "Combu-Express":
                    url = new URL("http://factura.combuexpress.mx/cefactura3.3/timbrarws1.3.php");
                    break;
                case "Repsol" :
                    url = new URL("http://factura.combuexpress.mx/cerepsol/timbrarws1.3.php");
                    break;
                case "Ener":
                    url = new URL("http://factura.combuexpress.mx/cefactura3.3/timbrarws1.3.php");
                    break;
                case "Total":
                    url = new URL("http://factura.combuexpress.mx/cefactura3.3/timbrarws1.3.php");
                    break;
                case "3":
                    url = new URL("http://factura.combuexpress.mx/cerepsol/timbrarws1.3-fast.php");
                    break;
            }
            //url = new URL("http://factura.combuexpress.mx/cefactura3.3/timbrarws1.3.php");

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
                    .appendQueryParameter("categoria", categoria)
                    .appendQueryParameter("id_cliente",id_cliente)
                    .appendQueryParameter("id_domicilio",id_domicilio)
                    .appendQueryParameter("id_formpago",id_formpago)
                    .appendQueryParameter("id_producto",id_producto)
                    .appendQueryParameter("numcuenta",numcuenta)
                    .appendQueryParameter("cveest",cveest)
                    .appendQueryParameter("ticket",ticket)
                    .appendQueryParameter("fecha_ticket",fecha_ticket)
                    .appendQueryParameter("producto",producto)
                    .appendQueryParameter("bomba",bomba)
                    .appendQueryParameter("preunitario",preunitario)
                    .appendQueryParameter("mtogto",mtogto)
                    .appendQueryParameter("importe",importe)
                    .appendQueryParameter("copia",copia)
                    .appendQueryParameter("comentarios",comentarios)
                    .appendQueryParameter("nip",nip)
                    .appendQueryParameter("usocfdi",usocfdi);
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
        delegate.processFinish(result);
    }

}
