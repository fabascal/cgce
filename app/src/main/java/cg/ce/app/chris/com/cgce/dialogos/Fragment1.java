package cg.ce.app.chris.com.cgce.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cg.ce.app.chris.com.cgce.ClassImpresionCorteCinepolis;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.LogCE;
import cg.ce.app.chris.com.cgce.MacActivity;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.VentaActivity;
import cg.ce.app.chris.com.cgce.cgticket;

import static cg.ce.app.chris.com.cgce.R.layout.dialog_singout;


/**
 * Created by chris on 22/03/17.
 */

public class Fragment1 extends DialogFragment {
    MacActivity macActivity = new MacActivity();
    cgticket cg = new cgticket();
    public static Fragment1 newInstance(String title){
        Fragment1 fragment = new Fragment1();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        String title = getArguments().getString("title");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new AlertDialog.Builder(getActivity())
                    .setView(dialog_singout)
                    .setIcon(R.drawable.combuito)
                    .setTitle(title)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            Dialog f = (Dialog) dialog;
                            EditText contrasena_input = (EditText) f.findViewById(R.id.contrasena_input);
                            Log.w("cont", String.valueOf(contrasena_input.getText()));
                            Boolean nip_val = validar_nip(String.valueOf(contrasena_input.getText()));
                            Log.w("nip", String.valueOf(nip_val));
                            if (nip_val == true) {
                                corte_cinepolis();
                                corte_finalizar();


                                Intent i = getActivity().getPackageManager()
                                        .getLaunchIntentForPackage( getActivity().getPackageName() );
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                //((VentaActivity)getActivity()).doPositiveClick();
                            }else if(nip_val==false){
                                Toast.makeText(getActivity(),"Nip Erroneo!!!",Toast.LENGTH_SHORT).show();
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("metodo","cierre sesion");
                                    jsonObject.put("data",String.valueOf(contrasena_input.getText()));
                                    jsonObject.put("state","false");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                LogCE logCe=new LogCE();
                                logCe.EscirbirLog(getActivity(),jsonObject);
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            ((VentaActivity)getActivity()).doNegativeClick();
                        }
                    })
                    .create();
        }
        return null;
    }
    public boolean validar_nip(String nip){
        ResultSet r;
        Boolean res=false;
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(getActivity());
        String query = "select c.id as id from corte as c\n" +
                "left outer join despachadores as d on d.id=c.id_despachador\n" +
                "left outer join dispositivos as disp on disp.id=c.id_dispositivo\n" +
                "where c.status=0 and disp.mac_adr='"+macActivity.getMacAddress()+"' and d.pass='"+nip+"'";
        Log.w("qwery_cerrar",query);
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = true;
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }
    public boolean corte_cinepolis(){
        JSONObject js = cg.corte_cinepolis(getActivity());
        JSONObject js2 = cg.corte_datos((getActivity()));
        try {
            js.put("despachador",cg.nombre_depsachador(getActivity()));
            js.put("hora_entrada",js2.getString("hora_entrada"));
            js.put("hora_salida",js2.getString("hora_salida"));
            Log.w("newJs", js.toString());
            ClassImpresionCorteCinepolis classImpresionCorteCinepolis = new ClassImpresionCorteCinepolis(getActivity(),getActivity(),js);
            classImpresionCorteCinepolis.execute();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }
    public boolean corte_finalizar () {
        String Log_state="";

        LogCE logce= new LogCE();
        JSONObject jsonLog = new JSONObject();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(getActivity());
        String query = "update c set c.status=1,c.hora_salida=getdate() \n" +
                "from corte c\n" +
                "left outer join dispositivos d on d.id=c.id_dispositivo \n" +
                "where d.mac_adr='"+macActivity.getMacAddress()+"' and c.status=0";
        Log.w("qwery_cerrar",query);
        try {
            jsonLog.put("metodo","finalizar corte");
            jsonLog.put("data",query);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            conn.close();
            try {
                jsonLog.put("state","true");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.w("combu", "true");
            logce.EscirbirLog(getActivity(),jsonLog);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                jsonLog.put("state","false");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            Log.w("combu", "false");
            logce.EscirbirLog(getActivity(),jsonLog);
            return false;
        }

    }
}
