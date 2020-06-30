package cg.ce.app.chris.com.cgce.dialogos;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cg.ce.app.chris.com.cgce.AceiteActivity;
import cg.ce.app.chris.com.cgce.AceiteList;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.listeners.StringListener;


public class AceiteCantidad extends AppCompatDialogFragment {
    private AceiteCantidadListener listener;
    private ArrayList<AceiteList> aceiteLists;
    private TextView tv_denominacion, tv_precio, tv_cantidad, tv_total;
    Button decrease, increase;
    Integer codprd,cantidad;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_aceitecantidad, null);

        final Bundle bundle = getArguments();


        builder.setView(view)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        listener.applyTexts(codprd,cantidad);
                    }
                });
        cantidad = bundle.getInt("cantidad");
        codprd = bundle.getInt("codprd");
        tv_denominacion = view.findViewById(R.id.tv_denominacion);
        tv_denominacion.setText(bundle.getString("descripcion"));
        tv_precio = view.findViewById(R.id.tv_precio);
        tv_precio.setText(String.valueOf(bundle.getDouble("precio")));
        tv_cantidad = view.findViewById(R.id.tv_cantidad);
        tv_cantidad.setText(String.valueOf(bundle.getInt("cantidad")));
        tv_total = view.findViewById(R.id.tv_total);
        tv_total.setText(String.valueOf(bundle.getInt("cantidad") * bundle.getDouble("precio")));
        decrease = view.findViewById(R.id.decrease);
        increase = view.findViewById(R.id.increase);
        decrease.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                CharSequence value = tv_cantidad.getText();
                Minus_Click(Integer.parseInt(value.toString()),bundle.getDouble("precio"));
            }
        });
        increase.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                CharSequence value = tv_cantidad.getText();
                Plus_Click(Integer.parseInt(value.toString()),bundle.getDouble("precio"));
            }
        });


        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AceiteCantidadListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement AceiteCantidadListener");
        }
    }

    public interface AceiteCantidadListener{

        void applyTexts(Integer codprd, Integer cantidad);
    }

    public void Plus_Click(Integer cant, Double precio){
        cant += 1;
        if (cant<=100) {
            cantidad=cant;
            tv_cantidad.setText(String.valueOf(cant));
            tv_total.setText(String.valueOf(cant * precio));
        }else{
            String e = "No puedes vender mas de 100 piezas";
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error)
                    .setMessage(e)
                    .setPositiveButton(R.string.btn_ok,null).show();
        }
    }
    public void Minus_Click(Integer cant, Double precio){
        cant -= 1;
        if (cant>=1) {
            cantidad=cant;
            tv_cantidad.setText(String.valueOf(cant ));
            tv_total.setText(String.valueOf(cant * precio));

        }else{
            String e = "No puedes vender 0 piezas";
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error)
                    .setMessage(e)
                    .setPositiveButton(R.string.btn_ok,null).show();
        }
    }

}
