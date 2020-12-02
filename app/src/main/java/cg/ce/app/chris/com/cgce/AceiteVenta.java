package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cg.ce.app.chris.com.cgce.ControlGas.GetPumpPosition;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetPumpPositionListener;
import cg.ce.app.chris.com.cgce.common.Variables;

public class AceiteVenta extends AppCompatActivity implements View.OnClickListener, GetPumpPositionListener {
    CardView cardViewContado,cardViewCredito;
    ValidateTablet tablet = new ValidateTablet();
    Spinner spn_dispensarios;
    ResultSet rs;
    Cursor c;
    Connection connect;
    PreparedStatement stmt;
    MacActivity mac = new MacActivity();
    LogCE logCE = new LogCE();
    Drawable icon;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        spn_dispensarios = (Spinner) findViewById(R.id.spn_dispensario);
        FillPosicion();
        cardViewContado = (CardView) findViewById(R.id.CardViewContado);
        cardViewContado.setOnClickListener(this);
        cardViewCredito = (CardView) findViewById(R.id.CardViewCredito);
        cardViewCredito.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent=null;
        String tipo_venta="0";
        switch (view.getId()) {
            case R.id.CardViewContado:
                tipo_venta="1";
                intent = new Intent(this, AceiteActivity.class);
                break;
            case R.id.CardViewCredito:
                tipo_venta="2";
                intent = new Intent(this, AceiteCreditoMetodo.class);
                break;
        }
        if (intent!=null){
            intent.putExtra("bomba",spn_dispensarios.getSelectedItem().toString());
            intent.putExtra("tipo_venta",tipo_venta);
            startActivity(intent);
        }
    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_aceite_venta);
                icon = getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_aceite_venta_repsol);
                icon = getDrawable(R.drawable.isologo_repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_aceite_venta_ener);
                icon = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_aceite_venta_total);
                icon = getDrawable(R.drawable.total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    public void FillPosicion(){
        GetPumpPosition getPumpPosition = new GetPumpPosition(this, getApplicationContext());
        getPumpPosition.delegate=this;
        getPumpPosition.execute(mac.getMacAddress());
    }

    @Override
    public void GetPumpPositionFinish(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt(Variables.CODE_ERROR)==0) {
                ArrayList<String> data = (ArrayList<String>) jsonObject.get(Variables.POSICIONES);
                ArrayAdapter NoCoreAdapter = new ArrayAdapter(getApplicationContext(),
                        R.layout.spinner_bombas, data);
                spn_dispensarios.setAdapter(NoCoreAdapter);
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + jsonObject.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(AceiteVenta.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(jsonObject.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(AceiteVenta.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }

    }
}
