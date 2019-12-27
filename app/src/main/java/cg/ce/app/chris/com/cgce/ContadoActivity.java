package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class ContadoActivity extends AppCompatActivity {
    ImageButton imbtn_ticket,imbtn_cfdi;
    Spinner spn_dispensarios;
    ResultSet rs;
    Cursor c;
    Connection connect;
    PreparedStatement stmt;
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contado);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        sensores.bluetooth();
        sensores.wifi(this,true);
        imbtn_cfdi = (ImageButton) findViewById(R.id.imbtn_cfdi);
        imbtn_ticket = (ImageButton) findViewById(R.id.imbtn_ticket);
        spn_dispensarios = (Spinner) findViewById(R.id.spn_dispensario);
        addListenerOnButton();
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
        } catch (SQLException e) {

            e.printStackTrace();
        }
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

                        /*Bundle bundle = new Bundle();
                        JSONObject jsonObject = new JSONObject();
                        try {
                            bundle.putString("bomba",jsonObject.put("bomba",String.valueOf(spn_dispensarios.getSelectedItem().toString())).toString());
                        } catch (JSONException e) {
                            Crashlytics.log(e.toString());
                            e.printStackTrace();
                        }
                        ticketFragment ticketfragment = new ticketFragment();
                        ticketfragment.setArguments(bundle);
                        transaction.add(R.id.activity_contado,ticketfragment);
                        break;*/
                }

                transaction.commit();
            }
        });
    }
}
