package cg.ce.app.chris.com.cgce.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cg.ce.app.chris.com.cgce.AdapterCustomerVale.CustomerAdapterRV;
import cg.ce.app.chris.com.cgce.DataCustomerCG;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.common.Variables;

public class RefreshCustomerVale extends DialogFragment {

    ConstraintLayout root;
    private Context mContext = null;
    Drawable icon;
    ImageView icon_root;
    RecyclerView mRVCustomerCG;
    CustomerAdapterRV mAdapter;
    List<DataCustomerCG> dataCustomerCG;


    public static RefreshCustomerVale newInstance(String title, List<DataCustomerCG> js){
        RefreshCustomerVale fragment = new RefreshCustomerVale();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.PassJson(js);
        fragment.setArguments(args);
        return fragment;
    }
    public void PassJson(List<DataCustomerCG> dataCustomerCG){
        this.dataCustomerCG = dataCustomerCG;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        root = (ConstraintLayout) inflater.inflate(R.layout.dialog_refreshcustomervale, null);
        mContext = this.getActivity();
        icon_root = (ImageView) root.findViewById(R.id.icon);

        mRVCustomerCG = (RecyclerView) root.findViewById(R.id.rvclientes);
        mAdapter = new CustomerAdapterRV(mContext, dataCustomerCG);
        mRVCustomerCG.setAdapter(mAdapter);
        mRVCustomerCG.setLayoutManager(new LinearLayoutManager(mContext));
        BrandSharedPreferences();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new AlertDialog.Builder(getActivity())
                    .setView(root)
                    .setIcon(icon)
                    .create();
        }
        return null;
    }
    public void BrandSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (Objects.requireNonNull(sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express"))) {
            case "Combu-Express":
                icon_root.setImageDrawable(getActivity().getDrawable(R.drawable.combuito));
                icon = getActivity().getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                icon_root.setImageDrawable(getActivity().getDrawable(R.drawable.isologo_repsol));
                icon = getActivity().getDrawable(R.drawable.isologo_repsol);
                break;
            case "Ener":
                icon_root.setImageDrawable(getActivity().getDrawable(R.drawable.logo_impresion_ener));
                icon = getActivity().getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                icon_root.setImageDrawable(getActivity().getDrawable(R.drawable.total));
                icon = getActivity().getDrawable(R.drawable.total);
                break;
        }
    }
}
