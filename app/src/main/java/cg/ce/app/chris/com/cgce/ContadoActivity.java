package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cg.ce.app.chris.com.cgce.ControlGas.ControlGasListener;
import cg.ce.app.chris.com.cgce.ControlGas.GetPumpPosition;
import cg.ce.app.chris.com.cgce.common.Variables;


public class ContadoActivity extends AppCompatActivity {
    ImageButton imbtn_ticket,imbtn_cfdi;
    Spinner spn_dispensarios;
    ResultSet rs;
    Cursor c;
    Connection connect;
    PreparedStatement stmt;
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    LogCE logCE = new LogCE();
    Drawable icon;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        sensores.bluetooth();
        sensores.wifi(this,true);
        imbtn_cfdi = (ImageButton) findViewById(R.id.imbtn_cfdi);
        imbtn_ticket = (ImageButton) findViewById(R.id.imbtn_ticket);
        spn_dispensarios = (Spinner) findViewById(R.id.spn_dispensario);
        addListenerOnButton();
        MacActivity mac = new MacActivity();
        new GetPumpPosition(this, getApplicationContext(), new ControlGasListener() {
            @Override
            public void processFinish(JSONObject output) {
                try {
                    if (output.getInt(Variables.CODE_ERROR)==0){
                        ArrayList<String> data = (ArrayList<String>) output.get(Variables.POSICIONES);
                        ArrayAdapter NoCoreAdapter = new ArrayAdapter(getApplicationContext(),
                                R.layout.spinner_bombas, data);
                        spn_dispensarios.setAdapter(NoCoreAdapter);
                    }else{
                        logCE.EscirbirLog2(getApplicationContext(),"ContadoActivity_GetPumpPosition - " +
                                output.getString(Variables.MESSAGE_ERROR));
                        new AlertDialog.Builder(ContadoActivity.this)
                                .setTitle(R.string.error)
                                .setIcon(icon)
                                .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                .setPositiveButton(R.string.btn_ok,null).show();
                    }
                } catch (JSONException e) {
                    logCE.EscirbirLog2(getApplicationContext(),"ContadoActivity_GetPumpPosition - " + e);
                    new AlertDialog.Builder(ContadoActivity.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }
            }
        }).execute(mac.getMacAddress());
    }

    private void addListenerOnButton() {
        imbtn_ticket = (ImageButton) findViewById(R.id.imbtn_ticket);
        imbtn_cfdi = (ImageButton) findViewById(R.id.imbtn_cfdi);

        imbtn_cfdi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (v.getId()) {
                    case R.id.imbtn_cfdi:
                        Intent intent=null;
                        intent = new Intent(getApplicationContext(),ClienteBusqueda.class);
                        intent.putExtra("bomba", spn_dispensarios.getSelectedItem().toString());
                        startActivity(intent);
                }

                transaction.commit();
            }
        });
        imbtn_ticket.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (v.getId()) {
                    case R.id.imbtn_ticket:
                        Intent intent = new Intent(ContadoActivity.this,ActivityTicket.class);
                        intent.putExtra("bomba",spn_dispensarios.getSelectedItem().toString());
                        startActivity(intent);
                }

                transaction.commit();
            }
        });
    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_contado);
                icon = getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_contado_repsol);
                icon = getDrawable(R.drawable.isologo_repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_contado_ener);
                icon = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_contado_total);
                icon = getDrawable(R.drawable.total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
