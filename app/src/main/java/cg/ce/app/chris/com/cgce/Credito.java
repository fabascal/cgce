package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cg.ce.app.chris.com.cgce.dialogos.close_credito;

public class Credito extends AppCompatActivity implements View.OnClickListener {
    ValidateTablet tablet = new ValidateTablet();
    Spinner spn_posicion;
    Drawable icon;
    ImageButton btn_print;
    JSONObject Posiciones = new JSONObject();
    JSONArray Logicos = new JSONArray();
    final static String POSICION = "Posicion";
    final static String POSICIONES = "Posiciones";
    CardView CardViewRFID, CardViewNIP, CardViewNOMBRE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        spn_posicion = findViewById(R.id.spn_posicion);
        FillPosicion();
        CardViewRFID = findViewById(R.id.CardViewRFID);
        CardViewNIP = findViewById(R.id.CardViewNIP);
        CardViewNOMBRE = findViewById(R.id.CardViewNOMBRE);
        CardViewRFID.setOnClickListener((View.OnClickListener) this);
        CardViewNIP.setOnClickListener((View.OnClickListener) this);
        CardViewNOMBRE.setOnClickListener((View.OnClickListener) this);
        btn_print = findViewById(R.id.btncredito);
        spn_posicion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    PosicionesIncludes();
                } catch (JSONException e) {
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_credito, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                close_credito dialogFragment = close_credito
                        .newInstance();
                dialogFragment.show(getFragmentManager(), "dialog");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void BtnCredito(View view){
        Toast.makeText(this,"Pressed", Toast.LENGTH_LONG).show();

    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.AppThemeCredito);
                setContentView(R.layout.activity_credito);
                icon = getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_credito);
                icon = getDrawable(R.drawable.isologo_repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_credito);
                icon = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_credito);
                icon = getDrawable(R.drawable.total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    @Override
    public void onBackPressed() {
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        String Text = "No puedes regresar en este menu!!!, favor de usar el icono de cerrar en la parte superior";
        new AlertDialog.Builder(Credito.this)
                .setTitle(R.string.error)
                .setIcon(icon)
                .setMessage(Text)
                .setPositiveButton(R.string.btn_ok,null).show();
    }
    private void PosicionesIncludes() throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(POSICIONES);
        for ( int Posicion = 0; Posicion < Validar.length(); Posicion ++){
            if (Validar.getJSONObject(Posicion).getString(POSICION)==spn_posicion.getSelectedItem().toString()) {
                Log.w("Bomba", Validar.getJSONObject(Posicion).getString(POSICION));
            }
        }
    }
    public void FillPosicion(){
        ResultSet rs;
        Cursor c;
        Connection connect;
        PreparedStatement stmt;
        MacActivity mac = new MacActivity();
        String query = "select p.numero_logico as logico from posicion as p \n" +
                "left outer join dispensario as disp on disp.id=p.id_dispensario\n" +
                "left outer join corte as c on c.id_dispensario=disp.id\n" +
                "left outer join dispositivos as dispo on dispo.id=c.id_dispositivo\n" +
                "where c.status =0 and dispo.mac_adr='"+mac.getMacAddress()+"'";
        try {
            DataBaseCG gc = new DataBaseCG();
            connect = gc.control_gas(getApplicationContext());
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();

            ArrayList<String> data = new ArrayList<String>();
            while (rs.next()) {
                JSONObject Posicion = new JSONObject();
                String id = rs.getString("logico");
                data.add(id);
                Posicion.put(POSICION,rs.getInt("logico"));
                Logicos.put(Posicion);
                Posiciones.put(POSICIONES,Logicos);
            }
            Log.w("Posiciones", String.valueOf(Posiciones));
            String[] array = data.toArray(new String[0]);
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    R.layout.spinner_bombas_credito, data);
            connect.close();
            spn_posicion.setAdapter(NoCoreAdapter);
        } catch (SQLException | JSONException e) {
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.CardViewRFID:
                Toast.makeText(this,"RFID",Toast.LENGTH_LONG).show();
                break;
            case R.id.CardViewNIP:
                Toast.makeText(this,"NIP",Toast.LENGTH_LONG).show();
                break;
            case R.id.CardViewNOMBRE:
                Toast.makeText(this,"NOMBRE",Toast.LENGTH_LONG).show();
                break;
        }
    }
}
