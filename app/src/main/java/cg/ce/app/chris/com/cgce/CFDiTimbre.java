package cg.ce.app.chris.com.cgce;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

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
import java.net.SocketException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;

/**
 * Created by chris on 29/06/17.
 */

public class CFDiTimbre extends AsyncTask<String, Void, JSONObject> {
    ProgressDialog pdLoading;
    HttpURLConnection conn;
    URL url = null;
    public static final int CONNECTION_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 30000;
    cgticket cg = new cgticket();
    public CfdiResultListener delegate = null;
    Context  context;
    JSONObject data;
    JSONArray dataArray = new JSONArray();


    public CFDiTimbre (Context context, JSONObject jsonObject){
        this.context=context;
        pdLoading = new ProgressDialog(context);
        Log.w("json_envio",jsonObject.toString());
        this.dataArray.put(jsonObject);
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
    protected JSONObject doInBackground(String... params) {
        try {
            url = new URL("http://"+params[0]+"/integral/ws/timbrarws1.1.php");
            Log.w("url", String.valueOf(url));
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
            Log.w("dataarray",String.valueOf(dataArray));
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("trama",String.valueOf(dataArray));
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
                Log.w("js", String.valueOf(result));
                JSONObject js= new JSONObject(String.valueOf(result));
                return (js);

            } else {
                return null;
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        } finally {
            conn.disconnect();
        }
    }

    /*@Override
    protected String doInBackground(String... strings) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String res = "{\"error\":1,\"numfactura\":\"COCEBR9\",\"erfc\":\"ICE8105062K6\",\"enombre\":\"INDUSTRIAS CEBRA SA DE CV\",\"rrfc\":\"GOSP730801AT4\",\"rnombre\":\"ADRIAN SANCHEZ DIAZ2\",\"rdomicilio\":\"AV. 8 DE JULIO 2355 \",\"rcolonia\":\"ZONA INDUSTRIAL ,,GUADALAJARA\",\"rpais\":\"Mexico\",\"rcp\":\"44940\",\"subtotal\":519.22,\"iva\":80.78,\"total\":600,\"productos\":\"15101514|PL\\/873\\/EXP\\/ES\\/2015-503529800|32.80481|LTR|LITRO|Gasolina advanced 87 octanos|15.82750|519.22|504.89|002|Tasa|0.160000|80.78\",\"selloCFD\":\"R5uHvdakGtSwYEN7YngU6S7rVzNncwFoEnrtB\\/vUPO\\/cXFupN4NW\\/Bwb1YJv0nFN9lxqXNjjeoTVEsFKlMilK5QIgnSxopzW1\\/JLoBSfnEBqukWOhkrzGAYijFdnl8zeehU0\\/PI39NCKQO0j\\/Nlz7Wfgbhg2HYKj0HpQm\\/m7qcjK44mQNNynAhOBLf17TEVS6Dgw4TZ+7i6MdxLijfE3AjOe1XP84ni0K6tqNi+fuBwQz0ywiQOX9Ao4Q+BxHwI1W\\/EytZn8krlU\\/zDVcBZ1u0LM9VD1iXSWfaHO2IW3AUHpVgQlSm5ZcOAhKu8ppfcDBlY6JCjJgOPBP7belcDEMg==\",\"FechaTimbrado\":\"2020-12-15T11:06:30\",\"UUID\":\"0cd19537-ad20-4f92-a158-8f7202f50ff5\",\"noCertificadoSAT\":\"00001000000408254801\",\"version\":\"1.1\",\"selloSAT\":\"RpWz+jyjQ9w71aQS1ia5RyYMMi68ExsWB9qTuMgB6qX3QwUeEztkAjJwslLOYNqJA1tlZWOq98CGrvGFrodbPQAVzgHSEj9IHGUfRLzpMEPhfleewdQZMFdShym6eyxZY8lJtA3ujnAwmkJqOYrH57fw\\/UZel\\/nHAS8uLcfH8xX4MN4Xs3KAUTOX3clqPyphVhq9Loasgd9\\/L3SGua1i0hmLQCF2DstcxjnUaUMqurKx+rUgbGVDAEEy4m9buVg23pNtVtoklBOnJdkd04RQx8F4dtATCvstI+GWnnWmdf1gZVVksd81G5\\/QsQAlAViY8NWYnyKnKQGxv\\/cuPc08ow==\"}";
        return res;
    }*/



    @Override
    protected void onPostExecute(JSONObject result) {
        if(pdLoading.isShowing())
            pdLoading.dismiss();
        Log.w("Producto",String.valueOf(result));
        super.onPostExecute(result);
        delegate.processFinish(result);
    }

}
