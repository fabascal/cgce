package cg.ce.app.chris.com.cgce;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.epson.epos2.printer.Printer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

public class CinepolisBoleto extends DialogFragment implements View.OnClickListener, CinepolisAsyncResponse {

    View root;
    ImageButton imbtn_finalizar,imbtn_venta;
    TextView tvmail,tvfolio,tvprecio,tvboletos;
    JSONObject jsonrespuesta = new JSONObject();
    String mensaje=null;
    cgticket cgticket = new cgticket();
    private Printer mPrinter = null;
    Context context;
    String fecha= null;
    JSONObject res ;

    public static CinepolisBoleto newInstance(Context context){
        CinepolisBoleto fragment = new CinepolisBoleto();
        context=context;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        root = inflater.inflate(R.layout.cinepolisboleto, null);
        Bundle bundle = getArguments();

        tvmail = (TextView) root.findViewById(R.id.tvmail);
        tvfolio = (TextView) root.findViewById(R.id.tvfolio);
        tvprecio = (TextView) root.findViewById(R.id.tvprecio);
        tvboletos = (TextView) root.findViewById(R.id.tvboletos);
        tvmail.setText(bundle.getString("subject",""));
        tvfolio.setText(bundle.getString("folio",""));
        tvprecio.setText(bundle.getString("precio",""));
        tvboletos.setText(bundle.getString("boletos",""));
        imbtn_finalizar = (ImageButton) root.findViewById(R.id.imbtn_finalizar);
        imbtn_finalizar.setOnClickListener(this);
        imbtn_venta = (ImageButton) root.findViewById(R.id.imbtn_venta);
        imbtn_venta.setOnClickListener(this);
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
            case R.id.imbtn_finalizar:
                if (Float.parseFloat(tvboletos.getText().toString())>0){
                    ServiciosActivity serviciosActivity = new ServiciosActivity();
                    try {
                        serviciosActivity.sendMessage(tvmail.getText().toString(),tvfolio.getText().toString(),tvprecio.getText().toString(),mensaje,tvboletos.getText().toString(),cgticket.get_estacion(getActivity()));
                    } catch (ClassNotFoundException | SQLException | java.lang.InstantiationException | JSONException | IllegalAccessException e) {
                        new android.support.v7.app.AlertDialog.Builder(CinepolisBoleto.this.getActivity())
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        e.printStackTrace();
                    }
                    try {
                        cgticket.guardarnrotrn3(getActivity(),tvfolio.getText().toString(),tvmail.getText().toString(),tvboletos.getText().toString(),tvprecio.getText().toString() ,3);
                    } catch (ClassNotFoundException | SQLException | java.lang.InstantiationException |JSONException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(getActivity(),VentaActivity.class);
                startActivity(intent);
                break;
            case R.id.imbtn_venta:
                Cinepolis cinepolis = new Cinepolis(getActivity(),tvmail.getText().toString(),getActivity());
                cinepolis.delegate = this;
                cinepolis.execute();
                //getDialog().dismiss();
                break;
        }
    }
    public String calculafolio(String folio){
        String folios=null;
        if (tvfolio.length()>0){
            folios=tvfolio.getText().toString()+","+folio;
        }else{
            folios=folio;
        }
        return folios;
    }
    public String calculaprecio(String precio){
        return String.valueOf(Float.parseFloat(tvprecio.getText().toString())+Float.parseFloat(precio));
    }
    public String calculanumentradas(String numentrada){
        return String.valueOf(Float.parseFloat(tvboletos.getText().toString())+Float.parseFloat(numentrada));
    }
    @Override
    public void processFinish(String output){
        toJson tojson = new toJson();
        jsonrespuesta=tojson.strtojson(output);

        try {
            res=new JSONObject(String.valueOf(jsonrespuesta.getString("0")));
            tvfolio.setText(calculafolio(res.getString("folio")));
            tvprecio.setText(calculaprecio(res.getString("precio")));
            tvboletos.setText(calculanumentradas(res.getString("numEntradas")));
            mensaje=res.getString("descripcion");
            fecha=res.getString("fecha");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}
