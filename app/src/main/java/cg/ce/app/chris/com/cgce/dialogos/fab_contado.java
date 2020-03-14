package cg.ce.app.chris.com.cgce.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;

import cg.ce.app.chris.com.cgce.ClassImpresionTicket;
import cg.ce.app.chris.com.cgce.LogCE;
import cg.ce.app.chris.com.cgce.Printing.TicketPrint;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.cgticket;


/**
 * Created by chris on 17/08/17.
 */

public class fab_contado extends DialogFragment implements View.OnClickListener{

    Spinner mSpinner;
    ImageButton imgbtn_cancel,imgbtn_imprimir;
    private static ArrayList<String>bombas;
    LinearLayout root;
    JSONObject servicio = null;
    String tur;
    int tiptrn;
    Spinner spn_metodo;

    public static fab_contado newInstance(String title, ArrayList<String> data){
        fab_contado fragment = new fab_contado();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        bombas=data;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        root = (LinearLayout) inflater.inflate(R.layout.dialog_fab_contado, null);
        spn_metodo = root.findViewById(R.id.spn_metedo);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(root.getContext(),
                R.array.mPagos_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_tiptrn);
// Apply the adapter to the spinner
        spn_metodo.setAdapter(adapter);
        mSpinner=(Spinner)root.findViewById(R.id.spn_fab_contado);
        ArrayAdapter NoCoreAdapter = new ArrayAdapter(root.getContext(),
                R.layout.spinner_bombas_dialog, bombas);
        mSpinner.setAdapter(NoCoreAdapter);
        imgbtn_cancel = (ImageButton) root.findViewById(R.id.imgbtn_cancel);
        imgbtn_cancel.setOnClickListener(this);
        imgbtn_imprimir = (ImageButton) root.findViewById(R.id.imgbtn_imprimir);
        imgbtn_imprimir.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new AlertDialog.Builder(getActivity())
                    .setView(root)
                    .setIcon(R.drawable.combuito)
                    .create();
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgbtn_imprimir:
                cgticket ticket = new cgticket();
                LogCE logCE = new LogCE();
                try {
                    servicio = ticket.consulta_servicio(getActivity(), String.valueOf(mSpinner.getSelectedItem()));
                    Integer impreso=ticket.cant_impreso(getActivity(),servicio.getString("nrotrn"));
                    /*
                    * se crea validacion en base a si el ticket ha  sido impreso,
                    * si impreso es igual a 10 el ticket no existe en la base cecg_app
                    * por ende nunca ha sido impreso, por lo que se tiene que imprimir el original;
                    * si impreso es igual a 0 quiere decir que se inicio el proceso de impresion en alguna ocacion pero no se completo
                    * por "X" motivo por lo que existe el registro en la base mas nunca se logro la impresion, por lo que se tiene que imprimir el original;
                    * si impreso es igual a 1 quiere decir que el ticket ya fue impreso por lo que se tiene que imprimir una copia del mismo.
                     */
                    Log.w("impreso",impreso.toString());
                    servicio.put("impreso",impreso);
                    servicio.put("tipo_venta",1);
                    TicketPrint print = null;
                    //se valida el metodo de pago
                    if (spn_metodo.getSelectedItem().toString().equals("Efectivo")){
                        tur="1|Efectivo";
                        tiptrn=49;
                    }else if(spn_metodo.getSelectedItem().toString().equals("T. Credito")){
                        tur="2|T. Credito";
                        tiptrn=51;
                    }else if(spn_metodo.getSelectedItem().toString().equals("T. Debito")){
                        tur="3|T. Debito";
                        tiptrn=51;
                    }else if(spn_metodo.getSelectedItem().toString().equals("Anticipos")){
                        tur="4|Anticipos";
                        tiptrn=50;
                    }else if(spn_metodo.getSelectedItem().toString().equals("Combu-Vale")){
                        tur="5|Combu-Vale";
                        tiptrn=50;
                    }
                    servicio.put("rut",tur);
                    servicio.put("tiptrn",tiptrn);
                    if (impreso == 10){
                        /*
                        * guardamos el ticket en la base cecg_app para poder iniciar el proceso de impresion
                         */
                        ticket.guardarnrotrn(getActivity().getApplicationContext(), servicio,servicio.getInt("tipo_venta"));
                        /*
                        * iniciamos la comunicacion con impresora y la impresion del despacho
                         */
                        ClassImpresionTicket impresionTicket= new ClassImpresionTicket(getActivity(),getActivity(),imgbtn_imprimir,servicio);
                        impresionTicket.execute();
                    }else if(impreso.toString() == "0"){
                        ClassImpresionTicket impresionTicket= new ClassImpresionTicket(getActivity(),getActivity(),imgbtn_imprimir,servicio);
                        impresionTicket.execute();
                    }else {
                        ClassImpresionTicket impresionTicket= new ClassImpresionTicket(getActivity(),getActivity(),imgbtn_imprimir,servicio);
                        impresionTicket.execute();
                    }
                    getDialog().dismiss();
                    Log.w("servicio",servicio.toString());
                } catch (SQLException | IllegalAccessException | java.lang.InstantiationException | ClassNotFoundException e) {
                    try {
                        JSONObject jsonError = new JSONObject(e.toString());
                        logCE.EscirbirLog(getActivity(),jsonError);
                        e.printStackTrace();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                } catch (JSONException e) {
                    try {
                        JSONObject jsonError = new JSONObject(e.toString());
                        logCE.EscirbirLog(getActivity(),jsonError);
                        e.printStackTrace();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
                break;
            case R.id.imgbtn_cancel:
                getDialog().dismiss();
                break;
        }
    }
}
