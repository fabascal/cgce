package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Razon_Social extends AppCompatActivity implements View.OnClickListener{
    Spinner spn_estacion;
    JSONObject cursor=null;
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;
    EditText et_razonsocial,et_rfc,et_calle,et_numext,et_numint,et_colonia,et_cp,et_localidad,et_municipio,et_estado,et_pais,et_telefono,et_cveest;
    Button btn_dfinserta;
    JSONObject data= new JSONObject();
    ValidateTablet tablet = new ValidateTablet();


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_razon__social);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        spn_estacion = (Spinner) findViewById(R.id.spn_estacion);
        et_razonsocial = (EditText) findViewById(R.id.et_razonsocial);
        et_rfc = (EditText) findViewById(R.id.et_rfc);
        et_cveest = (EditText) findViewById(R.id.et_cveest);
        et_calle = (EditText) findViewById(R.id.et_calle);
        et_numext = (EditText) findViewById(R.id.et_numext);
        et_numint = (EditText) findViewById(R.id.et_numint);
        et_colonia = (EditText) findViewById(R.id.et_colonia);
        et_cp = (EditText) findViewById(R.id.et_cp);
        et_localidad = (EditText) findViewById(R.id.et_localidad);
        et_municipio = (EditText) findViewById(R.id.et_municipio);
        et_estado = (EditText) findViewById(R.id.et_estado);
        et_pais = (EditText) findViewById(R.id.et_pais);
        et_telefono = (EditText) findViewById(R.id.et_telefono);
        btn_dfinserta =(Button) findViewById(R.id.btn_dfinserta);
        btn_dfinserta.setOnClickListener(this);
        String query = "select nombre from estacion";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            ArrayList<String> data = new ArrayList<String>();
            while (rs.next()) {
                String id = rs.getString("nombre");
                data.add(id);
            }
            String[] array = data.toArray(new String[0]);
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1, data);
            spn_estacion.setAdapter(NoCoreAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        spn_estacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String estacion = spn_estacion.getItemAtPosition(position).toString();
                String query = "SELECT df.id_estacion,df.razon_social as razon, df.rfc as rfc, df.calle as calle, df.num_exterior as exterior ,df.num_interior as interior,\n"+
                        "df.colonia as colonia, df.codigo_postal as cp, df.localidad as localidad, df.municipio as municipio, df.estado as estado, df.pais as pais,\n"+
                        "df.telefono as telefono, df.regimen_fiscal as regimen, df.cveest as cveest FROM datos_factura as df\n" +
                        "left outer join estacion as e on e.id=df.id_estacion\n" +
                        "where e.nombre='"+estacion+"'";
                try {
                    connect = control_gas();
                    stmt = connect.prepareStatement(query);
                    rs = stmt.executeQuery();
                    String nulo = "";
                    if (rs.next()) {
                        et_razonsocial.setText(rs.getString("razon"));
                        et_rfc.setText(rs.getString("rfc"));
                        et_cveest.setText(rs.getString("cveest"));
                        et_calle.setText( rs.getString("calle"));
                        et_numext.setText( rs.getString("exterior"));
                        et_numint.setText(rs.getString("interior"));
                        et_colonia.setText( rs.getString("colonia"));
                        et_cp.setText( rs.getString("cp"));
                        et_localidad.setText( rs.getString("localidad"));
                        et_municipio.setText( rs.getString("municipio"));
                        et_estado.setText( rs.getString("estado"));
                        et_pais.setText( rs.getString("pais"));
                        et_telefono.setText(rs.getString("telefono"));
                    }
                    else {
                        et_razonsocial.setText(nulo);
                        et_rfc.setText(nulo);
                        et_cveest.setText(nulo);
                        et_calle.setText( nulo);
                        et_numext.setText( nulo);
                        et_numint.setText(nulo);
                        et_colonia.setText( nulo);
                        et_cp.setText( nulo);
                        et_localidad.setText(nulo);
                        et_municipio.setText( nulo);
                        et_estado.setText( nulo);
                        et_pais.setText( nulo);
                        et_telefono.setText(nulo);
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }

                Toast.makeText(Razon_Social.this,estacion,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // sometimes you need nothing here
            }
        });
    }

    public Connection control_gas(){
        DataBaseManager manager = new DataBaseManager(getApplicationContext());
        cursor = manager.cargarcursorodbc2();
        String direccion=null;
        String puerto=null;
        String user=null;
        String base=null;
        String pass = null;
        try {
            direccion = cursor.getString("ip");
            puerto = cursor.getString("puerto");
            user = cursor.getString("userdb");
            base = cursor.getString("db");
            pass = cursor.getString("passdb");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        connect = CONN(user, pass, base, direccion, Integer.valueOf(puerto));
        return connect;
    }
    @SuppressLint("NewApi")
    private Connection CONN(String _user, String _pass, String _DB,
                            String _server, Integer _puerto) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnURL = "jdbc:jtds:sqlserver://" + _server + ":"+_puerto+";"
                    + "databaseName=" + _DB + ";user=" + _user + ";password="
                    + _pass + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
        }
        return conn;
    }
    public boolean actualizar_datos(JSONObject jsonObject){
        String query = null;
        try {
            query = "insert into datos_factura (id_estacion,razon_social,rfc,calle,num_exterior,num_interior,colonia,codigo_postal,localidad,municipio,estado,pais,telefono,regimen_fiscal,cveest)\n"+
                    "values("+jsonObject.getString("id_estacion").toUpperCase()+",'"+jsonObject.getString("razon").toUpperCase()+"','"+jsonObject.getString("rfc").toUpperCase()+"',\n"+
                    "'"+jsonObject.getString("calle").toUpperCase()+"','"+jsonObject.getString("exterior").toUpperCase()+"','"+jsonObject.getString("interior").toUpperCase()+"',\n"+
                    "'"+jsonObject.getString("colonia").toUpperCase()+"','"+jsonObject.getString("cp").toUpperCase()+"','"+jsonObject.getString("localidad").toUpperCase()+"',\n"+
                    "'"+jsonObject.getString("municipio").toUpperCase()+"','"+jsonObject.getString("estado").toUpperCase()+"','"+jsonObject.getString("pais").toUpperCase()+"',\n"+
                    "'"+jsonObject.getString("telefono").toUpperCase()+"','Regimen General de Ley, Personas Morales','"+jsonObject.getString("cveest")+"')";
            Log.w("qwery",query);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            stmt.execute();
            return true;
        }catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }
    public Integer busca_estacion (String estacion){
        Integer res=0;
        String query = "select id from estacion where nombre = '"+estacion+"'";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            if (rs.next()) {
                res=rs.getInt("id");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_dfinserta:
                try {
                    data.put("id_estacion",busca_estacion(String.valueOf(spn_estacion.getSelectedItem())));
                    data.put("razon",et_razonsocial.getText().toString());
                    data.put("rfc",et_rfc.getText().toString());
                    data.put("cveest",et_cveest.getText().toString());
                    data.put("calle",et_calle.getText().toString());
                    data.put("exterior",et_numext.getText().toString());
                    data.put("interior",et_numint.getText().toString());
                    data.put("colonia",et_colonia.getText().toString());
                    data.put("cp",et_cp.getText().toString());
                    data.put("localidad",et_localidad.getText().toString());
                    data.put("municipio",et_municipio.getText().toString());
                    data.put("estado",et_estado.getText().toString());
                    data.put("pais",et_pais.getText().toString());
                    data.put("telefono",et_telefono.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                actualizar_datos(data);
                break;
        }
    }
}
