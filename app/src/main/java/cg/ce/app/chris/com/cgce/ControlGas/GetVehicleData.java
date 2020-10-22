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

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetVehicleDataListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetVehicleData extends AsyncTask <String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    public GetVehicleDataListener delegate=null;
    JSONObject result = new JSONObject();
    Connection connection= null;
    Statement stmt = null;

    public GetVehicleData(Activity activity, Context context) {
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
        mProgressDialog.setMessage("Favor de esperar, estamos obteniendo informacion del vehiculo...");
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        ResultSet r;
        DataBaseCG cg = new DataBaseCG();

        try {
            connection = cg.odbc_cg(mContext);
            stmt = connection.createStatement();
            String query = "select top 1 d.nrotrn,cv.plc,cv.rsp,cv.nroeco,d.odm,cv.codcli,cv.nroveh," +
                    "d.codcli,d.nroveh, cv.tar from Despachos as d\n" +
                    "inner join ClientesVehiculos as cv on d.nroveh=cv.nroveh and d.codcli=cv.codcli\n" +
                    "where d.nrotrn="+params[0]+" and d.nrobom="+params[1]+" order by d.nrotrn desc";
            r = stmt.executeQuery(query);
            if(!r.next()){
                result.put("placa","Sin Placa");
                result.put("rsp","Sin Chofer");
                result.put("nroeco","Sin No° Economico");
                result.put("ultodm","Sin Odometro");
                result.put("tar",0);
            }else{
                result.put("placa", r.getString("plc"));
                result.put("rsp", r.getString("rsp"));
                result.put("nroeco", r.getString("nroeco"));
                result.put("ultodm", r.getString("odm"));
                result.put("tar",r.getString("tar"));
            }
            result.put(Variables.CODE_ERROR,0);
            connection.close();
            stmt.close();
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException |
                SQLException | JSONException e) {
            try {
                connection.close();
                stmt.close();
                result.put(Variables.CODE_ERROR,1);
                result.put(Variables.MESSAGE_ERROR,e);
                e.printStackTrace();
            } catch (JSONException | SQLException ex) {
                ex.printStackTrace();
            }

        }
        System.out.println("GetVehicleData" + result);
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (mProgressDialog!= null){
            mProgressDialog.dismiss();
        }
        delegate.GetVehicleDataFinish(jsonObject);
        super.onPostExecute(jsonObject);
    }
}
