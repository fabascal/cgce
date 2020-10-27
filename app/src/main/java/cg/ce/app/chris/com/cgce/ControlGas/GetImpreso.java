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

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetImpresoListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetImpreso extends AsyncTask <String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    Integer res=10;
    Connection conn = null;
    Statement stmt;
    JSONObject cursor = null;
    ResultSet r;
    JSONObject result = new JSONObject();
    public GetImpresoListener delegate = null;

    public GetImpreso(Activity activity, Context context) {
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = context;
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("CombuGo");
        // Progress dialog message
        mProgressDialog.setMessage("Favor de esperar, Obteniendo estatus de impresion...");
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }
    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            conn = cg.odbc_cecg_app(mContext);
            DataBaseManager manager = new DataBaseManager(mContext);
            cursor = manager.cargarcursorodbc2();
            String query = "select impreso from despachos where nrotrn="+params[0]+"";
            Log.w("query impreso", query);
            stmt = conn.createStatement();
            r = stmt.executeQuery(query);
            if (r.next()) {
                res = r.getInt("impreso");
            }
            result.put(Variables.CODE_ERROR,0);
            result.put(Variables.KEY_IMPRESO,res);
            conn.close();
            stmt.close();
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
        System.out.println("GetImpreso " + result);
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (mProgressDialog!= null){
            mProgressDialog.dismiss();
        }
        super.onPostExecute(jsonObject);
        delegate.GetImpresoFinish(jsonObject);
    }
}
