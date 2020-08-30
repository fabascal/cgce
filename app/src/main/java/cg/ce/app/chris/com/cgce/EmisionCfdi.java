package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.printer.Printer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;


public class EmisionCfdi extends AppCompatActivity implements View.OnClickListener,CfdiResultListener {
    TextView tvrazon, tvrfc, tvdomicilio, tvcolonia, tvestado, tvciudad, tvcp, tvfolio, tvvendedor,
            tvprecio, tvvolumen, tvimporte, tvmetodo, tvcuenta, tvproducto;
    Button btnwebservice;
    JSONObject cfdienvio = new JSONObject();
    private Printer mPrinter = null;
    private Context mContext = null;
    Activity activity;
    JSONObject jsonrespuesta = new JSONObject();
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador21 = new DecimalFormat("######.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    String res = null;
    Cursor c;
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    String Bandera;
    LogCE logCE = new LogCE();


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        sensores.bluetooth();
        sensores.wifi(this,true);
        tvrazon = (TextView) findViewById(R.id.tvrazon);
        tvrfc = (TextView) findViewById(R.id.tvrfc);
        tvdomicilio = (TextView) findViewById(R.id.tvdomicilio);
        tvcolonia = (TextView) findViewById(R.id.tvcolonia);
        tvestado = (TextView) findViewById(R.id.tvestado);
        tvciudad = (TextView) findViewById(R.id.tvciudad);
        tvcp = (TextView) findViewById(R.id.tvcp);
        tvfolio = (TextView) findViewById(R.id.tvfolio);
        tvvendedor = (TextView) findViewById(R.id.tvvendedor);
        tvprecio = (TextView) findViewById(R.id.tvprecio);
        tvvolumen = (TextView) findViewById(R.id.tvvolumen);
        tvimporte = (TextView) findViewById(R.id.tvimporte);
        tvmetodo = (TextView) findViewById(R.id.tvmetodo);
        tvcuenta = (TextView) findViewById(R.id.tvcuenta);
        tvproducto = (TextView) findViewById(R.id.tvproducto);
        mContext = this;
        activity = this;

        try {
            //json con datos de Control-Gas
            //JSONObject ticket = cgticket_obj.consulta_servicio(getApplicationContext());
            cfdienvio = new JSONObject(getIntent().getStringExtra("json"));
            tvfolio.setText("Folio                   : " + cfdienvio.getInt("ticket")+"0");
            tvvendedor.setText("Despachador    : " + cfdienvio.getString("despachador"));
            tvprecio.setText("Precio                : " + formateador2.format(cfdienvio.getDouble("preunitario")));
            tvvolumen.setText("Cantidad            : " + formateador4.format(cfdienvio.getDouble("cantidad")));
            tvimporte.setText("Importe              : " + formateador2.format(cfdienvio.getDouble("importe")));
            tvrazon.setText("Razon          : " + cfdienvio.getString("cliente"));
            tvrfc.setText("R.F.C.           : " + cfdienvio.getString("rfc"));
            tvdomicilio.setText("Calle             : " + cfdienvio.getString("domicilio"));
            tvcolonia.setText("Colonia        : " + cfdienvio.getString("colonia"));
            tvestado.setText("Estado         : " + cfdienvio.getString("estado"));
            tvciudad.setText("Municipio    : " + cfdienvio.getString("municipio"));
            tvcp.setText("C.P.               : " + cfdienvio.getString("cp"));
            tvmetodo.setText("Metodo        : " + cfdienvio.getString("formapago"));
            tvproducto.setText("Producto           : " + cfdienvio.getString("producto"));
            if (cfdienvio.getString("numcuenta").length() != 0) {
                tvcuenta.setVisibility(View.VISIBLE);
            }
            tvcuenta.setText("Cuenta         : " + cfdienvio.getString("numcuenta"));
        } catch (JSONException e) {
            new AlertDialog.Builder(EmisionCfdi.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            logCE.EscirbirLog2(getApplicationContext(),"EmisionCfdi_onCreate - " + e);
        }
        btnwebservice = (Button) findViewById(R.id.btnwebservice);
        btnwebservice.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        cgticket cgticket_obj = new cgticket();
        JSONObject validar=null;
        try {
            Log.w("cliente1",String.valueOf(cfdienvio.getInt("cg_cliente")));
            if (cfdienvio.getInt("cg_cliente")!=0){
                validar=cgticket_obj.consulta_credito(getApplicationContext(),cfdienvio.getInt("cg_cliente"));
                Log.w("validar",validar.toString());
            }
        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException |JSONException  e) {
            new AlertDialog.Builder(EmisionCfdi.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            logCE.EscirbirLog2(getApplicationContext(),"EmisionCfdi_onClick - " + e);
            e.printStackTrace();
        }
        switch (view.getId()) {
            case R.id.btnwebservice:
                sensores.bluetooth();
                try {
                    cfdienvio.put("bandera",Bandera);
                } catch (JSONException e) {
                    new AlertDialog.Builder(EmisionCfdi.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    logCE.EscirbirLog2(getApplicationContext(),"EmisionCfdi_onClick - " + e);
                    e.printStackTrace();
                }
                CFDiTimbre timbre = new CFDiTimbre(EmisionCfdi.this, cfdienvio);
                timbre.delegate = this;
                timbre.execute();
                //String  res ="2030|18909|01||E07846|30257940|2017/02/01|2|1|16.530000686645508|0.0|100.0|MWCONRANC1199888|1199888|OD9yhPFD81BvVG+L8TX9gzrkaK4hQj5k+Zz3GiZbq1dao4PXRfIAjtI1NikihhEmrDPAJdKZO5nVI5YXK37eQGeWM1PPyY4bdYGQbF1Qfy/YkEkCJ567DndhwQPuBuo1E6xseFLi7GHJCKUrDndgm4tGzg3UU9geQzXAlJe1EAWkkhQhomkJ+K3gwx5GLCa4MHHuJ/qWVqj3u2pup2vjAUHH7PyYjQ45y6x0HWOYDQCAR38Qc8jRx6YvCIU5MRwNUzMGqnthAZX09W3zo2PWzoZp8XmkshUP1JLQzGJ4eOQg1UEHsdEtFhOPQsO8hN9/8/ZGNJXWfBrCl0E2aKNklA==|2017-02-01T17:54:20|d1c13433-1402-4ce2-b9a8-16f3c5f7f6ae|00001000000301634628|1.0|PgsPnqjYsKg1KAWGFLghg+3+hsNmkRf+MOhdzZmi5sS5AxnbrvZVR4TBM6DvtQQSzMFW/ZCHbYiYt4rfB2Gojg9xavBmE/8zi6CI8ziuuu23l8w5tGh+BYnqK58vsRN15xqp7LJ2G9WjPms4d2KLcOgaRe47/KRNRVcmXesUEFI=";
                break;
        }
    }

    @Override
    public void processFinish(String output){
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
        Log.w("processFinish",output);
        toJson tojson = new toJson();
        jsonrespuesta=tojson.strtojson(output);
        Log.w("jsonrespuesta",jsonrespuesta.toString());
        try {
            Log.w("res1",String.valueOf(jsonrespuesta.getString("0")));
            JSONObject res ;
            res=new JSONObject(String.valueOf(jsonrespuesta.getString("0")));
            if (res.has("mensaje")){
                new AlertDialog.Builder(EmisionCfdi.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(res.getString("mensaje")))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(getApplicationContext(),"EmisionCfdi_processFinish - " + res.getString("mensaje"));
            }else {
                JSONObject sello_cfd, fecha_timbre, uuid, certificado_sat, version, sello_sat;
                Log.w("res2", res.toString());
                if(res.has("factura")) {
                    cfdienvio.put("folio", res.getString("factura"));
                }if(res.has("selloCFD")) {
                    cfdienvio.put("sello_cfd", res.getString("selloCFD"));
                }if(res.has("FechaTimbrado")) {
                    cfdienvio.put("fecha_timbre", res.getString("FechaTimbrado"));
                }if(res.has("UUID")) {
                    cfdienvio.put("uuid", res.getString("UUID"));
                }if(res.has("noCertificadoSAT")) {
                    cfdienvio.put("certificado_sat", res.getString("noCertificadoSAT"));
                }if(res.has("version")) {
                    cfdienvio.put("version", res.getString("version"));
                }if (res.has("selloSAT")) {
                    cfdienvio.put("sello_sat", res.getString("selloSAT"));
                }if(res.has("erfc")) {
                    cfdienvio.put("rfc_emisor", res.getString("erfc"));
                }if(res.has("ecalle")) {
                    cfdienvio.put("calle_emisor", res.getString("ecalle"));
                }if (res.has("enumexterior")) {
                    cfdienvio.put("numext_emisor", res.getString("enumexterior"));
                }if (res.has("ecolonia")) {
                    cfdienvio.put("colonia_emisor", res.getString("ecolonia"));
                }if(res.has("emunicipio")) {
                    cfdienvio.put("municipio_emisor", res.getString("emunicipio"));
                }if(res.has("eestado")) {
                    cfdienvio.put("estado_emisor", res.getString("eestado"));
                }if(res.has("epais")) {
                    cfdienvio.put("pais_emisor", res.getString("epais"));
                }if(res.has("ecp")) {
                    cfdienvio.put("cp_emisor", res.getString("ecp"));
                }if(res.has("enombre")) {
                    cfdienvio.put("nombre_emisor", res.getString("enombre"));
                }if(res.has("subtotal")) {
                    cfdienvio.put("subtotal", res.getString("subtotal"));
                }if(res.has("iva")) {
                    cfdienvio.put("iva", res.getString("iva"));
                }if(res.has("claveprodserv")) {
                    cfdienvio.put("claveprodserv", res.getString("claveprodserv"));
                }if(res.has("claveunidad")) {
                    cfdienvio.put("claveunidad", res.getString("claveunidad"));
                }if(res.has("unidad_medida")) {
                    cfdienvio.put("unidad_medida", res.getString("unidad_medida"));
                }if(res.has("descripcion")) {
                    cfdienvio.put("descripcion", res.getString("descripcion"));
                }
                new ClassImpresionCFDi(EmisionCfdi.this, getApplicationContext(), btnwebservice, cfdienvio).execute();
                Intent intent = new Intent(EmisionCfdi.this,VentaActivity.class);
                startActivity(intent);
            }


        } catch (JSONException e) {
            new AlertDialog.Builder(EmisionCfdi.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            logCE.EscirbirLog2(getApplicationContext(),"EmisionCfdi_processFinish - " + e);
            e.printStackTrace();
        }
    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.ContentMainSearch);
                setContentView(R.layout.activity_emision_cfdi);
                Bandera="Combu-Express";
                break;
            case "Repsol":
                setTheme(R.style.ContentMainSearchRepsol);
                setContentView(R.layout.activity_emision_cfdi);
                Bandera = "Repsol";
                break;
            case "Ener":
                setTheme(R.style.ContentMainSearchEner);
                setContentView(R.layout.activity_emision_cfdi);
                Bandera = "Ener";
                break;
            case "Total":
                setTheme(R.style.ContentMainSearchTotal);
                setContentView(R.layout.activity_emision_cfdi);
                Bandera = "Total";
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
