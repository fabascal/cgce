package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static cg.ce.app.chris.com.cgce.LoginDespachador_busqueda.CONNECTION_TIMEOUT;
import static cg.ce.app.chris.com.cgce.LoginDespachador_busqueda.READ_TIMEOUT;

public class Login_Despachador extends AppCompatActivity implements View.OnClickListener{
    Button btn_despachador_registrar;
    JSONObject cursor=null;
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;
    Spinner spn_dispensario;
    private EditText et_facturaweb,et_despachador;
    JSONObject json_data = new JSONObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login__despachador);
        Bundle bundle = getIntent().getExtras();
        btn_despachador_registrar = (Button) findViewById(R.id.btn_despachador_registrar);
        btn_despachador_registrar.setOnClickListener(Login_Despachador.this);
        spn_dispensario = ( Spinner )findViewById(R.id.spn_dispensario);
        et_facturaweb = (EditText) findViewById(R.id.et_facturaweb);
        et_despachador = (EditText) findViewById(R.id.et_despachador);

        String query = "select d.numero_logico as logico from dispensario as d where id not in(select distinct c.id_dispensario from corte as c where c.status=0)";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            ArrayList<String> data = new ArrayList<String>();
            while (rs.next()) {
                String id = rs.getString("logico");
                data.add(id);
            }
            String[] array = data.toArray(new String[0]);

            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1, data);
            spn_dispensario.setAdapter(NoCoreAdapter);
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(bundle.getString("msg")!= null)
        {
            Toast.makeText(this,bundle.getString("msg"),Toast.LENGTH_SHORT).show();
        }
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
    private class AsyncLogin extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(Login_Despachador.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://factura.combuexpress.mx/kioscoce/despachadores-search_fa.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("cveest", params[0])
                        .appendQueryParameter("password", params[1]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.w("result",result.toString());
                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if(result.equals("no rows")) {
                Toast.makeText(Login_Despachador.this, "No se encontro usuario de Facturacion Web", Toast.LENGTH_LONG).show();
            }else {

                try {

                    JSONArray jArray = new JSONArray(result);
                    for (int i = 0; i < jArray.length(); i++) {
                        json_data = jArray.getJSONObject(i);
                        Log.w("json", json_data.getString("id"));
                    }

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Log.w("ERR", e.toString());
                    Log.w("ERR", result.toString());

                }
            }
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_despachador_registrar:
                Log.w("web","btn_ok");
                if (et_facturaweb.getText().length()==0){
                    Toast.makeText(Login_Despachador.this, "Favor de escribir un NIP Facturacion", Toast.LENGTH_SHORT).show();
                }else if (et_despachador.getText().length()==0) {
                    Toast.makeText(Login_Despachador.this, "Favor de escribir un clave de despachador", Toast.LENGTH_SHORT).show();
                }
                else if(et_facturaweb.getText().length()!=0 || et_despachador.getText().length()!=0) {
                    Log.w("web", "inicia");
                    //obtener datos
                    final String facturaweb = et_facturaweb.getText().toString();
                    final String cveest = cveest();
                    // inciar  AsyncLogin() a factura web con datos otenidos
                    new AsyncLogin().execute(cveest, facturaweb);
                    Integer clave = id_despachador(et_despachador.getText());
                    if (clave == 0) {
                        Toast.makeText(Login_Despachador.this, "Favor de escribir un clave de despachador valida", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            Log.w("dispositivo", id_dispositivo().toString());
                            json_data.put("clave_despachador", clave);
                            json_data.put("dispositivo", id_dispositivo().toString());
                            json_data.put("dispensario", id_dispensario(spn_dispensario.getSelectedItem().toString()));
                            //validacion despachador y dispensario
                            Log.w("desp", String.valueOf(validar_despachador(clave)));
                            Log.w("disp", String.valueOf(validar_dispensario(json_data.getInt("dispensario"))));
                            if (validar_despachador(clave) == 0 && validar_dispensario(json_data.getInt("dispensario")) == 0) {
                                et_despachador.setText("");
                                et_facturaweb.setText("");
                                String msj="";
                                JSONObject bomba_llena=new JSONObject();
                                bomba_llena=datos_bomba_llena(json_data.getInt("dispensario"));
                                msj="Dispensario "+json_data.getInt("dispensario")+
                                        " se encuentra con el corte ID:(" +
                                        bomba_llena.getString("corte")+
                                        ") con operador "+bomba_llena.getString("despachador")+
                                        " con fecha de alta "+bomba_llena.getString("inicio");
                                for (int j=0;j<2;j++){
                                    Toast.makeText(Login_Despachador.this, msj, Toast.LENGTH_LONG).show();
                                }
                            } else if (validar_despachador(clave) == 1 && validar_dispensario(json_data.getInt("dispensario")) == 1) {
                                //alta de corte
                                alta_corte(json_data);
                                Intent ventas = new Intent(Login_Despachador.this, VentaActivity.class);
                                startActivity(ventas);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
        }
    }
    public Boolean alta_corte (JSONObject jsonObject){
        String query = null;
        String data_log="";
        LogCE logce= new LogCE();
        JSONObject jsonLog = new JSONObject();

        try {
            query = "insert into corte (id_despachador,id_dispensario,id_dispositivo,hora_entrada,status)\n"+
                    "values("+jsonObject.getString("clave_despachador")+","+jsonObject.getString("dispensario")+","+jsonObject.getString("dispositivo")+",getdate(),default)";
            data_log=jsonObject.getString("clave_despachador")+"|"+jsonObject.getString("dispensario");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.w("quwery",query);
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            stmt.execute();
            Calendar c = Calendar.getInstance();
            System.out.println("Current time => "+c.getTime());

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());
            try {
                jsonLog.put("fecha",formattedDate);
                jsonLog.put("metodo","alta corte");
                jsonLog.put("data",data_log);
                jsonLog.put("state","true");
                //logce.escribirFicheroMemoriaInterna(getApplicationContext(),jsonLog);
                logce.EscirbirLog(getApplicationContext(),jsonLog);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            connect.close();


            return true;
        }catch(SQLException e){
            e.printStackTrace();

            try {
                jsonLog.put("state","false");
                logce.EscirbirLog(getApplicationContext(),jsonLog);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            return false;
        }
    }
    public Integer validar_despachador (Integer despachador){
        Integer res=1;
        String query = "select top 1 status from corte where id_despachador="+despachador+" order by hora_entrada desc";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                res = rs.getInt("status");
            }
            connect.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        try {
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }
    public Integer validar_dispensario (Integer dispensario){
        Integer res=1   ;
        String query = "select top 1 status from corte where id_dispensario="+dispensario+"  order by hora_entrada desc";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                res = rs.getInt("status");
            }
            connect.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return res;
    }
    public String cveest (){
        String cveest ="";
        String query = "select top 1 cveest from datos_factura";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                cveest = rs.getString("cveest");
            }
            connect.close();
        }catch(SQLException e){
            e.printStackTrace();
        }

        return cveest;
    }
    @Override
    public void onBackPressed() {

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
    public Integer id_despachador (Editable clave){
        Integer res=0;
        String query = "select id from despachadores where pass="+clave+"";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                res = rs.getInt("id");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        try {
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;

    }
    public Integer id_dispositivo (){
        Integer res=0;
        MacActivity macActivity= new MacActivity();

        String query = "select id from dispositivos where mac_adr='"+macActivity.getMacAddress().toString()+"'";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                res = rs.getInt("id");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        try {
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public Integer id_dispensario (String  dispensario){
        Integer res=0;
        Log.w("logico",dispensario);
        String query = "select id from dispensario where numero_logico="+dispensario+"";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                res = rs.getInt("id");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        try {
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public JSONObject datos_bomba_llena(Integer logico){
        JSONObject res=new JSONObject();
        String query="select c.id as corte,des.nombre as despachador,dis.numero_logico as dispensario,c.hora_entrada as inicio\n" +
                " from dispensario as dis \n" +
                "inner join corte as c on c.id_dispensario=dis.id\n" +
                "inner join despachadores as des on des.id=c.id_despachador\n" +
                "where dis.numero_logico="+logico+" and c.status=0";
        try {
            connect = control_gas();
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                res.put("corte",rs.getString("corte"));
                res.put("despachador",rs.getString("despachador"));
                res.put("dispensario",rs.getString("dispensario"));
                res.put("inicio",rs.getString("inicio"));
            }
        }catch(SQLException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
}
