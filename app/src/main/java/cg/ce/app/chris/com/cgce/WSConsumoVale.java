package cg.ce.app.chris.com.cgce;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by chris on 29/05/17.
 */

public class WSConsumoVale extends AsyncTask<JSONObject, String, JSONObject> {
    ProgressDialog pdLoading ;
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 7000;
    URL url = null;
    public WSConsumoVale(Context context){
        pdLoading = new ProgressDialog(context);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //this method will be running on UI thread
        pdLoading.setIndeterminate(true);
        pdLoading.setCancelable(false);
        pdLoading.setTitle("Combu-Express");
        pdLoading.setMessage("Validando Vale...");
        pdLoading.show();
    }
    @Override
    protected JSONObject doInBackground(JSONObject... jsonObjects) {
        try {

            // Enter URL address where your php file resides
            url = new URL("http://combuexpress.mx/bajio/consumirva-ws.php");

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return null;
    }
    @Override
    protected void onPostExecute(JSONObject result) {
        if(pdLoading.isShowing())
            pdLoading.dismiss();

    }
}
