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

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.ControlGasListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetDespachadorListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetEstacionDataListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.MacActivity;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetDespachador extends AsyncTask<String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    public GetDespachadorListener delegate=null;
    JSONObject result = new JSONObject();
    Connection conn = null;
    Statement stmt = null;
    MacActivity mac = new MacActivity();

    public GetDespachador(Activity activity, Context context){
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
        mProgressDialog.setMessage("Favor de esperar, estamos obteniendo informacion del despachador...");
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        ResultSet rs;
        DataBaseCG cg = new DataBaseCG();
        try {
            conn = cg.odbc_cecg_app(mContext);
            String query = "select d.nombre as despachador, d.id as id_despachador from despachadores d \n" +
                    "left outer join corte c on c.id_despachador=d.id \n" +
                    "left outer join dispositivos dis on dis.id=c.id_dispositivo\n " +
                    "where dis.mac_adr='"+mac.getMacAddress()+"' and c.status=0";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.put(Variables.NIP_DESPACHADOR, rs.getString("id_despachador"));
                result.put(Variables.KEY_TICKET_DESPACHADOR, rs.getString("despachador"));
                result.put(Variables.CODE_ERROR,0);
            }
            conn.close();
            stmt.close();
            rs.close();
        } catch (JSONException | ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            try {
                if(conn!=null) {
                    conn.close();
                }
                if (stmt!=null) {
                    stmt.close();
                }
                result.put(Variables.CODE_ERROR, 1);
                result.put(Variables.MESSAGE_ERROR, e);
                e.printStackTrace();
            }catch (JSONException | SQLException ex){
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
        delegate.processFinish(jsonObject);
    }
}
