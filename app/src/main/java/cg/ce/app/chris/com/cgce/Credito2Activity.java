package cg.ce.app.chris.com.cgce;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cg.ce.app.chris.com.cgce.dialogos.close_credito;

public class Credito2Activity extends AppCompatActivity implements View.OnClickListener {
    ValidateTablet tablet = new ValidateTablet();
    boolean IsTablet = false;
    Spinner spn_dispensarios;
    ResultSet rs;
    JSONObject cursor=null;
    Connection connect;
    PreparedStatement stmt;
    ArrayList<String> data = new ArrayList<String>();
    JSONArray jsonArray = new JSONArray();
    List<JSONObject> myJSONObjects = new  ArrayList<JSONObject>() ;
    final static String TAG_POSICION="posicion";
    CardView rfid, nip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credito2);
        spn_dispensarios = (Spinner) findViewById(R.id.spn_dispensario);
        rfid = (CardView) findViewById(R.id.CardViewRFID);
        rfid.setOnClickListener(this);
        nip = (CardView) findViewById(R.id.CardViewNIP);
        nip.setOnClickListener(this);
        /*funcion para la orientacion de la pantalla*/
        setOrientation();
        /*funcion para llenar las posiciones del dispositivo*/
        fillPosicion();
        /*funcion paArrayList<String> data = new ArrayList<String>();NObject por cada posicion*/
        try {
            createElementsPosicion();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*funcion para coordinar los elementos*/
        try {
            setLayoutElements();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*se llama la posicion del spinner para mostrar la interfaz en funcino de la posicion*/
        spn_dispensarios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    setLayoutElements();
                } catch (JSONException e) {
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
    @Override
    public void onBackPressed() {
        Toast.makeText(Credito2Activity.this,
                "No puedes regresar en este menu!!!, favor de usar el icono de cerrar en la parte superior",
                Toast.LENGTH_SHORT).show();
    }

    private void setOrientation(){
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Log.w("Tableta","es Tableta");
            IsTablet=true;
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            IsTablet=false;
            Log.w("Tableta","no es Tableta");
        }
    }
    private void fillPosicion(){
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

            while (rs.next()) {
                String id = rs.getString("logico");
                data.add(id);
            }
            connect.close();
            stmt.close();
            rs.close();
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    R.layout.spinner_bombas, data);
            spn_dispensarios.setAdapter(NoCoreAdapter);
        } catch (SQLException e) {

        }
    }
    public void createElementsPosicion() throws JSONException {
        for (int i = 0; i<data.size(); i++){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(TAG_POSICION,data.get(i));
            jsonArray.put(jsonObject);
            myJSONObjects.add(jsonObject);
        }
        System.out.println(jsonArray);
    }
    public void setLayoutElements() throws JSONException {
        int posicion = spn_dispensarios.getSelectedItemPosition();
        boolean metodo = jsonArray.getJSONObject(posicion).has("metodo");
        if (metodo){
            rfid.setVisibility(View.GONE);
            nip.setVisibility(View.GONE);
        }else{
            rfid.setVisibility(View.VISIBLE);
            nip.setVisibility(View.VISIBLE);
        }

    }
    /*funcion para actualizar el listado de json*/
    public void updateJson(int position,String label, String value) throws JSONException {
        jsonArray.getJSONObject(position).put(label,value);

        System.out.println("Update js :" + jsonArray);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.CardViewRFID:
                try {
                    updateJson(spn_dispensarios.getSelectedItemPosition(),"metodo","rfid");
                    setLayoutElements();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.CardViewNIP:
                    try {
                        updateJson(spn_dispensarios.getSelectedItemPosition(),"metodo","nip");
                        setLayoutElements();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                break;
        }
    }
}
