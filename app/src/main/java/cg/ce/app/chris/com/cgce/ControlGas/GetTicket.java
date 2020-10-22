package cg.ce.app.chris.com.cgce.ControlGas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.ControlGasListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetTicket extends AsyncTask<String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    public ControlGasListener delegate=null;
    JSONObject cursor = null;
    JSONObject result = new JSONObject();
    /*peticion 0 es contado y 1 es credito*/
    int peticion=0;
    Statement stmt =null;
    Connection conn = null;


    public GetTicket(Activity activity, ControlGasListener delegate){
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
        mProgressDialog.setMessage("Favor de esperar, estamos obteniendo el servicio...");
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        peticion = Integer.parseInt(params[2]);
        DataBaseManager manager = new DataBaseManager(mContext);
        cursor = manager.cargarcursorodbc2();
        DataBaseCG dbcg = new DataBaseCG();
        ResultSet r;
        String base;
        try {
            base = cursor.getString("db_cg");
            conn = dbcg.odbc_cecg_app(mContext);
            stmt = conn.createStatement();
            r = stmt.executeQuery("SELECT top 1 desp.nrotrn,desp.can,desp.mto,desp.pre,prod.den,desp.nrobom,resp.den,\n" +
                    "Convert(VARCHAR(10), cast(cast(desp.fchtrn-1 as int) as datetime) , 111),desp.codprd,gas.cveest,desp.mtogto,desp.codcli,\n" +
                    "cli.den,desp.hratrn,desp.codgas,desp.codprd,desp.nroveh,desp.odm,desp.fchcor,desp.nrotur,desp.nrocte,cli.tipval,\n" +
                    "(select top 1 pre from ["+base+"].[dbo].[Precios] where codprd=desp.codprd and codgas=desp.codgas and fch<=desp.fchtrn order by fch desc),\n" +
                    "(select top 1 iva from ["+base+"].[dbo].[Precios] where codprd=desp.codprd and codgas=desp.codgas and fch<=desp.fchtrn order by fch desc) ,\n" +
                    "(select top 1 preiie from ["+base+"].[dbo].[Precios] where codprd=desp.codprd and codgas=desp.codgas and fch<=desp.fchtrn order by fch desc), " +
                    "desp.rut as rut  \n" +
                    "FROM ["+base+"].[dbo].[Despachos] as desp\n" +
                    "left outer join ["+base+"].[dbo].[Productos] as prod on prod.cod=desp.codprd \n" +
                    "left outer join ["+base+"].[dbo].[Responsables] as resp on resp.cod=desp.codres \n" +
                    "left outer join ["+base+"].[dbo].[Gasolineras] as gas on gas.cod=desp.codgas \n" +
                    "left outer join ["+base+"].[dbo].[Clientes] as cli on cli.cod=desp.codcli \n" +
                    "where desp.nrobom ="+params[0]+" order by desp.nrotrn desc");
                    /*"where desp.nrotrn='40022970' order by desp.nrotrn desc");*/
            if (r.next()) {
                float a = r.getFloat(11);
                if (a<0){
                    a=a*-1;
                }
                result.put(Variables.KEY_TICKET_NROTRN,r.getInt(1));
                result.put(Variables.KEY_TICKET_CANTIDAD,(r.getFloat(3)+a)/r.getFloat(4));
                result.put(Variables.KEY_TICKET_PRECIO,r.getFloat(4));
                result.put(Variables.KEY_TICKET_TOTAL,String.format("%.2f",r.getFloat(3)+a));
                result.put(Variables.KEY_TICKET_PRODUCTO,r.getString(5));
                result.put(Variables.KEY_TICKET_BOMBA,r.getInt(6));
                result.put(Variables.KEY_TICKET_DESPACHADOR, nombre_despachador(mContext,params[1]));
                result.put(Variables.KEY_TICKET_FECHA,r.getString(8));
                result.put(Variables.KEY_TICKET_ID_PRODUCTO,r.getInt(9));
                result.put(Variables.KEY_TICKET_CVEEST,r.getString(10));
                result.put(Variables.KEY_TICKET_CODCLI,r.getInt(12));
                result.put(Variables.KEY_TICKET_DENCLI,r.getString(13));
                result.put(Variables.KEY_TICKET_HORA,hora(String.valueOf(r.getInt(14))));
                result.put(Variables.KEY_TICKET_CODPRD,r.getInt(16));
                result.put(Variables.KEY_TICKET_NROVEH,r.getInt(17));
                result.put(Variables.KEY_ODM,r.getString(18));
                result.put(Variables.KEY_TICKET_FCHCOR,r.getString(19));
                result.put(Variables.KEY_TICKET_NROTUR,r.getString(20));
                result.put(Variables.KEY_TICKET_NROCTE,r.getString(21));
                result.put(Variables.KEY_TICKET_CLIENTE_TIPVAL,r.getInt(22));
                result.put(Variables.KEY_TICKET_CLIENTE_TIPVAL_DEN,CalculateMetoPago(String.valueOf(r.getInt(22))));
                result.put(Variables.KEY_RUT,r.getString("rut"));
                result.put(Variables.KEY_TICKET_IVA, r.getDouble(24));
                result.put(Variables.KEY_TICKET_IEPS, r.getDouble(25));
                result.put(Variables.NIP_DESPACHADOR,nip_desp(mContext,params[1]));
                result.put(Variables.CODE_ERROR,0);
            }
            stmt.close();
            conn.close();
            r.close();
        } catch (JSONException | SQLException | ClassNotFoundException | InstantiationException |
                IllegalAccessException | SocketException e) {
            try {
                stmt.close();
                conn.close();
                result.put(Variables.CODE_ERROR,1);
                result.put(Variables.MESSAGE_ERROR,e);
                e.printStackTrace();
            } catch (JSONException | SQLException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("GetTicket" + String.valueOf(result));
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
    public String nombre_despachador (Context con, String mac) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException{
        String res="DESPACHADOR";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        ResultSet r;
        String query = "select d.nombre as pass from despachadores d \n" +
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
        r.close();
        return res;
    }
    public String hora (String hora){
        if (hora.length()==1){
            hora="000"+hora;
        }else if (hora.length()==2){
            hora="00"+hora;
        }else if (hora.length()==3){
            hora="0"+hora;
        }
        String hora_impresa;
        hora_impresa=hora.substring(0,2)+":"+hora.substring(2,4);
        return hora_impresa;
    }
    public String nip_desp (Context con, String mac) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException, SocketException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        ResultSet r;
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
        r.close();
        return res;
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
