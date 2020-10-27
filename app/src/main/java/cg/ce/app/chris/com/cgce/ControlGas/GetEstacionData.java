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

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetEstacionDataListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetEstacionData extends AsyncTask <String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    public GetEstacionDataListener delegate=null;
    JSONObject result = new JSONObject();
    Connection conn = null;
    Statement stmt = null;

    public GetEstacionData(Activity activity, Context context){
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
        mProgressDialog.setMessage("Favor de esperar, estamos obteniendo informacion de la estacion...");
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
            String query = "select df.calle as calle,df.num_exterior as exterior,df.num_interior as interior,df.colonia as colonia,df.codigo_postal as cp,\n" +
                    "df.localidad as localidad,df.municipio as municipio,df.estado as estado,df.pais as pais,df.rfc as rfc,df.telefono as telefono,\n" +
                    "df.regimen_fiscal as regimen,df.cveest as cveest,e.nombre as estacion  \n" +
                    "from datos_factura as df left outer join estacion as e on e.id=df.id_estacion";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.put("calle",rs.getString("calle"));
                result.put("exterior",rs.getString("exterior"));
                result.put("interior",rs.getString("interior"));
                result.put("colonia",rs.getString("colonia"));
                result.put("cp",rs.getString("cp"));
                result.put("localidad",rs.getString("localidad"));
                result.put("municipio",rs.getString("municipio"));
                result.put("estado",rs.getString("estado"));
                result.put("pais",rs.getString("pais"));
                result.put("rfc",rs.getString("rfc"));
                result.put("telefono",rs.getString("telefono"));
                result.put("regimen",rs.getString("regimen"));
                result.put("cveest",rs.getString("cveest"));
                result.put("estacion",rs.getString("estacion"));
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
        delegate.GetEstacionDataFinish(jsonObject);
    }
}
