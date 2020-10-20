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

import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.common.Variables;

public class ValidarUltimoNROTRN extends AsyncTask<String, Void, JSONObject>{

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    public ControlGasListener delegate=null;

    public ValidarUltimoNROTRN(Activity activity, Context context, ControlGasListener delegate) {
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = context;
        this.delegate = delegate;
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("AsyncTask");
        // Progress dialog message
        mProgressDialog.setMessage("Favor de esperar, estamos validando informacion relevante...");
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String bomba = params[0];
        String resultado="";
        JSONObject res = new JSONObject();
        ResultSet r;
        try {
            Connection connection= cg.odbc_cg(mContext);
            Statement stmt = connection.createStatement();
            String query = "select top 1 nrotrn from Despachos where nrobom ="+bomba+" order by nrotrn desc";
            r = stmt.executeQuery(query);
            while (r.next()) {
                res.put(Variables.CODE_ERROR,0);
                res.put(Variables.KEY_ULT_NROTRN, r.getString("nrotrn"));
            }
            connection.close();
            r.close();
            stmt.close();
            connection.close();
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException |
                SQLException | JSONException e) {
            e.printStackTrace();
            try {
                res.put(Variables.CODE_ERROR,1);
                res.put(Variables.MESSAGE_ERROR,String.valueOf(e));
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        Log.w("async ultnrotrn",String.valueOf(res));
        return res;
    }

    @Override
    protected void onPostExecute(JSONObject s) {
        if (mProgressDialog!= null){
            mProgressDialog.dismiss();
        }
        System.out.println("async response");
        System.out.println(s);
        try {
            delegate.processFinish(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onPostExecute(s);
    }
}
