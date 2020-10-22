package cg.ce.app.chris.com.cgce.ControlGas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetPumpPositionListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.MacActivity;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetPumpPosition extends AsyncTask<String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    public GetPumpPositionListener delegate=null;
    Connection connect;
    PreparedStatement stmt;

    public GetPumpPosition(Activity activity, Context context) {
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
        mProgressDialog.setMessage("Favor de esperar, estamos validando las posiciones...");
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }
    @Override
    protected JSONObject doInBackground(String... params) {
        ResultSet rs;
        Cursor c;
        JSONObject res = new JSONObject();
        String query = "select p.numero_logico as logico from posicion as p \n" +
                "left outer join dispensario as disp on disp.id=p.id_dispensario\n" +
                "left outer join corte as c on c.id_dispensario=disp.id\n" +
                "left outer join dispositivos as dispo on dispo.id=c.id_dispositivo\n" +
                "where c.status =0 and dispo.mac_adr='"+params[0]+"'";
        try {
            DataBaseCG gc = new DataBaseCG();
            connect = gc.odbc_cecg_app(mContext);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            ArrayList<String> data = new ArrayList<String>();
            while (rs.next()) {
                data.add(rs.getString("logico"));
            }
            res.put(Variables.CODE_ERROR,0);
            res.put(Variables.POSICIONES,data);
            rs.close();
            stmt.close();
            connect.close();
        } catch (InstantiationException | JSONException | ClassNotFoundException |
                IllegalAccessException | SQLException e) {
            try {
                connect.close();
                stmt.close();
                res.put(Variables.CODE_ERROR,1);
                res.put(Variables.MESSAGE_ERROR,e);
                e.printStackTrace();
            } catch (JSONException | SQLException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }
    @Override
    protected void onPostExecute(JSONObject s) {
        if (mProgressDialog!= null){
            mProgressDialog.dismiss();
        }
        System.out.println("async response");
        System.out.println(s);
        delegate.GetPumpPositionFinish(s);
        super.onPostExecute(s);
    }
}
