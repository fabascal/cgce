package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import java.net.SocketException;
import java.net.URL;
import java.sql.SQLException;


public class AceiteCreditoMetodoNFC extends AppCompatActivity implements View.OnClickListener,ProductoResultListener,EstacionResultListener, ProductoWebResultListener {
    NfcAdapter adapter;
    Tag myTag;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    public static final int CONNECTION_TIMEOUT = 20000;
    public static final int READ_TIMEOUT = 25000;
    private final int DURACION_SPLASH = 3000; // 3 segundos
    private final int DURACION_SPLASH_5 = 5000; // 5 segundos
    String bomba,estacion;;
    TextView tvrfid1,tvcreditolabel1,tvcreditomsj1,tvname,etprecio;

    ImageView imagen,img_aceite;
    ImageButton  btn_creditoticket,imgbtnscan;
    View vtitle;
    JSONObject jsonproducto = new JSONObject();
    JSONObject jsonenviaproducto = new JSONObject();
    JSONObject jsonwebresult = new JSONObject();
    cgticket ticket = new cgticket();
    private static final boolean DEVELOPER_MODE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aceite_credito_metodo_nfc);
        tvrfid1 = (TextView)findViewById(R.id.tvrfid1);
        tvcreditolabel1 = (TextView) findViewById(R.id.tvcreditolabel1);
        tvcreditomsj1 = (TextView) findViewById(R.id.tvcreditomsj1);
        imagen  =(ImageView)findViewById(R.id.imagen);
        btn_creditoticket = (ImageButton) findViewById(R.id.btn_creditoticket);
        etprecio= (TextView) findViewById(R.id.etprecio);
        imgbtnscan = (ImageButton) findViewById(R.id.imgbtnscan);
        imgbtnscan.setOnClickListener(this);
        vtitle = (View) findViewById(R.id.vtitle);
        btn_creditoticket.setOnClickListener(this);
        tvname = (TextView)findViewById(R.id.tvname);
        adapter = NfcAdapter.getDefaultAdapter(this);
        img_aceite =(ImageView)findViewById(R.id.img_aceite);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};
        Bundle bundle = getIntent().getExtras();
        if (DEVELOPER_MODE) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        if(bundle.getString("tipo_venta")!= null)
        {
            try {
                jsonenviaproducto.put("tipo_venta",bundle.getString("tipo_venta"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
                JSONObject data = json.strtojson(etiqueta,"|");
                try {
                    jsonenviaproducto.put("rfc", data.getString("1"));
                    Log.w("json",jsonenviaproducto.toString());
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
    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,data));
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
    public boolean vista(){
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgbtnscan:
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                break;
            case R.id.btn_creditoticket:
                try {
                    jsonenviaproducto.put("precio",String.valueOf(etprecio.getText()));
                    jsonenviaproducto.put("isla",ticket.get_isla(this,jsonenviaproducto));
                    jsonenviaproducto.put("cantidad",1);
                    int nrotrn=ticket.guardaraceite(this,jsonenviaproducto);
                    jsonenviaproducto.put("nrotrn",nrotrn);
                    jsonenviaproducto.put("nota",nrotrn*10  );
                    WSProductoWeb wsProductoWeb = new WSProductoWeb(AceiteCreditoMetodoNFC.this,jsonenviaproducto);
                    wsProductoWeb.delegate = this;
                    wsProductoWeb.execute();
                    Intent intent = new Intent(this,VentaActivity.class);
                    startActivity(intent);
                } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                Log.w("json",jsonenviaproducto.toString());
                break;
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            String a ="FORMAT: " + scanFormat;
            String b="CONTENT: " + scanContent;
            WSEstacionOdoo estacionOdoo = new WSEstacionOdoo(AceiteCreditoMetodoNFC.this,estacion);
            WSProductOdoo productOdoo = null;
            try {
                productOdoo = new WSProductOdoo(AceiteCreditoMetodoNFC.this,scanContent);
            } catch (ClassNotFoundException | SQLException | InstantiationException | JSONException | IllegalAccessException e) {
                new AlertDialog.Builder(AceiteCreditoMetodoNFC.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                e.printStackTrace();
            }
            productOdoo.delegate = this;
            productOdoo.execute();
        }else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    @Override
    public void processFinishE(String output) {
        estacion=output;
    }
    @Override
    public void processFinish(String output) {
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
        try {
            jsonproducto = new JSONObject(output);
            //Log.w("processFinish",jsonproducto.getString("name"));
            if (jsonproducto.has("101")) {
                tvname.setText("Producto inexistente!!!.");
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Intent intent =new Intent(getApplication(),VentaActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, DURACION_SPLASH_5);
            } else {
                jsonenviaproducto.put("config_id",String.valueOf(ticket.get_configid(this)));
                jsonenviaproducto.put("cveest",String.valueOf(ticket.get_cveest(this)));
                jsonenviaproducto.put("codigo",jsonproducto.getString("id_producto"));
                jsonenviaproducto.put("producto",jsonproducto.getString("name"));
                jsonenviaproducto.put("id_despachador",String.valueOf(ticket.id_depsachador(this)));
                jsonenviaproducto.put("despachador", String.valueOf(ticket.nombre_depsachador(this)));
                jsonenviaproducto.put("corte",ticket.get_corte(this));


                tvname.setText(jsonproducto.getString("name"));
                etprecio.setText(jsonproducto.getString("precio"));
                imgbtnscan.setVisibility(View.GONE);
                btn_creditoticket.setVisibility(View.VISIBLE);
                img_aceite.setImageBitmap(decodeBase64(jsonproducto.getString("imagen")));
                img_aceite.setBackgroundColor(Color.TRANSPARENT);
            }
        }catch(JSONException | ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException | SocketException e){
            e.printStackTrace();
        }
    }
    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = new byte[0];
        decodedBytes = Base64.decode(input.toString(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    public void processFinish2(String output) {
        try {
            jsonwebresult = new JSONObject(output);
            Log.w("proc2",jsonwebresult.toString());
            if (jsonwebresult.getString("mensaje").equals(String.valueOf(1))){
                ticket.updateaceiteweb(this,jsonenviaproducto);
            }
            new ClassImpresionAceite(AceiteCreditoMetodoNFC.this,getApplicationContext(),btn_creditoticket,jsonenviaproducto).execute();

        } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private class AsyncFetch extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(AceiteCreditoMetodoNFC.this);
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
                Toast.makeText(AceiteCreditoMetodoNFC.this, "No Results found for entered query", Toast.LENGTH_LONG).show();
            }else{

                try {
                    /*ValidacionFlotillero vf = new ValidacionFlotillero();
                    int estacion=vf.validar_estacion(AceiteCreditoMetodoNFC.this,bin2hex(myTag.getId()));
                    int dia=vf.carga_dia(AceiteCreditoMetodoNFC.this,bin2hex(myTag.getId()));
                    int hora=vf.validar_hora(AceiteCreditoMetodoNFC.this,bin2hex(myTag.getId()));
                    int monto=vf.validar_monto(AceiteCreditoMetodoNFC.this,bin2hex(myTag.getId()));
                    String producto = vf.validar_producto(AceiteCreditoMetodoNFC.this,bin2hex(myTag.getId()));*/
                    JSONObject json = new JSONObject(result);
                    tvrfid1.setTextSize(20);

                    tvrfid1.setText(json.getString("cliente"));

                    jsonenviaproducto.put("cliente",json.getString("cliente"));


                    if (json.getDouble("saldo")>0){
                        tvcreditolabel1.setVisibility(View.VISIBLE);
                        tvcreditomsj1.setVisibility(View.VISIBLE);
                        tvcreditomsj1.setText("AUTORIZADO");
                        imagen.setImageResource(R.drawable.ok);
                        imagen.setVisibility(View.VISIBLE);

                        jsonenviaproducto.put("odoo","AUTORIZADO");
                        jsonenviaproducto.put("fleet","AUTORIZADO");
                        jsonenviaproducto.put("imagen","ok");
                        imgbtnscan.setVisibility(View.VISIBLE);
                        btn_creditoticket.setVisibility(View.GONE);

                    }else if(json.getDouble("saldo")<=0){
                        //tvrfid.setText("Cliente "+json.getString("cliente")+" sin credito!!!");
                        tvcreditolabel1.setVisibility(View.VISIBLE);
                        tvcreditomsj1.setVisibility(View.VISIBLE);
                        vtitle.setVisibility(View.VISIBLE);
                        tvcreditomsj1.setText("SIN CREDITO");
                        imagen.setImageResource(R.drawable.cancel);
                        imagen.setVisibility(View.VISIBLE);
                        btn_creditoticket.setVisibility(View.GONE);
                        Toast.makeText(AceiteCreditoMetodoNFC.this,"Sin Credito",Toast.LENGTH_LONG).show();

                        jsonenviaproducto.put("odoo","SIN CREDITO");
                        jsonenviaproducto.put("imagen","no");


                    }
                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(AceiteCreditoMetodoNFC.this,"Cliente no entontrado",Toast.LENGTH_SHORT).show();
                    Log.w("ERR",e.toString());

                }

            }

        }

    }
}
