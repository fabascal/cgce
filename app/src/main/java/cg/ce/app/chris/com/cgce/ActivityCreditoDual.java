package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cg.ce.app.chris.com.cgce.dialogos.close_credito;
import cg.ce.app.chris.com.cgce.dialogos.fab_contado;

import static cg.ce.app.chris.com.cgce.RfidCredito.activity;
import static java.lang.Thread.sleep;

public class ActivityCreditoDual extends AppCompatActivity implements View.OnClickListener {
    final static String TV_NFC="Presente el TAG en el dispositivo.";
    final static String TV_NIP="Identificacion de credito mediante NIP.";
    final static String TV_NOMBRE="Buscar cliente.";
    final static int MIN_SEARCH=3;
    ToggleButton tgl_area;
    CardView cardView_1, cardView_2,CardViewNIP1,CardViewNIP2,CardViewTEXT1,CardViewTEXT2;
    ImageButton imbtn_ticket_1, imbtn_ticket_2, btn_creditoticket,imbtn_clienteodoo,imbtn_clientecg,imbtn_search;
    Spinner spn_dispensarios_1, spn_dispensarios_2;
    ImageView imageView_1, imageView_2;
    ResultSet rs;
    JSONObject cursor=null;
    Connection connect;
    PreparedStatement stmt;
    JSONObject jsonObject_1 = new JSONObject();
    JSONObject jsonObject_2 = new JSONObject();
    NfcAdapter adapter;
    Tag myTag;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Boolean odoo_activo;
    public static final int CONNECTION_TIMEOUT = 20000;
    public static final int READ_TIMEOUT = 25000;
    private final int DURACION_SPLASH = 3000; // 3 segundos
    private final int DURACION_SPLASH_5 = 5000; // 5 segundos
    TextView tvrfid1,tvcg,tvcreditolabel1,tvcreditomsj1,tvfleetlabel1,tvfleetmsj1,tvproductolabel1,
            tvproductomsj1,tvbomba,tvvehiculo_cliente,tvvehiculo_rfc,tvvehiculo_codcli,tvden,tvcodcli,
            tvrfc,plc,den_vehicle,rsp,tvvehiculo_cliente2,tvvehiculo_rfc2,tvvehiculo_codcli2;
    EditText et_odm,et_clientecg,etSearchCustomer,etSearchCustomer2;
    String bomba,ult_nrotrn,odm;
    ImageView imagen;
    View vtitle;
    View vg;
    ProgressDialog pdLoading ;
    JSONObject servicio = null;
    Integer impreso;
    cgticket cgticket_obj = new cgticket();
    FloatingActionButton fab;
    Integer bomba_libre;
    private Printer mPrinter = null;
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    JSONObject ticket_otra_bomba = null;
    JSONObject ticket1 = null;
    JSONObject ticket2=null;
    private Context mContext = null;
    private static final boolean DEVELOPER_MODE = true;
    LinearLayout ll_clienteodoo,ll_busqueda_nip;
    public ValidacionFlotillero vf = new ValidacionFlotillero();
    Sensores sensores = new Sensores();
    LogCE logCE = new LogCE();
    JSONObject jsonObjectError = new JSONObject();
    boolean IsTablet = false;
    ValidateTablet tablet = new ValidateTablet();
    JSONObject js;
    View layout_nombre,layout_nombre_vehiculo,layout_nombre2,layout_nombre_vehiculo2;
    private RecyclerView mRVCustomerCG, mRVCustomerVehicleCG,mRVCustomerCG2,mRVCustomerVehicleCG2;
    private AdapterCustomerCG mAdapter, mAdapter2;
    private AdapterCustomerVehicleCG mAdapterVehicle,mAdapterVehicle2;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credito_dual);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Log.w("Tableta","es Tableta");
            IsTablet=true;
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            IsTablet=false;
            Log.w("Tableta","no es Tableta");
        }

        vg = findViewById (R.id.activity_credito_dual);
        sensores.bluetooth();
        sensores.wifi(this, true);
        if (!IsTablet) {
            adapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
            pendingIntent = PendingIntent.getActivity(ActivityCreditoDual.this, 0,
                    new Intent(ActivityCreditoDual.this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
            writeTagFilters = new IntentFilter[]{tagDetected};
        }
        ll_clienteodoo = (LinearLayout)findViewById(R.id.ll_clienteodoo);
        ll_busqueda_nip = (LinearLayout)findViewById(R.id.ll_busqueda_nip);
        CardViewNIP1 = (CardView)findViewById(R.id.CardViewNIP1);
        CardViewNIP2 = (CardView)findViewById(R.id.CardViewNIP2);
        tgl_area = (ToggleButton)findViewById(R.id.tgl_area);
        imbtn_clienteodoo = ( ImageButton ) findViewById(R.id.imbtn_clienteodoo);
        imbtn_clienteodoo.setOnClickListener(this);
        imbtn_clientecg =( ImageButton ) findViewById(R.id.imbtn_clientecg);
        imbtn_clientecg.setOnClickListener(this);
        et_clientecg = (EditText) findViewById(R.id.et_clientecg);
        layout_nombre = (View) findViewById(R.id.layout_nombre);
        layout_nombre2 = (View) findViewById(R.id.layout_nombre2);
        imbtn_search = (ImageButton) findViewById(R.id.imbtn_search);
        etSearchCustomer = (EditText) findViewById(R.id.etSearchCustomer);
        etSearchCustomer.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        etSearchCustomer2 = (EditText) findViewById(R.id.etSearchCustomer2);
        etSearchCustomer2.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        layout_nombre_vehiculo = (View) findViewById(R.id.layout_nombre_vehiculo);
        layout_nombre_vehiculo2 = (View) findViewById(R.id.layout_nombre_vehiculo2);
        tvvehiculo_cliente = (TextView) findViewById(R.id.tvvehiculo_cliente);
        tvvehiculo_codcli = (TextView) findViewById(R.id.tvvehiculo_codcli);
        tvvehiculo_rfc = (TextView) findViewById(R.id.tvvehiculo_rfc);
        tvvehiculo_cliente2 = (TextView) findViewById(R.id.tvvehiculo_cliente2);
        tvvehiculo_codcli2 = (TextView) findViewById(R.id.tvvehiculo_codcli2);
        tvvehiculo_rfc2 = (TextView) findViewById(R.id.tvvehiculo_rfc2);
        tvden = (TextView) findViewById(R.id.tvden);
        tvcodcli = (TextView) findViewById(R.id.tvcodcli);
        tvrfc = (TextView) findViewById(R.id.tvrfc);
        plc = (TextView) findViewById(R.id.plc);
        den_vehicle = (TextView) findViewById(R.id.den_vehicle);
        rsp = (TextView) findViewById(R.id.rsp);

        mContext=this;

        if (DEVELOPER_MODE) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        tvcreditolabel1 = (TextView) findViewById(R.id.tvcreditolabel1);
        tvcreditomsj1 = (TextView) findViewById(R.id.tvcreditomsj1);
        tvfleetlabel1 = (TextView) findViewById(R.id.tvfleetlabel1);
        tvfleetmsj1 = (TextView) findViewById(R.id.tvfleetmsj1);
        tvbomba = (TextView)findViewById(R.id.tvbomba);
        if(!tgl_area.isChecked()){
            if (jsonObject_1.has("bomba")){
                tvbomba.setVisibility(View.VISIBLE);
                try {
                    tvbomba.setText("Bomba : "+jsonObject_1.getString("bomba"));
                } catch (JSONException e) {
                    try {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoBomba1",e));
                    }catch (JSONException e1){
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }else{
                tvbomba.setVisibility(View.GONE);
            }
        }else {
            if (jsonObject_2.has("bomba")){
                tvbomba.setVisibility(View.VISIBLE);
                try {
                    tvbomba.setText("Bomba : "+jsonObject_2.getString("bomba"));
                } catch (JSONException e) {
                    try {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoBomba2",e));
                    }catch (JSONException e1){
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }else{
                tvbomba.setVisibility(View.GONE);
            }

        }
        tvproductolabel1 = (TextView) findViewById(R.id.tvproductolabel1);
        fab =(FloatingActionButton) findViewById(R.id.fab_ticket_ladob);
        tvproductomsj1 = (TextView) findViewById(R.id.tvproductomsj1);
        et_odm = (EditText)findViewById(R.id.et_odm);
        vtitle = (View) findViewById(R.id.vtitle);
        imagen = (ImageView)findViewById(R.id.imagen);
        tvrfid1 = (TextView)findViewById(R.id.tvrfid1);
        activity = ActivityCreditoDual.this;
        pdLoading = new ProgressDialog(activity);
        btn_creditoticket = (ImageButton)findViewById(R.id.btn_creditoticket);
        btn_creditoticket.setOnClickListener(this);

        tgl_area.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    //se muestra area1
                    //validando las variables
                    if (jsonObject_1.has("odm")){
                        try {
                            et_odm.setText(jsonObject_1.getString("odm"));
                        } catch (JSONException e) {
                            try {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoOdm1",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else{
                        et_odm.setText(null);
                    }
                    if (jsonObject_1.has("nip")){
                        try {
                            et_clientecg.setText(jsonObject_1.getString("nip"));
                        } catch (JSONException e) {
                            try {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoNip1",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else{
                        et_clientecg.setText(null);
                    }
                    if (jsonObject_1.has("fleet")){
                        try {
                            tvfleetmsj1.setText(jsonObject_1.getString("fleet"));
                        }catch (JSONException e) {
                            try {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoFleet1",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else {
                        tvfleetlabel1.setVisibility(View.GONE);
                        tvfleetmsj1.setVisibility(View.GONE);
                        tvfleetmsj1.setText(null);
                    }
                    if ( jsonObject_1.has("odoo")){
                        try{
                            tvcreditomsj1.setText(jsonObject_1.getString("odoo"));
                        }catch (JSONException e) {
                            try {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoOdoo1",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else {
                        tvcreditolabel1.setVisibility(View.GONE);
                        tvcreditomsj1.setVisibility(View.GONE);
                        tvcreditomsj1.setText(null);
                    }
                    if (jsonObject_1.has("cliente")){
                        try {
                            tvrfid1.setText(jsonObject_1.getString("cliente"));
                        }catch (JSONException e) {
                            try {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCliente1",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else {
                        vtitle.setVisibility(View.GONE);
                        tvrfid1.setText(R.string.rfid);
                    }
                    if (jsonObject_1.has("imagen")){
                        try {
                            if (jsonObject_1.getString("imagen").equals("ok")){
                                imagen.setImageResource(R.drawable.ok);
                            }else {
                                btn_creditoticket.setVisibility(View.GONE);
                                imagen.setImageResource(R.drawable.cancel);
                            }
                        }catch (JSONException e) {
                            try {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoImagen1",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else {
                        imagen.setVisibility(View.GONE);
                    }
                    Log.w("Area","1");
                    if (!jsonObject_1.has("bomba")) {
                        Log.w("json",jsonObject_1.toString());
                        layout_nombre.setVisibility(View.GONE);
                        layout_nombre2.setVisibility(View.GONE);
                        layout_nombre_vehiculo.setVisibility(View.GONE);
                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                        tvbomba.setVisibility(View.GONE);
                        imageView_2.setVisibility(View.GONE);
                        imageView_1.setVisibility(View.VISIBLE);
                        spn_dispensarios_2.setVisibility(View.GONE);
                        spn_dispensarios_1.setVisibility(View.VISIBLE);
                        imbtn_ticket_2.setVisibility(View.GONE);
                        imbtn_ticket_1.setVisibility(View.VISIBLE);
                        CardViewNIP1.setVisibility(View.GONE);
                        CardViewNIP2.setVisibility(View.GONE);
                        tvrfid1.setVisibility(View.GONE);
                        cardView_2.setVisibility(View.GONE);
                        CardViewTEXT1.setVisibility(View.GONE);
                        CardViewTEXT2.setVisibility(View.GONE);
                        btn_creditoticket.setVisibility(View.GONE);
                        tvcreditolabel1.setVisibility(View.GONE);
                        tvcreditomsj1.setVisibility(View.GONE);
                        tvfleetmsj1.setVisibility(View.GONE);
                        tvfleetlabel1.setVisibility(View.GONE);
                        tvproductomsj1.setVisibility(View.GONE);
                        tvproductolabel1.setVisibility(View.GONE);
                        imagen.setVisibility(View.GONE);
                        et_odm.setVisibility(View.GONE);
                        fab.setVisibility(View.GONE);
                        vtitle.setVisibility(View.GONE);
                        ll_clienteodoo.setVisibility(View.GONE);
                        ll_busqueda_nip.setVisibility(View.GONE);
                    }
                    else if (jsonObject_1.has("bomba")) {
                        Log.w("json",jsonObject_1.toString());
                        try {
                            tvbomba.setText("Bomba : "+jsonObject_1.getString("bomba"));
                        } catch (JSONException e) {
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoBomba1",e));
                            }catch (JSONException e1){
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                        if(!jsonObject_1.has("metodo")) {
                            Log.w("bomba", "1");
                            layout_nombre.setVisibility(View.GONE);
                            layout_nombre_vehiculo.setVisibility(View.GONE);
                            layout_nombre2.setVisibility(View.GONE);
                            layout_nombre_vehiculo2.setVisibility(View.GONE);
                            tvbomba.setVisibility(View.VISIBLE);
                            imbtn_ticket_1.setVisibility(View.GONE);
                            imbtn_ticket_2.setVisibility(View.GONE);
                            spn_dispensarios_1.setVisibility(View.GONE);
                            spn_dispensarios_2.setVisibility(View.GONE);
                            imageView_1.setVisibility(View.GONE);
                            imageView_2.setVisibility(View.GONE);
                            if (!IsTablet) {
                                cardView_1.setVisibility(View.VISIBLE);
                            }
                            cardView_2.setVisibility(View.GONE);
                            CardViewNIP1.setVisibility(View.VISIBLE);
                            CardViewNIP2.setVisibility(View.GONE);
                            CardViewTEXT1.setVisibility(View.VISIBLE);
                            CardViewTEXT2.setVisibility(View.GONE);
                            tvrfid1.setVisibility(View.GONE);
                            btn_creditoticket.setVisibility(View.GONE);
                            tvcreditolabel1.setVisibility(View.GONE);
                            tvcreditomsj1.setVisibility(View.GONE);
                            tvfleetmsj1.setVisibility(View.GONE);
                            tvfleetlabel1.setVisibility(View.GONE);
                            tvproductomsj1.setVisibility(View.GONE);
                            tvproductolabel1.setVisibility(View.GONE);
                            imagen.setVisibility(View.GONE);
                            et_odm.setVisibility(View.GONE);
                            fab.setVisibility(View.VISIBLE);
                            vtitle.setVisibility(View.GONE);
                            ll_clienteodoo.setVisibility(View.GONE);
                            ll_busqueda_nip.setVisibility(View.GONE);
                        }else if (jsonObject_1.has("metodo")){
                            if (!jsonObject_1.has("rfc")) {
                                try {
                                    if(jsonObject_1.getString("metodo").equals("nfc")){
                                        tvbomba.setVisibility(View.VISIBLE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        imbtn_ticket_2.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        spn_dispensarios_2.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        imageView_2.setVisibility(View.GONE);
                                        cardView_1.setVisibility(View.GONE);
                                        cardView_2.setVisibility(View.GONE);
                                        CardViewNIP1.setVisibility(View.GONE);
                                        CardViewNIP2.setVisibility(View.GONE);
                                        CardViewTEXT1.setVisibility(View.GONE);
                                        CardViewTEXT2.setVisibility(View.GONE);
                                        tvrfid1.setVisibility(View.VISIBLE);
                                        tvrfid1.setText(TV_NFC);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        vtitle.setVisibility(View.GONE);
                                        btn_creditoticket.setVisibility(View.GONE);
                                        tvcreditolabel1.setVisibility(View.GONE);
                                        tvcreditomsj1.setVisibility(View.GONE);
                                        tvfleetmsj1.setVisibility(View.GONE);
                                        tvfleetlabel1.setVisibility(View.GONE);
                                        tvproductomsj1.setVisibility(View.GONE);
                                        tvproductolabel1.setVisibility(View.GONE);
                                        imagen.setVisibility(View.GONE);
                                        et_odm.setVisibility(View.GONE);
                                        fab.setVisibility(View.VISIBLE);
                                        ll_clienteodoo.setVisibility(View.GONE);
                                        ll_busqueda_nip.setVisibility(View.GONE);
                                        layout_nombre.setVisibility(View.GONE);
                                        layout_nombre_vehiculo.setVisibility(View.GONE);
                                        layout_nombre2.setVisibility(View.GONE);
                                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                                    }
                                    else if (jsonObject_1.getString("metodo").equals("nip")){
                                        tvbomba.setVisibility(View.VISIBLE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        imbtn_ticket_2.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        spn_dispensarios_2.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        imageView_2.setVisibility(View.GONE);
                                        cardView_1.setVisibility(View.GONE);
                                        cardView_2.setVisibility(View.GONE);
                                        CardViewNIP1.setVisibility(View.GONE);
                                        CardViewNIP2.setVisibility(View.GONE);
                                        CardViewTEXT1.setVisibility(View.GONE);
                                        CardViewTEXT2.setVisibility(View.GONE);
                                        tvrfid1.setVisibility(View.VISIBLE);
                                        tvrfid1.setText(TV_NIP);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        vtitle.setVisibility(View.GONE);
                                        btn_creditoticket.setVisibility(View.GONE);
                                        tvcreditolabel1.setVisibility(View.GONE);
                                        tvcreditomsj1.setVisibility(View.GONE);
                                        tvfleetmsj1.setVisibility(View.GONE);
                                        tvfleetlabel1.setVisibility(View.GONE);
                                        tvproductomsj1.setVisibility(View.GONE);
                                        tvproductolabel1.setVisibility(View.GONE);
                                        imagen.setVisibility(View.GONE);
                                        et_odm.setVisibility(View.GONE);
                                        fab.setVisibility(View.VISIBLE);
                                        ll_clienteodoo.setVisibility(View.VISIBLE);
                                        ll_busqueda_nip.setVisibility(View.VISIBLE);
                                        layout_nombre.setVisibility(View.GONE);
                                        layout_nombre_vehiculo.setVisibility(View.GONE);
                                        layout_nombre2.setVisibility(View.GONE);
                                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                                    }else if(jsonObject_1.getString("metodo").equals("nombre")){
                                        tvbomba.setVisibility(View.VISIBLE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        imbtn_ticket_2.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        spn_dispensarios_2.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        imageView_2.setVisibility(View.GONE);
                                        cardView_1.setVisibility(View.GONE);
                                        cardView_2.setVisibility(View.GONE);
                                        CardViewNIP1.setVisibility(View.GONE);
                                        CardViewNIP2.setVisibility(View.GONE);
                                        CardViewTEXT1.setVisibility(View.GONE);
                                        CardViewTEXT2.setVisibility(View.GONE);
                                        tvrfid1.setVisibility(View.VISIBLE);
                                        tvrfid1.setText(TV_NOMBRE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        vtitle.setVisibility(View.GONE);
                                        btn_creditoticket.setVisibility(View.GONE);
                                        tvcreditolabel1.setVisibility(View.GONE);
                                        tvcreditomsj1.setVisibility(View.GONE);
                                        tvfleetmsj1.setVisibility(View.GONE);
                                        tvfleetlabel1.setVisibility(View.GONE);
                                        tvproductomsj1.setVisibility(View.GONE);
                                        tvproductolabel1.setVisibility(View.GONE);
                                        imagen.setVisibility(View.GONE);
                                        et_odm.setVisibility(View.GONE);
                                        fab.setVisibility(View.VISIBLE);
                                        ll_clienteodoo.setVisibility(View.GONE);
                                        ll_busqueda_nip.setVisibility(View.GONE);
                                        if(jsonObject_1.has("nroveh")){
                                            layout_nombre.setVisibility(View.GONE);
                                            layout_nombre_vehiculo.setVisibility(View.VISIBLE);
                                        }else if(!jsonObject_1.has("nroveh")){
                                            layout_nombre.setVisibility(View.VISIBLE);
                                            layout_nombre_vehiculo.setVisibility(View.GONE);
                                        }
                                        layout_nombre2.setVisibility(View.GONE);
                                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    try {
                                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                                .setTitle(R.string.error)
                                                .setMessage(String.valueOf(e))
                                                .setPositiveButton(R.string.btn_ok,null).show();
                                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoMetodo1",e));
                                    }catch (JSONException e1){
                                        e1.printStackTrace();
                                    }
                                    e.printStackTrace();
                                }

                            }else if (jsonObject_1.has("rfc") ){
                                tvbomba.setVisibility(View.VISIBLE);
                                imbtn_ticket_1.setVisibility(View.GONE);
                                imbtn_ticket_2.setVisibility(View.GONE);
                                spn_dispensarios_1.setVisibility(View.GONE);
                                spn_dispensarios_2.setVisibility(View.GONE);
                                imageView_1.setVisibility(View.GONE);
                                imageView_2.setVisibility(View.GONE);
                                cardView_1.setVisibility(View.GONE);
                                cardView_2.setVisibility(View.GONE);
                                CardViewNIP1.setVisibility(View.GONE);
                                CardViewNIP2.setVisibility(View.GONE);
                                CardViewTEXT1.setVisibility(View.GONE);
                                CardViewTEXT2.setVisibility(View.GONE);
                                tvrfid1.setVisibility(View.VISIBLE);
                                spn_dispensarios_1.setVisibility(View.GONE);
                                imageView_1.setVisibility(View.GONE);
                                fab.setVisibility(View.VISIBLE);
                                ll_busqueda_nip.setVisibility(View.GONE);
                                try {
                                    if (jsonObject_1.getString("metodo").equals("nfc")) {
                                        ll_clienteodoo.setVisibility(View.VISIBLE);
                                    } else {
                                        ll_clienteodoo.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    new AlertDialog.Builder(ActivityCreditoDual.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
                                    try {
                                        logCE.EscirbirLog(getApplicationContext(), jsonObjectError.put("CreditoMetodo1", e));
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                    e.printStackTrace();
                                }
                                if (jsonObject_1.has("imagen")) {
                                    try {
                                        if (jsonObject_1.getString("imagen").equals("ok")) {
                                            btn_creditoticket.setVisibility(View.VISIBLE);
                                        } else {
                                            btn_creditoticket.setVisibility(View.GONE);
                                        }
                                    } catch (JSONException e) {
                                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                                .setTitle(R.string.error)
                                                .setMessage(String.valueOf(e))
                                                .setPositiveButton(R.string.btn_ok,null).show();
                                        try {
                                            logCE.EscirbirLog(getApplicationContext(), jsonObjectError.put("CreditoImagen1", e));
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    if (!jsonObject_1.getString("metodo").equals("nombre")){
                                        vtitle.setVisibility(View.VISIBLE);
                                        tvcreditolabel1.setVisibility(View.VISIBLE);
                                        tvcreditomsj1.setVisibility(View.VISIBLE);
                                        tvfleetmsj1.setVisibility(View.VISIBLE);
                                        tvfleetlabel1.setVisibility(View.VISIBLE);
                                        tvproductomsj1.setVisibility(View.VISIBLE);
                                        tvproductolabel1.setVisibility(View.VISIBLE);
                                        imagen.setVisibility(View.VISIBLE);
                                        et_odm.setVisibility(View.VISIBLE);
                                        layout_nombre.setVisibility(View.GONE);
                                        layout_nombre_vehiculo.setVisibility(View.GONE);
                                    }else if(jsonObject_1.getString("metodo").equals("nombre")){
                                        if (jsonObject_1.has("nroveh")){
                                            tvfleetmsj1.setVisibility(View.VISIBLE);
                                            tvfleetlabel1.setVisibility(View.VISIBLE);
                                            tvproductomsj1.setVisibility(View.VISIBLE);
                                            tvproductolabel1.setVisibility(View.VISIBLE);
                                            imagen.setVisibility(View.VISIBLE);
                                            et_odm.setVisibility(View.VISIBLE);
                                            layout_nombre.setVisibility(View.GONE);
                                            layout_nombre_vehiculo.setVisibility(View.GONE);
                                        }else if(!jsonObject_1.has("nroveh")){
                                            tvfleetmsj1.setVisibility(View.GONE);
                                            tvfleetlabel1.setVisibility(View.GONE);
                                            tvproductomsj1.setVisibility(View.GONE);
                                            tvproductolabel1.setVisibility(View.GONE);
                                            imagen.setVisibility(View.GONE);
                                            et_odm.setVisibility(View.GONE);
                                            layout_nombre.setVisibility(View.GONE);
                                            layout_nombre_vehiculo.setVisibility(View.VISIBLE);
                                        }
                                        layout_nombre2.setVisibility(View.GONE);
                                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    new AlertDialog.Builder(ActivityCreditoDual.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
                                    e.printStackTrace();

                                }
                            }
                        }
                    }
                } else {
                    Log.w("Area","2");
                    //se muestra area2
                    //validando las variables

                    if (jsonObject_2.has("odm")){
                        try {
                            et_odm.setText(jsonObject_2.getString("odm"));
                        } catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoOdm2",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else{
                        et_odm.setText(null);
                    }
                    if (jsonObject_2.has("nip")){
                        try {
                            et_clientecg.setText(jsonObject_2.getString("nip"));
                        } catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoNip2",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else{
                        et_clientecg.setText(null);

                    }
                    if (jsonObject_2.has("fleet")){
                        try {
                            tvfleetmsj1.setText(jsonObject_2.getString("fleet"));
                        }catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoFleet2",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else {
                        tvfleetmsj1.setVisibility(View.GONE);
                        tvfleetlabel1.setVisibility(View.GONE);
                        tvfleetmsj1.setText(null);
                    }
                    if ( jsonObject_2.has("odoo")){
                        try{
                            tvcreditomsj1.setText(jsonObject_2.getString("odoo"));
                        }catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoOdoo2",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else {
                        tvcreditomsj1.setVisibility(View.GONE);
                        tvcreditolabel1.setVisibility(View.GONE);
                        tvcreditomsj1.setText(null);
                    }
                    if (jsonObject_2.has("cliente")){
                        try {
                            tvrfid1.setText(jsonObject_2.getString("cliente"));
                        }catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCliente2",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else {
                        vtitle.setVisibility(View.GONE);
                        tvrfid1.setText(R.string.rfid);
                    }
                    if (jsonObject_2.has("imagen")){
                        try {
                            if (jsonObject_2.getString("imagen").equals("ok")){
                                imagen.setImageResource(R.drawable.ok);
                            }else {
                                imbtn_ticket_1.setVisibility(View.GONE);
                                imbtn_ticket_2.setVisibility(View.GONE);
                                imagen.setImageResource(R.drawable.cancel);
                            }
                        }catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoImagen2",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else{
                        imagen.setVisibility(View.GONE);
                    }
                    if (!jsonObject_2.has("bomba")) {
                        tvbomba.setVisibility(View.GONE);
                        imageView_1.setVisibility(View.GONE);
                        imageView_2.setVisibility(View.VISIBLE);
                        spn_dispensarios_1.setVisibility(View.GONE);
                        spn_dispensarios_2.setVisibility(View.VISIBLE);
                        imbtn_ticket_1.setVisibility(View.GONE);
                        imbtn_ticket_2.setVisibility(View.VISIBLE);
                        cardView_1.setVisibility(View.GONE);
                        cardView_2.setVisibility(View.GONE);
                        CardViewNIP1.setVisibility(View.GONE);
                        CardViewNIP2.setVisibility(View.GONE);
                        CardViewTEXT1.setVisibility(View.GONE);
                        CardViewTEXT2.setVisibility(View.GONE);
                        tvrfid1.setVisibility(View.GONE);
                        btn_creditoticket.setVisibility(View.GONE);
                        vtitle.setVisibility(View.GONE);
                        tvcreditolabel1.setVisibility(View.GONE);
                        tvcreditomsj1.setVisibility(View.GONE);
                        tvfleetmsj1.setVisibility(View.GONE);
                        tvfleetlabel1.setVisibility(View.GONE);
                        tvproductomsj1.setVisibility(View.GONE);
                        tvproductolabel1.setVisibility(View.GONE);
                        imagen.setVisibility(View.GONE);
                        et_odm.setVisibility(View.GONE);
                        fab.setVisibility(View.GONE);
                        ll_clienteodoo.setVisibility(View.GONE);
                        ll_busqueda_nip.setVisibility(View.GONE);
                        layout_nombre.setVisibility(View.GONE);
                        layout_nombre2.setVisibility(View.GONE);
                        layout_nombre_vehiculo.setVisibility(View.GONE);
                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                    }
                    else if (jsonObject_2.has("bomba")) {
                        Log.i("json",jsonObject_2.toString());
                        try {
                            tvbomba.setText("Bomba : "+jsonObject_2.getString("bomba"));
                        } catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoBomba2",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                        if(!jsonObject_2.has("metodo")) {
                            tvbomba.setVisibility(View.VISIBLE);
                            imageView_1.setVisibility(View.GONE);
                            imageView_2.setVisibility(View.GONE);
                            spn_dispensarios_1.setVisibility(View.GONE);
                            spn_dispensarios_2.setVisibility(View.GONE);
                            imbtn_ticket_1.setVisibility(View.GONE);
                            imbtn_ticket_2.setVisibility(View.GONE);
                            cardView_1.setVisibility(View.GONE);
                            if (!IsTablet) {
                                cardView_2.setVisibility(View.VISIBLE);
                            }
                            CardViewNIP1.setVisibility(View.GONE);
                            CardViewNIP2.setVisibility(View.VISIBLE);
                            CardViewTEXT1.setVisibility(View.GONE);
                            CardViewTEXT2.setVisibility(View.VISIBLE);
                            tvrfid1.setVisibility(View.GONE);
                            btn_creditoticket.setVisibility(View.GONE);
                            tvcreditolabel1.setVisibility(View.GONE);
                            tvcreditomsj1.setVisibility(View.GONE);
                            vtitle.setVisibility(View.GONE);
                            tvfleetmsj1.setVisibility(View.GONE);
                            tvfleetlabel1.setVisibility(View.GONE);
                            tvproductomsj1.setVisibility(View.GONE);
                            tvproductolabel1.setVisibility(View.GONE);
                            imagen.setVisibility(View.GONE);
                            et_odm.setVisibility(View.GONE);
                            fab.setVisibility(View.VISIBLE);
                            ll_clienteodoo.setVisibility(View.GONE);
                            ll_busqueda_nip.setVisibility(View.GONE);
                            layout_nombre.setVisibility(View.GONE);
                            layout_nombre2.setVisibility(View.GONE);
                            layout_nombre_vehiculo.setVisibility(View.GONE);
                            layout_nombre_vehiculo2.setVisibility(View.GONE);
                        }else if(jsonObject_2.has("metodo")){
                            if(!jsonObject_2.has("rfc")) {
                                try {
                                    if (jsonObject_2.getString("metodo").equals("nfc")){
                                        tvbomba.setVisibility(View.VISIBLE);
                                        imageView_1.setVisibility(View.GONE);
                                        imageView_2.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        spn_dispensarios_2.setVisibility(View.GONE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        imbtn_ticket_2.setVisibility(View.GONE);
                                        cardView_1.setVisibility(View.GONE);
                                        cardView_2.setVisibility(View.GONE);
                                        CardViewNIP1.setVisibility(View.GONE);
                                        CardViewNIP2.setVisibility(View.GONE);
                                        CardViewTEXT1.setVisibility(View.GONE);
                                        CardViewTEXT2.setVisibility(View.GONE);
                                        tvrfid1.setVisibility(View.VISIBLE);
                                        tvrfid1.setText(TV_NFC);
                                        vtitle.setVisibility(View.GONE);
                                        btn_creditoticket.setVisibility(View.GONE);
                                        tvcreditolabel1.setVisibility(View.GONE);
                                        tvcreditomsj1.setVisibility(View.GONE);
                                        tvfleetmsj1.setVisibility(View.GONE);
                                        tvfleetlabel1.setVisibility(View.GONE);
                                        tvproductomsj1.setVisibility(View.GONE);
                                        tvproductolabel1.setVisibility(View.GONE);
                                        imagen.setVisibility(View.GONE);
                                        et_odm.setVisibility(View.GONE);
                                        fab.setVisibility(View.VISIBLE);
                                        ll_clienteodoo.setVisibility(View.GONE);
                                        ll_busqueda_nip.setVisibility(View.GONE);
                                        layout_nombre.setVisibility(View.GONE);
                                        layout_nombre2.setVisibility(View.GONE);
                                        layout_nombre_vehiculo.setVisibility(View.GONE);
                                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                                    }else if(jsonObject_2.getString("metodo").equals("nip")){
                                        tvbomba.setVisibility(View.VISIBLE);
                                        imageView_1.setVisibility(View.GONE);
                                        imageView_2.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        spn_dispensarios_2.setVisibility(View.GONE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        imbtn_ticket_2.setVisibility(View.GONE);
                                        cardView_1.setVisibility(View.GONE);
                                        cardView_2.setVisibility(View.GONE);
                                        CardViewNIP1.setVisibility(View.GONE);
                                        CardViewNIP2.setVisibility(View.GONE);
                                        CardViewTEXT1.setVisibility(View.GONE);
                                        CardViewTEXT2.setVisibility(View.GONE);
                                        tvrfid1.setVisibility(View.VISIBLE);
                                        tvrfid1.setText(TV_NIP);
                                        vtitle.setVisibility(View.GONE);
                                        btn_creditoticket.setVisibility(View.GONE);
                                        tvcreditolabel1.setVisibility(View.GONE);
                                        tvcreditomsj1.setVisibility(View.GONE);
                                        tvfleetmsj1.setVisibility(View.GONE);
                                        tvfleetlabel1.setVisibility(View.GONE);
                                        tvproductomsj1.setVisibility(View.GONE);
                                        tvproductolabel1.setVisibility(View.GONE);
                                        imagen.setVisibility(View.GONE);
                                        et_odm.setVisibility(View.GONE);
                                        fab.setVisibility(View.VISIBLE);
                                        ll_clienteodoo.setVisibility(View.VISIBLE);
                                        ll_busqueda_nip.setVisibility(View.VISIBLE);
                                        layout_nombre.setVisibility(View.GONE);
                                        layout_nombre2.setVisibility(View.GONE);
                                        layout_nombre_vehiculo.setVisibility(View.GONE);
                                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                                    }else if(jsonObject_2.getString("metodo").equals("nombre")){
                                        tvbomba.setVisibility(View.VISIBLE);
                                        imageView_1.setVisibility(View.GONE);
                                        imageView_2.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        spn_dispensarios_2.setVisibility(View.GONE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        imbtn_ticket_2.setVisibility(View.GONE);
                                        cardView_1.setVisibility(View.GONE);
                                        cardView_2.setVisibility(View.GONE);
                                        CardViewNIP1.setVisibility(View.GONE);
                                        CardViewNIP2.setVisibility(View.GONE);
                                        CardViewTEXT1.setVisibility(View.GONE);
                                        CardViewTEXT2.setVisibility(View.GONE);
                                        tvrfid1.setVisibility(View.VISIBLE);
                                        tvrfid1.setText(TV_NOMBRE);
                                        vtitle.setVisibility(View.GONE);
                                        btn_creditoticket.setVisibility(View.GONE);
                                        tvcreditolabel1.setVisibility(View.GONE);
                                        tvcreditomsj1.setVisibility(View.GONE);
                                        tvfleetmsj1.setVisibility(View.GONE);
                                        tvfleetlabel1.setVisibility(View.GONE);
                                        tvproductomsj1.setVisibility(View.GONE);
                                        tvproductolabel1.setVisibility(View.GONE);
                                        imagen.setVisibility(View.GONE);
                                        et_odm.setVisibility(View.GONE);
                                        fab.setVisibility(View.VISIBLE);
                                        ll_clienteodoo.setVisibility(View.GONE);
                                        ll_busqueda_nip.setVisibility(View.GONE);
                                        if(jsonObject_2.has("nroveh")){
                                            layout_nombre2.setVisibility(View.GONE);
                                            layout_nombre_vehiculo2.setVisibility(View.VISIBLE);
                                        }else if(!jsonObject_2.has("nroveh")){
                                            layout_nombre2.setVisibility(View.VISIBLE);
                                            layout_nombre_vehiculo2.setVisibility(View.GONE);
                                        }
                                        layout_nombre.setVisibility(View.GONE);
                                        layout_nombre_vehiculo.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    new AlertDialog.Builder(ActivityCreditoDual.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
                                    try {
                                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoMetodo2",e));
                                    }catch (JSONException e1){
                                        e1.printStackTrace();
                                    }
                                    e.printStackTrace();
                                }
                            }else if(jsonObject_2.has("rfc")){
                                try {
                                    if (jsonObject_2.getString("metodo").equals("nfc")) {
                                        tvbomba.setVisibility(View.VISIBLE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        imbtn_ticket_2.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        spn_dispensarios_2.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        imageView_2.setVisibility(View.GONE);
                                        cardView_1.setVisibility(View.GONE);
                                        cardView_2.setVisibility(View.GONE);
                                        CardViewNIP1.setVisibility(View.GONE);
                                        CardViewNIP2.setVisibility(View.GONE);
                                        CardViewTEXT1.setVisibility(View.GONE);
                                        CardViewTEXT2.setVisibility(View.GONE);
                                        tvrfid1.setVisibility(View.VISIBLE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        vtitle.setVisibility(View.VISIBLE);
                                        fab.setVisibility(View.VISIBLE);
                                        ll_busqueda_nip.setVisibility(View.GONE);
                                        layout_nombre.setVisibility(View.GONE);
                                        layout_nombre_vehiculo.setVisibility(View.GONE);
                                        layout_nombre2.setVisibility(View.GONE);
                                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                                    }else if (jsonObject_2.getString("metodo").equals("nip")){
                                        tvbomba.setVisibility(View.VISIBLE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        imbtn_ticket_2.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        spn_dispensarios_2.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        imageView_2.setVisibility(View.GONE);
                                        cardView_1.setVisibility(View.GONE);
                                        cardView_2.setVisibility(View.GONE);
                                        CardViewNIP1.setVisibility(View.GONE);
                                        CardViewNIP2.setVisibility(View.GONE);
                                        CardViewTEXT1.setVisibility(View.GONE);
                                        CardViewTEXT2.setVisibility(View.GONE);
                                        tvrfid1.setVisibility(View.VISIBLE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        vtitle.setVisibility(View.VISIBLE);
                                        fab.setVisibility(View.VISIBLE);
                                        ll_busqueda_nip.setVisibility(View.VISIBLE);
                                        layout_nombre.setVisibility(View.GONE);
                                        layout_nombre_vehiculo.setVisibility(View.GONE);
                                        layout_nombre2.setVisibility(View.GONE);
                                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                                    }else if(jsonObject_2.getString("metodo").equals("nombre")){
                                        tvbomba.setVisibility(View.VISIBLE);
                                        imbtn_ticket_1.setVisibility(View.GONE);
                                        imbtn_ticket_2.setVisibility(View.GONE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        spn_dispensarios_2.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        imageView_2.setVisibility(View.GONE);
                                        cardView_1.setVisibility(View.GONE);
                                        cardView_2.setVisibility(View.GONE);
                                        CardViewNIP1.setVisibility(View.GONE);
                                        CardViewNIP2.setVisibility(View.GONE);
                                        CardViewTEXT1.setVisibility(View.GONE);
                                        CardViewTEXT2.setVisibility(View.GONE);
                                        tvrfid1.setVisibility(View.VISIBLE);
                                        spn_dispensarios_1.setVisibility(View.GONE);
                                        imageView_1.setVisibility(View.GONE);
                                        vtitle.setVisibility(View.VISIBLE);
                                        fab.setVisibility(View.VISIBLE);
                                        ll_busqueda_nip.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    new AlertDialog.Builder(ActivityCreditoDual.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
                                    try {
                                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoMetodo2",e));
                                    }catch (JSONException e1){
                                        e1.printStackTrace();
                                    }
                                    e.printStackTrace();
                                }

                                try {
                                    if (jsonObject_2.getString("metodo").equals("nfc")){
                                        ll_clienteodoo.setVisibility(View.VISIBLE);
                                    }else{
                                        ll_clienteodoo.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    new AlertDialog.Builder(ActivityCreditoDual.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
                                    try {
                                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoMetodo2",e));
                                    }catch (JSONException e1){
                                        e1.printStackTrace();
                                    }
                                    e.printStackTrace();
                                }
                                if (jsonObject_2.has("imagen")){
                                    try {
                                        if (jsonObject_2.getString("imagen").equals("ok")){
                                            btn_creditoticket.setVisibility(View.VISIBLE);
                                        }else {
                                            btn_creditoticket.setVisibility(View.GONE);
                                        }
                                    }catch (JSONException e) {
                                        try {
                                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                                    .setTitle(R.string.error)
                                                    .setMessage(String.valueOf(e))
                                                    .setPositiveButton(R.string.btn_ok,null).show();
                                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoImagen2",e));
                                        }catch (JSONException e1){
                                            e1.printStackTrace();
                                        }
                                        e.printStackTrace();
                                    }
                                }

                                try {
                                    if (!jsonObject_2.getString("metodo").equals("nombre")){
                                        vtitle.setVisibility(View.VISIBLE);
                                        tvcreditolabel1.setVisibility(View.VISIBLE);
                                        tvcreditomsj1.setVisibility(View.VISIBLE);
                                        tvfleetmsj1.setVisibility(View.VISIBLE);
                                        tvfleetlabel1.setVisibility(View.VISIBLE);
                                        tvproductomsj1.setVisibility(View.VISIBLE);
                                        tvproductolabel1.setVisibility(View.VISIBLE);
                                        imagen.setVisibility(View.VISIBLE);
                                        et_odm.setVisibility(View.VISIBLE);
                                        layout_nombre2.setVisibility(View.GONE);
                                        layout_nombre_vehiculo2.setVisibility(View.GONE);
                                    }else if(jsonObject_2.getString("metodo").equals("nombre")){
                                        if (jsonObject_2.has("nroveh")){
                                            tvfleetmsj1.setVisibility(View.VISIBLE);
                                            tvfleetlabel1.setVisibility(View.VISIBLE);
                                            tvproductomsj1.setVisibility(View.VISIBLE);
                                            tvproductolabel1.setVisibility(View.VISIBLE);
                                            imagen.setVisibility(View.VISIBLE);
                                            et_odm.setVisibility(View.VISIBLE);
                                            layout_nombre2.setVisibility(View.GONE);
                                            layout_nombre_vehiculo2.setVisibility(View.GONE);
                                        }else if(!jsonObject_2.has("nroveh")){
                                            tvfleetmsj1.setVisibility(View.GONE);
                                            tvfleetlabel1.setVisibility(View.GONE);
                                            tvproductomsj1.setVisibility(View.GONE);
                                            tvproductolabel1.setVisibility(View.GONE);
                                            imagen.setVisibility(View.GONE);
                                            et_odm.setVisibility(View.GONE);
                                            layout_nombre2.setVisibility(View.GONE);
                                            layout_nombre_vehiculo2.setVisibility(View.VISIBLE);
                                        }
                                        layout_nombre.setVisibility(View.GONE);
                                        layout_nombre_vehiculo.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    new AlertDialog.Builder(ActivityCreditoDual.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
                                    e.printStackTrace();

                                }
                            }
                        }
                    }

                }
            }
        });
        spn_dispensarios_1 = (Spinner) findViewById(R.id.spn_dispensario_1);
        spn_dispensarios_2 = (Spinner) findViewById(R.id.spn_dispensario_2);
        imageView_1 = (ImageView) findViewById(R.id.imageView_1);
        imageView_2 = (ImageView) findViewById(R.id.imageView_2);
        cardView_1 = (CardView) findViewById(R.id.CardViewRFID) ;
        cardView_2 = (CardView) findViewById(R.id.CardViewRFID2) ;
        CardViewTEXT1 = (CardView)findViewById(R.id.CardViewTEXT1);
        CardViewTEXT2 = (CardView)findViewById(R.id.CardViewTEXT2);
        cardView_1.setOnClickListener(this);
        cardView_2.setOnClickListener(this);
        CardViewNIP1.setOnClickListener(this);
        CardViewNIP2.setOnClickListener(this);
        CardViewTEXT1.setOnClickListener(this);
        CardViewTEXT2.setOnClickListener(this);
        addListenerOnButton();
        MacActivity mac = new MacActivity();
        String query = "select p.numero_logico as logico from posicion as p \n" +
                "left outer join dispensario as disp on disp.id=p.id_dispensario\n" +
                "left outer join corte as c on c.id_dispensario=disp.id\n" +
                "left outer join dispositivos as dispo on dispo.id=c.id_dispositivo\n" +
                "where c.status =0 and dispo.mac_adr='"+mac.getMacAddress()+"'";
        try {
            DataBaseCG cg = new DataBaseCG();
            connect = cg.odbc_cecg_app(getApplicationContext());
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
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    R.layout.spinner_bombas, data);
            spn_dispensarios_1.setAdapter(NoCoreAdapter);
            spn_dispensarios_2.setAdapter(NoCoreAdapter);
            spn_dispensarios_2.setSelection(1);
        } catch (SQLException | IllegalAccessException | ClassNotFoundException | InstantiationException | JSONException e) {
            new AlertDialog.Builder(ActivityCreditoDual.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            try {
                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoPopulateSpinnerCG",e));
            }catch (JSONException e1){
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        et_odm.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!et_odm.getText().toString().isEmpty()){
                    try {
                        if( !tgl_area.isChecked()) {
                            Log.w("odm","1"+et_odm.getText());
                            jsonObject_1.put("odm", et_odm.getText());
                        }else if (tgl_area.isChecked()){
                            Log.w("odm","2"+et_odm.getText());
                            jsonObject_2.put("odm",et_odm.getText());
                        }
                    } catch (JSONException e) {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        try {
                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Credito",e));
                        }catch (JSONException e1){
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }

            }
        });
        et_clientecg.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!et_clientecg.getText().toString().isEmpty()){
                    try {
                        if( !tgl_area.isChecked()) {
                            Log.w("nip","1"+et_clientecg.getText());
                            jsonObject_1.put("nip", et_clientecg.getText());
                        }else if (tgl_area.isChecked()){
                            Log.w("nip","2"+et_clientecg.getText());
                            jsonObject_2.put("nip",et_clientecg.getText());
                        }
                    } catch (JSONException e) {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        e.printStackTrace();
                    }
                }

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (view.getId()) {
                    case R.id.fab_ticket_ladob:
                        //comando para llamar funcion de envio de mail
                        //sendEmail("Surtiendo en");
                        int bomba1=0;
                        int bomba2=0;
                        if(jsonObject_1.has("bomba")){
                            try {
                                bomba1=jsonObject_1.getInt("bomba");
                            } catch (JSONException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                try {
                                    logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoBomba1",e));
                                }catch (JSONException e1){
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            }
                        }
                        if (jsonObject_2.has("bomba")){
                            try {
                                bomba2=jsonObject_2.getInt("bomba");
                            } catch (JSONException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                try {
                                    logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoBomba2",e));
                                }catch (JSONException e1){
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            }
                        }
                        MacActivity mac = new MacActivity();
                        String query = "select p.numero_logico as logico from posicion as p \n" +
                                "left outer join dispensario as disp on disp.id=p.id_dispensario\n" +
                                "left outer join corte as c on c.id_dispensario=disp.id\n" +
                                "left outer join dispositivos as dispo on dispo.id=c.id_dispositivo\n" +
                                "where c.status =0 and dispo.mac_adr='"+mac.getMacAddress()+"' and " +
                                "p.numero_logico not in("+bomba1+","+bomba2+")";
                        try {
                            DataBaseCG cg = new DataBaseCG();
                            connect = cg.odbc_cecg_app(getApplicationContext());
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
                            fab_contado dialogFragment = fab_contado
                                    .newInstance("Ticket Contado",data);
                            dialogFragment.show(getFragmentManager(), "dialog");
                        } catch (SQLException | JSONException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoPopulateSpinnerFabCG",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }

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
                Log.w("tag",data.toString());
                try {
                    if(!tgl_area.isChecked()){
                        if(jsonObject_1.has("metodo") && jsonObject_1.getString("metodo").equals("nfc")) {
                            jsonObject_1.put("rfc", data.getString("1"));
                            jsonObject_1.put("id_tag",bin2hex(myTag.getId()));
                            new ActivityCreditoDual.AsyncFetch(data.getString("1"),jsonObject_1.getString("metodo")).execute();
                        }else {
                            Toast.makeText(this,"Metodo de identificacion seleccionado NIP",Toast.LENGTH_LONG).show();
                        }
                    }else{
                        if(jsonObject_2.has("metodo") && jsonObject_2.getString("metodo").equals("nfc")) {
                            jsonObject_2.put("rfc", data.getString("1"));
                            jsonObject_2.put("id_tag",bin2hex(myTag.getId()));
                            new ActivityCreditoDual.AsyncFetch(data.getString("1"),jsonObject_2.getString("metodo")).execute();
                        } else {
                            Toast.makeText(this,"Metodo de identificacion seleccionado NIP",Toast.LENGTH_LONG).show();
                        }
                    }

                } catch (JSONException e) {
                    new AlertDialog.Builder(ActivityCreditoDual.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    try {
                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoRfidIdentifiacion",e));
                    }catch (JSONException e1){
                        e1.printStackTrace();
                    }
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
            new AlertDialog.Builder(ActivityCreditoDual.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            try {
                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoGetTextFromNdefRecord",e));
            }catch (JSONException e1){
                e1.printStackTrace();
            }
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }


    @Override
    public void onBackPressed() {
        Toast.makeText(ActivityCreditoDual.this,
                "No puedes regresar en este menu!!!, favor de usar el icono de cerrar en la parte superior",
                Toast.LENGTH_SHORT).show();
    }

    public void onPause(){
        super.onPause();
        if (!IsTablet) {
            WriteModeOff();
        }
    }
    public void onResume(){
        super.onResume();
        if (!IsTablet) {
            WriteModeOn();
        }
    }
    @SuppressLint("NewApi") private void WriteModeOn(){
        writeMode = true;
        if (!IsTablet) {
            adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        }
    }

    @SuppressLint("NewApi") private void WriteModeOff(){
        writeMode = false;
        if (!IsTablet) {
            adapter.disableForegroundDispatch(this);
        }
    }

    private void addListenerOnButton() {
        imbtn_ticket_1 = (ImageButton) findViewById(R.id.imbtn_ticket_1);
        imbtn_ticket_2 = (ImageButton) findViewById(R.id.imbtn_ticket_2);
        imbtn_ticket_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (v.getId()) {
                    case R.id.imbtn_ticket_1:
                        if (!jsonObject_2.has("bomba")) {
                            try {
                                jsonObject_1.put("bomba", spn_dispensarios_1.getSelectedItem().toString());
                                tvbomba.setText("Bomba : "+jsonObject_1.getString("bomba"));
                                fab.setVisibility(View.VISIBLE);
                            } catch (JSONException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                e.printStackTrace();
                                try {
                                    logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoImageButtonimbtn_ticket1",e));
                                }catch (JSONException e1){
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            }
                            imbtn_ticket_1.setVisibility(View.GONE);
                            spn_dispensarios_1.setVisibility(View.GONE);
                            imageView_1.setVisibility(View.GONE);
                            if (!IsTablet) {
                                cardView_1.setVisibility(View.VISIBLE);
                            }
                            CardViewNIP1.setVisibility(View.VISIBLE);
                            CardViewTEXT1.setVisibility(View.VISIBLE);
                            tvbomba.setVisibility(View.VISIBLE);
                        }else{
                            try {
                                Log.w("bomba1",spn_dispensarios_1.getSelectedItem().toString());
                                Log.w("bomba2",jsonObject_2.getString("bomba"));
                                if (jsonObject_2.getString("bomba").equals(spn_dispensarios_1.getSelectedItem())){
                                    Toast.makeText(ActivityCreditoDual.this,"No se pude seleccionar la misma bomba",Toast.LENGTH_SHORT).show();
                                }else{
                                    try {
                                        jsonObject_1.put("bomba", spn_dispensarios_1.getSelectedItem().toString());
                                        tvbomba.setText("Bomba : "+jsonObject_1.getString("bomba"));
                                        fab.setVisibility(View.VISIBLE);
                                    } catch (JSONException e) {
                                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                                .setTitle(R.string.error)
                                                .setMessage(String.valueOf(e))
                                                .setPositiveButton(R.string.btn_ok,null).show();
                                        try {
                                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoSpinnerGetBomba1",e));
                                        }catch (JSONException e1){
                                            e1.printStackTrace();
                                        }
                                        e.printStackTrace();
                                    }
                                    imbtn_ticket_1.setVisibility(View.GONE);
                                    spn_dispensarios_1.setVisibility(View.GONE);
                                    imageView_1.setVisibility(View.GONE);
                                    if (!IsTablet) {
                                        cardView_1.setVisibility(View.VISIBLE);
                                    }
                                    CardViewNIP1.setVisibility(View.VISIBLE);
                                    CardViewTEXT1.setVisibility(View.VISIBLE);
                                    tvbomba.setVisibility(View.VISIBLE);
                                }
                            } catch (JSONException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                try {
                                    logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoBomba",e));
                                }catch (JSONException e1){
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            }
                        }
                        break;
                }

                transaction.commit();
            }
        });
        imbtn_ticket_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (v.getId()) {
                    case R.id.imbtn_ticket_2:
                        if (!jsonObject_1.has("bomba")) {
                            try {
                                jsonObject_2.put("bomba", spn_dispensarios_2.getSelectedItem().toString());
                                tvbomba.setText("Bomba : "+jsonObject_2.getString("bomba"));
                                fab.setVisibility(View.VISIBLE);
                            } catch (JSONException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                try {
                                    logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoImageButtonimbtn_ticket2",e));
                                }catch (JSONException e1){
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            }
                            imbtn_ticket_2.setVisibility(View.GONE);
                            spn_dispensarios_2.setVisibility(View.GONE);
                            imageView_2.setVisibility(View.GONE);
                            if (!IsTablet) {
                                cardView_2.setVisibility(View.VISIBLE);
                            }
                            CardViewNIP2.setVisibility(View.VISIBLE);
                            CardViewTEXT2.setVisibility(View.VISIBLE);
                            tvbomba.setVisibility(View.VISIBLE);
                        }else{
                            try {
                                Log.w("bomba2",spn_dispensarios_2.getSelectedItem().toString());
                                Log.w("bomba1",jsonObject_1.getString("bomba"));
                                if (jsonObject_1.getString("bomba").equals(spn_dispensarios_2.getSelectedItem())){
                                    Toast.makeText(ActivityCreditoDual.this,"No se pude seleccionar la misma bomba",Toast.LENGTH_SHORT).show();
                                }else{
                                    try {
                                        jsonObject_2.put("bomba", spn_dispensarios_2.getSelectedItem().toString());
                                        tvbomba.setText("Bomba : "+jsonObject_2.getString("bomba"));
                                        fab.setVisibility(View.VISIBLE);
                                    } catch (JSONException e) {
                                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                                .setTitle(R.string.error)
                                                .setMessage(String.valueOf(e))
                                                .setPositiveButton(R.string.btn_ok,null).show();
                                        try {
                                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoSpinnerGetBomba2",e));
                                        }catch (JSONException e1){
                                            e1.printStackTrace();
                                        }
                                        e.printStackTrace();
                                    }
                                    imbtn_ticket_2.setVisibility(View.GONE);
                                    spn_dispensarios_2.setVisibility(View.GONE);
                                    imageView_2.setVisibility(View.GONE);
                                    if (!IsTablet) {
                                        cardView_2.setVisibility(View.VISIBLE);
                                    }
                                    CardViewNIP2.setVisibility(View.VISIBLE);
                                    CardViewTEXT2.setVisibility(View.VISIBLE);
                                    tvbomba.setVisibility(View.VISIBLE);
                                }
                            } catch (JSONException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                try {
                                    logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoBomba2",e));
                                }catch (JSONException e1){
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            }
                        }
                        break;
                }

                transaction.commit();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.CardViewRFID:
                if(adapter.isEnabled()) {
                    cardView_1.setVisibility(View.GONE);
                    CardViewNIP1.setVisibility(View.GONE);
                    CardViewTEXT1.setVisibility(View.GONE);
                    tvrfid1.setVisibility(View.VISIBLE);
                    tvrfid1.setText(TV_NFC);
                    if (!tgl_area.isChecked()) {

                        try {
                            jsonObject_1.put("odoo_activo", odoo_activo);
                            jsonObject_1.put("metodo", "nfc");
                        } catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCardViewRFID1",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            jsonObject_2.put("odoo_activo", odoo_activo);
                            jsonObject_2.put("metodo", "nfc");
                        } catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCardViewRFID2",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }
                }else{
                    Toast.makeText(this,"Prender NFC",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.CardViewNIP1:
                cardView_1.setVisibility(View.GONE);
                CardViewNIP1.setVisibility(View.GONE);
                CardViewTEXT1.setVisibility(View.GONE);
                tvrfid1.setVisibility(View.VISIBLE);
                tvrfid1.setText(TV_NIP);
                ll_clienteodoo.setVisibility(View.VISIBLE);
                ll_busqueda_nip.setVisibility(View.VISIBLE);
                if(!tgl_area.isChecked()) {
                    try {
                        jsonObject_1.put("odoo_activo",odoo_activo);
                        jsonObject_1.put("metodo", "nip");
                    } catch (JSONException e) {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        try {
                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCardViewNIP1",e));
                        }catch (JSONException e1){
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }else{
                    try {
                        jsonObject_2.put("odoo_activo",odoo_activo);
                        jsonObject_2.put("metodo", "nip");
                    } catch (JSONException e) {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        try {
                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCardViewNIP2",e));
                        }catch (JSONException e1){
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.CardViewRFID2:
                cardView_2.setVisibility(View.GONE);
                CardViewNIP2.setVisibility(View.GONE);
                CardViewTEXT2.setVisibility(View.GONE);
                tvrfid1.setVisibility(View.VISIBLE);
                tvrfid1.setText(TV_NFC);
                if(tgl_area.isChecked()) {
                    try {
                        jsonObject_2.put("metodo", "nfc");
                        Log.w("metodo",jsonObject_2.getString("metodo"));
                    } catch (JSONException e) {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        try {
                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCardViewRFID22",e));
                        }catch (JSONException e1){
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }else{
                    try {
                        jsonObject_1.put("metodo", "nfc");
                        Log.w("metodo",jsonObject_1.getString("metodo"));
                    } catch (JSONException e) {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        try {
                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCardViewRFID21",e));
                        }catch (JSONException e1){
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.CardViewNIP2:
                cardView_2.setVisibility(View.GONE);
                CardViewNIP2.setVisibility(View.GONE);
                CardViewTEXT2.setVisibility(View.GONE);
                tvrfid1.setVisibility(View.VISIBLE);
                tvrfid1.setText(TV_NIP);

                ll_clienteodoo.setVisibility(View.VISIBLE);
                ll_busqueda_nip.setVisibility(View.VISIBLE);
                if(!tgl_area.isChecked()) {
                    try {
                        jsonObject_1.put("odoo_activo",odoo_activo);
                        jsonObject_1.put("metodo", "nip");
                    } catch (JSONException e) {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        try {
                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCardViewNIP21",e));
                        }catch (JSONException e1){
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }else{
                    try {
                        jsonObject_2.put("odoo_activo",odoo_activo);
                        jsonObject_2.put("metodo", "nip");
                    } catch (JSONException e) {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        try {
                            logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("CreditoCardViewNIP22",e));
                        }catch (JSONException e1){
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
                Log.w("json",jsonObject_2.toString());
                break;
            case R.id.CardViewTEXT1:
                cardView_1.setVisibility(View.GONE);
                CardViewNIP1.setVisibility(View.GONE);
                CardViewTEXT1.setVisibility(View.GONE);
                tvrfid1.setText(TV_NOMBRE);
                tvrfid1.setVisibility(View.VISIBLE);
                layout_nombre.setVisibility(View.VISIBLE);
                try {
                    jsonObject_1.put("odoo_activo",odoo_activo);
                    jsonObject_1.put("metodo", "nombre");
                } catch (JSONException e) {
                    new AlertDialog.Builder(ActivityCreditoDual.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }

                break;
            case R.id.CardViewTEXT2:
                cardView_2.setVisibility(View.GONE);
                CardViewNIP2.setVisibility(View.GONE);
                CardViewTEXT2.setVisibility(View.GONE);
                tvrfid1.setText(TV_NOMBRE);
                tvrfid1.setVisibility(View.VISIBLE);
                layout_nombre2.setVisibility(View.VISIBLE);
                try {
                    jsonObject_2.put("odoo_activo",odoo_activo);
                    jsonObject_2.put("metodo", "nombre");
                } catch (JSONException e) {
                    new AlertDialog.Builder(ActivityCreditoDual.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }
                break;
            case R.id.btn_creditoticket:

                ticketFragment tf = new ticketFragment();
                cgticket ticket = new cgticket();
                String nrotrn = null;
                try {
                    if (!tgl_area.isChecked()) {
                        nrotrn = vf.validar_utlimo_nrotrn(ActivityCreditoDual.this, jsonObject_1.getString("bomba"));
                    }else{
                        nrotrn = vf.validar_utlimo_nrotrn(ActivityCreditoDual.this, jsonObject_2.getString("bomba"));
                    }
                    odm = String.valueOf(et_odm.getText());
                    Log.w("odm1", odm);
                    if (odm.equals("")) {
                        odm = "0";
                    }
                    //aqui se valida que sea un servicio diferente despues de leer la etiqueta
                    Log.w("ult",ult_nrotrn+nrotrn);
                    String ultnrotrn="";
                    if(!tgl_area.isChecked()){
                        try {
                            ultnrotrn=jsonObject_1.getString("ult_nrotrn");
                        } catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Creditoc_reditoticket_ultnrotr",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            ultnrotrn=jsonObject_2.getString("ult_nrotrn");

                        } catch (JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Credito_creditoticket_ultnrotr",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }
                    if (ultnrotrn.equals(nrotrn)) {
                        JSONObject codcli=null;
                        //obtenemos el codigo del cliente
                        if(!tgl_area.isChecked()) {
                            try {
                                Log.i("validar codcli","inicia");
                                Log.i("json",String.valueOf(jsonObject_1));
                                codcli = vf.get_codcli(ActivityCreditoDual.this, jsonObject_1.getString("id_tag"), jsonObject_1.getString("metodo"));
                                Log.w("codigo", codcli.toString());
                            } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                try {
                                    logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Credito_creditoticket_get_codcli1",e));
                                }catch (JSONException e1){
                                    e1.printStackTrace();
                                }
                                /*e.printStackTrace();*/
                            }
                        }else{
                            try {
                                codcli = vf.get_codcli(ActivityCreditoDual.this, jsonObject_2.getString("id_tag"), jsonObject_2.getString("metodo"));
                            } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                try {
                                    logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Credito_creditoticket_get_codcli2",e));
                                }catch (JSONException e1){
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            }
                        }


                        //validar que el ticket no haya sido impreso
                        try {
                            if (!tgl_area.isChecked()) {
                                servicio = ticket.consulta_servicio(ActivityCreditoDual.this, jsonObject_1.getString("bomba"));
                            }else{
                                servicio = ticket.consulta_servicio(ActivityCreditoDual.this, jsonObject_2.getString("bomba"));
                            }
                        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException |JSONException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Credito_creditoticket_consulta_servicio",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            /*e.printStackTrace();*/
                        }

                        try {
                            impreso = ticket.cant_impreso(getApplicationContext(), servicio.getString("nrotrn"));
                            if (!impreso.equals(1) ) {
                                try {
                                    //realizamos el update para asignar el ticket al cliente de credito

                                    ticket.update_codcli(ActivityCreditoDual.this, nrotrn,
                                            codcli.getString("cliente"), codcli.getString("vehiculo"),
                                            odm,codcli.getString("tar"));
                                    cgticket_obj.guardarnrotrn(getApplicationContext(), servicio, 2);
                                } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                                    new AlertDialog.Builder(ActivityCreditoDual.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
                                    try {
                                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Credito_creditoticket_guardarnrotrn",e));
                                    }catch (JSONException e1){
                                        e1.printStackTrace();
                                    }
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException | SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Credito_creditoticket_cant_impreso",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }
                            /*e.printStackTrace();*/
                        }


                        //impresion con copia
                        cgticket cgticket_obj = new cgticket();
                        try {

                            if(!tgl_area.isChecked()) {
                                ticket1 = cgticket_obj.consulta_servicio(getApplicationContext(), jsonObject_1.getString("bomba"));
                                ticket1.put("impreso", impreso);
                                ticket1.put("tag",jsonObject_1.getString("id_tag"));
                                ticket1.put("bomba", jsonObject_1.getString("bomba"));
                                ticket1.put("odm", odm);
                                ticket1.put("tipo_venta",2);
                                ticket1.put("nip",cgticket_obj.nip_desp(this));


                                //impresion anterior
                                new ClassImpresionTicket(ActivityCreditoDual.this, getApplicationContext(), this.btn_creditoticket, ticket1).execute();

                            }else{
                                ticket2 = cgticket_obj.consulta_servicio(getApplicationContext(), jsonObject_2.getString("bomba"));
                                ticket2.put("impreso", impreso);
                                ticket2.put("tag", jsonObject_2.getString("id_tag"));
                                ticket2.put("bomba", jsonObject_2.getString("bomba"));
                                ticket2.put("odm", odm);
                                ticket2.put("tipo_venta",2);
                                ticket2.put("nip",cgticket_obj.nip_desp(this));


                                new ClassImpresionTicket(ActivityCreditoDual.this, getApplicationContext(), this.btn_creditoticket, ticket2).execute();
                            }
                            ticket_otra_bomba=new JSONObject();

                        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException | JSONException  e) {
                            new AlertDialog.Builder(ActivityCreditoDual.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            try {
                                logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Credito_creditoticket_consulta_impresion",e));
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }

                            e.printStackTrace();
                        }


                        //validar que flotillero este activo
                        Boolean flot_activo = false ; //vf.isServerReachable("http://187.210.108.135", getApplicationContext());
                        Log.w("floot", String.valueOf(flot_activo));
                        if (flot_activo.equals(true)) {
                            ClassFlotillero flot = new ClassFlotillero(ActivityCreditoDual.this, servicio);
                            flot.execute();
                        }
                        borrar();
                    } else {
                        //condicion cuando no llega el nuevo servicio
                        Toast.makeText(this, "Servicio aun no listo, espere un momento.", Toast.LENGTH_LONG).show();
                }
                } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                    new AlertDialog.Builder(ActivityCreditoDual.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    try {
                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("Creditobtn_creditoticket_validarultimonrotrn",e));
                    }catch (JSONException e1){
                        e1.printStackTrace();
                    }
                    /*e.printStackTrace();*/
                }
                break;
            case R.id.imbtn_clientecg:
                int len_nip = et_clientecg.getText().length();
                if (len_nip==0){
                    Toast.makeText(this,"Se requiere NIP!!!",Toast.LENGTH_LONG).show();
                    et_clientecg.requestFocus();
                }else if (len_nip>0) {
                    try {
                        if(!tgl_area.isChecked()){
                            if(jsonObject_1.has("metodo") && jsonObject_1.getString("metodo").equals("nip")) {
                                jsonObject_1.put("rfc", cgticket_obj.get_rfc_nip(this,String.valueOf(et_clientecg.getText()),jsonObject_1.getString("metodo")));
                                jsonObject_1.put("id_tag",et_clientecg.getText());
                                new ActivityCreditoDual.AsyncFetch(jsonObject_1.getString("rfc"),jsonObject_1.getString("metodo")).execute();
                            }else {
                                Toast.makeText(this,"Metodo de identificacion seleccionado NFC",Toast.LENGTH_LONG).show();
                            }
                        }else{
                            if(jsonObject_2.has("metodo") && jsonObject_2.getString("metodo").equals("nip")) {
                                jsonObject_2.put("rfc", cgticket_obj.get_rfc_nip(this,String.valueOf(et_clientecg.getText()),jsonObject_2.getString("metodo")));
                                jsonObject_2.put("id_tag",et_clientecg.getText());
                                new ActivityCreditoDual.AsyncFetch(jsonObject_2.getString("rfc"),jsonObject_1.getString("metodo")).execute();
                            } else {
                                Toast.makeText(this,"Metodo de identificacion seleccionado NFC",Toast.LENGTH_LONG).show();
                            }
                        }

                    } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                        new AlertDialog.Builder(ActivityCreditoDual.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        e.printStackTrace();
                    }
                }
                break;
        }

    }



    private class AsyncFetch extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(ActivityCreditoDual.this);
        HttpURLConnection conn;
        URL url = null;
        String searchQuery;
        String metodo;



        public AsyncFetch(String searchQuery,String metodo){
            this.searchQuery=searchQuery;
            this.metodo=metodo;
            Log.w("metodo",metodo);
            odoo_activo=false; //vf.isServerReachable("http://189.206.183.110", getApplicationContext());
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
            Log.w("odoo",odoo_activo.toString());

            JSONObject j = new JSONObject();
            try {
                j.put("saldo",1);
                j.put("deuda",0);

                j.put("limite",1);
                if (!tgl_area.isChecked()) {
                    j.put("cliente", cgticket_obj.get_cliente_den(ActivityCreditoDual.this, jsonObject_1.getString("id_tag"), metodo));
                }else{
                    j.put("cliente", cgticket_obj.get_cliente_den(ActivityCreditoDual.this, jsonObject_2.getString("id_tag"), metodo));
                }

            } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                new AlertDialog.Builder(ActivityCreditoDual.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                e.printStackTrace();
            }
            return (j.toString());

        }

        @Override
        protected void onPostExecute(String result) {
            //this method will be running on UI thread

            pdLoading.dismiss();
            Log.w("result",result.toString());
            if(result.equals("no rows")) {
                Toast.makeText(ActivityCreditoDual.this, "Cliente no encontrado", Toast.LENGTH_LONG).show();
            }else{

                try {
                    ValidacionFlotillero vf = new ValidacionFlotillero();
                    int estacion,dia,hora,monto,activo,cargas_turno;
                    String producto;

                    if (!tgl_area.isChecked()){
                        activo=vf.validar_estado(ActivityCreditoDual.this,jsonObject_1.getString("id_tag"),metodo);
                        estacion=vf.validar_estacion(ActivityCreditoDual.this,jsonObject_1.getString("id_tag"),metodo);
                        cargas_turno=vf.validar_cargas_turno(ActivityCreditoDual.this,jsonObject_1.getString("metodo"),jsonObject_1.getString("id_tag"));
                        dia=vf.carga_dia(ActivityCreditoDual.this,jsonObject_1.getString("id_tag"),metodo);
                        hora=vf.validar_hora(ActivityCreditoDual.this,jsonObject_1.getString("id_tag"),metodo);
                        monto=vf.validar_monto(ActivityCreditoDual.this,jsonObject_1.getString("id_tag"),metodo);
                        bomba=jsonObject_1.getString("bomba");
                        ult_nrotrn = vf.validar_utlimo_nrotrn(ActivityCreditoDual.this, jsonObject_1.getString("bomba"));
                        jsonObject_1.put("ult_nrotrn",ult_nrotrn);

                        producto = vf.validar_producto(ActivityCreditoDual.this,jsonObject_1.getString("id_tag"),metodo);
                    }else{
                        activo=vf.validar_estado(ActivityCreditoDual.this,jsonObject_2.getString("id_tag"),metodo);
                        estacion=vf.validar_estacion(ActivityCreditoDual.this,jsonObject_2.getString("id_tag"),metodo);
                        cargas_turno=vf.validar_cargas_turno(ActivityCreditoDual.this,jsonObject_2.getString("metodo"),jsonObject_2.getString("id_tag"));
                        dia=vf.carga_dia(ActivityCreditoDual.this,jsonObject_2.getString("id_tag"),metodo);
                        hora=vf.validar_hora(ActivityCreditoDual.this,jsonObject_2.getString("id_tag"),metodo);
                        monto=vf.validar_monto(ActivityCreditoDual.this,jsonObject_2.getString("id_tag"),metodo);
                        bomba=jsonObject_2.getString("bomba");
                        ult_nrotrn = vf.validar_utlimo_nrotrn(ActivityCreditoDual.this, jsonObject_2.getString("bomba"));
                        jsonObject_2.put("ult_nrotrn",ult_nrotrn);
                        producto = vf.validar_producto(ActivityCreditoDual.this,jsonObject_2.getString("id_tag"),metodo);
                    }

                    JSONObject json = new JSONObject(result);
                    Log.w("result",json.toString());
                    tvrfid1.setTextSize(20);
                    et_odm.setTextSize(15);
                    vtitle.setVisibility(View.VISIBLE);
                    tvrfid1.setText(json.getString("cliente"));
                    if (!tgl_area.isChecked()){
                        jsonObject_1.put("cliente",json.getString("cliente"));
                    }else {
                        jsonObject_2.put("cliente",json.getString("cliente"));
                    }
                    tvcreditomsj1.setTextSize(15);
                    tvcreditolabel1.setTextSize(15);
                    tvfleetmsj1.setTextSize(15);
                    tvfleetlabel1.setTextSize(15);
                    tvproductolabel1.setTextSize(15);
                    tvproductomsj1.setTextSize(15);
                    if (json.getDouble("saldo")>0){
                        tvcreditolabel1.setVisibility(View.VISIBLE);
                        tvcreditomsj1.setVisibility(View.VISIBLE);
                        tvcreditomsj1.setText("AUTORIZADO");
                        imagen.setImageResource(R.drawable.ok);
                        imagen.setVisibility(View.VISIBLE);
                        ll_clienteodoo.setVisibility(View.GONE);
                        if (!tgl_area.isChecked()){
                            jsonObject_1.put("odoo","AUTORIZADO");
                            jsonObject_1.put("fleet","AUTORIZADO");
                            jsonObject_1.put("imagen","ok");
                        }else {
                            jsonObject_2.put("odoo","AUTORIZADO");
                            jsonObject_2.put("fleet","AUTORIZADO");
                            jsonObject_2.put("imagen","ok");
                        }
                    }else if(json.getDouble("saldo")<=0){
                        //tvrfid.setText("Cliente "+json.getString("cliente")+" sin credito!!!");
                        tvcreditolabel1.setVisibility(View.VISIBLE);
                        tvcreditomsj1.setVisibility(View.VISIBLE);
                        tvcreditomsj1.setText("SIN CREDITO");
                        tvfleetmsj1.setVisibility(View.VISIBLE);
                        tvfleetlabel1.setVisibility(View.VISIBLE);
                        imagen.setImageResource(R.drawable.cancel);
                        imagen.setVisibility(View.VISIBLE);
                        btn_creditoticket.setVisibility(View.GONE);
                        ll_clienteodoo.setVisibility(View.GONE);
                        Toast.makeText(ActivityCreditoDual.this,"Sin Credito",Toast.LENGTH_LONG).show();
                        if (!tgl_area.isChecked()){
                            jsonObject_1.put("odoo","SIN CREDITO");
                            jsonObject_1.put("imagen","no");
                        }else {
                            jsonObject_2.put("odoo","SIN CREDITO");
                            jsonObject_2.put("imagen","no");
                        }

                    }
                    Log.w("Activo",String.valueOf(activo));
                    if (activo==0){
                        tvfleetmsj1.setVisibility(View.VISIBLE);
                        tvfleetlabel1.setVisibility(View.VISIBLE);
                        tvfleetmsj1.setText("Unidad deshabilitada o dada de baja.");
                        imagen.setImageResource(R.drawable.cancel);
                        Toast.makeText(ActivityCreditoDual.this, "Unidad deshabilitada o dada de baja.", Toast.LENGTH_LONG).show();
                        if (!tgl_area.isChecked()) {
                            jsonObject_1.put("fleet", "Unidad deshabilitada o dada de baja.");
                            jsonObject_1.put("imagen", "no");
                        } else {
                            jsonObject_2.put("fleet", "Unidad deshabilitada o dada de baja.");
                            jsonObject_2.put("imagen", "no");
                        }

                    }else {
                        if (estacion == 0) {
                            tvfleetmsj1.setVisibility(View.VISIBLE);
                            tvfleetlabel1.setVisibility(View.VISIBLE);
                            tvfleetmsj1.setText("Sin permiso de cargar(estacion invalida)");
                            imagen.setImageResource(R.drawable.cancel);
                            Toast.makeText(ActivityCreditoDual.this, "Sin permiso de cargar(estacion invalida)", Toast.LENGTH_LONG).show();
                            if (!tgl_area.isChecked()) {
                                jsonObject_1.put("fleet", "Sin permiso de cargar(estacion invalida)");
                                jsonObject_1.put("imagen", "no");
                            } else {
                                jsonObject_2.put("fleet", "Sin permiso de cargar(estacion invalida)");
                                jsonObject_2.put("imagen", "no");
                            }
                            //borrar

                        } else if (estacion == 1) {
                            if (dia == 0) {
                                tvfleetmsj1.setVisibility(View.VISIBLE);
                                tvfleetlabel1.setVisibility(View.VISIBLE);
                                tvfleetmsj1.setText("Sin permiso de cargar(dia invalido)");
                                Toast.makeText(ActivityCreditoDual.this, "Sin permiso de cargar(dia invalido)", Toast.LENGTH_LONG).show();
                                imagen.setImageResource(R.drawable.cancel);
                                if (!tgl_area.isChecked()) {
                                    jsonObject_1.put("fleet", "Sin permiso de cargar(dia invalido)");
                                    jsonObject_1.put("imagen", "no");
                                } else {
                                    jsonObject_2.put("fleet", "Sin permiso de cargar(dia invalido)");
                                    jsonObject_2.put("imagen", "no");
                                }
                                //borrar

                            } else if (dia == 1) {
                                if (hora == 1) {
                                    if (monto == 1) {
                                        if (cargas_turno == 0){
                                            tvfleetmsj1.setVisibility(View.VISIBLE);
                                            tvfleetlabel1.setVisibility(View.VISIBLE);
                                            tvfleetmsj1.setText("Sin permiso de cargar(Cargas por turnos excedidos)");
                                            Toast.makeText(ActivityCreditoDual.this, "Sin permiso de cargar(Cargas por turnos excedidos)", Toast.LENGTH_LONG).show();
                                            imagen.setImageResource(R.drawable.cancel);
                                            if (!tgl_area.isChecked()) {
                                                jsonObject_1.put("fleet", "Sin permiso de cargar(Cargas por turnos excedidos)");
                                                jsonObject_1.put("imagen", "no");
                                            } else {
                                                jsonObject_2.put("fleet", "Sin permiso de cargar(Cargas por turnos excedidos)");
                                                jsonObject_2.put("imagen", "no");
                                            }

                                        }else if (cargas_turno == 1){
                                            //btn_creditoticket.setVisibility(View.VISIBLE);
                                            et_odm.setVisibility(View.VISIBLE);
                                            Log.w("producto", producto);
                                            if (producto.equals("TODOS")) {
                                                tvfleetmsj1.setVisibility(View.VISIBLE);
                                                tvfleetlabel1.setVisibility(View.VISIBLE);
                                                tvfleetmsj1.setText("AUTORIZADO");
                                                tvproductomsj1.setVisibility(View.VISIBLE);
                                                tvproductolabel1.setVisibility(View.VISIBLE);
                                                tvproductomsj1.setText("TODOS");
                                                if (!tgl_area.isChecked()) {
                                                    jsonObject_1.put("fleet", "AUTORIZADO");
                                                    jsonObject_1.put("producto", "TODOS");
                                                } else {
                                                    jsonObject_2.put("fleet", "AUTORIZADO");
                                                    jsonObject_2.put("producto", "TODOS");
                                                }
                                            } else {
                                                tvfleetmsj1.setVisibility(View.VISIBLE);
                                                tvfleetlabel1.setVisibility(View.VISIBLE);
                                                tvfleetmsj1.setText("AUTORIZADO");
                                                tvproductomsj1.setVisibility(View.VISIBLE);
                                                tvproductolabel1.setVisibility(View.VISIBLE);
                                                tvproductomsj1.setText(producto);
                                                if (!tgl_area.isChecked()) {
                                                    jsonObject_1.put("fleet", "AUTORIZADO");
                                                    jsonObject_1.put("producto", producto);
                                                } else {
                                                    jsonObject_2.put("fleet", "AUTORIZADO");
                                                    jsonObject_2.put("producto", producto);
                                                }
                                            }
                                            if (!tgl_area.isChecked()) {
                                                if (jsonObject_1.getString("fleet").equals("AUTORIZADO") || jsonObject_1.getString("odoo").equals("AUTORIZADO")) {
                                                    btn_creditoticket.setVisibility(View.VISIBLE);
                                                }
                                            } else {
                                                if (jsonObject_2.getString("fleet").equals("AUTORIZADO") || jsonObject_2.getString("odoo").equals("AUTORIZADO")) {
                                                    btn_creditoticket.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }



                                    } else if (monto == 0) {
                                        tvfleetmsj1.setVisibility(View.VISIBLE);
                                        tvfleetlabel1.setVisibility(View.VISIBLE);
                                        tvfleetmsj1.setText("Sin permiso de cargar(Monto no disponible)");
                                        Toast.makeText(ActivityCreditoDual.this, "Sin permiso de cargar(Monto no disponible)", Toast.LENGTH_LONG).show();
                                        imagen.setImageResource(R.drawable.cancel);
                                        if (!tgl_area.isChecked()) {
                                            jsonObject_1.put("fleet", "Sin permiso de cargar(Monto no disponible)");
                                            jsonObject_1.put("imagen", "no");
                                        } else {
                                            jsonObject_2.put("fleet", "Sin permiso de cargar(Monto no disponible)");
                                            jsonObject_2.put("imagen", "no");
                                        }
                                        //borrar
                                        if (!tgl_area.isChecked()) {
                                            if (jsonObject_1.getString("fleet").equals("AUTORIZADO") || jsonObject_1.getString("odoo").equals("AUTORIZADO")) {
                                                btn_creditoticket.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            if (jsonObject_2.getString("fleet").equals("AUTORIZADO") || jsonObject_2.getString("odoo").equals("AUTORIZADO")) {
                                                btn_creditoticket.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                } else if (hora == 0) {
                                    tvfleetmsj1.setVisibility(View.VISIBLE);
                                    tvfleetlabel1.setVisibility(View.VISIBLE);
                                    tvfleetmsj1.setText("Sin permiso de cargar(hora invalida)");
                                    Toast.makeText(ActivityCreditoDual.this, "Sin permiso de cargar(hora invalida)", Toast.LENGTH_LONG).show();
                                    imagen.setImageResource(R.drawable.cancel);
                                    if (!tgl_area.isChecked()) {
                                        jsonObject_1.put("fleet", "Sin permiso de cargar(hora invalida)");
                                        jsonObject_1.put("imagen", "no");
                                    } else {
                                        jsonObject_2.put("fleet", "Sin permiso de cargar(hora invalida)");
                                        jsonObject_2.put("imagen", "no");
                                    }
                                    //borrar


                                    if (!tgl_area.isChecked()) {
                                        if (jsonObject_1.getString("fleet").equals("AUTORIZADO") || jsonObject_1.getString("odoo").equals("AUTORIZADO")) {
                                            btn_creditoticket.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        if (jsonObject_2.getString("fleet").equals("AUTORIZADO") || jsonObject_2.getString("odoo").equals("AUTORIZADO")) {
                                            btn_creditoticket.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    Log.w("turno",String.valueOf(cargas_turno));
                                }
                            }
                        }
                    }
                    if(!tgl_area.isChecked()){
                        Log.w("json",jsonObject_1.toString());
                        if(jsonObject_1.getString("imagen").equals("no")){
                            try {
                                int waited = 0;
                                // Splash screen pause time
                                while (waited < 1500) {
                                    sleep(100);
                                    waited += 100;
                                }
                                borrar();
                            } catch (InterruptedException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                e.printStackTrace();
                            }
                        }
                    }else{
                        Log.w("json",jsonObject_2.toString());
                        if(jsonObject_2.getString("imagen").equals("no")){

                            try {
                                int waited = 0;
                                // Splash screen pause time
                                while (waited < 1500) {
                                    sleep(100);
                                    waited += 100;
                                }
                                borrar();
                            } catch (InterruptedException e) {
                                new AlertDialog.Builder(ActivityCreditoDual.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok,null).show();
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                    // You to understand what actually error is and handle it appropriately
                    new AlertDialog.Builder(ActivityCreditoDual.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    try {
                        logCE.EscirbirLog(getApplicationContext(),jsonObjectError.put("creditoticket_validarrestricciones",e));
                    }catch (JSONException e1){
                        e1.printStackTrace();
                    }
                }

            }

        }

    }



    public void borrar(){

        if (!tgl_area.isChecked()){
            if (jsonObject_1.has("bomba")) {
                jsonObject_1.remove("bomba");
            }
            if (jsonObject_1.has("metodo")) {
                jsonObject_1.remove("metodo");
            }
            if (jsonObject_1.has("rfc")) {
                jsonObject_1.remove("rfc");
            }
            if (jsonObject_1.has("cliente")) {
                jsonObject_1.remove("cliente");
            }
            if (jsonObject_1.has("odoo")) {
                jsonObject_1.remove("odoo");
            }
            if (jsonObject_1.has("fleet")) {
                jsonObject_1.remove("fleet");
            }
            if (jsonObject_1.has("imagen")) {
                jsonObject_1.remove("imagen");
            }
            if (jsonObject_1.has("producto")) {
                jsonObject_1.remove("producto");
            }
            if(jsonObject_1.has("odm")){
                jsonObject_1.remove("odm");
            }
            et_odm.setText("");
            imageView_1.setVisibility(View.VISIBLE);
            spn_dispensarios_1.setVisibility(View.VISIBLE);
            imbtn_ticket_1.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);

        }else {
            if (jsonObject_2.has("bomba")) {
                jsonObject_2.remove("bomba");
            }
            if (jsonObject_2.has("metodo")) {
                jsonObject_2.remove("metodo");
            }
            if (jsonObject_2.has("rfc")) {
                jsonObject_2.remove("rfc");
            }
            if (jsonObject_2.has("cliente")) {
                jsonObject_2.remove("cliente");
            }
            if (jsonObject_2.has("odoo")) {
                jsonObject_2.remove("odoo");
            }
            if (jsonObject_2.has("fleet")) {
                jsonObject_2.remove("fleet");
            }
            if (jsonObject_2.has("imagen")) {
                jsonObject_2.remove("imagen");
            }
            if (jsonObject_2.has("producto")) {
                jsonObject_2.remove("producto");
            }
            if (jsonObject_2.has("odm")){
                jsonObject_2.remove("odm");
            }
            et_odm.setText("");
            imageView_2.setVisibility(View.VISIBLE);
            spn_dispensarios_2.setVisibility(View.VISIBLE);
            imbtn_ticket_2.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        }
        et_clientecg.setText("");
        btn_creditoticket.setVisibility(View.GONE);
        imagen.setVisibility(View.GONE);
        tvfleetlabel1.setVisibility(View.GONE);
        tvfleetmsj1.setVisibility(View.GONE);
        tvcreditolabel1.setVisibility(View.GONE);
        tvcreditomsj1.setVisibility(View.GONE);
        tvproductolabel1.setVisibility(View.GONE);
        tvproductomsj1.setVisibility(View.GONE);
        tvrfid1.setVisibility(View.GONE);
        et_odm.setVisibility(View.GONE);
        vtitle.setVisibility(View.GONE);
        tvrfid1.setText(R.string.rfid);
        vg.invalidate();
    }


    protected void sendEmail(String metodo) {
        Log.i("SendMailActivity", "Send Button Clicked.");

        String fromEmail = "facturacion@combu-express.com.mx";
        String fromPassword = "2495@2495";
        String toEmails = "amontes@combu-express.com.mx";
        List<String> toEmailList = Arrays.asList(toEmails
                .split("\\s*,\\s*"));
        Log.i("SendMailActivity", "To List: " + toEmailList);
        String emailSubject = null;
        try {
            emailSubject = metodo+" 0"+jsonObject_1.getString("bomba");
        } catch (JSONException e) {
            new AlertDialog.Builder(ActivityCreditoDual.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
        String emailBody = "Prueba de correo";
        new SendMailTask(ActivityCreditoDual.this).execute(fromEmail,
                fromPassword, toEmailList, emailSubject, emailBody);
    }
    public void doNegativeClick(){
        Toast.makeText(this, "Ha pulsado Cancelar", Toast.LENGTH_SHORT).show();
    }
    public void SearchCustomerName(View v){
        try {
            if (!tgl_area.isChecked()){
                if (etSearchCustomer.getText().length()<MIN_SEARCH){
                    String error = "El minimo de caracteres para buscar es (" + String.valueOf(MIN_SEARCH)
                            + "), la busqueda actual es de "+ etSearchCustomer.getText().length()
                            + " caracter(es).";
                    new AlertDialog.Builder(ActivityCreditoDual.this)
                            .setTitle(R.string.error)
                            .setMessage(error)
                            .setPositiveButton(R.string.btn_ok,null).show();
                }else {
                    Log.i("cliente buscar", etSearchCustomer.getText().toString());
                    List<DataCustomerCG> dataCustomerCG = new ArrayList<>(cgticket_obj.getCustomerCG(
                            this, etSearchCustomer.getText().toString()));
                    mRVCustomerCG = (RecyclerView) findViewById(R.id.clientes_cg);
                    mAdapter = new AdapterCustomerCG(ActivityCreditoDual.this, dataCustomerCG);
                    mRVCustomerCG.setAdapter(mAdapter);
                    mRVCustomerCG.setLayoutManager(new LinearLayoutManager(ActivityCreditoDual.this));
                }
            }else{
                if (etSearchCustomer2.getText().length()<MIN_SEARCH){
                    String error = "El minimo de caracteres para buscar es (" + String.valueOf(MIN_SEARCH)
                            + "), la busqueda actual es de "+ etSearchCustomer.getText().length()
                            + " caracter(es).";
                    new AlertDialog.Builder(ActivityCreditoDual.this)
                            .setTitle(R.string.error)
                            .setMessage(error)
                            .setPositiveButton(R.string.btn_ok,null).show();
                }else {
                    Log.i("cliente buscar", etSearchCustomer2.getText().toString());
                    List<DataCustomerCG> dataCustomerCG2 = new ArrayList<>(cgticket_obj.getCustomerCG(
                            this, etSearchCustomer2.getText().toString()));
                    mRVCustomerCG2 = (RecyclerView) findViewById(R.id.clientes_cg2);
                    mAdapter2 = new AdapterCustomerCG(ActivityCreditoDual.this, dataCustomerCG2);
                    mRVCustomerCG2.setAdapter(mAdapter2);
                    mRVCustomerCG2.setLayoutManager(new LinearLayoutManager(ActivityCreditoDual.this));
                }
            }

        } catch (ClassNotFoundException | SQLException | InstantiationException | JSONException | IllegalAccessException e) {
            new AlertDialog.Builder(ActivityCreditoDual.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
        }
        Log.i("ET Search Customer CG", etSearchCustomer.getText().toString());

        CloseKeyboard();
    }
    public void CloseKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    public void AdapterClickCustomerCG(JSONObject js){
        Log.i("AdapterClickCustomerCG",String.valueOf(js));
        try {
            tvrfid1.setText(js.getString("cliente"));
        } catch (JSONException e) {
            new AlertDialog.Builder(ActivityCreditoDual.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
        if (!tgl_area.isChecked()) {
            layout_nombre.setVisibility(View.GONE);
            layout_nombre_vehiculo.setVisibility(View.VISIBLE);
            try {
                tvvehiculo_cliente.setText(js.getString("cliente"));
                tvvehiculo_codcli.setText(js.getString("codcli"));
                tvvehiculo_rfc.setText(js.getString("rfc"));
                List<DataCustomerCG> dataCustomerVehicleCG = new ArrayList<>(cgticket_obj.getCustomerVehicleCG(
                        this,js.getString("codcli")));
                mRVCustomerVehicleCG = (RecyclerView) findViewById(R.id.clientesvehiculos_cg);
                mAdapterVehicle = new AdapterCustomerVehicleCG(ActivityCreditoDual.this,dataCustomerVehicleCG);
                mRVCustomerVehicleCG.setAdapter(mAdapterVehicle);
                mRVCustomerVehicleCG.setLayoutManager(new LinearLayoutManager(ActivityCreditoDual.this));
                Log.i("vehiculo", String.valueOf(dataCustomerVehicleCG));
            } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                new AlertDialog.Builder(ActivityCreditoDual.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                e.printStackTrace();
            }
        }else if(tgl_area.isChecked()){
            layout_nombre2.setVisibility(View.GONE);
            layout_nombre_vehiculo2.setVisibility(View.VISIBLE);
            try {
                tvvehiculo_cliente2.setText(js.getString("cliente"));
                tvvehiculo_codcli2.setText(js.getString("codcli"));
                tvvehiculo_rfc2.setText(js.getString("rfc"));
                List<DataCustomerCG> dataCustomerVehicleCG2 = new ArrayList<>(cgticket_obj.getCustomerVehicleCG(
                        this,js.getString("codcli")));
                mRVCustomerVehicleCG2 = (RecyclerView) findViewById(R.id.clientesvehiculos_cg2);
                mAdapterVehicle2 = new AdapterCustomerVehicleCG(ActivityCreditoDual.this,dataCustomerVehicleCG2);
                mRVCustomerVehicleCG2.setAdapter(mAdapterVehicle2);
                mRVCustomerVehicleCG2.setLayoutManager(new LinearLayoutManager(ActivityCreditoDual.this));
                Log.i("vehiculo", String.valueOf(dataCustomerVehicleCG2));
            } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                new AlertDialog.Builder(ActivityCreditoDual.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                e.printStackTrace();
            }
        }
        try {
            PutJsonAdapterToggle(tgl_area.isChecked(),js);
        } catch (JSONException e) {
            new AlertDialog.Builder(ActivityCreditoDual.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }

    }
    public void AdapterClickCustomerVehicleCG(JSONObject js){
        Log.i("CustomerVehicleCG",String.valueOf(js));
        try {
            PutJsonAdapterToggle(tgl_area.isChecked(),js);
            if(!tgl_area.isChecked()){
                new ActivityCreditoDual.AsyncFetch(jsonObject_1.getString("rfc"),jsonObject_1.getString("metodo")).execute();
                tvfleetmsj1.setVisibility(View.VISIBLE);
                tvfleetlabel1.setVisibility(View.VISIBLE);
                tvproductomsj1.setVisibility(View.VISIBLE);
                tvproductolabel1.setVisibility(View.VISIBLE);
                imagen.setVisibility(View.VISIBLE);
                et_odm.setVisibility(View.VISIBLE);
                layout_nombre.setVisibility(View.GONE);
                layout_nombre_vehiculo.setVisibility(View.GONE);
            }else{
                new ActivityCreditoDual.AsyncFetch(jsonObject_2.getString("rfc"),jsonObject_2.getString("metodo")).execute();
                tvfleetmsj1.setVisibility(View.VISIBLE);
                tvfleetlabel1.setVisibility(View.VISIBLE);
                tvproductomsj1.setVisibility(View.VISIBLE);
                tvproductolabel1.setVisibility(View.VISIBLE);
                imagen.setVisibility(View.VISIBLE);
                et_odm.setVisibility(View.VISIBLE);
                layout_nombre2.setVisibility(View.GONE);
                layout_nombre_vehiculo2.setVisibility(View.GONE);

            }
        } catch (JSONException e) {
            new AlertDialog.Builder(ActivityCreditoDual.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
    }

    public void PutJsonAdapterToggle(boolean b,JSONObject js) throws JSONException {
        Log.i("Putjson",String.valueOf(js));
        if (!b){
            if (js.has("rfc")) {
                jsonObject_1.put("rfc", js.getString("rfc"));
            }if (js.has("cliente")) {
                jsonObject_1.put("cliente", js.getString("cliente"));
            }if(js.has("codcli")) {
                jsonObject_1.put("codcli", js.getString("codcli"));
            }if(js.has("vehiculo")){
                jsonObject_1.put("vehiculo",js.getString("vehiculo"));
            }if(js.has("rsp")){
                jsonObject_1.put("rsp",js.getString("rsp"));
            }if(js.has("nroveh")){
                jsonObject_1.put("nroveh",js.getString("nroveh"));
            }if(js.has("tar")){
                jsonObject_1.put("id_tag",js.getString("tar"));
            jsonObject_1.put("tar",js.getString("tar"));
            }
        }else{
            if (js.has("rfc")) {
                jsonObject_2.put("rfc", js.getString("rfc"));
            }if (js.has("cliente")) {
                jsonObject_2.put("cliente", js.getString("cliente"));
            }if(js.has("codcli")) {
                jsonObject_2.put("codcli", js.getString("codcli"));
            }if(js.has("vehiculo")){
                jsonObject_2.put("vehiculo",js.getString("vehiculo"));
            }if(js.has("rsp")){
                jsonObject_2.put("rsp",js.getString("rsp"));
            }if(js.has("nroveh")){
                jsonObject_2.put("nroveh",js.getString("nroveh"));
            }if(js.has("tar")){
                jsonObject_2.put("tar",js.getString("tar"));
                jsonObject_2.put("id_tag",js.getString("tar"));
            }
        }
    }
}
