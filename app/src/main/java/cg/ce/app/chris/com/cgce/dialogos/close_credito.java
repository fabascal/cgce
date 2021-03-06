package cg.ce.app.chris.com.cgce.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Objects;

import cg.ce.app.chris.com.cgce.Credito;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.VentaActivity;

/**
 * Created by chris on 4/09/17.
 */

public class close_credito extends DialogFragment implements View.OnClickListener{
    LinearLayout root;
    ImageButton btn_regresar,btn_cancelar;
    String marca;
    Drawable icon;
    ImageView ivicon;

    public static close_credito newInstance(){
        close_credito fragment = new close_credito();
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        root = (LinearLayout) inflater.inflate(R.layout.dialog_close_credito, null);
        BrandSharedPreferences();
        ivicon = (ImageView) root.findViewById(R.id.icon);
        ivicon.setImageDrawable(icon);
        btn_regresar = (ImageButton) root.findViewById(R.id.btn_regresar);
        btn_regresar.setOnClickListener(this);
        btn_cancelar = (ImageButton) root.findViewById(R.id.btn_cancelar);
        btn_cancelar.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new AlertDialog.Builder(getActivity())
                    .setView(root)
                    .setIcon(icon)
                    .create();
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_regresar:
                Intent intent = new Intent(getActivity(),VentaActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_cancelar:
                getDialog().dismiss();
                break;
        }

    }
    public void BrandSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Brand", Context.MODE_PRIVATE);
        marca=sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express");
        switch (Objects.requireNonNull(sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express"))) {
            case "Combu-Express":
                icon = getActivity().getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                icon = getActivity().getDrawable(R.drawable.isologo_repsol);
                break;
            case "Ener":
                icon = getActivity().getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                icon = getActivity().getDrawable(R.drawable.total);
                break;
        }
    }
}
