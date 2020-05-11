package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.printer.Printer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;


public class RfidCredito extends AppCompatActivity implements View.OnClickListener {
    String bomba,ult_nrotrn,odm;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag myTag;
    boolean writeMode;
    public static final int CONNECTION_TIMEOUT = 20000;
    public static final int READ_TIMEOUT = 25000;
    TextView tvrfid,tvcg,tvcreditolabel,tvcreditomsj,tvfleetlabel,tvfleetmsj,tvproductolabel,tvproductomsj;
    ImageButton btn_creditoticket;
    ImageView imagen;
    View vtitle;
    String Tag="";
    private final int DURACION_SPLASH = 3000; // 3 segundos
    private final int DURACION_SPLASH_5 = 5000; // 5 segundos
    JSONObject cursor=null;
    Connection connect;
    PreparedStatement stmt;
    private Printer mPrinter = null;
    Integer impreso;
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    public static Activity activity;
    JSONObject jsonimprimir = new JSONObject();
    cgticket cgticket_obj = new cgticket();
    JSONObject servicio = null;
    ProgressDialog pdLoading ;
    EditText et_odm;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid_credito);
        tvrfid = (TextView) findViewById(R.id.tvrfid);
        tvcreditolabel = (TextView) findViewById(R.id.tvcreditolabel);
        tvcreditomsj = (TextView) findViewById(R.id.tvcreditomsj);
        tvfleetlabel = (TextView) findViewById(R.id.tvfleetlabel);
        tvfleetmsj = (TextView) findViewById(R.id.tvfleetmsj);
        tvproductolabel = (TextView) findViewById(R.id.tvproductolabel);
        tvproductomsj = (TextView) findViewById(R.id.tvproductomsj);
        vtitle = (View) findViewById(R.id.vtitle);
        tvcg = (TextView) findViewById(R.id.tvcg);
        btn_creditoticket = (ImageButton) findViewById(R.id.btn_creditoticket);
        btn_creditoticket.setOnClickListener(this);
        et_odm = (EditText)findViewById(R.id.et_odm);
        imagen = (ImageView)findViewById(R.id.imagen);
        Bundle bundle = getIntent().getExtras();
        activity = RfidCredito.this;
        pdLoading = new ProgressDialog(activity);
        if(bundle.getString("bomba")!= null)
        {
            bomba=bundle.getString("bomba");
            //Toast.makeText(this,bomba,Toast.LENGTH_SHORT).show();
        }
        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_ticket_ladob);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (view.getId()) {
                    case R.id.fab_ticket_ladob:
                        pdLoading.setIndeterminate(true);
                        pdLoading.setCancelable(false);
                        pdLoading.setTitle("Combu-Express");
                        pdLoading.setMessage("Imprimiendo ...");
                        pdLoading.show();
                        JSONObject ticket_otra_bomba = null;
                        cgticket ticket = new cgticket();
                        Integer bomba_libre = null;
                        try {
                            bomba_libre = ticket.get_bomba_libre(getApplicationContext(), bomba);
                        } catch (ClassNotFoundException | SQLException | InstantiationException | JSONException | IllegalAccessException e) {
                            e.printStackTrace();
                            new AlertDialog.Builder(RfidCredito.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                        }
                        try {
                            servicio = ticket.consulta_servicio(RfidCredito.this, bomba);
                            ticket_otra_bomba = ticket.consulta_servicio(getApplicationContext(), String.valueOf(bomba_libre));
                            ticket_otra_bomba.put("impreso", impreso = cant_impreso(getApplicationContext(), servicio.getString("nrotrn")));
                            ticket.guardarnrotrn(getApplicationContext(), ticket_otra_bomba, 1);
                            new ClassImpresionTicket(RfidCredito.this,getApplicationContext(),fab,ticket_otra_bomba).execute();
                            //new ClassImpresionTicket(RfidCredito.this,getApplicationContext(),btn_creditoticket,ticket_otra_bomba).disconnectPrinter();
                            //updateButtonState(false);
                            //if (!runPrintReceiptSequence()) {
                             //   updateButtonState(true);
                            //}
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        try {
                            Toast.makeText(RfidCredito.this, String.valueOf(ticket_otra_bomba.getString("nrotrn")), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pdLoading.dismiss();
                        break;
                        //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        //        .setAction("Action", null).show();
                }
            }
        });

    }
    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,data));
    }
    @SuppressLint("NewApi") protected void onNewIntent(Intent intent){
        String etiqueta="";
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (parcelables!= null && parcelables.length>0){
                etiqueta=readTextFromMessage((NdefMessage)parcelables[0]);
                etiqueta=etiqueta+"|"+bin2hex(myTag.getId());
                toJson json = new toJson();
                JSONObject data = json.strtojson(etiqueta);
                try {
                    new AsyncFetch(data.getString("1")).execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(this,"no id",Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(this, etiqueta+"|" + bin2hex(myTag.getId()), Toast.LENGTH_LONG).show();
        }
    }
    private String readTextFromMessage(NdefMessage ndefMessage) {

        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        if(ndefRecords != null && ndefRecords.length>0){

            NdefRecord ndefRecord = ndefRecords[0];

            String tagContent = getTextFromNdefRecord(ndefRecord);

            return tagContent;


        }else
        {
            Toast.makeText(this, "No NDEF records found!", Toast.LENGTH_SHORT).show();
            return "";
        }
    }
    public String getTextFromNdefRecord(NdefRecord   ndefRecord)
    {
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            Log.w("textEncoding",textEncoding);
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1,
                    payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }

    public void onPause(){
        super.onPause();
        WriteModeOff();
    }
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }
    @SuppressLint("NewApi") private void WriteModeOn(){
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    @SuppressLint("NewApi") private void WriteModeOff(){
        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }


    @Override
    public void onClick(View view) {
        ValidacionFlotillero vf = new ValidacionFlotillero();
        ticketFragment tf = new ticketFragment();
        cgticket ticket= new cgticket();
        switch (view.getId()) {
            case R.id.btn_creditoticket:
                pdLoading.setIndeterminate(true);
                pdLoading.setCancelable(false);
                pdLoading.setTitle("Combu-Express");
                pdLoading.setMessage("Imprimiendo ...");
                pdLoading.show();
                String nrotrn= null;
                try {
                    nrotrn = vf.validar_utlimo_nrotrn(RfidCredito.this,bomba);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                odm= String.valueOf(et_odm.getText());
                //aqui se valida que sea un servicio diferente despues de leer la etiqueta
                if (ult_nrotrn.equals(nrotrn)) {
                    //obtenemos el codigo del cliente
                    JSONObject codcli= null;
                    try {
                        codcli = vf.get_codcli(RfidCredito.this,bin2hex(myTag.getId()),"nfc");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    try {
                        //realizamos el update para asignar el ticket al cliente de credito
                        ticket.update_codcli(RfidCredito.this, nrotrn, codcli.getString("cliente"), codcli.getString("vehiculo"),odm,codcli.getString("tar"));
                        //validar que el ticket no haya sido impreso


                        try {
                            servicio = ticket.consulta_servicio(RfidCredito.this,bomba);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        try {
                            impreso=cant_impreso(getApplicationContext(),servicio.getString("nrotrn"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.w("cant_imp", String.valueOf(impreso));
                        if (impreso.equals(0)) {
                            try {
                                cgticket_obj.guardarnrotrn(getApplicationContext(), servicio,2);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            try {
                                actualizar_cant_impreso(this, servicio.getString("nrotrn"),impreso);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    //impresion con copia

                    for(int i=0;i<2;i++) {

                        cgticket cgticket_obj = new cgticket();
                        try {
                            JSONObject ticket1 = cgticket_obj.consulta_servicio(getApplicationContext(), bomba);
                            ticket1.put("impreso",impreso);
                            ticket1.put("tag",bin2hex(myTag.getId()));
                            ticket1.put("bomba",bomba);
                            ticket1.put("odm",odm);
                            new ClassImpresionTicket(RfidCredito.this,getApplicationContext(),btn_creditoticket,ticket1).execute();
                            //new ClassImpresionTicket(RfidCredito.this,getApplicationContext(),btn_creditoticket,ticket1).disconnectPrinter();
                            //updateButtonState(false);
                            //if(!runPrintReceiptSequence()){
                            //    updateButtonState(true);
                            //}
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }

                    //validar que flotillero este activo
                    Boolean flot_activo = vf.isServerReachable("http://187.210.108.135",getApplicationContext());
                    Log.w("floot", String.valueOf(flot_activo));
                    if (flot_activo.equals(true)) {
                        ClassFlotillero flot = new ClassFlotillero(RfidCredito.this, servicio);
                        flot.execute();
                    }
                    Intent intent = new Intent(RfidCredito.this,VentaActivity.class);
                    startActivity(intent);
                    } else{
                    //condicion cuando no llega el nuevo servicio
                    Toast.makeText(this,"Servicio aun no listo, espere un momento.",Toast.LENGTH_LONG).show();
                }
                pdLoading.dismiss();
                break;
        }
    }


    // Crear class AsyncFetch para buscar cliente en base de odoo
    private class AsyncFetch extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(RfidCredito.this);
        HttpURLConnection conn;
        URL url = null;
        String searchQuery;

        public AsyncFetch(String searchQuery){
            this.searchQuery=searchQuery;
        }

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
                url = new URL("http://189.206.183.110:1390/cliente_odoo-search_fa.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // add parameter to our above url
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("searchQuery", searchQuery);
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
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

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    return("Connection error");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {
            String a="";

            //this method will be running on UI thread
            pdLoading.dismiss();


            pdLoading.dismiss();
            if(result.equals("no rows")) {
                Toast.makeText(RfidCredito.this, "No Results found for entered query", Toast.LENGTH_LONG).show();
            }else{

                try {
                    ValidacionFlotillero vf = new ValidacionFlotillero();
                    int estacion=vf.validar_estacion(RfidCredito.this,bin2hex(myTag.getId()),"nfc");
                    int dia=vf.carga_dia(RfidCredito.this,bin2hex(myTag.getId()),"nfc");
                    int hora=vf.validar_hora(RfidCredito.this,bin2hex(myTag.getId()),"nfc");
                    int monto=vf.validar_monto(RfidCredito.this,bin2hex(myTag.getId()),"nfc");
                    ult_nrotrn=vf.validar_utlimo_nrotrn(RfidCredito.this,bomba);
                    String producto = vf.validar_producto(RfidCredito.this,bin2hex(myTag.getId()),"nfc");
                    JSONObject json = new JSONObject(result);
                    a ="el cliente" + json.getString("cliente") +"con un credito de " +json.getDouble("limite") + "tiene disponible " +json.getDouble("saldo");
                    tvrfid.setTextSize(20);
                    et_odm.setTextSize(15);
                    vtitle.setVisibility(View.VISIBLE);
                    tvrfid.setText(json.getString("cliente"));
                    tvcreditomsj.setTextSize(15);
                    tvcreditolabel.setTextSize(15);
                    tvfleetmsj.setTextSize(15);
                    tvfleetlabel.setTextSize(15);
                    tvproductolabel.setTextSize(15);
                    tvproductomsj.setTextSize(15);
                    if (json.getDouble("saldo")>0){
                        //tvrfid.setText("Credito del cliente "+json.getString("cliente")+" regularizado, puede surtir!!!");
                        tvcreditolabel.setVisibility(View.VISIBLE);
                        tvcreditomsj.setVisibility(View.VISIBLE);
                        tvcreditomsj.setText("AUTORIZADO");
                        imagen.setY(35);
                        imagen.setX(245);
                        imagen.setImageResource(R.drawable.ok);
                        imagen.setVisibility(View.VISIBLE);
                    }else if(json.getDouble("saldo")<=0){
                        //tvrfid.setText("Cliente "+json.getString("cliente")+" sin credito!!!");
                        tvcreditolabel.setVisibility(View.VISIBLE);
                        tvcreditomsj.setVisibility(View.VISIBLE);
                        tvcreditomsj.setText("SIN CREDITO");
                        imagen.setY(35);
                        imagen.setX(245);
                        imagen.setImageResource(R.drawable.cancel);
                        imagen.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                Intent intent =new Intent(getApplication(),VentaActivity.class);
                                startActivity(intent);

                                finish();
                            }
                        }, DURACION_SPLASH_5);

                    }
                    if (estacion==0){
                        tvfleetmsj.setVisibility(View.VISIBLE);
                        tvfleetlabel.setVisibility(View.VISIBLE);
                        tvfleetmsj.setText("Sin permiso de cargar(estacion invalida)");
                        imagen.setY(35);
                        imagen.setX(245);
                        imagen.setImageResource(R.drawable.cancel);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                Intent intent =new Intent(getApplication(),VentaActivity.class);
                                startActivity(intent);

                                finish();
                            }
                        }, DURACION_SPLASH_5);
                    }else if(estacion==1) {
                        if (dia == 0) {
                            tvfleetmsj.setVisibility(View.VISIBLE);
                            tvfleetlabel.setVisibility(View.VISIBLE);
                            tvfleetmsj.setText("Sin permiso de cargar(dia invalido)");
                            imagen.setY(35);
                            imagen.setX(245);
                            imagen.setImageResource(R.drawable.cancel);
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    Intent intent =new Intent(getApplication(),VentaActivity.class);
                                    startActivity(intent);

                                    finish();
                                }
                            }, DURACION_SPLASH_5);
                        } else if (dia == 1) {
                            if (hora==1) {
                                if (monto==1) {
                                    btn_creditoticket.setVisibility(View.VISIBLE);
                                    et_odm.setVisibility(View.VISIBLE);
                                    Log.w("producto",producto);
                                    if (producto.equals("TODOS")) {
                                        tvfleetmsj.setVisibility(View.VISIBLE);
                                        tvfleetlabel.setVisibility(View.VISIBLE);
                                        tvfleetmsj.setText("AUTORIZADO");
                                        tvproductomsj.setVisibility(View.VISIBLE);
                                        tvproductolabel.setVisibility(View.VISIBLE);
                                        tvproductomsj.setText("TODOS");
                                    }else{
                                        tvfleetmsj.setVisibility(View.VISIBLE);
                                        tvfleetlabel.setVisibility(View.VISIBLE);
                                        tvfleetmsj.setText("OK");
                                        tvproductomsj.setVisibility(View.VISIBLE);
                                        tvproductolabel.setVisibility(View.VISIBLE);
                                        tvproductomsj.setText(producto);
                                    }
                                }else if(monto==0){
                                    tvfleetmsj.setVisibility(View.VISIBLE);
                                    tvfleetlabel.setVisibility(View.VISIBLE);
                                    tvfleetmsj.setText("Sin permiso de cargar(Monto no disponible)");
                                    imagen.setY(35);
                                    imagen.setX(245);
                                    imagen.setImageResource(R.drawable.cancel);
                                    new Handler().postDelayed(new Runnable() {
                                        public void run() {
                                            Intent intent =new Intent(getApplication(),VentaActivity.class);
                                            startActivity(intent);

                                            finish();
                                        }
                                    }, DURACION_SPLASH_5);
                                }
                            }else if(hora==0){
                                tvfleetmsj.setVisibility(View.VISIBLE);
                                tvfleetlabel.setVisibility(View.VISIBLE);
                                tvfleetmsj.setText("Sin permiso de cargar(hora invalida)");
                                imagen.setY(35);
                                imagen.setX(245);
                                imagen.setImageResource(R.drawable.cancel);
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        Intent intent =new Intent(getApplication(),VentaActivity.class);
                                        startActivity(intent);

                                        finish();
                                    }
                                }, DURACION_SPLASH_5);
                            }
                        }
                    }

                } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(RfidCredito.this,"Cliente no entontrado",Toast.LENGTH_SHORT).show();
                    Log.w("ERR",e.toString());

                }

            }

        }

    }

    public Integer cant_impreso (Context con, String ticket ){
        ResultSet rs;
        Integer res=0;
        Log.w("nrotrn",ticket);
        String query = "select impreso from despachos where nrotrn="+ticket+"";
        try {
            connect = control_gas(con);
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                res = rs.getInt("impreso");
            }
            connect.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return res;
    }
    public Connection control_gas(Context con ){
        DataBaseManager manager = new DataBaseManager(con);
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
    public boolean actualizar_cant_impreso (Context con, String ticket ,int impreso){
        Log.w("nrotrn",ticket);
        int imp= impreso+1;
        String query = "update despachos set impreso="+imp+" where nrotrn="+ticket+"";
        try {
            connect = control_gas(con);
            stmt = connect.prepareStatement(query);
            stmt.executeUpdate();
            connect.close();
            return true;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

}
