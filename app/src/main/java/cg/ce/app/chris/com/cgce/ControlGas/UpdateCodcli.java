package cg.ce.app.chris.com.cgce.ControlGas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.ControlGasListener;
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
    Connection conn = null;
    Statement stmt = null;

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
            conn = dbcg.odbc_cg(mContext);
            stmt = conn.createStatement();
            stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set codcli ="+cliente+", " +
                    "nroveh="+vehiculo+", odm="+odm+", tar="+tar+" where nrotrn = "+ticket+"");
            res.put(Variables.CODE_ERROR,0);
            stmt.close();
            conn.close();

        } catch (JSONException | IllegalAccessException | ClassNotFoundException | InstantiationException | SQLException e) {
            try {
                if(conn!=null) {
                    conn.close();
                }
                if (stmt!=null) {
                    stmt.close();
                }
                res.put(Variables.CODE_ERROR,1);
                res.put(Variables.MESSAGE_ERROR, e);
            } catch (JSONException | SQLException ex) {
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
        delegate.processFinish(jsonObject);
        super.onPostExecute(jsonObject);
    }
}
