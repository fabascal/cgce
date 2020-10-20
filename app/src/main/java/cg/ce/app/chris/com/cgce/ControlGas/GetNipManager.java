package cg.ce.app.chris.com.cgce.ControlGas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.LogCE;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetNipManager extends AsyncTask<String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    public ControlGasListener delegate=null;
    JSONObject cursor = null;
    ResultSet r;
    JSONObject result = new JSONObject();
    LogCE logCE = new LogCE();

    public GetNipManager(Activity activity, Context context, ControlGasListener delegate) {
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
        mProgressDialog.setMessage("Favor de esperar, Obteniendo permisos...");
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        DataBaseManager manager = new DataBaseManager(mContext);
        cursor = manager.cargarcursorodbc2();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = null;
        Statement stmt =null;
        try {
            conn = dbcg.odbc_cecg_app(mContext);
            stmt = conn.createStatement();
            String query = "select top 1 pass as pass from despachadores where gerente = 1 ";
            r = stmt.executeQuery(query);
            while (r.next()){
                result.put(Variables.CODE_ERROR,0);
                result.put(Variables.NIP_MANAGER,r.getString("pass"));
            }
            conn.close();
            stmt.close();
            r.close();
        } catch (JSONException | ClassNotFoundException | InstantiationException |
                IllegalAccessException | SQLException e) {
            try {
                result.put(Variables.CODE_ERROR,1);
                result.put(Variables.MESSAGE_ERROR,e);
                logCE.EscirbirLog2(mContext,"GetNipManager - " + e);
                e.printStackTrace();
            } catch (JSONException ex) {
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
        try {
            delegate.processFinish(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onPostExecute(jsonObject);
    }
}
