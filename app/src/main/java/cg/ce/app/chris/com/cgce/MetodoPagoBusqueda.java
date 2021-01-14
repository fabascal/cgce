package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.net.SocketException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cg.ce.app.chris.com.cgce.ControlGas.GetTicket;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.ControlGasListener;
import cg.ce.app.chris.com.cgce.Facturacion.Utils.MetodoPagoEntity;
import cg.ce.app.chris.com.cgce.Facturacion.Utils.UsoCFDiEntity;
import cg.ce.app.chris.com.cgce.common.Variables;

public class MetodoPagoBusqueda extends AppCompatActivity implements View.OnClickListener {
    TextView tv_cliente_cfdi,tv_rfc_cfdi,tv_correo_cfdi,tv_calle_cfdi,tv_colonia_cfdi,tv_estado_cfdi,tv_municipio_cfdi,tv_cp_cfdi;
    EditText et_correo2,numcuenta,comentario;
    Spinner spn_metodo,spn_id,spn_uso;
    Button btn_cfdi;
    JSONObject cfdi_data;
    String id_metodo2,usocfdi;
    ArrayAdapter<String> adapter_id=null;
    ArrayAdapter<String> adapter_usocfdi_id=null;

    List<MetodoPagoEntity> ListMetodoPago1;
    List<String> ListMetodoPago=new ArrayList<>();
    List<UsoCFDiEntity> ListUsoCFDi;

    List<String> MetodoPagoId;
    List<String> MetodoPagoClave;
    List<String> MetodoPagoDescripcion;
    List<String> MetodoPagoActivo;
    List<String> UsoCFDiId;
    List<String> UsoCFDiClave;
    List<String> UsoCFDiDescripcion;
    List<String> UsoCFDiActivo;

    JSONObject Productos = new JSONObject();
    JSONObject Data = new JSONObject();
    JSONArray ProductosArray = new JSONArray();

    MacActivity mac = new MacActivity();

    String bomba;
    public static final int CONNECTION_TIMEOUT = 20000;
    public static final int READ_TIMEOUT = 25000;
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    String Bandera;
    LogCE logCE = new LogCE();
    Drawable icon;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        sensores.bluetooth();
        sensores.wifi(this,true);
        et_correo2 = (EditText)findViewById(R.id.et_correo2);
        numcuenta = (EditText)findViewById(R.id.numcuenta);
        spn_metodo = (Spinner)findViewById(R.id.spn_metodo);
        spn_uso = (Spinner)findViewById(R.id.spn_uso);
        comentario = (EditText)findViewById(R.id.comentario);
        spn_metodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (spn_metodo.getSelectedItem().equals("Efectivo") || spn_metodo.getSelectedItem().equals("Otros")
                        || spn_metodo.getSelectedItem().equals("Vales de despensa") || spn_metodo.getSelectedItem().equals("Por definir")  ){
                    numcuenta.setVisibility(View.GONE);
                }else {
                    numcuenta.setVisibility(View.GONE);
                }
                /*Integer position_id = spn_metodo.getSelectedItemPosition();
                id_metodo2=String.valueOf(adapter_id.getItem(position_id));*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        spn_uso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //usocfdi=String.valueOf(spn_uso.getSelectedItem());
                /*Integer position_id = spn_uso.getSelectedItemPosition();
                usocfdi=String.valueOf(adapter_usocfdi_id.getItem(position_id));*/
                //Toast.makeText(MetodoPagoBusqueda.this,usocfdi,Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        btn_cfdi =(Button)findViewById(R.id.btn_cfdi) ;
        btn_cfdi.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            try {
                cfdi_data = new JSONObject(getIntent().getStringExtra("cliente"));
                tv_cliente_cfdi = (TextView)findViewById(R.id.tv_cliente_cfdi);
                tv_rfc_cfdi = (TextView)findViewById(R.id.tv_rfc_cfdi);
                tv_correo_cfdi = (TextView)findViewById(R.id.tv_correo_cfdi);
                tv_calle_cfdi = (TextView)findViewById(R.id.tv_calle_cfdi);
                tv_colonia_cfdi = (TextView)findViewById(R.id.tv_colonia_cfdi);
                tv_estado_cfdi = (TextView)findViewById(R.id.tv_estado_cfdi);
                tv_cp_cfdi = (TextView)findViewById(R.id.tv_cp_cfdi);
                tv_municipio_cfdi = (TextView)findViewById(R.id.tv_municipio_cfdi);
                tv_cliente_cfdi.setText(cfdi_data.getString("razon_social"));
                tv_rfc_cfdi.setText("R.F.C. :"+cfdi_data.getString("RFC"));
                tv_correo_cfdi.setText("Correo :"+cfdi_data.getString("correo"));
                tv_calle_cfdi.setText("Domicilio :"+cfdi_data.getString("calle")+" "+cfdi_data.getString("exterior")+" "+cfdi_data.getString("interior")+" ");
                tv_colonia_cfdi.setText("Colonia :"+cfdi_data.getString("colonia"));
                tv_estado_cfdi.setText("Estado :"+cfdi_data.getString("estado"));
                tv_municipio_cfdi.setText("Municipio :"+cfdi_data.getString("municipio"));
                tv_cp_cfdi.setText("CP :"+cfdi_data.getString("cp"));
                bomba=cfdi_data.getString("bomba");
                FillMetodoPago();
                FillUsoCFDi();
                /*new AsyncUsoCFDi().execute();*/
                /*new AsyncFetch(cfdi_data).execute();*/


            } catch (JSONException e) {
                new AlertDialog.Builder(MetodoPagoBusqueda.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"MetodoPagoBusqueda_onCreate - " + e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cfdi:
                getJson(cfdi_data);
                break;
        }
    }
    public void getJson (final JSONObject jsonObject) {
        cgticket cgticket_obj = new cgticket();
        final JSONObject[] ticket = {null};
        final String nip = null;
        final JSONObject jsoncfdi = new JSONObject();

        new GetTicket(this, new ControlGasListener() {
            @Override
            public void processFinish(JSONObject output) {
                Log.w("output",String.valueOf(output));
                try {
                    if (output.getInt(Variables.CODE_ERROR)==0){
                        ticket[0] = output;
                        try {
                            jsoncfdi.put("nip",nip);
                            jsoncfdi.put("categoria","cfdi");
                            jsoncfdi.put("id_cliente",jsonObject.getInt("id_cliente"));
                            jsoncfdi.put("razon_social",jsonObject.getString("razon_social"));
                            jsoncfdi.put("RFC",jsonObject.getString("RFC"));
                            jsoncfdi.put("id_domicilio",jsonObject.getInt("id_domicilio"));
                            jsoncfdi.put("calle",jsonObject.getString("calle"));
                            jsoncfdi.put("exterior",jsonObject.getString("exterior"));
                            jsoncfdi.put("interior",jsonObject.getString("interior"));
                            jsoncfdi.put("colonia",jsonObject.getString("colonia"));
                            jsoncfdi.put("estado",jsonObject.getString("estado"));
                            jsoncfdi.put("id_estado",jsonObject.getString("id_estado"));
                            jsoncfdi.put("municipio",jsonObject.getString("municipio"));
                            jsoncfdi.put("pais","Mexico");
                            jsoncfdi.put("cp",jsonObject.getString("cp"));
                            jsoncfdi.put("numcuenta",numcuenta.getText());
                            jsoncfdi.put("nrocte", ticket[0].getInt(Variables.KEY_TICKET_NROTRN) * 10);
                            jsoncfdi.put("nrotrn", ticket[0].getInt(Variables.KEY_TICKET_NROTRN) );
                            jsoncfdi.put("copia",et_correo2.getText());
                            jsoncfdi.put("correo",jsonObject.getString("correo"));
                            Data.put("cveest", ticket[0].getString(Variables.KEY_TICKET_CVEEST));
                            Data.put("despachador", ticket[0].getString(Variables.KEY_TICKET_DESPACHADOR));
                            Data.put("comentarios",comentario.getText());
                            Data.put("MetodoPagoId", MetodoPagoId.get((int) spn_metodo.getSelectedItemId()));
                            Data.put("forma_pago", MetodoPagoClave.get((int) spn_metodo.getSelectedItemId()));
                            Data.put("MetodoPagoDescripcion", MetodoPagoDescripcion.get((int) spn_metodo.getSelectedItemId()));
                            Data.put("UsoCFDiId",UsoCFDiId.get((int) spn_metodo.getSelectedItemId()));
                            Data.put("UsoCFDiDescripcion",UsoCFDiDescripcion.get((int) spn_uso.getSelectedItemId()));
                            Data.put("uso_cfdi",UsoCFDiClave.get((int) spn_uso.getSelectedItemId()));
                            Data.put("nip_despachador", ticket[0].getInt(Variables.NIP_DESPACHADOR));
                            Productos.put("tipo","1");
                            Productos.put("nrotrn", ticket[0].getInt(Variables.KEY_TICKET_NROTRN));
                            Productos.put("ticket", ticket[0].getInt(Variables.KEY_TICKET_NROTRN) * 10);
                            Productos.put("cg_cliente", ticket[0].getInt(Variables.KEY_TICKET_CODCLI));
                            Productos.put("fecha_ticket", ticket[0].getString(Variables.KEY_TICKET_FECHA));
                            Productos.put("id_producto", ticket[0].getInt(Variables.KEY_TICKET_ID_PRODUCTO));
                            Productos.put("descripcion", ticket[0].getString(Variables.KEY_TICKET_PRODUCTO).replace(" ",""));
                            Productos.put("bomba", ticket[0].getInt(Variables.KEY_TICKET_BOMBA));
                            Productos.put("precio_unitario", ticket[0].getDouble(Variables.KEY_TICKET_PRECIO));
                            Productos.put("importe", ticket[0].getDouble(Variables.KEY_TICKET_TOTAL));
                            Productos.put("cantidad", ticket[0].getDouble(Variables.KEY_TICKET_CANTIDAD));
                            ProductosArray.put(Productos);
                            Data.put("productos",ProductosArray);
                            jsoncfdi.put("data", Data);
                            Log.w("cfdi_envio", String.valueOf(jsoncfdi));
                            Intent intent=null;
                            intent=new Intent(getApplicationContext(),EmisionCfdi.class);
                            intent.putExtra("json",jsoncfdi.toString());
                            if (intent!=null){
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                    stacktraceObj[2].getMethodName() + "|" + e);
                            new AlertDialog.Builder(MetodoPagoBusqueda.this)
                                    .setTitle(R.string.error)
                                    .setIcon(icon)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok, null).show();
                            e.printStackTrace();
                        }
                    }else{
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                        new AlertDialog.Builder(MetodoPagoBusqueda.this)
                                .setTitle(R.string.error)
                                .setIcon(icon)
                                .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                .setPositiveButton(R.string.btn_ok, null).show();
                    }
                } catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(MetodoPagoBusqueda.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
            }
        }).execute(bomba,mac.getMacAddress(),"0");

    }
    public void FillMetodoPago(){
        MetodoPagoId = Arrays.asList(getResources().getStringArray(R.array.MetodoPagoId));
        MetodoPagoClave = Arrays.asList(getResources().getStringArray(R.array.MetodoPagoClave));
        MetodoPagoDescripcion = Arrays.asList(getResources().getStringArray(R.array.MetodoPagoDescripcion));
        MetodoPagoActivo = Arrays.asList(getResources().getStringArray(R.array.MetodoPagoActivo));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MetodoPagoBusqueda.this,android.R.layout.simple_spinner_dropdown_item, MetodoPagoDescripcion);
        spn_metodo.setAdapter(adapter);
    }
    public void FillUsoCFDi(){
        UsoCFDiId = Arrays.asList(getResources().getStringArray(R.array.UsoCFDiId));
        UsoCFDiClave = Arrays.asList(getResources().getStringArray(R.array.UsoCFDiClave));
        UsoCFDiDescripcion = Arrays.asList(getResources().getStringArray(R.array.UsoCFDiDescripcion));
        UsoCFDiActivo = Arrays.asList(getResources().getStringArray(R.array.UsoCFDiActivo));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MetodoPagoBusqueda.this,android.R.layout.simple_spinner_dropdown_item, UsoCFDiDescripcion);
        spn_uso.setAdapter(adapter);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.ContentMainSearch);
                setContentView(R.layout.activity_metodo_pago_busqueda);
                Bandera="Combu-Express";
                icon = getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainSearchRepsol);
                setContentView(R.layout.activity_metodo_pago_busqueda);
                Bandera = "Repsol";
                icon = getDrawable(R.drawable.repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainSearchEner);
                setContentView(R.layout.activity_metodo_pago_busqueda);
                Bandera = "Ener";
                icon = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainSearchTotal);
                setContentView(R.layout.activity_metodo_pago_busqueda);
                Bandera = "Total";
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
