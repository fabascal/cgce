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
import java.util.ArrayList;
import java.util.Collections;

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.ControlGasListener;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.common.GetHour;
import cg.ce.app.chris.com.cgce.common.Variables;

public class GetVehicleRestrictions extends AsyncTask<String, Void, JSONObject> {

    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;
    DataBaseCG cg = new DataBaseCG();
    public ControlGasListener delegate=null;
    int []dias={1,2,4,8,16,32,64};
    Connection connection= null;
    Statement stmt = null;

    public GetVehicleRestrictions(Activity activity, Context context, ControlGasListener delegate) {
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = context;
        this.delegate = delegate;
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
        String tag = params[0];
        int diacar=0;
        ResultSet r;
        JSONObject res = new JSONObject();
        JSONObject result = new JSONObject();
        ArrayList<Integer>dias_carga = new ArrayList<>() ;

        try {
            connection = cg.odbc_cg(mContext);
            stmt = connection.createStatement();
            String query="";
            switch (params[1]){
                case Variables.KEY_NOMBRE:
                    query = "select cv.diacar as diacar,cv.codgas as codgas, getdate() as fecha,\n" +
                            "datepart(WEEKDAY,getdate()) as dia, cv.est as est,\n" +
                            "(select cod from Gasolineras where codest=0 and cod !=0) as cveest,\n" +
                            "cv.hraini as hraini,cv.hrafin as hrafin,cv.hraini2 as hraini2,\n" +
                            "cv.hrafin2 as hrafin2,cv.hraini3 as hraini3,cv.hrafin3 as hrafin3,\n" +
                            "cv.codprd as codprd,\n" +
                            "(select den from Productos where cod=codprd) as combustible\n" +
                            "from ClientesVehiculos as cv where cv.tar= '"+ tag +"'";
                    break;
                case Variables.KEY_RFID:
                    query = "select cv.diacar as diacar,cv.codgas as codgas, getdate() as fecha,\n" +
                            "datepart(WEEKDAY,getdate()) as dia, cv.est as est,\n" +
                            "(select cod from Gasolineras where codest=0 and cod !=0) as cveest,\n" +
                            "cv.hraini as hraini,cv.hrafin as hrafin,cv.hraini2 as hraini2,\n" +
                            "cv.hrafin2 as hrafin2,cv.hraini3 as hraini3,cv.hrafin3 as hrafin3," +
                            "cv.codprd as codprd,\n" +
                            "(select den from Productos where cod=codprd) as combustible\n" +
                            "from ClientesVehiculos as cv where cv.tag= '"+ tag +"'";
                    break;
                case Variables.KEY_NIP:
                    query = "select cv.diacar as diacar,cv.codgas as codgas, getdate() as fecha,\n" +
                            "datepart(WEEKDAY,getdate()) as dia, cv.est as est,\n" +
                            "(select cod from Gasolineras where codest=0 and cod !=0) as cveest,\n" +
                            "cv.hraini as hraini,cv.hrafin as hrafin,cv.hraini2 as hraini2,\n" +
                            "cv.hrafin2 as hrafin2,cv.hraini3 as hraini3,cv.hrafin3 as hrafin3,\n" +
                            "cv.codprd as codprd,\n" +
                            "(select den from Productos where cod=codprd) as combustible\n" +
                            "from ClientesVehiculos as cv where cv.tag= '"+ tag +"'";
                    break;
            }
            r = stmt.executeQuery(query);
            while (r.next()) {
                diacar= r.getInt("diacar");
                res.put("Dia",r.getString("dia"));
                res.put("Fecha",r.getDate("fecha"));
                res.put("EstPermitida", r.getString("codgas"));
                res.put("EstLocal",r.getString("cveest"));
                res.put("Est",r.getString("est"));
                res.put("hraini", r.getInt("hraini"));
                res.put("hrafin", r.getInt("hrafin"));
                res.put("hraini2", r.getInt("hraini2"));
                res.put("hrafin2", r.getInt("hrafin2"));
                res.put("hraini3", r.getInt("hraini3"));
                res.put("hrafin3", r.getInt("hrafin3"));
                res.put("HoraActual",Integer.valueOf(GetHour.hora_actual()));
                res.put("CodPrd",r.getInt("codprd"));
                res.put("Combustible",r.getString("combustible"));
            }
            int validador=0;
            for (int i=dias.length;i!=0;i--){
                validador+=dias[i-1];
                if (diacar>=validador){
                    dias_carga.add(i);
                }else{
                    validador-=dias[i-1];
                }
            }
            r.close();
            stmt.close();
            connection.close();
            Collections.sort(dias_carga);
            res.put("PermisoEstacion",res.getString("EstPermitida").equals(res.getString("EstLocal")));
            res.put("Array",dias_carga);
            result.put("Data",res);
            result.put(Variables.CODE_ERROR,0);
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException | SQLException | JSONException e) {
            try {
                connection.close();
                stmt.close();
                res.put(Variables.CODE_ERROR,1);
                res.put(Variables.MESSAGE_ERROR,e);
                e.printStackTrace();
            } catch (JSONException | SQLException ex) {
                ex.printStackTrace();
            }
        }
        Log.w("GetVehicle",String.valueOf(result));
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
