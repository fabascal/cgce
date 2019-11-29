package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cg.ce.app.chris.com.cgce.dialogos.close_credito;

public class VentaTPVBomba extends AppCompatActivity implements View.OnClickListener{
    TextView tpv_nombre;
    JSONObject tpv_data;
    Sensores sensores = new Sensores();
    ImageButton imbtn_ticket,imbtn_cfdi;
    Spinner spn_dispensarios;
    ResultSet rs;
    Cursor c;
    Connection connect;
    PreparedStatement stmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta_tpvbomba);
        sensores.bluetooth();
        sensores.wifi(this,true);
        tpv_nombre = (TextView) findViewById(R.id.tpv_nombre);
        imbtn_cfdi = (ImageButton) findViewById(R.id.imbtn_cfdi);
        imbtn_cfdi.setOnClickListener(this);
        imbtn_ticket = (ImageButton) findViewById(R.id.imbtn_ticket);
        imbtn_ticket.setOnClickListener(this);
        spn_dispensarios = (Spinner) findViewById(R.id.spn_dispensario);
        try {
            tpv_data = new JSONObject(getIntent().getStringExtra("tpv_data"));
            tpv_nombre.setText(tpv_data.getString("nombre"));
            if (tpv_data.getInt("se_factura")==0){
                imbtn_cfdi.setVisibility(View.GONE);
            } else if(tpv_data.getInt("se_factura")==1){
                imbtn_cfdi.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            spn_dispensarios.setAdapter(NoCoreAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_credito, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                Intent intent = new Intent(VentaTPVBomba.this,VentaActivity.class);
                startActivity(intent);
                close_credito dialogFragment = close_credito
                        .newInstance();
                dialogFragment.show(getFragmentManager(), "dialog");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (view.getId()) {

            case R.id.imbtn_ticket:

                Bundle bundle = new Bundle();
                try {
                    tpv_data.put("bomba",spn_dispensarios.getSelectedItem().toString());
                    bundle.putString("bomba",tpv_data.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bundle.putString("json_data",tpv_data.toString());
                ticketFragment ticketfragment = new ticketFragment();
                ticketfragment.setArguments(bundle);
                transaction.add(R.id.activity_venta_tpvbomba,ticketfragment);
                break;
        }
        transaction.commit();
    }
    /*
    @Override
    public void onBackPressed() {
        Toast.makeText(VentaTPVBomba.this,"No puedes regresar en este menu!!!, favor de usar el icono de cerrar en la parte superior",Toast.LENGTH_SHORT).show();
    }*/
}
