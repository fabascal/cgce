package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import org.json.JSONException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CreditoActivity extends AppCompatActivity {
    ImageButton imbtn_ticket;
    Spinner spn_dispensarios;
    ResultSet rs;
    Cursor c;
    Connection connect;
    PreparedStatement stmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credito);
        spn_dispensarios = (Spinner) findViewById(R.id.spn_dispensario);
        addListenerOnButton();
        MacActivity mac = new MacActivity();
        String query = "select p.numero_logico as logico from posicion as p \n" +
                "left outer join dispensario as disp on disp.id=p.id_dispensario\n" +
                "left outer join corte as c on c.id_dispensario=disp.id\n" +
                "left outer join dispositivos as dispo on dispo.id=c.id_dispositivo\n" +
                "where c.status =0 and dispo.mac_adr='"+mac.getMacAddress()+"'";
        try {
            DataBaseCG cg = new DataBaseCG();
            connect = cg.control_gas(getApplicationContext());
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            ArrayList<String> data = new ArrayList<String>();
            while (rs.next()) {
                String id = rs.getString("logico");
                data.add(id);
            }
            connect.close();
            stmt.close();
            rs.close();
            String[] array = data.toArray(new String[0]);
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1, data);
            spn_dispensarios.setAdapter(NoCoreAdapter);
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
    }
    private void addListenerOnButton() {
        imbtn_ticket = (ImageButton) findViewById(R.id.imbtn_ticket);
        imbtn_ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (v.getId()) {
                    case R.id.imbtn_ticket:
                        Intent intent=null;
                        intent = new Intent(CreditoActivity.this,CreditoMetodo.class);
                        intent.putExtra("bomba", spn_dispensarios.getSelectedItem().toString());
                        startActivity(intent);
                }

                transaction.commit();
            }
        });
    }
}
