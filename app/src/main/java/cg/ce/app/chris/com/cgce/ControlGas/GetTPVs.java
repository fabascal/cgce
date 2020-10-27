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
import java.util.ArrayList;
import java.util.List;

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.ControlGasListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.TPVs;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetTPVs extends AsyncTask <String,Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    public ControlGasListener delegate=null;
    JSONObject cursor = null;
    JSONObject result = new JSONObject();
    Statement stmt =null;
    Connection conn = null;

    public GetTPVs(Activity activity, Context context, ControlGasListener delegate){
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
        mProgressDialog.setMessage("Favor de esperar, estamos obteniendo los datos...");
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        DataBaseManager manager = new DataBaseManager(mContext);
        cursor = manager.cargarcursorodbc2();
        DataBaseCG dbcg = new DataBaseCG();

        ResultSet rs;
        String base;
        List<TPVs> tpVsList;
        ArrayList<String> data = new ArrayList<String>();
        tpVsList=new ArrayList<>();
        JSONObject tpvs = new JSONObject();
        DataBaseCG cg = new DataBaseCG();
        try {
            conn = cg.odbc_cecg_app(mContext);
            String query = "select id,nombre from tpv where bancaria = '"+params[0]+"' and activo =1";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()){
                String id = rs.getString("nombre");
                data.add(id);
            }
            conn.close();
            stmt.close();
            rs.close();
            result.put(Variables.CODE_ERROR,0);
            result.put(Variables.TPV_LIST,data);
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
        delegate.processFinish(jsonObject);
        super.onPostExecute(jsonObject);
    }
}
