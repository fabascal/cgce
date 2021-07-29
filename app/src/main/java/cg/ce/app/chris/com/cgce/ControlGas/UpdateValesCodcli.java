package cg.ce.app.chris.com.cgce.ControlGas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.UpdateValesCodcliListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.common.Variables;

public class UpdateValesCodcli extends AsyncTask<String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    Connection conn = null;
    Statement stmt = null;
    JSONObject cursor = null;
    ResultSet r;
    JSONObject result = new JSONObject();
    public UpdateValesCodcliListener delegate = null;

    public UpdateValesCodcli (Activity activity,UpdateValesCodcliListener delegate){
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = activity.getApplicationContext();
        this.delegate = delegate;
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("CombuGo");
        // Progress dialog message
        mProgressDialog.setMessage("Favor de esperar, actualizando datos de cliente...");
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String query = "update vale set codcli="+params[0]+", dencli='"+params[1]+"',urlfile='"+params[2]+"' where id=1";
        Log.w("query", query);
        DataBaseCG dbcg = new DataBaseCG();
        try {
            conn = dbcg.odbc_cecg_app(mContext);
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            conn.close();
            result.put(Variables.CODE_ERROR,0);
        } catch (JSONException | ClassNotFoundException | InstantiationException |
                IllegalAccessException | SQLException e) {
            try {
                if(conn!=null) {
                    conn.close();
                }
                if (stmt!=null) {
                    stmt.close();
                }
                result.put(Variables.CODE_ERROR,1);
                result.put(Variables.MESSAGE_ERROR,e);
                e.printStackTrace();
            } catch (JSONException | SQLException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (mProgressDialog!= null){
            mProgressDialog.dismiss();
        }
        super.onPostExecute(jsonObject);
        delegate.UpdateValesCodcliFinish(jsonObject);
    }
}
