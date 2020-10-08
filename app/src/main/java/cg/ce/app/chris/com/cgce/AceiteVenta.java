package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AceiteVenta extends AppCompatActivity implements View.OnClickListener{
    CardView cardViewContado,cardViewCredito;
    ValidateTablet tablet = new ValidateTablet();
    Spinner spn_dispensarios;
    ResultSet rs;
    Cursor c;
    Connection connect;
    PreparedStatement stmt;

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
                String id = rs.getString("logico");
                data.add(id);
            }
            String[] array = data.toArray(new String[0]);
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    R.layout.spinner_bombas, data);
            connect.close();
            spn_dispensarios.setAdapter(NoCoreAdapter);
        } catch (SQLException | JSONException e) {
            new AlertDialog.Builder(AceiteVenta.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }

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
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_aceite_venta_repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_aceite_venta_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_aceite_venta_total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
