package cg.ce.app.chris.com.cgce.ControlGas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.common.Variables;

public class UpdateCodcli extends AsyncTask<String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    JSONObject cursor = null;
    JSONObject res = new JSONObject();
    public ControlGasListener delegate=null;

    public UpdateCodcli(Activity activity, Context context, ControlGasListener delegate) {
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = context;
        this.delegate = delegate;
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("CombuGo");
        // Progress dialog message
        mProgressDialog.setMessage("Favor de esperar, actualizando informacion del ticket...");
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }
    @Override
    protected JSONObject doInBackground(String... params) {
        String cliente = params[0];
        String vehiculo = params[1];
        String odm = params[2];
        String tar = params[3];
        String ticket = params[4];
        try {
            DataBaseManager manager = new DataBaseManager(mContext);
            cursor = manager.cargarcursorodbc2();
            String base = null;
            base = cursor.getString("db_cg");
            DataBaseCG dbcg = new DataBaseCG();
            Connection conn = dbcg.odbc_cg(mContext);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set codcli ="+cliente+", " +
                    "nroveh="+vehiculo+", odm="+odm+", tar="+tar+" where nrotrn = "+ticket+"");
            res.put(Variables.CODE_ERROR,0);
            stmt.close();
            conn.close();

        } catch (JSONException | IllegalAccessException | ClassNotFoundException | InstantiationException | SQLException e) {
            try {
                res.put(Variables.CODE_ERROR,1);
                res.put(Variables.MESSAGE_ERROR, e);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return res;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (mProgressDialog!= null){
            mProgressDialog.dismiss();
        }
        try {
            delegate.processFinish(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onPostExecute(jsonObject);
    }
}
