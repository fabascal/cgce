package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cg.ce.app.chris.com.cgce.common.UtilsBrand;

public class MarcaActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    ValidateTablet tablet = new ValidateTablet();
    TextView tvCfdiURL;
    Spinner spn_brand;
    ImageView ivBrandImage;
    Button btn_actualizar;
    cgticket cg = new cgticket();
    JSONObject jsActivity = new JSONObject();
    private final String COMBU_URL="combuurl";
    private final String REPSOL_URL="repsolurl";
    private final String ENER_URL="enerurl";
    private final String TOTAL_URL="totalurl";

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marca);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        tvCfdiURL = ( TextView ) findViewById(R.id.tvCfdiURL);
        btn_actualizar = ( Button ) findViewById(R.id.btn_actualizar);
        spn_brand = (Spinner) findViewById(R.id.spn_brand);
        ivBrandImage = (ImageView) findViewById(R.id.ivBrandImage);
        btn_actualizar.setOnClickListener(this);
        spn_brand.setOnItemSelectedListener(this);
        List<String> Brands = new ArrayList<String>();
        Brands.add("Combu-Express");
        Brands.add("Repsol");
        Brands.add("Ener");
        Brands.add("Total");
        ArrayAdapter <String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, Brands);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_brand.setAdapter(dataAdapter);
        ShowDataPreference();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_actualizar:
                try {
                    SaveDataPreference();
                    String e = "Datos guardados exitosamente.";
                    new AlertDialog.Builder(MarcaActivity.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                } catch (JSONException e) {
                    new AlertDialog.Builder(MarcaActivity.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                }
                /*Intent intent = new Intent(this,MarcaUpdateActivity.class);
                startActivity(intent);*/
                break;
        }

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MarcaActivity.this,MainConfiguracionActivity.class);
        startActivity(intent);
    }
    private void ShowDataPreference(){
        Log.w("Js_Show",String.valueOf(jsActivity));
        SharedPreferences sharedPreferences = getSharedPreferences("Brand",Context.MODE_PRIVATE);
        tvCfdiURL.setText(sharedPreferences.getString(getResources().getString(R.string.CfdiURL),COMBU_URL));
        int spn_position = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.BrandID),"0"));
        spn_brand.setSelection(spn_position);
        int BrandImage = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.BrandImage),String.valueOf(R.drawable.logo_impresion)));

        ivBrandImage.setImageResource(BrandImage);
    }
    private void SaveDataPreference() throws JSONException {
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getResources().getString(R.string.BrandID),jsActivity.
                getString(getResources().getString(R.string.BrandID)));
        editor.putString(getResources().getString(R.string.BrandName),jsActivity.
                getString(getResources().getString(R.string.BrandName)));
        editor.putString(getResources().getString(R.string.CfdiURL),jsActivity.
                getString(getResources().getString(R.string.CfdiURL)));
        editor.putString(getResources().getString(R.string.BrandImage),
                jsActivity.getString(getResources().getString(R.string.BrandImage)));
        editor.commit();
        ShowDataPreference();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            jsActivity.put(getResources().getString(R.string.BrandID),position);
            jsActivity.put(getResources().getString(R.string.BrandName),
                    parent.getItemAtPosition(position).toString());
            switch (parent.getItemAtPosition(position).toString()) {
                case "Combu-Express":
                    jsActivity.put(getResources().getString(R.string.CfdiURL),COMBU_URL);
                    jsActivity.put(getResources().getString(R.string.BrandImage),R.drawable.logo_impresion);
                    tvCfdiURL.setText(COMBU_URL);
                    ivBrandImage.setImageResource(R.drawable.logo_impresion);
                    break;
                case "Repsol":
                    jsActivity.put(getResources().getString(R.string.CfdiURL),REPSOL_URL);
                    jsActivity.put(getResources().getString(R.string.BrandImage),R.drawable.logo_impresion_repsol);
                    tvCfdiURL.setText(REPSOL_URL);
                    ivBrandImage.setImageResource(R.drawable.logo_impresion_repsol);
                    break;
                case "Ener":
                    jsActivity.put(getResources().getString(R.string.CfdiURL),ENER_URL);
                    jsActivity.put(getResources().getString(R.string.BrandImage),R.drawable.logo_impresion_ener);
                    tvCfdiURL.setText(ENER_URL);
                    ivBrandImage.setImageResource(R.drawable.logo_impresion_ener);
                    break;
                case "Total":
                    jsActivity.put(getResources().getString(R.string.CfdiURL),TOTAL_URL);
                    jsActivity.put(getResources().getString(R.string.BrandImage),R.drawable.logo_impresion_total);
                    tvCfdiURL.setText(TOTAL_URL);
                    ivBrandImage.setImageResource(R.drawable.logo_impresion_total);
                    break;
            }
        } catch (JSONException e) {
            new AlertDialog.Builder(MarcaActivity.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
