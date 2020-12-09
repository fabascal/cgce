package cg.ce.app.chris.com.cgce.ControlGas.Listeners;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.LogCE;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetCorte extends AsyncTask<String, Void, JSONObject> {

    JSONObject cursor = null;
    ResultSet r;
    JSONObject result = new JSONObject();
    LogCE logCE = new LogCE();
    Connection conn = null;
    Statement stmt =null;
    private Context mContext;



    public GetCorte(Context context){
        this.mContext = context;
    }
    @Override
    protected JSONObject doInBackground(String... params) {
        DataBaseManager manager = new DataBaseManager(mContext);
        cursor = manager.cargarcursorodbc2();
        DataBaseCG dbcg = new DataBaseCG();
        try {
            conn = dbcg.odbc_cecg_app(mContext);
            stmt = conn.createStatement();
            String base = null;
            base = cursor.getString("db");
            String query = "SELECT top 1 c.status FROM ["+base+"].[dbo].[corte] as c " +
                    "left outer join ["+base+"].[dbo].[dispositivos] as d on d.id=c.id_dispositivo " +
                    "where c.status = 0 and d.mac_adr = '"+params[0]+"'";
            r = stmt.executeQuery(query);
            if (!r.next()){
                result.put(Variables.CODE_ERROR,0);
                result.put(Variables.CORTE,1);
            }else {
                result.put(Variables.CODE_ERROR,0);
                result.put(Variables.CORTE,r.getInt("status"));
            }
            conn.close();
            stmt.close();
            r.close();
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
                logCE.EscirbirLog2(mContext,"GetCorte - " + e);
                e.printStackTrace();
            } catch (JSONException | SQLException ex) {
                ex.printStackTrace();
            }

        }

        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
    }
}
