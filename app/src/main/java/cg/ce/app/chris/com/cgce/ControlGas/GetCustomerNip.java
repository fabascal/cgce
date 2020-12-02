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
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetCustomerNipListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.DataCustomerCG;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetCustomerNip extends AsyncTask< String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    public GetCustomerNipListener delegate=null;
    JSONObject result = new JSONObject();
    Connection conn = null;
    Statement stmt = null;

    public GetCustomerNip(Activity activity){
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = activity.getApplicationContext();
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("CombuGo");
        // Progress dialog message
        mProgressDialog.setMessage("Favor de esperar, estamos obteniendo datos del cliente mediante NIP...");
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        List<DataCustomerCG> dataCustomerCGS = new ArrayList<>();
        ResultSet rs;
        DataBaseCG cg = new DataBaseCG();
            try {
            conn = cg.odbc_cg(mContext);
            String query = "select v.rsp as rsp, v.plc as plc, v.den as den_vehicle, v.tar as tar, " +
                    "v.nroveh as nroveh, c.cod as codcli, c.den as den, c.rfc as rfc, v.nroeco as nroeco,c.tipval as tipval " +
                    "from ClientesVehiculos as v left outer join Clientes as c on c.cod=v.codcli " +
                    "where v.tag='" + params[0] + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()){
                DataCustomerCG data = new DataCustomerCG();
                data.codcli = rs.getString("codcli");
                data.den = rs.getString("den");
                data.rfc = rs.getString("rfc");
                data.rsp = rs.getString("rsp");
                data.plc = rs.getString("plc");
                data.den_vehicle = rs.getString("den_vehicle");
                data.tar = rs.getInt("tar");
                data.nroveh = rs.getInt("nroveh");
                data.nroeco = rs.getString("nroeco");
                data.tipval = CalculateMetoPago(String.valueOf(rs.getInt("tipval")));
                dataCustomerCGS.add(data);
            }
            conn.close();
            stmt.close();
            result.put(Variables.CODE_ERROR,0);
            result.put(Variables.DATA_CUSTOMER,dataCustomerCGS);
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException |
                SQLException | JSONException e) {
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
        delegate.GetCustomerNipFinish(jsonObject);
        super.onPostExecute(jsonObject);
    }
    public String CalculateMetoPago(String data){
        String res = "Cliente Contado";
        switch (data){
            case "3":
                res = "Cliente Credito";
                break;
            case "4":
                res = "Cliente Debito";
                break;
            case "0":
                res = "Cliente Contado";
                break;
        }
        return res;
    }
}
