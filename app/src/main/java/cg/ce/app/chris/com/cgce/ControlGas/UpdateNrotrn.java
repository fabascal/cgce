package cg.ce.app.chris.com.cgce.ControlGas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.UpdateNrotrnListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.MacActivity;
import cg.ce.app.chris.com.cgce.ValidarDispositivo;
import cg.ce.app.chris.com.cgce.common.Variables;

public class UpdateNrotrn extends AsyncTask <JSONObject, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    Connection conn = null;
    Statement stmt = null;
    JSONObject cursor = null;
    ResultSet r;
    JSONObject result = new JSONObject();
    public UpdateNrotrnListener delegate = null;
    String mac, venta;

    public UpdateNrotrn (Activity activity, Context context, String mac, String venta){
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = context;
        this.mac = mac;
        this.venta = venta;
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("CombuGo");
        // Progress dialog message
        mProgressDialog.setMessage("Favor de esperar, actualizando servicio...");
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(JSONObject... ticket) {
        try {
            UpdateControGas(ticket[0]);
            UpdateCecg_App(ticket[0]);
            result.put(Variables.CODE_ERROR,0);
        } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException | SocketException e) {
            try {
                result.put(Variables.CODE_ERROR,1);
                result.put(Variables.MESSAGE_ERROR,e);
                e.printStackTrace();
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (mProgressDialog!= null){
            mProgressDialog.dismiss();
        }
        super.onPostExecute(jsonObject);
        delegate.UpdateNrotrnFinish(jsonObject);
    }
    private void UpdateControGas(JSONObject ticket) throws JSONException, ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException, SocketException {
        DataBaseManager manager = new DataBaseManager(mContext);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db_cg");
        ResultSet r = null;
        JSONObject st=new JSONObject();
        ValidarDispositivo vd = new ValidarDispositivo();
        DataBaseCG dbcg = new DataBaseCG();
        conn = dbcg.odbc_cg(mContext);
        stmt = conn.createStatement();
        String ticket2=ticket.getString(Variables.KEY_TICKET_NROTRN)+"0";
        System.out.println("UpdateNrotrn" + ticket);
        if (ticket.has(Variables.KEY_RUT)){
            if(ticket.getString(Variables.KEY_RUT).substring(0,1).equals("6")){
                stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set codres='"+
                        getCodDespCG()+"' , rut='"+ ticket.getString(Variables.KEY_RUT) +"', tiptrn= "+
                        ticket.getInt(Variables.KEY_TIPTRN) +" where nrotrn = "+ticket.getString(Variables.KEY_TICKET_NROTRN)+"");
            }else{
                stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+"," +
                        "codres='"+getCodDespCG()+"' , rut='"+ ticket.getString(Variables.KEY_RUT) +"', " +
                        "tiptrn= "+ ticket.getInt(Variables.KEY_TIPTRN) +" where nrotrn = "+ticket.getString(Variables.KEY_TICKET_NROTRN)+"");
            }

        }else{
            System.out.println("identificacion es credito");
            stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+", " +
                    "codres='"+getCodDespCG()+"' where nrotrn = "+ticket.getString(Variables.KEY_TICKET_NROTRN)+"");
        }
        stmt.close();
        conn.close();
    }
    private void UpdateCecg_App(JSONObject ticket) throws JSONException, SQLException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, SocketException {
        DataBaseManager manager = new DataBaseManager(mContext);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        ValidarDispositivo vd = new ValidarDispositivo();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(mContext);
        String ticket2= ticket.getString(Variables.KEY_TICKET_NROTRN)+"0";
        String query = "insert into ["+base+"].[dbo].[despachos] (nrotrn,nota,corte,impreso,tipo_venta," +
                "flotillero) values("+ticket.getString(Variables.KEY_TICKET_NROTRN)+","+ticket2+","+get_corte()+",0,"+venta+",0)";
        Statement stmt = conn.createStatement();
        Log.w("query", query);
        stmt.execute(query);
        stmt.close();
        conn.close();
    }
    public String getCodDespCG () throws ClassNotFoundException, SQLException,
            InstantiationException, IllegalAccessException, JSONException, SocketException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(mContext);
        String query = "select cod as cod from Responsables where tag = '"+nip_desp()+"'";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("cod");
        }
        conn.close();
        stmt.close();
        Log.w("rut", res);
        return res;
    }
    public String nip_desp () throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException, SocketException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(mContext);
        String query = "select d.pass as pass from despachadores d \n" +
                "left outer join corte c on c.id_despachador=d.id \n" +
                "left outer join dispositivos dis on dis.id=c.id_dispositivo\n " +
                "where dis.mac_adr='"+mac+"' and c.status=0";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("pass");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public Integer get_corte() throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException, SocketException {
        Integer res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(mContext);
        DataBaseManager manager = new DataBaseManager(mContext);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        String query = "select c.id as cor from ["+base+"].[dbo].[corte ] as c\n" +
                "left outer join dispositivos as d on d.id=c.id_dispositivo\n" +
                "where d.mac_adr='"+mac+"' and c.status=0";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        while (r.next()) {
            res = r.getInt("cor");
        }
        conn.close();
        stmt.close();
        Log.w("qwery",query);
        Log.w("res", String.valueOf(res));
        return res;
    }
}
