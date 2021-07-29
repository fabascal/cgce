 package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import cg.ce.app.chris.com.cgce.ControlGas.GetCustomerName;
import cg.ce.app.chris.com.cgce.ControlGas.GetCustomerNip;
import cg.ce.app.chris.com.cgce.ControlGas.GetCustomerTag;
import cg.ce.app.chris.com.cgce.ControlGas.GetCustomerVale;
import cg.ce.app.chris.com.cgce.ControlGas.GetCustomerVehicle;
import cg.ce.app.chris.com.cgce.ControlGas.GetDespachador;
import cg.ce.app.chris.com.cgce.ControlGas.GetEstacionData;
import cg.ce.app.chris.com.cgce.ControlGas.GetImpreso;
import cg.ce.app.chris.com.cgce.ControlGas.GetLastNROTRN;
import cg.ce.app.chris.com.cgce.ControlGas.GetPumpPosition;
import cg.ce.app.chris.com.cgce.ControlGas.GetTicket;
import cg.ce.app.chris.com.cgce.ControlGas.GetValesCodcli;
import cg.ce.app.chris.com.cgce.ControlGas.GetVehicleRestrictions;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.ControlGasListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetCustomerNameListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetCustomerNipListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetCustomerVehicleListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetDespachadorListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetEstacionDataListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetImpresoListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetPumpPositionListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.UpdateNrotrnListener;
import cg.ce.app.chris.com.cgce.ControlGas.PutImpreso;
import cg.ce.app.chris.com.cgce.ControlGas.UpdateCFDi;
import cg.ce.app.chris.com.cgce.ControlGas.UpdateCodcli;
import cg.ce.app.chris.com.cgce.ControlGas.UpdateNrotrn;
import cg.ce.app.chris.com.cgce.ValesWS.Listeners.NominativaListener;
import cg.ce.app.chris.com.cgce.ValesWS.Listeners.NotaCreditoListener;
import cg.ce.app.chris.com.cgce.ValesWS.Listeners.ValesWSListeners;
import cg.ce.app.chris.com.cgce.ValesWS.Nominativa;
import cg.ce.app.chris.com.cgce.ValesWS.NotaCredito;
import cg.ce.app.chris.com.cgce.ValesWS.ValeAdapterRV;
import cg.ce.app.chris.com.cgce.ValesWS.ValesList;
import cg.ce.app.chris.com.cgce.ValesWS.ValesWS;
import cg.ce.app.chris.com.cgce.common.Variables;
import cg.ce.app.chris.com.cgce.dialogos.close_credito;
import cg.ce.app.chris.com.cgce.dialogos.fab_contado;

import static android.view.View.GONE;

public class Credito extends AppCompatActivity implements View.OnClickListener,
        com.epson.epos2.printer.ReceiveListener, GetPumpPositionListener, GetEstacionDataListener,
        GetImpresoListener, UpdateNrotrnListener, GetCustomerNipListener, GetCustomerNameListener,
        GetCustomerVehicleListener, GetDespachadorListener, ValesWSListeners, NominativaListener {
    ValidateTablet tablet = new ValidateTablet();
    Spinner spn_posicion;
    Drawable icon;
    String marca;
    ImageButton btn_print, imbtn_clientecg;
    JSONObject Posiciones = new JSONObject();
    JSONArray Logicos = new JSONArray();
    Variables variables = new Variables();
    final static String ERROR_POSICION_CONTADO = "Todas las posiciones tienen una venta en curso.";
    final static String MESSAGE_CLEAN_DATA = "Si continuas con esta accion eliminaras los datos " +
            "almacenados para esta posicion.";
    final static String MESSAGE_METODO_NFC = "Primero selecciona un metodo para la posicion deseada.";
    CardView CardViewRFID, CardViewNIP, CardViewNOMBRE, CardViewVALE;
    Boolean hasMetodo = false, hasCliente = false, state_btn, flag_TicketImpreso = false;
    ViewFlipper viewFlipper;
    final static int MIN_SEARCH = 3;
    EditText etSearchCustomer, et_clientecg;
    TextView tvvehiculo_cliente, tvvehiculo_rfc, tvvehiculo_codcli, lunes, martes, miercoles, jueves, viernes, sabado, domingo;
    /*elementos de activity_credito_impresion*/
    TextView tv_cliente, tv_rfc, tv_codcli, tv_placa, tv_vehiculo, tv_chofer, semana, tvhora, tvhora1, tvhora2, tvhora3;
    TextView tvestado1, tvestado2, tvproducto1, tvproducto2;
    EditText et_odm, filterPLC;
    cgticket cgticket_obj = new cgticket();
    ValidacionFlotillero validacionFlotillero = new ValidacionFlotillero();
    private RecyclerView mRVCustomerCG, mRVCustomerVehicleCG;
    private AdapterCustomerCG mAdapter;
    private AdapterCustomerVehicleCG mAdapterVehicle;
    List<DataCustomerCG> dataCustomerCG;
    /*Elementos de SDK Epson*/
    private Printer mPrinter = null;
    private Context mContext = null;
    private static final int REQUEST_PERMISSION = 100;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds
    /*Elemento Progress para imprimir*/
    ProgressDialog pdLoading;
    LogCE logCE = new LogCE();
    Integer flag = 0;
    /*Elementos de formato*/
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    FloatingActionButton fab;
    Sensores sensores = new Sensores();
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;
    Boolean flag_clean_data = true;
    Tag myTag;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean IsTablet = false;
    boolean writeMode;
    MacActivity mac = new MacActivity();
    JSONObject datos_domicilio = new JSONObject();
    String FacturacionURL = null;

    /*Elementos para vales*/
    Button ws_vale, scan;
    final static String SCAN_PROMPT = "Escanear vales.";
    final static String ERROR_NO_VALE = "Sin vales para procesar.";
    final static String ERROR_NO_WSVALE = "Primero se requiere consumir los vales";
    final static String ERROR_VALE_CONSUMIDO = "No se debe escanear un vale posterior a realizar el consumo de los mismos.";
    final static String ERROR_VALE_NOMINATIVA = "No se puede continuar con el proceso, no existe vale consumido.";
    JSONObject jsVales = new JSONObject();
    final static String VALE_MSJ_VALIDATE = "En espera de validacion";
    ValeAdapterRV valeAdapterRV;
    RecyclerView rv_vales;
    ImageView vale_image;
    toJson json = new toJson();
    TextView cliente_vale, total_vale;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Log.w("Tableta","es Tableta");
            IsTablet=true;
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            IsTablet=false;
            Log.w("Tableta","no es Tableta");
        }
        spn_posicion = findViewById(R.id.spn_posicion);
        FillPosicion();
        sensores.bluetooth();
        sensores.wifi(this,true);
        CardViewRFID = findViewById(R.id.CardViewRFID);
        if (!IsTablet){
            try {
                onCreateNFC();
            } catch (ClassNotFoundException | SQLException | InstantiationException |
                    IllegalAccessException | JSONException e) {
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + e);
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok, null).show();
                e.printStackTrace();
            }
        }
        else if(IsTablet) {
            CardViewRFID.setVisibility(View.GONE);
        }
        mContext = this;
        viewFlipper = findViewById(R.id.viewFlipper);
        rv_vales = findViewById(R.id.rv_vales);
        rv_vales.setHasFixedSize(true);
        rv_vales.setLayoutManager(new LinearLayoutManager(this));
        vale_image = (ImageView) findViewById(R.id.vale_image);
        cliente_vale = (TextView) findViewById(R.id.cliente_vale);
        total_vale = (TextView) findViewById(R.id.total_vale);
        CardViewNIP = findViewById(R.id.CardViewNIP);
        CardViewVALE = findViewById(R.id.CardViewVALE);
        CardViewNOMBRE = findViewById(R.id.CardViewNOMBRE);
        tvvehiculo_cliente = findViewById(R.id.tvvehiculo_cliente);
        tvvehiculo_rfc = findViewById(R.id.tvvehiculo_rfc);
        tvvehiculo_codcli = findViewById(R.id.tvvehiculo_codcli);
        tv_cliente = findViewById(R.id.tv_cliente);
        tv_rfc = findViewById(R.id.tv_rfc);
        tv_codcli = findViewById(R.id.tv_codcli);
        tv_placa = findViewById(R.id.tv_placa);
        tv_vehiculo = findViewById(R.id.tv_vehiculo);
        tv_chofer = findViewById(R.id.tv_chofer);
        et_odm = findViewById(R.id.et_odm);
        et_odm.setFocusableInTouchMode(false);
        et_odm.setFocusable(false);
        et_odm.setFocusableInTouchMode(true);
        et_odm .setFocusable(true);
        CardViewRFID.setOnClickListener((View.OnClickListener) this);
        CardViewNIP.setOnClickListener((View.OnClickListener) this);
        CardViewNOMBRE.setOnClickListener((View.OnClickListener) this);
        CardViewVALE.setOnClickListener((View.OnClickListener)this);
        etSearchCustomer = findViewById(R.id.etSearchCustomer);
        btn_print = findViewById(R.id.btncredito);
        fab = findViewById(R.id.fab);
        imbtn_clientecg = findViewById(R.id.imbtn_clientecg);
        et_clientecg = findViewById(R.id.et_clientecg);
        mRVCustomerVehicleCG = (RecyclerView) findViewById(R.id.clientesvehiculos_cg);
        lunes = (TextView) findViewById(R.id.lunes);
        martes = (TextView) findViewById(R.id.martes);
        miercoles = (TextView) findViewById(R.id.miercoles);
        jueves = (TextView) findViewById(R.id.jueves);
        viernes = (TextView) findViewById(R.id.viernes);
        sabado = (TextView) findViewById(R.id.sabado);
        domingo = (TextView) findViewById(R.id.domingo);
        semana = (TextView) findViewById(R.id.semana);
        tvhora = (TextView) findViewById(R.id.tvhora);
        tvhora1 = (TextView) findViewById(R.id.tvhora1);
        tvhora2 = (TextView) findViewById(R.id.tvhora2);
        tvhora3 = (TextView) findViewById(R.id.tvhora3);
        tvestado1 = (TextView) findViewById(R.id.tvestado1);
        tvestado2 = (TextView) findViewById(R.id.tvestado2);
        tvproducto1 = (TextView) findViewById(R.id.tvproducto1);
        tvproducto2 = (TextView) findViewById(R.id.tvproducto2);
        filterPLC = (EditText) findViewById(R.id.filterPLC);
        scan = (Button) findViewById(R.id.scan);
        scan.setOnClickListener(this);
        ws_vale = (Button) findViewById(R.id.ws_vale);
        ws_vale.setOnClickListener(this);
        spn_posicion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    CoreScreen();
                } catch (JSONException | InstantiationException | SQLException |
                        IllegalAccessException | ClassNotFoundException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        et_odm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final StringBuilder sb = new StringBuilder(s.length());
                sb.append(s);
                try {
                    PutData(variables.KEY_ODM,sb.toString());
                } catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        /*Se inicializa el objeto impresora*/
        initializeObject();
        pdLoading = new ProgressDialog(Credito.this);
        GetEstacionData getEstacionData = new GetEstacionData(this, getApplicationContext());
        getEstacionData.delegate= this;
        getEstacionData.execute();

        filterPLC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("Credito ontext "+ s);
                mAdapterVehicle.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) {
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
    public void BtnCredito(View view){
        new GetTicket(this, new ControlGasListener() {
            @Override
            public void processFinish(JSONObject output) {
                try {
                    if (output.getInt(Variables.CODE_ERROR)==0){
                        final JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
                        int index = spn_posicion.getSelectedItemPosition();
                        Validar.getJSONObject(index).put(Variables.KEY_TICKET,output);
                        if (Integer.parseInt(GetData(variables.KEY_ULT_NROTRN))<Integer.parseInt(GetTicketData(variables.KEY_TICKET_NROTRN))) {
                            UpdateCodcli();
                        }else{
                            new AlertDialog.Builder(Credito.this)
                                    .setTitle(R.string.error)
                                    .setIcon(icon)
                                    .setMessage(R.string.EsperaServicio)
                                    .setPositiveButton(R.string.btn_ok,null).show();
                        }
                    }else{
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                .setPositiveButton(R.string.btn_ok, null).show();
                    }
                } catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
            }
        }).execute(spn_posicion.getSelectedItem().toString(),mac.getMacAddress(),"1");

    }
    private void UpdateCodcli() throws JSONException  {
        GetImpreso getImpreso = new GetImpreso(this, getApplicationContext());
        getImpreso.delegate=this;
        getImpreso.execute(GetTicketData(Variables.KEY_TICKET_NROTRN));
    }
    private void PrintReceip()  {
        this.updateButtonState(false);
        try {
            if (GetData(variables.KEY_IMPRESO).equals("10")){
                JSONArray Validar = null;
                try {
                    Validar = Posiciones.getJSONArray(variables.POSICIONES);
                    int index = spn_posicion.getSelectedItemPosition();
                    UpdateNrotrn updateNrotrn = new UpdateNrotrn(this, getApplicationContext(),mac.getMacAddress(),"2");
                    updateNrotrn.delegate=this;
                    System.out.println(Validar.getJSONObject(index).getJSONObject(Variables.KEY_TICKET));
                    updateNrotrn.execute(Validar.getJSONObject(index).getJSONObject(Variables.KEY_TICKET));

                } catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }
            }

        } catch ( JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setIcon(icon)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            updateButtonState(true);
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        marca=sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express");
        switch (Objects.requireNonNull(sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express"))){
            case "Combu-Express":
                setTheme(R.style.AppThemeCredito);
                setContentView(R.layout.activity_credito);
                icon = getDrawable(R.drawable.combuito);
                FacturacionURL = "facturacion.combuexpress.mx";
                break;
            case "Repsol":
                setTheme(R.style.AppThemeCreditoRepsol);
                setContentView(R.layout.activity_credito_repsol);
                icon = getDrawable(R.drawable.isologo_repsol);
                FacturacionURL = "facturacionrepsol.combuexpress.mx";
                break;
            case "Ener":
                setTheme(R.style.AppThemeCreditoEner);
                setContentView(R.layout.activity_credito_ener);
                icon = getDrawable(R.drawable.logo_impresion_ener);
                FacturacionURL = "facturacionener.combuexpress.mx";
                break;
            case "Total":
                setTheme(R.style.AppThemeCreditoTotal);
                setContentView(R.layout.activity_credito_total);
                icon = getDrawable(R.drawable.total);
                FacturacionURL = "facturaciontotal.combuexpress.mx";
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    @Override
    public void onBackPressed() {
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        String Text = "No puedes regresar en este menu!!!, favor de usar el icono de cerrar en la parte superior";
        new AlertDialog.Builder(Credito.this)
                .setTitle(R.string.error)
                .setIcon(icon)
                .setMessage(Text)
                .setPositiveButton(R.string.btn_ok,null).show();
    }
    /*Llenado inicial del Json, se ejecuta una ves en el oncreate*/
    public void FillPosicion(){
        GetPumpPosition getPumpPosition = new GetPumpPosition(this, getApplicationContext());
        getPumpPosition.delegate=this;
        getPumpPosition.execute(mac.getMacAddress());
    }
    private void CoreScreen() throws JSONException, ClassNotFoundException, SQLException,
            InstantiationException, IllegalAccessException {
        /*Posiciones del ViewFlipper
        * 0.- Actividad para seleccionar el metodo
        * 1.- Metodo para seleccionar NFC
        * 2.- Metodo para seleccionar NIP
        * 3.- Metodo para seleccionar NOMBRE
        * 4.- Mostrar vehiculos del metodo nombre
        * 5.- Metodo final, ya con todos los parametros y en espera de impresion
        * 6.- Metodo vales
        * */
        Log.w(variables.POSICIONES,String.valueOf(Posiciones));

        et_clientecg.setText("");
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        hasMetodo = Validar.getJSONObject(spn_posicion.getSelectedItemPosition()).has(variables.METODO);
        hasCliente = Validar.getJSONObject(spn_posicion.getSelectedItemPosition()).has(variables.KEY_CLIENTE_VEHICULO_TAR);
        if ( hasCliente ){
            FillScreenFinalData();
            viewFlipper.setDisplayedChild(5);
            if (ValidacionCarga()) {
                btn_print.setVisibility(View.VISIBLE);
            }else {
                btn_print.setVisibility(GONE);
            }
        }
        else if(hasMetodo && !hasCliente){
            switch (Validar.getJSONObject(spn_posicion.getSelectedItemPosition()).getString(variables.METODO)){
                case "Rfid":
                    btn_print.setVisibility(GONE);
                    viewFlipper.setDisplayedChild(1);
                    break;
                case "Nip":
                    btn_print.setVisibility(GONE);
                    viewFlipper.setDisplayedChild(2);
                    break;
                case "Nombre":
                    btn_print.setVisibility(GONE);
                    if (Validar.getJSONObject(spn_posicion.getSelectedItemPosition()).has(variables.KEY_CLIENTE)){
                        if (!Validar.getJSONObject(spn_posicion.getSelectedItemPosition()).has(variables.KEY_CLIENTE_VEHICULO_TAR)) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("rfc",GetData(variables.KEY_RFC));
                            jsonObject.put("cliente",GetData(variables.KEY_CLIENTE));
                            jsonObject.put("codcli",GetData(variables.KEY_CODCLI));
                            AdapterClickCustomerCG(jsonObject, false);
                        }
                        viewFlipper.setDisplayedChild(4);
                    }else {
                        viewFlipper.setDisplayedChild(3);
                    }
                    break;
                case "Vale":
                    if ( HasData(variables.VALESLISTS)){
                        List<ValesList> ValesLists = (List<ValesList>) Validar.getJSONObject(
                                spn_posicion.getSelectedItemPosition()).get(variables.VALESLISTS);
                        Log.w("data",ValesLists.get(0).getFolio());
                        valeAdapterRV = new ValeAdapterRV(ValesLists, getApplicationContext(), Credito.this);
                        rv_vales.setAdapter(valeAdapterRV);
                        valeAdapterRV.notifyDataSetChanged();
                        if (!HasData(variables.VALE_WS_CONSUMO)) {
                            total_vale.setText("0.0");
                            cliente_vale.setText(getResources().getString(R.string.label_cliente));
                        }
                        if (HasData(variables.VALE_NOMINATIVA)){
                            if (GetData(variables.VALE_NOMINATIVA).equals("1")){
                                ws_vale.setText(getResources().getString(R.string.title_ticket));
                            }else{
                                ws_vale.setText(getResources().getString(R.string.ws_vale));
                            }
                        }else{
                            ws_vale.setText(getResources().getString(R.string.ws_vale));
                        }
                    }else{
                        total_vale.setText("0.0");
                        cliente_vale.setText(getResources().getString(R.string.label_cliente));
                        rv_vales.removeAllViewsInLayout();
                        ws_vale.setText(getResources().getString(R.string.ws_vale));
                    }
                    btn_print.setVisibility(GONE);
                    viewFlipper.setDisplayedChild(6);
                    break;
            }
        }else if(!hasMetodo) {
            btn_print.setVisibility(GONE);
            viewFlipper.setDisplayedChild(0);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.CardViewRFID:
                try {
                    PutData(variables.METODO, variables.KEY_RFID);
                    CoreScreen();
                } catch (JSONException | ClassNotFoundException | SQLException |
                        InstantiationException | IllegalAccessException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
                Toast.makeText(this,"RFID",Toast.LENGTH_LONG).show();
                break;
            case R.id.CardViewNIP:
                try {
                    PutData(variables.METODO, variables.KEY_NIP);
                    CoreScreen();
                } catch (JSONException | ClassNotFoundException | SQLException |
                        InstantiationException | IllegalAccessException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
                Toast.makeText(this,"NIP",Toast.LENGTH_LONG).show();
                break;
            case R.id.CardViewNOMBRE:
                try {
                    PutData(variables.METODO, variables.KEY_NOMBRE);
                    CoreScreen();
                } catch (JSONException | ClassNotFoundException | SQLException |
                        InstantiationException | IllegalAccessException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
                Toast.makeText(this,"NOMBRE",Toast.LENGTH_LONG).show();
                break;
            case R.id.CardViewVALE:
                try {
                    PutData(variables.METODO,variables.KEY_VALE);
                    CoreScreen();
                }catch (JSONException | ClassNotFoundException | SQLException |
                        InstantiationException | IllegalAccessException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
                Toast.makeText(this,"VALE",Toast.LENGTH_LONG).show();
                break;
            case R.id.scan:
                try {
                    if (!HasData(variables.KEY_ULT_NROTRN) ){
                        PutLastNROTRN();
                    }
                    if (HasData(variables.VALE_WS_CONSUMO)) {
                        if (GetData(variables.VALE_WS_CONSUMO).equals("0") ) {
                            scan_vale();
                        }else{
                            new AlertDialog.Builder(Credito.this)
                                    .setTitle(R.string.error)
                                    .setMessage(ERROR_VALE_CONSUMIDO)
                                    .setPositiveButton(R.string.btn_ok, null).show();
                        }
                    }else{
                        scan_vale();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ws_vale:
                try {
                    if (!HasData(variables.VALE_WS_CONSUMO)){
                        /*no se ha escaneado ningun vale, mostrar error para esto*/
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setMessage(ERROR_NO_VALE)
                                .setPositiveButton(R.string.btn_ok, null).show();
                    }else{
                         /*flag_vale si tiene valor 0 indica que aun no se consumen los vales
                        si el valor es 1 tenemos que validar que el servicio haya terminado para poder
                        marcar el servicio con el codcli default de vales, ejecutar el cfdi nominativo,
                        llamar el cfdi credito y ejecutar la impresion.*/
                        if (GetData(variables.VALE_WS_CONSUMO).equals("0")) {
                            WsVale();
                        }else if(GetData(variables.VALE_NOMINATIVA).equals("1")){
                            PutTicketDataVale();
                            /*if (Integer.valueOf(GetData(variables.KEY_ULT_NROTRN)) < Integer.valueOf(GetTicketData(variables.KEY_TICKET_NROTRN))) {
                                WsValeNominativa();
                            }else{
                                new AlertDialog.Builder(Credito.this)
                                        .setTitle(R.string.error)
                                        .setIcon(icon)
                                        .setMessage(R.string.EsperaServicio)
                                        .setPositiveButton(R.string.btn_ok,null).show();
                            }*/
                        }else{
                            new AlertDialog.Builder(Credito.this)
                                    .setTitle(R.string.error)
                                    .setMessage(ERROR_VALE_NOMINATIVA)
                                    .setPositiveButton(R.string.btn_ok, null).show();
                        }
                    }
                } catch (JSONException e) {
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
                break;
        }
    }
    public void PutLastNROTRN(){
        new GetLastNROTRN(this, getApplicationContext(), new ControlGasListener() {
            @Override
            public void processFinish(JSONObject output) {
                try {
                    if (output.getInt(variables.CODE_ERROR) == 0) {
                        PutData(variables.KEY_ULT_NROTRN, output.getString(variables.KEY_ULT_NROTRN));
                    } else {
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(), getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + output.getString(variables.MESSAGE_ERROR));
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setMessage(output.getString(variables.MESSAGE_ERROR))
                                .setPositiveButton(R.string.btn_ok, null).show();
                    }
                } catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(), getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }

            }
        }).execute(spn_posicion.getSelectedItem().toString());
    }
    public void WsValeNominativa(){
        try {
            JSONObject WsData = new JSONObject(GetData(variables.VALE_WS_RESPONSE));
            WsData.put("despachador",GetData("despachador"));
            WsData.put("nip_despachador",GetData("nip_despachador"));
            //PutTicketDataVale(WsData);
            PutTicketDataValeFinish(WsData);

        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
    }
    @Override
    public void NominativaFinish(JSONObject jsonObject) {

        Log.w("NominativaFinish", String.valueOf(jsonObject));
        try {
            if(jsonObject.getInt(variables.CODE_ERROR)==0){
                final JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
                int index = spn_posicion.getSelectedItemPosition();
                final JSONObject dato_cliente = new JSONObject((String) Validar.getJSONObject(index).get("ValeWSResponse"));
                final JSONObject js = new JSONObject((String) jsonObject.get("Result"));
                Log.w("NominativaFinish2", String.valueOf(dato_cliente.get("cliente")));
                new UpdateCFDi(Credito.this, getApplicationContext(), new ControlGasListener() {
                    @Override
                    public void processFinish(JSONObject output) {
                        try {
                            if (output.getInt(Variables.CODE_ERROR) == 0) {
                                new NotaCredito(Credito.this, getApplicationContext(),new NotaCreditoListener(){
                                    @Override
                                    public void NotaCreditoFinish(JSONObject jsonObject) {
                                        Log.w("NotaCredito", String.valueOf(jsonObject));
                                    }
                                }).execute(js.getString("id_cliente"),js.getString("id_estacion"),
                                        js.getString("satuid"), js.getString("satrfc"),
                                        String.valueOf(dato_cliente.get("cliente")),GetTicketData(Variables.KEY_TICKET_TOTAL));
                            }else{
                                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                        stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                                new AlertDialog.Builder(Credito.this)
                                        .setTitle(R.string.error)
                                        .setIcon(icon)
                                        .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                        .setPositiveButton(R.string.btn_ok, null).show();
                            }
                        }catch (JSONException e) {
                            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                    stacktraceObj[2].getMethodName() + "|" + e);
                            new AlertDialog.Builder(Credito.this)
                                    .setTitle(R.string.error)
                                    .setIcon(icon)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok, null).show();
                            e.printStackTrace();

                        }
                    }
                }).execute(js.getString("id_factura"),js.getString("satuid"),js.getString("satrfc"),GetTicketData(Variables.KEY_TICKET_NROTRN));
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + jsonObject.getString(variables.MESSAGE_ERROR));
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(jsonObject.getString(variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

       /* try {
            JSONObject js = new JSONObject(String.valueOf(js1));
            Log.w("Json_valida_cfdi" , String.valueOf(js.get("id_estacion")));
            new NotaCredito(this, getApplicationContext(),new NotaCreditoListener(){
                @Override
                public void NotaCreditoFinish(JSONObject jsonObject) {
                    Log.w("NotaCredito", String.valueOf(jsonObject));
                }
            }).execute(js.getString("id_cliente"),js.getString("id_estacion"),
                    js.getString("uuid_origen"), js.getString("satrfc"),
                    js.getString("cliente"),js.getString("importe"));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
       //impresion
    }
    private void PutTicketDataValeFinish(JSONObject WsData){
        try {
            WsData.put("nrotrn", GetTicketData(variables.KEY_TICKET_NROTRN));
            WsData.put("id_producto",GetTicketData(variables.KEY_TICKET_ID_PRODUCTO));
            WsData.put("nrocte",GetTicketData(variables.KEY_TICKET_NROCTE));
            GetCustomerDataVale(WsData);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void GetCustomerDataVale(JSONObject WsData1){
        final JSONObject WsData = WsData1;
        new GetValesCodcli(this, new ControlGasListener() {
            @Override
            public void processFinish(JSONObject output) {
                try {
                    if (output.getInt(variables.CODE_ERROR) == 0) {
                        new GetCustomerVale(Credito.this, getApplicationContext(), new ControlGasListener() {
                            @Override
                            public void processFinish(JSONObject output) {
                                try {
                                    if(output.getInt(variables.CODE_ERROR)==0){
                                        JSONObject jsonObject= new JSONObject();
                                        dataCustomerCG = (List<DataCustomerCG>) output.get(variables.GET_CUSTOMER_RESULT);
                                        if ( dataCustomerCG.size()>0) {
                                            try {
                                                jsonObject.put("codcli", dataCustomerCG.get(0).codcli);
                                                jsonObject.put("chofer", dataCustomerCG.get(0).rsp);
                                                jsonObject.put("placa", dataCustomerCG.get(0).plc);
                                                jsonObject.put("vehiculo", dataCustomerCG.get(0).den_vehicle);
                                                jsonObject.put("tar", dataCustomerCG.get(0).tar);
                                                jsonObject.put("nroveh", dataCustomerCG.get(0).nroveh);
                                                jsonObject.put("cliente", dataCustomerCG.get(0).den);
                                                jsonObject.put("rfc", dataCustomerCG.get(0).rfc);
                                                jsonObject.put("nroeco", dataCustomerCG.get(0).nroeco);
                                                jsonObject.put("tagadi", "");
                                                jsonObject.put("tipval",dataCustomerCG.get(0).tipval);
                                                FillCustomerData(jsonObject);
                                                FillCustomerVehicleData(jsonObject);
                                                CoreScreen();
                                                CloseKeyboard();
                                            } catch (JSONException | ClassNotFoundException | SQLException |
                                                    InstantiationException | IllegalAccessException e) {
                                                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                                        stacktraceObj[2].getMethodName() + "|" + e);
                                                new AlertDialog.Builder(Credito.this)
                                                        .setTitle(R.string.error)
                                                        .setIcon(icon)
                                                        .setMessage(String.valueOf(e))
                                                        .setPositiveButton(R.string.btn_ok, null).show();
                                                e.printStackTrace();
                                            }
                                            JSONArray array = new JSONArray();
                                            array.put(WsData.toString());
                                            new UpdateCodcli(Credito.this, getApplicationContext(), new ControlGasListener() {
                                                @Override
                                                public void processFinish(JSONObject output){
                                                    try {
                                                        if (output.getInt(Variables.CODE_ERROR)==1){
                                                            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                                            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                                                    stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                                                            new AlertDialog.Builder(Credito.this)
                                                                    .setTitle(R.string.error)
                                                                    .setIcon(icon)
                                                                    .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                                                    .setPositiveButton(R.string.btn_ok, null).show();
                                                        }
                                                    } catch (JSONException e) {
                                                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                                                stacktraceObj[2].getMethodName() + "|" + e);
                                                        new AlertDialog.Builder(Credito.this)
                                                                .setTitle(R.string.error)
                                                                .setIcon(icon)
                                                                .setMessage(String.valueOf(e))
                                                                .setPositiveButton(R.string.btn_ok, null).show();
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).execute(
                                                    GetData(variables.KEY_CODCLI),
                                                    GetData(variables.KEY_CLIENTE_VEHICULO_NROVEH),
                                                    GetDataODM(),
                                                    GetData(variables.KEY_CLIENTE_VEHICULO_TAR),
                                                    GetTicketData(variables.KEY_TICKET_NROTRN));
                                            Nominativa nominativa = new Nominativa(Credito.this, getApplicationContext(), WsData);
                                            nominativa.delegate=Credito.this;
                                            nominativa.execute();
                                        }
                                    }else{
                                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                                stacktraceObj[2].getMethodName() + "|" + output.getString(variables.MESSAGE_ERROR));
                                        new AlertDialog.Builder(Credito.this)
                                                .setTitle(R.string.error)
                                                .setIcon(icon)
                                                .setMessage(output.getString(variables.MESSAGE_ERROR))
                                                .setPositiveButton(R.string.btn_ok, null).show();
                                    }
                                } catch (JSONException e) {
                                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                            stacktraceObj[2].getMethodName() + "|" + e);
                                    new AlertDialog.Builder(Credito.this)
                                            .setTitle(R.string.error)
                                            .setIcon(icon)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok, null).show();
                                    e.printStackTrace();
                                }
                            }}).execute(output.getString(variables.KEY_CODCLI));
                    }else{
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + output.getString(variables.MESSAGE_ERROR));
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setIcon(icon)
                                .setMessage(output.getString(variables.MESSAGE_ERROR))
                                .setPositiveButton(R.string.btn_ok, null).show();
                    }
                }catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
            }
        }).execute();
    }
    private void PutTicketDataVale(){
        new GetTicket(this, new ControlGasListener() {
            @Override
            public void processFinish(JSONObject output)  {
                try {
                    if (output.getInt(Variables.CODE_ERROR)==0){
                        final JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
                        int index = spn_posicion.getSelectedItemPosition();
                        Validar.getJSONObject(index).put(Variables.KEY_TICKET,output);
                        /*WsValeNominativa();*/
                        if (Integer.valueOf(GetData(variables.KEY_ULT_NROTRN)) < Integer.valueOf(GetTicketData(variables.KEY_TICKET_NROTRN))) {
                            WsValeNominativa();
                        }else{
                            new AlertDialog.Builder(Credito.this)
                                    .setTitle(R.string.error)
                                    .setIcon(icon)
                                    .setMessage(R.string.EsperaServicio)
                                    .setPositiveButton(R.string.btn_ok,null).show();
                        }
                    }else{
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                .setPositiveButton(R.string.btn_ok, null).show();
                    }
                } catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
            }
        }).execute(spn_posicion.getSelectedItem().toString(),mac.getMacAddress(),"1");
    }
    public void WsVale(){
        GetDespachador getDespachador = new GetDespachador(this, getApplicationContext());
        getDespachador.delegate=this;
        getDespachador.execute();
    }
    @Override
    public void processFinish(JSONObject output) {
        final JSONObject JsVales = new JSONObject();
        ArrayList<String> cadena_vales = new ArrayList<String>();
        JSONArray array_vales = new JSONArray();
        try {
            JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
            for (ValesList vale : (List<ValesList>) Validar.getJSONObject(
                    spn_posicion.getSelectedItemPosition()).get(variables.VALESLISTS)){
                cadena_vales.add(String.valueOf(vale.getFolio()));
                array_vales.put(vale.getFolio());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JsVales.put("cveest",datos_domicilio.getString("cveest"));
            JsVales.put("id_despachador", output.getString(variables.NIP_DESPACHADOR));
            JsVales.put("despachador", output.getString(variables.KEY_TICKET_DESPACHADOR));
            JsVales.put("codvales", array_vales);
            PutData(variables.VALES_LIST, String.valueOf(array_vales));
            PutData("despachador",output.getString(variables.KEY_TICKET_DESPACHADOR));
            PutData("nip_despachador",output.getString(variables.NIP_DESPACHADOR));
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
        ValesWS valesWS = new ValesWS(this, getApplicationContext(),JsVales);
        valesWS.delegate=this;
        valesWS.execute();

    }
    @Override
    public void ValesWSFinish(JSONObject result) {
        try {
            if ( result.getInt(variables.CODE_ERROR) == 0){
                List<ValesList> ValesLists = new ArrayList<ValesList>();
                JSONObject jsonObject = new JSONObject(String.valueOf(result.get(variables.RESULT)));
                JSONArray array_vales = new JSONArray();
                String[] res_vales = new ArrayList<String>().toArray(new String[0]);
                Log.w("res", String.valueOf(jsonObject));
                Log.w("vales" , String.valueOf(jsonObject.get("cveest")));
                res_vales=jsonObject.get("vales").toString().split(",");
                for (String res_vale : res_vales){
                    array_vales.put( json.strtojson(res_vale,"|"));
                }
                jsonObject.put("vales",array_vales);
                ValesList valesList = null;

                Double total_vale_data=0.0;
                for ( int i = 0; i < jsonObject.getJSONArray("vales").length(); i++){
                    JSONObject valetemp  = (JSONObject) jsonObject.getJSONArray("vales").get(i);
                    if (valetemp.getString("0").equals("1") && valetemp.getString("6").equals("0")){
                        total_vale_data += Double.parseDouble(valetemp.getString("3"));
                        PutData(variables.VALE_NOMINATIVA,"1");
                        valesList = new ValesList(
                                R.drawable.ic_update_content,
                                R.drawable.donevector,
                                valetemp.getString("2"),
                                valetemp.getString("7"),
                                Double.parseDouble(valetemp.getString("3")),
                                Integer.parseInt(valetemp.getString("0")),
                                Integer.parseInt(valetemp.getString("6"))
                        );
                    }else{
                        valesList = new ValesList(
                                R.drawable.ic_update_content,
                                android.R.drawable.ic_menu_close_clear_cancel,
                                valetemp.getString("2"),
                                valetemp.getString("7"),
                                0.00,
                                0,1
                        );
                    }

                    ValesLists.add(valesList);
                }
                //DATO A BORRAR, SOLO PARA CONTINUAR FLUJO
                //PutData(variables.FLAG_VALE,"1");
                PutData(variables.VALE_WS_CONSUMO,"1");
                PutData(variables.TOTAL_VALE_CONSUMO, String.valueOf(total_vale_data));
                total_vale.setText(String.valueOf(total_vale_data));
                cliente_vale.setText(jsonObject.getString("cliente"));
                valeAdapterRV = new ValeAdapterRV(ValesLists, getApplicationContext(), Credito.this);
                rv_vales.setAdapter(valeAdapterRV);
                valeAdapterRV.notifyDataSetChanged();
                PutList(variables.VALESLISTS, ValesLists);
                PutData(variables.VALE_WS_RESPONSE, String.valueOf(jsonObject));
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(), getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + result.getString(variables.MESSAGE_ERROR));
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setMessage(result.getString(variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
            CoreScreen();
        } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }
    public void scan_vale(){
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.setPrompt(SCAN_PROMPT);
        if (IsTablet) {
            scanIntegrator.addExtra("SCAN_CAMERA_ID", 1);
            scanIntegrator.setOrientationLocked(true);
        }else{
            scanIntegrator.addExtra("SCAN_CAMERA_ID", 0);
            scanIntegrator.setCaptureActivity(ScanActivityPortrait.class);
            scanIntegrator.setOrientationLocked(false);
        }
        scanIntegrator.setBeepEnabled(true);
        scanIntegrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null && resultCode==RESULT_OK) {

            String scanContent = scanningResult.getContents();
            //add data to reciclerview
            boolean flag_add_vale = true;
            JSONArray Validar = null;
            List<ValesList> ValesLists =new ArrayList<ValesList>();
            try {
                Validar = Posiciones.getJSONArray(variables.POSICIONES);
                if (HasData(variables.VALESLISTS)) {
                    ValesLists = (List<ValesList>) Validar.getJSONObject(
                            spn_posicion.getSelectedItemPosition()).get(variables.VALESLISTS);
                }
                for (ValesList vale : ValesLists){
                    if (vale.getFolio().equals(scanContent)){
                        flag_add_vale = false;
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setIcon(icon)
                                .setMessage("Vale duplicado.")
                                .setPositiveButton(R.string.btn_ok,null).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (flag_add_vale) {
                ValesList valesList = new ValesList(
                        R.drawable.ic_update_content,
                        android.R.drawable.ic_menu_close_clear_cancel,
                        scanContent,
                        VALE_MSJ_VALIDATE,
                        0.00,
                        0,1
                );

                ValesLists.add(valesList);
                try {
                    PutList(variables.VALESLISTS, ValesLists);
                    PutData(variables.VALE_WS_CONSUMO, String.valueOf(0));
                    PutData(variables.VALE_NOMINATIVA, String.valueOf(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                valeAdapterRV = new ValeAdapterRV(ValesLists, getApplicationContext(), Credito.this);
                rv_vales.setAdapter(valeAdapterRV);
                valeAdapterRV.notifyDataSetChanged();
            }
        }
    }

    public void SearchCustomerNip(final View v){
        if (et_clientecg.getText().length() == 0){
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage("El dato de busqueda no puede ser nulo.")
                    .setPositiveButton(R.string.btn_ok,null).show();
        }else {
            new GetLastNROTRN(this, getApplicationContext(), new ControlGasListener() {
                @Override
                public void processFinish(JSONObject output) {
                    try {
                        if (output.getInt(variables.CODE_ERROR) == 0) {
                            PutData(variables.KEY_ULT_NROTRN, output.getString(variables.KEY_ULT_NROTRN));
                        } else {
                            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                            logCE.EscirbirLog2(getApplicationContext(), getLocalClassName() + "|" +
                                    stacktraceObj[2].getMethodName() + "|" + output.getString(variables.MESSAGE_ERROR));
                            new AlertDialog.Builder(Credito.this)
                                    .setTitle(R.string.error)
                                    .setMessage(output.getString(variables.MESSAGE_ERROR))
                                    .setPositiveButton(R.string.btn_ok, null).show();
                        }
                    } catch (JSONException e) {
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(), getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + e);
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok, null).show();
                        e.printStackTrace();
                    }

                }
            }).execute(spn_posicion.getSelectedItem().toString());

            try {
                PutData(variables.KEY_TAG, et_clientecg.getText().toString());
                GetCustomerNip getCustomerNip = new GetCustomerNip(this);
                getCustomerNip.delegate = this;
                getCustomerNip.execute(GetData(variables.KEY_TAG));
            } catch (JSONException e) {
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(), getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + e);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok, null).show();
                e.printStackTrace();
            }
        }
    }
    public void SearchCustomerName(View v){
        if (etSearchCustomer.getText().length()<MIN_SEARCH){
            String error = "El mínimo de caracteres para buscar es (" + String.valueOf(MIN_SEARCH)
                    + "), la búsqueda actual es de "+ etSearchCustomer.getText().length()
                    + " carácter(es), favor de corregir el parámetro de búsqueda.";
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(error)
                    .setPositiveButton(R.string.btn_ok,null).show();
        }else {
            GetCustomerName getCustomerName = new GetCustomerName(this);
            getCustomerName.delegate=this;
            getCustomerName.execute(etSearchCustomer.getText().toString());
        }
        CloseKeyboard();
    }
    public void AdapterClickCustomerCG(JSONObject js, Boolean First)  {
        try {
            if(First){
            FillCustomerData(js);}
            tvvehiculo_cliente.setText(GetData(variables.KEY_CLIENTE));
            tvvehiculo_rfc.setText(GetData(variables.KEY_RFC));
            tvvehiculo_codcli.setText(GetData(variables.KEY_CODCLI));

            GetCustomerVehicle getCustomerVehicle = new GetCustomerVehicle(this);
            getCustomerVehicle.delegate=this;
            getCustomerVehicle.execute(js.getString("codcli"), String.valueOf(First));


        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
    }
    public void AdapterClickCustomerVehicleCG(JSONObject js){
        try {
            FillCustomerVehicleData(js);
            if ( HasTagadi() ){
                PutData(variables.KEY_ULT_NROTRN, validacionFlotillero.validar_utlimo_nrotrn(this,
                        spn_posicion.getSelectedItem().toString()));
                final EditText nipCustomer = new EditText(this);
                    nipCustomer.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    nipCustomer.setLongClickable(false);
                    nipCustomer.setFocusableInTouchMode(false);
                    nipCustomer.setFocusable(false);
                    nipCustomer.setFocusableInTouchMode(true);
                    nipCustomer.setFocusable(true);
                    nipCustomer.setTransformationMethod(PasswordTransformationMethod.getInstance());
                new AlertDialog.Builder(Credito.this)
                        .setIcon(icon)
                        .setTitle(marca)
                        .setView(nipCustomer)
                        .setMessage(DialogMsgNip())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    if (nipCustomer.getText().toString().equals
                                            (GetData(variables.KEY_CLIENTE_VEHICULO_TAGADI))){
                                        ValidacionCarga();
                                        FillScreenFinalData();
                                        viewFlipper.setDisplayedChild(5);
                                        btn_print.setVisibility(View.VISIBLE);
                                    }else{
                                        String e = "Nip erroneo favor de validar el dato.";
                                        new AlertDialog.Builder(Credito.this)
                                                .setTitle(R.string.error)
                                                .setMessage(e)
                                                .setPositiveButton(R.string.btn_ok,null).show();
                                    }
                                } catch (JSONException e) {
                                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                            stacktraceObj[2].getMethodName() + "|" + e);
                                    new AlertDialog.Builder(Credito.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok, null).show();
                                    e.printStackTrace();
                                }
                            }
                        }).show();

            }else {
                PutData(variables.KEY_ULT_NROTRN, validacionFlotillero.validar_utlimo_nrotrn(this,
                        spn_posicion.getSelectedItem().toString()));
                ValidacionCarga();
                FillScreenFinalData();
                viewFlipper.setDisplayedChild(5);
                btn_print.setVisibility(View.VISIBLE);
            }
        } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
    }
    private Boolean HasTagadi() throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        if (Validar.getJSONObject(index).getString(variables.KEY_CLIENTE_VEHICULO_TAGADI).equals("0")){
            return false;
        }
        return true;
    }
    /*Funcion para el llenado del xml activity_credito_impresion*/
    private void FillScreenFinalData() throws JSONException {
        /*ValidacionCarga();*/
        tv_cliente.setText(GetData(variables.KEY_CLIENTE));
        tv_rfc.setText(GetData(variables.KEY_RFC));
        tv_codcli.setText(GetData(variables.KEY_CODCLI));
        tv_placa.setText(GetData(Variables.KEY_CLIENTE_VEHICULO_PLACA));
        tv_vehiculo.setText(GetData(variables.KEY_CLIENTE_VEHICULO_DEN));
        tv_chofer.setText(GetData(variables.KEY_CLIENTE_VEHICULO_CHOFER));
        if (HasData(variables.KEY_ODM)) {
            et_odm.setText(GetData(variables.KEY_ODM));
        } else{
            et_odm.setText("");
        }
    }
    private String DialogMsgNip() throws JSONException {
        String res = "Favor de introducir el nip para el vehiculo con placa : "
                + GetData(variables.KEY_CLIENTE_VEHICULO_PLACA) + ", del cliente " + GetData(variables.KEY_CLIENTE);
        return res;
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
    /*Funcion para llenar Json Posiciones a partir de la data obtenida del cliente,
    ya sea por NFC, NIP o Nombre*/
    private void FillCustomerData(JSONObject data) throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE,data.getString("cliente"));
        Validar.getJSONObject(index).put(variables.KEY_RFC,data.getString("rfc"));
        Validar.getJSONObject(index).put(variables.KEY_CODCLI,data.getString("codcli"));
        Validar.getJSONObject(index).put(variables.KEY_TICKET_CLIENTE_TIPVAL_DEN,data.getString("tipval"));
    }
    private void FillCustomerVehicleData(JSONObject data) throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        Log.w("Data", String.valueOf(data));
        int index = spn_posicion.getSelectedItemPosition();
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_TAR,data.getString("tar"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_DEN,data.getString("vehiculo"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_NROVEH,data.getString("nroveh"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_TAGADI, data.getString("tagadi"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_PLACA, ValidateOptionalData(data,"placa"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_CHOFER, ValidateOptionalData(data,"chofer"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_NROECO,ValidateOptionalData(data,"nroeco"));
    }
    /*Funcion para validar datos opcionales en el json*/
    private String ValidateOptionalData(JSONObject data, String key) throws JSONException {
        if (data.has(key)){
            return data.getString(key);
        }else{
            String res = "Sin dato de " + key;
            return res;
        }
    }

    private void PutTicketData(){
        new GetTicket(this, new ControlGasListener() {
            @Override
            public void processFinish(JSONObject output)  {
                try {
                    if (output.getInt(Variables.CODE_ERROR)==0){
                        final JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
                        int index = spn_posicion.getSelectedItemPosition();
                        Validar.getJSONObject(index).put(Variables.KEY_TICKET,output);
                    }else{
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                .setPositiveButton(R.string.btn_ok, null).show();
                    }
                } catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
            }
        }).execute(spn_posicion.getSelectedItem().toString(),mac.getMacAddress(),"1");
    }
    private void UpdateTicketData(String key, String data) throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        Validar.getJSONObject(index).getJSONObject(variables.KEY_TICKET).put(key,data);
    }
    private String GetTicketData(String key) throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        return String.valueOf(Validar.getJSONObject(index).getJSONObject(variables.KEY_TICKET).getString(key));
    }
    private String GetDataODM() throws JSONException {
        String res = "0";
        if (HasData(variables.KEY_ODM) && GetData(variables.KEY_ODM) != ""){
            res = GetData(variables.KEY_ODM);
        }
        return res;
    }
    private String CalculateMetoPago(String data){
        String res = "Cliente Contado";
        switch (data){
            case "3":
                res = "Cliente Credito";
                break;
            case "4":
                res = "Cliente Debito";
                break;
            case "0":
                res = "Cliente Contado";
                break;
        }
        return res;
    }
    private String CalculateVenta(String data){
        String res = "Contado";
        switch (data){
            case "3":
                res = "Credito";
                break;
            case "4":
                res = "Debito";
                break;
            case "0":
                res = "Contado";
                break;
        }
        return res;
    }
    public void MethodCleanData(View view){
        new AlertDialog.Builder(Credito.this)
                .setTitle(R.string.error)
                .setIcon(icon)
                .setMessage(MESSAGE_CLEAN_DATA)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    flag_clean_data=false;
                                    CloseKeyboard();
                                    CleanData();

                                    flag_clean_data=true;
                                } catch (JSONException | ClassNotFoundException | SQLException |
                                        InstantiationException | IllegalAccessException e) {
                                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                            stacktraceObj[2].getMethodName() + "|" + e);
                                    new AlertDialog.Builder(Credito.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok, null).show();
                                    e.printStackTrace();
                                }
                            }
                        }
                )
                .setNegativeButton("Cancelar", null).show();
    }
    private void CleanData() throws JSONException, ClassNotFoundException, SQLException,
            InstantiationException, IllegalAccessException {
        ArrayList<String> Elementos = new ArrayList<String>();
        Elementos.add(variables.METODO);
        Elementos.add(variables.KEY_TAG);
        Elementos.add(variables.KEY_RFID);
        Elementos.add(variables.KEY_NIP);
        Elementos.add(variables.KEY_NOMBRE);
        Elementos.add(variables.KEY_CLIENTE);
        Elementos.add(variables.KEY_RFC);
        Elementos.add(variables.KEY_CODCLI);
        Elementos.add(variables.KEY_CLIENTE_VEHICULO_DEN);
        Elementos.add(variables.KEY_CLIENTE_VEHICULO_TAR);
        Elementos.add(variables.KEY_CLIENTE_VEHICULO_NROVEH);
        Elementos.add(variables.KEY_CLIENTE_VEHICULO_TAGADI);
        Elementos.add(Variables.KEY_CLIENTE_VEHICULO_PLACA);
        Elementos.add(variables.KEY_CLIENTE_VEHICULO_CHOFER);
        Elementos.add(variables.KEY_ULT_NROTRN);
        Elementos.add(variables.KEY_TICKET);
        Elementos.add(variables.KEY_TICKET_NROTRN);
        Elementos.add(variables.KEY_TICKET_CANTIDAD);
        Elementos.add(variables.KEY_TICKET_PRECIO);
        Elementos.add(variables.KEY_TICKET_TOTAL);
        Elementos.add(variables.KEY_TICKET_PRODUCTO);
        Elementos.add(variables.KEY_TICKET_BOMBA);
        Elementos.add(variables.KEY_TICKET_DESPACHADOR);
        Elementos.add(variables.KEY_TICKET_FECHA);
        Elementos.add(variables.KEY_TICKET_ID_PRODUCTO);
        Elementos.add(variables.KEY_TICKET_CVEEST);
        Elementos.add(variables.KEY_TICKET_CODCLI);
        Elementos.add(variables.KEY_TICKET_DENCLI);
        Elementos.add(variables.KEY_TICKET_HORA);
        Elementos.add(variables.KEY_TICKET_CODGAS);
        Elementos.add(variables.KEY_TICKET_CODPRD);
        Elementos.add(variables.KEY_TICKET_NROVEH);
        Elementos.add(variables.KEY_ODM);
        Elementos.add(variables.KEY_TICKET_FCHCOR);
        Elementos.add(variables.KEY_TICKET_NROTUR);
        Elementos.add(variables.KEY_TICKET_NROCTE);
        Elementos.add(variables.KEY_TICKET_IVA);
        Elementos.add(variables.KEY_TICKET_IEPS);
        Elementos.add(variables.KEY_TICKET_CLIENTE_TIPVAL);
        Elementos.add(variables.KEY_IMPRESO);
        Elementos.add(variables.VALESLISTS);
        Elementos.add(variables.VALE_WS_CONSUMO);
        Elementos.add(variables.VALE_NOMINATIVA);
        Elementos.add(variables.VALE_WS_RESPONSE);
        Log.w(variables.POSICIONES, String.valueOf(Posiciones.getJSONArray(variables.POSICIONES)));
        for (int i = 0; i<Elementos.size() ; i++){
            if (HasData(Elementos.get(i))) {
                Posiciones.getJSONArray(variables.POSICIONES).getJSONObject(spn_posicion.getSelectedItemPosition()).remove(Elementos.get(i));
            }
        }
        if ( flag_clean_data ) {
            spn_posicion.setSelection(0);
        }
        CoreScreen();
    }
    /*Funcion para acutalizar el diccionario en la posicion actual del jsonobject con la clave y
    data pasada a la funcion, la clave se crea como final string*/
    private void PutData(String key, String data) throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        Validar.getJSONObject(index).put(key,data);
    }
    private void PutList(String key, List<ValesList> data) throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        Validar.getJSONObject(index).put(key,data);
        Log.w("putlist", String.valueOf(index));
        Log.w("putlist", String.valueOf(data));
        Log.w("putlist", String.valueOf(Validar));
    }
    private String GetData(String key) throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        return String.valueOf(Validar.getJSONObject(index).getString(key));
    }
    private Boolean HasData(String key) throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        return Validar.getJSONObject(index).has(key);
    }
    private void updateButtonState(final boolean state) {
        state_btn = state;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn_print.setEnabled(state_btn);
            }
        });
    }
    private boolean runPrintReceiptSequence() throws SQLException, WriterException,
            InstantiationException, JSONException, ClassNotFoundException, IllegalAccessException,
            Epos2Exception {
        if (!createReceiptData()) {
            flag = 1;
            return false;
        }
        if (!printData()) {
            flag = 1;
            return false;
        }
        return true;
    }
    private boolean initializeObject() {
        try {
            mPrinter = new Printer(mPrinter.TM_M30, mPrinter.MODEL_ANK, mContext);
        } catch (Exception e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }
        mPrinter.setReceiveEventListener(this);
        return true;
    }
    public boolean createReceiptData() throws Epos2Exception, ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException, WriterException {
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.logo_impresion);
        switch (marca) {
            case "Combu-Express":
                logoData = BitmapFactory.decodeResource(getResources(), R.drawable.logo_impresion);
                break;
            case "Repsol":
                logoData = BitmapFactory.decodeResource(getResources(), R.drawable.logo_impresion_repsol);
                break;
            case "Ener":
                logoData = BitmapFactory.decodeResource(getResources(), R.drawable.logo_impresion_ener);
                break;
            case "Total":
                logoData = BitmapFactory.decodeResource(getResources(), R.drawable.logo_impresion_total);
                break;
        }
        final JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        JSONObject dato_cliente = new JSONObject((String) Validar.getJSONObject(index).get("ValeWSResponse"));
        StringBuilder textData = new StringBuilder();
        Numero_a_Letra letra = new Numero_a_Letra();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;
        Point point = new Point();
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? 380 : 380;
        if (mPrinter == null) {
            return false;
        }

        JSONObject vehiculo = new JSONObject();
        String titulo = "", folio_impreso = "", cliente = "", venta = "", tpv = "";
        String metodoPago = "";
        System.out.println("validar metodopago" + Posiciones);
        if (Integer.parseInt(GetData(variables.KEY_IMPRESO)) == 0 || Integer.parseInt(GetData(variables.KEY_IMPRESO)) == 10) {
            titulo = "O R I G I N A L";
            metodoPago = GetData(Variables.KEY_TICKET_CLIENTE_TIPVAL_DEN);
            folio_impreso = GetTicketData(variables.KEY_TICKET_NROTRN) + "0";

        } else if (Integer.parseInt(GetData(variables.KEY_IMPRESO)) == 1) {
            titulo = "C O P I A";
            folio_impreso = "C O P I A";
            metodoPago = GetTicketData(Variables.KEY_TICKET_CLIENTE_TIPVAL_DEN);
        }

        method = "addTextAlign";
        mPrinter.addTextAlign(Printer.ALIGN_CENTER);
        method = "addImage";
        mPrinter.addImage(logoData, 0, 0,
                logoData.getWidth(),
                logoData.getHeight(),
                Printer.COLOR_1,
                Printer.MODE_MONO,
                Printer.HALFTONE_DITHER,
                Printer.PARAM_DEFAULT,
                Printer.COMPRESS_AUTO);
        mPrinter.addTextAlign(Printer.ALIGN_CENTER);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        //textData.append("REPSOL"+"\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append("\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append(GetTicketData(variables.KEY_TICKET_CVEEST) + "\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append(datos_domicilio.getString("estacion") + "\n");
        textData.append(datos_domicilio.getString("calle") + " " + datos_domicilio.getString("exterior") + " " + datos_domicilio.getString("interior") + "\n");
        textData.append("COL." + datos_domicilio.getString("colonia") + " C.P. " + datos_domicilio.getString("cp") + "\n");
        textData.append(datos_domicilio.getString("localidad") + ", " + datos_domicilio.getString("municipio") + "\n");
        textData.append("TEL. "+datos_domicilio.getString("telefono") + "\n");
        textData.append(datos_domicilio.getString("rfc") + "\n");
        //textData.append("PERMISO C.C. C.R.E.: "+datos_domicilio.getString("permiso")+"\n");
        textData.append("\n");
        textData.append("Regimen Fiscal" + "\n");
        textData.append(datos_domicilio.getString("regimen") + "\n");
        textData.append("\n");
        textData.append("Lugar de Expedicion" + "\n");
        textData.append(datos_domicilio.getString("municipio") + " " + datos_domicilio.getString("estado") + "\n");
        textData.append("\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("***** " + titulo + " *****" + "\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append("\n");
        mPrinter.addTextAlign(Printer.ALIGN_LEFT);
        textData.append(GetData(variables.KEY_CLIENTE));

        textData.append(cliente + "\n");
        textData.append(metodoPago + "\n");
        if (HasData(Variables.METODO)){
            textData.append("Método de identificación - " + GetData(Variables.METODO) + "\n");
        }
        if (HasData(variables.KEY_CODCLI)) {
            if (HasData(variables.KEY_CLIENTE_VEHICULO_CHOFER)) {
                textData.append("Conductor     : " + GetData(variables.KEY_CLIENTE_VEHICULO_CHOFER) + "\n");
            }
            if (HasData(variables.KEY_CLIENTE_VEHICULO_NROECO)) {
                textData.append("No. Econ.     : " + GetData(variables.KEY_CLIENTE_VEHICULO_NROECO) + "\n");
            }
            if (HasData(variables.KEY_CLIENTE_VEHICULO_PLACA)) {
                textData.append("Placas        : " + GetData(variables.KEY_CLIENTE_VEHICULO_PLACA) + "\n");
            }
            if (HasData(variables.KEY_ODM)) {
                textData.append("Kilometraje   : " + GetDataODM() + "\n");
            }
            textData.append("------------------------------\n");
        }
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("TICKET    : " + folio_impreso + "   BOMBA : " + GetData(variables.POSICION) + "\n");
        textData.append("FECHA: " + GetTicketData(variables.KEY_TICKET_FECHA) + "  HORA: " + GetTicketData(variables.KEY_TICKET_HORA) + "\n");
        textData.append("VENDEDOR  : " + String.valueOf(GetTicketData(variables.KEY_TICKET_DESPACHADOR)).toUpperCase() + "\n");
        textData.append("PRECIO    : $ " + String.format("%.2f", Double.parseDouble(formateador2.format(Double.parseDouble(GetTicketData(variables.KEY_TICKET_PRECIO))))) + "\n");
        textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(Double.parseDouble(GetTicketData(variables.KEY_TICKET_CANTIDAD))))) + " LITROS " + String.valueOf(GetTicketData(variables.KEY_TICKET_PRODUCTO)).toUpperCase() + "\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("IMPORTE   : $ " + formateador2.format(Double.parseDouble(GetTicketData(variables.KEY_TICKET_TOTAL))) + "\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append(letra.Convertir(String.valueOf(formateador21.format(Double.parseDouble(GetTicketData(variables.KEY_TICKET_TOTAL)))), true) + "\n");
        //textData.append("\n");
        textData.append("\n");
        textData.append("------------------------------\n");
        textData.append("NOMBRE Y FIRMA CONDUCTOR\n");
        textData.append("________________________________\n");
        if ( GetData(Variables.METODO).equals(Variables.KEY_VALE)){
            textData.append("CLIENTE VALE  : " + dato_cliente.get("cliente") +"\n");
            textData.append("MONTO VALE    : " + GetData(variables.TOTAL_VALE_CONSUMO) +"\n");
            textData.append("VALES         : " + GetData(variables.VALES_LIST) + "\n");
        }else {
            textData.append("|TRAMITE SU FACTURA POR INTERNET|\n");
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append(FacturacionURL + "\n");
        }
        mPrinter.addTextAlign(Printer.ALIGN_LEFT);
        textData.append("________________________________\n");
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());

        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        // funcion para copia
        if (Integer.parseInt(GetData(variables.KEY_IMPRESO)) == 0 || Integer.parseInt(GetData(variables.KEY_IMPRESO)) == 10) {
            Bitmap qrrespol = null;
            QRCodeEncoder qrCodeEncoder1 = new QRCodeEncoder(repsolQR(datos_domicilio),
                    null,
                    Contents.Type.TEXT,
                    BarcodeFormat.QR_CODE.toString(),
                    smallerDimension);
            qrrespol = qrCodeEncoder1.encodeAsBitmap();
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addImage(qrrespol, 0, 0,
                    qrrespol.getWidth(),
                    qrrespol.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);
        }
        textData.append("\n");
        mPrinter.addText(textData.toString());
        ValidacionFlotillero vf = new ValidacionFlotillero();
        Integer sorteo = vf.validar_sorteo(mContext);
        String inicio = vf.sorteo_inicio(mContext);
        String fin = vf.sorteo_fin(mContext);
        /*funcion para imprimir copias*/
        if (!flag_TicketImpreso){
            mPrinter.addCut(Printer.CUT_FEED);
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            method = "addImage";
            mPrinter.addImage(logoData, 0, 0,
                    logoData.getWidth(),
                    logoData.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            //textData.append("REPSOL"+"\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            /* textData.append("\n");*/
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append(GetTicketData(variables.KEY_TICKET_CVEEST) + "\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append(datos_domicilio.getString("estacion") + "\n");
            textData.append(datos_domicilio.getString("calle") + " " +
                    datos_domicilio.getString("exterior") + " " +
                    datos_domicilio.getString("interior") + "\n");
            textData.append("COL." + datos_domicilio.getString("colonia") + " C.P. " +
                    datos_domicilio.getString("cp") + "\n");
            textData.append(datos_domicilio.getString("localidad") + ", " +
                    datos_domicilio.getString("municipio") + "\n");
            textData.append("TEL. "+datos_domicilio.getString("telefono") + "\n");
            textData.append(datos_domicilio.getString("rfc") + "\n");
            //textData.append("PERMISO C.C. C.R.E.: "+datos_domicilio.getString("permiso")+"\n");
            textData.append("\n");
            textData.append("Regimen Fiscal" + "\n");
            textData.append(datos_domicilio.getString("regimen") + "\n");
            textData.append("\n");
            textData.append("Lugar de Expedicion" + "\n");
            textData.append(datos_domicilio.getString("municipio") + " " + datos_domicilio.getString("estado") + "\n");
            textData.append("\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("***** " + "C O P I A" + " *****" + "\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append("\n");
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            textData.append(GetData(variables.KEY_CLIENTE));
            textData.append(cliente + "\n");
            textData.append(metodoPago + "\n");
            if (HasData(Variables.METODO)){
                textData.append("Método de identificación - " + GetData(Variables.METODO)  + "\n");
            }
            //textData.append("\n");
            if (HasData(variables.KEY_CODCLI)) {
                if (HasData(variables.KEY_CLIENTE_VEHICULO_CHOFER)) {
                    textData.append("Conductor     : " + GetData(variables.KEY_CLIENTE_VEHICULO_CHOFER) + "\n");
                }
                if (HasData(variables.KEY_CLIENTE_VEHICULO_NROECO)) {
                    textData.append("No. Econ.     : " + GetData(variables.KEY_CLIENTE_VEHICULO_NROECO) + "\n");
                }
                if (HasData(variables.KEY_CLIENTE_VEHICULO_PLACA)) {
                    textData.append("Placas        : " + GetData(variables.KEY_CLIENTE_VEHICULO_PLACA) + "\n");
                }
                if (HasData(variables.KEY_ODM)) {
                    textData.append("Kilometraje   : " + GetDataODM() + "\n");
                }
                textData.append("------------------------------\n");
            }
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("TICKET    : " + "C O P I A" + "   BOMBA : " + GetTicketData(variables.KEY_TICKET_BOMBA) + "\n");
            textData.append("FECHA: " + GetTicketData(variables.KEY_TICKET_FECHA) + "  HORA: " + GetTicketData(variables.KEY_TICKET_HORA) + "\n");
            textData.append("VENDEDOR  : " + String.valueOf(GetTicketData(variables.KEY_TICKET_DESPACHADOR)).toUpperCase() + "\n");
            textData.append("PRECIO    : $ " + String.format("%.2f", Double.valueOf(formateador2.format(Double.parseDouble(GetTicketData(variables.KEY_TICKET_PRECIO))))) + "\n");
            textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(Double.parseDouble(GetTicketData(variables.KEY_TICKET_CANTIDAD))))) + " LITROS " + String.valueOf(GetTicketData(variables.KEY_TICKET_PRODUCTO)).toUpperCase() + "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("IMPORTE   : $ " + formateador2.format(Double.parseDouble(GetTicketData(variables.KEY_TICKET_TOTAL))) + "\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append(letra.Convertir(String.valueOf(formateador21.format(Double.parseDouble(GetTicketData(variables.KEY_TICKET_TOTAL)))), true) + "\n");
            //textData.append("\n");
            textData.append("\n");
            textData.append("------------------------------\n");
            textData.append("NOMBRE Y FIRMA CONDUCTOR\n");
            textData.append("________________________________\n");
            if ( GetData(Variables.METODO).equals(Variables.KEY_VALE)){
                textData.append("CLIENTE VALE  : " + dato_cliente.get("cliente") +"\n");
                textData.append("MONTO VALE    : " + GetData(variables.TOTAL_VALE_CONSUMO) +"\n");
                textData.append("VALES         : " + GetData(variables.VALES_LIST) + "\n");
            }else {
                textData.append("|TRAMITE SU FACTURA POR INTERNET|\n");
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                textData.append(FacturacionURL + "\n");
            }
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            textData.append("________________________________\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("\n");
            mPrinter.addText(textData.toString());
        }
        /*funcion para los sorteos*/
        if (Integer.parseInt(GetData(variables.KEY_IMPRESO)) == 0 || Integer.parseInt(GetData(variables.KEY_IMPRESO)) == 10) {
            if (sorteo > 0) {
                if (Double.parseDouble(GetTicketData(variables.KEY_TICKET_TOTAL)) >= 200) {
                    Bitmap logoviaje = BitmapFactory.decodeResource(getResources(), R.drawable.ganaconcombu);
                    method = "addImage";
                    mPrinter.addImage(logoviaje, 0, 0,
                            logoviaje.getWidth(),
                            logoviaje.getHeight(),
                            Printer.COLOR_1,
                            Printer.MODE_MONO,
                            Printer.HALFTONE_DITHER,
                            Printer.PARAM_DEFAULT,
                            Printer.COMPRESS_AUTO);
                    mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                    mPrinter.addText(textData.toString());
                    textData.delete(0, textData.length());
                    textData.append("\n");
                    //textData.append("EL VIAJE DE TU VIDA\n");
                    mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
                    mPrinter.addText(textData.toString());
                    textData.delete(0, textData.length());
                    mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
                    mPrinter.addTextAlign(Printer.ALIGN_LEFT);
                    textData.append("             ESTACION   :   " + datos_domicilio.getString("estacion") + "\n");
                    textData.append("                FOLIO   :   " + folio_impreso + "\n");
                    textData.append("                FECHA   :   " + GetTicketData(variables.KEY_TICKET_FECHA) + "\n");
                    textData.append("                MONTO   :   " + formateador2.format(GetTicketData(variables.KEY_TICKET_TOTAL)) + "\n");
                    textData.append("             PRODUCTO   :   " + GetTicketData(variables.KEY_TICKET_PRODUCTO) + "\n");
                    mPrinter.addText(textData.toString());
                    textData.delete(0, textData.length());
                    mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                    textData.append("\n");
                    textData.append("\n");
                    mPrinter.addText(textData.toString());
                    textData.delete(0, textData.length());
                    mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                    textData.append("INGRESA A :\n");
                    textData.append("www.ganaconcombu.com\n");
                    textData.append("LLENA LA FORMA\n");
                    textData.append("Y GANA CON combu\n");
                    mPrinter.addText(textData.toString());
                    textData.delete(0, textData.length());
                    //QR

                    Bitmap qrcfdi = null;
                    String qr_cadena = "URL: https://ganaconcombu.com?es=" + GetTicketData(variables.KEY_TICKET_CVEEST)
                            + "&fo=" + String.valueOf(folio_impreso) + "&fe=" +
                            GetTicketData(variables.KEY_TICKET_FECHA) + "&mo=" + formateador2.format(GetTicketData(variables.KEY_TICKET_TOTAL))
                            + "&pr=" + GetTicketData(variables.KEY_TICKET_ID_PRODUCTO);
                    QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qr_cadena,
                            null,
                            Contents.Type.TEXT,
                            BarcodeFormat.QR_CODE.toString(),
                            smallerDimension);
                    qrcfdi = qrCodeEncoder.encodeAsBitmap();

                    mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                    mPrinter.addImage(qrcfdi, 0, 0,
                            qrcfdi.getWidth(),
                            qrcfdi.getHeight(),
                            Printer.COLOR_1,
                            Printer.MODE_MONO,
                            Printer.HALFTONE_DITHER,
                            Printer.PARAM_DEFAULT,
                            Printer.COMPRESS_AUTO);
                    textData.append("\n");
                    textData.append("SCANEA EL CODIGO QR\n");
                    textData.append("PARA INGRESAR A LA PROMOCION\n");
                    textData.append("\n");
                    textData.append("\n");
                    textData.append("PROMOCION : " + vf.sorteo_nombre(mContext) + "\n");
                    textData.append("VIGENCIA DEL " + inicio + " \n");
                    textData.append("AL " + fin + "\n");
                    textData.append("\n");
                }
            }
        }
        /*mPrinter.addText(textData.toString());*/
        method = "addCut";
        mPrinter.addCut(Printer.CUT_FEED);
        textData = null;
        return true;
    }
    private boolean printData() {
        if (mPrinter == null) {
            flag = 1;
            return false;
        }

        if (!connectPrinter()) {
            flag = 1;
            mPrinter.clearCommandBuffer();
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            flag=1;
            if (pdLoading != null) {
                pdLoading.dismiss();
            }
            flag = 1;
            mPrinter.clearCommandBuffer();
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            ShowMsg.showException(e, "sendData", getApplicationContext());
            try {
                mPrinter.disconnect();
            } catch (Exception ex) {
                if (pdLoading != null) {
                    pdLoading.dismiss();
                }
                // Do nothing
            }
            return false;
        }
        return true;
    }
    private boolean connectPrinter()  {
        if (mPrinter == null) {
            return false;
        }
        DataBaseManager manager = new DataBaseManager(mContext);
        String target = manager.target();
        try {
            mPrinter.connect(target, Printer.PARAM_DEFAULT);
        } catch (final Epos2Exception e) {
            flag=1;
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            runOnUiThread(new Runnable() {
                public synchronized void run() {
                    ShowMsg.showException(e, "connect", mContext);
                }
            });

            return false;
        }
        return true;
    }
    public String repsolQR(JSONObject jsdomicilio) throws JSONException {
        Double ieps = Double.parseDouble(GetTicketData(variables.KEY_TICKET_IEPS));
        Double iva_factor = Double.parseDouble(GetTicketData(variables.KEY_TICKET_IVA));
        Double lts = Double.parseDouble(GetTicketData(variables.KEY_TICKET_CANTIDAD));
        Double pre = Double.parseDouble(GetTicketData(variables.KEY_TICKET_PRECIO));
        Double ivaentre = Double.valueOf("1" + iva_factor);
        Double precioneto = ((Double.valueOf(pre) - ieps) / ivaentre) * 100;
        Double iva = ((precioneto * lts) * iva_factor) / 100;
        Double subtotal = (precioneto + ieps) * lts;
        String precio_total = String.valueOf(precioneto + ieps);
        String substr = ".";
        String inicio = precio_total.substring(0, precio_total.indexOf(substr));
        String fin = precio_total.substring(precio_total.indexOf(substr) + substr.length());
        String precio_impresion = inicio + "." + fin.substring(0, 2);
        String qr = "COMBUGO|" + GetTicketData(variables.KEY_TICKET_CVEEST) + "|" +
                jsdomicilio.getString("estacion") + "|" + GetTicketData(variables.KEY_TICKET_FECHA) +
                "|" + GetTicketData(variables.KEY_TICKET_HORA) + "|" + precio_impresion + "|" +
                formateador2.format(iva) + "|" + GetTicketData(variables.KEY_TICKET_TOTAL) + "|" +
                GetTicketData(variables.KEY_TICKET_NROTRN) + "|";
        return qr;
    }

    private String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";
        if (status.getOnline() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += getString(R.string.handlingmsg_err_autocutter);
            msg += getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += getString(R.string.handlingmsg_err_battery_real_end);
        }
        return msg;
    }
    private void dispPrinterWarnings(PrinterStatusInfo status) {
        String warningsMsg = "";
        if (status == null) {
            return;
        }
        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
        }
    }
    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        while (true) {
            try {
                mPrinter.disconnect();
                break;
            } catch (final Exception e) {
                if (pdLoading != null) {
                    pdLoading.dismiss();
                }
                if (e instanceof Epos2Exception) {
                    //Note: If printer is processing such as printing and so on, the disconnect API returns ERR_PROCESSING.
                    if (((Epos2Exception) e).getErrorStatus() == Epos2Exception.ERR_PROCESSING) {
                        try {
                            Thread.sleep(DISCONNECT_INTERVAL);
                        } catch (Exception ex) {
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            public synchronized void run() {
                                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                        stacktraceObj[2].getMethodName() + "|" + e);
                                ShowMsg.showException(e, "disconnect", mContext);
                            }
                        });
                        break;
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                    stacktraceObj[2].getMethodName() + "|" + e);
                            ShowMsg.showException(e, "disconnect", mContext);
                        }
                    });
                    break;
                }
            }
        }
        mPrinter.clearCommandBuffer();
    }
    public void ImpresionContado(View view) {
        String Bombas="";
        ArrayList<String> data = new ArrayList<String>();
        try {
            for ( int i = 0; i < Posiciones.getJSONArray(variables.POSICIONES).length();i++) {
                JSONObject jsonObject = (JSONObject) Posiciones.getJSONArray(variables.POSICIONES).get(i);
                if(!jsonObject.has(variables.METODO)){
                    data.add(((JSONObject) Posiciones.getJSONArray(variables.POSICIONES).get(i)).getString(variables.POSICION));
                }
            }
            if (data.size()<1){
                Log.w("Largo","Ok entro");
                new AlertDialog.Builder(Credito.this)
                        .setIcon(icon)
                        .setTitle(R.string.error)
                        .setMessage(ERROR_POSICION_CONTADO)
                        .setPositiveButton(R.string.btn_ok,null).show();
            }else {
                fab_contado dialogFragment = fab_contado
                        .newInstance("Ticket Contado", data);
                dialogFragment.show(getFragmentManager(), "dialog");
            }
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }

    }
    //Metodo NFC//
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        try {
            readFromIntent(intent);
        } catch (ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException | JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (!IsTablet) {
            WriteModeOff();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!IsTablet) {
            WriteModeOn();
        }
    }
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
    private void onCreateNFC() throws ClassNotFoundException, SQLException, InstantiationException,
            IllegalAccessException, JSONException {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new
                Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

    }
    private void readFromIntent(Intent intent) throws ClassNotFoundException, SQLException,
            InstantiationException, IllegalAccessException, JSONException {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs) throws JSONException {
        String text = "";
        if (myTag == null || bin2hex(myTag.getId()).length() == 0) return;
        if (msgs != null ) {
            if (msgs.length > 0){
                byte[] payload = msgs[0].getRecords()[0].getPayload();
                String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
                int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
                // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                try {
                    // Get the Text
                    text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
                } catch (UnsupportedEncodingException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    Log.e("UnsupportedEncoding", e.toString());
                }
            }
        }
        if (!HasData(variables.METODO) ) {
            new AlertDialog.Builder(Credito.this)
                    .setIcon(icon)
                    .setTitle(R.string.error)
                    .setMessage(MESSAGE_METODO_NFC)
                    .setPositiveButton(R.string.btn_ok, null).show();
        }else{
            if ( GetData(variables.METODO).equals(variables.KEY_RFID) && !HasData(variables.KEY_CLIENTE)) {
                PutData(variables.KEY_TAG,bin2hex(myTag.getId()));
                FillCustomerNFC(bin2hex(myTag.getId()));
            }
        }

        /*Log.e("NFC Tag: ", bin2hex(myTag.getId()));
        Log.e("NFC Content: ", text);*/
    }
    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,data));
    }
    private void FillCustomerNFC(String tag){
        new GetLastNROTRN(this, getApplicationContext(), new ControlGasListener() {
            @Override
            public void processFinish(JSONObject output) {
                try {
                    if (output.getInt(variables.CODE_ERROR)==0) {
                        PutData(variables.KEY_ULT_NROTRN, output.getString(variables.KEY_ULT_NROTRN));
                        Log.w("Listener cg", String.valueOf(output));
                    }else{
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" +
                                output.getString(variables.MESSAGE_ERROR));
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setIcon(icon)
                                .setMessage(output.getString(variables.MESSAGE_ERROR))
                                .setPositiveButton(R.string.btn_ok,null).show();
                    }
                }catch (JSONException e){
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
            }}).execute(spn_posicion.getSelectedItem().toString());
        new GetCustomerTag(this, getApplicationContext(), new ControlGasListener() {
            @Override
            public void processFinish(JSONObject output) {
                try {
                    if(output.getInt(variables.CODE_ERROR)==0){
                        JSONObject jsonObject= new JSONObject();
                        dataCustomerCG = (List<DataCustomerCG>) output.get(variables.GET_CUSTOMER_RESULT);
                        if ( dataCustomerCG.size()>0) {
                            try {
                                jsonObject.put("codcli", dataCustomerCG.get(0).codcli);
                                jsonObject.put("chofer", dataCustomerCG.get(0).rsp);
                                jsonObject.put("placa", dataCustomerCG.get(0).plc);
                                jsonObject.put("vehiculo", dataCustomerCG.get(0).den_vehicle);
                                jsonObject.put("tar", dataCustomerCG.get(0).tar);
                                jsonObject.put("nroveh", dataCustomerCG.get(0).nroveh);
                                jsonObject.put("cliente", dataCustomerCG.get(0).den);
                                jsonObject.put("rfc", dataCustomerCG.get(0).rfc);
                                jsonObject.put("nroeco", dataCustomerCG.get(0).nroeco);
                                jsonObject.put("tagadi", "");
                                jsonObject.put("tipval",dataCustomerCG.get(0).tipval);
                                FillCustomerData(jsonObject);
                                FillCustomerVehicleData(jsonObject);
                                CoreScreen();
                                CloseKeyboard();
                            } catch (JSONException | ClassNotFoundException | SQLException |
                                    InstantiationException | IllegalAccessException e) {
                                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                        stacktraceObj[2].getMethodName() + "|" + e);
                                new AlertDialog.Builder(Credito.this)
                                        .setTitle(R.string.error)
                                        .setIcon(icon)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok, null).show();
                                e.printStackTrace();
                            }
                        }
                    }else{
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + output.getString(variables.MESSAGE_ERROR));
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setIcon(icon)
                                .setMessage(output.getString(variables.MESSAGE_ERROR))
                                .setPositiveButton(R.string.btn_ok, null).show();
                    }
                } catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
            }}).execute(tag);
    }
    /*Validaciones del flotillero*/
    private Boolean ValidacionCarga()  {
        final boolean[] resdia = {true};
        final boolean[] resestacion = {true};
        final boolean[] reshora = {true};
        final boolean[] resestado = {true};
        final JSONObject[] dia = {null};
        try {
            String tag;
            if(GetData(variables.METODO).equals(variables.KEY_NOMBRE) || GetData(variables.METODO).equals(variables.KEY_VALE)){
                tag = GetData(variables.KEY_CLIENTE_VEHICULO_TAR);
            }else {
                tag = GetData(variables.KEY_TAG);
            }
            new GetVehicleRestrictions(this, getApplicationContext(), new ControlGasListener() {
                @Override
                public void processFinish(JSONObject output) {
                    try {
                        if(output.getInt(variables.CODE_ERROR)==0){
                            dia[0] = output.getJSONObject("Data");
                            ArrayList<Integer> dias_carga = (ArrayList<Integer>) dia[0].get("Array");
                            if (!ValidacionCargaDia(dias_carga, dia[0])){
                                resdia[0] = false;
                            }
                            if (!ValidacionCargaEstacion(dia[0])){
                                resestacion[0] = false;
                            }
                            if(!ValidacionCargaHora(dia[0])){
                                reshora[0] = false;
                            }
                            if (!ValidacionCargaEstado(dia[0])){
                                resestado[0] = false;
                            }
                            ValidacionCargaProducto(dia[0]);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute(tag, GetData(variables.METODO));
            if (!resdia[0] || !resestacion[0] || !reshora[0] || !resestado[0]){
                return false;
            }else {
                return true;
            }
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();

        }
        return true;
    }
    private Boolean ValidacionCargaDia(ArrayList<Integer> dias_carga, JSONObject dia) throws JSONException {
        ArrayList<Integer> dias_semana=new ArrayList<>();
        Boolean res  = false;
        if (dias_carga.contains(1)){
            lunes.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            lunes.setBackgroundResource(R.drawable.ok_vector);
        }else{
            lunes.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            lunes.setBackgroundResource(R.drawable.cancel_vector);
        }
        if (dias_carga.contains(2)){
            martes.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            martes.setBackgroundResource(R.drawable.ok_vector);
        }else{
            martes.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            martes.setBackgroundResource(R.drawable.cancel_vector);
        }
        if (dias_carga.contains(3)){
            miercoles.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            miercoles.setBackgroundResource(R.drawable.ok_vector);
        }else{
            miercoles.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            miercoles.setBackgroundResource(R.drawable.cancel_vector);
        }
        if (dias_carga.contains(4)){
            jueves.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            jueves.setBackgroundResource(R.drawable.ok_vector);
        }else{
            jueves.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            jueves.setBackgroundResource(R.drawable.cancel_vector);
        }
        if (dias_carga.contains(5)){
            viernes.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            viernes.setBackgroundResource(R.drawable.ok_vector);
        }else{
            viernes.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            viernes.setBackgroundResource(R.drawable.cancel_vector);
        }
        if (dias_carga.contains(6)){
            sabado.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            sabado.setBackgroundResource(R.drawable.ok_vector);
        }else{
            sabado.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            sabado.setBackgroundResource(R.drawable.cancel_vector);
        }
        if (dias_carga.contains(7)){
            domingo.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            domingo.setBackgroundResource(R.drawable.ok_vector);
        }else{
            domingo.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            domingo.setBackgroundResource(R.drawable.cancel_vector);
        }
        for (int i = 0 ; i < dias_carga.size() ; i++){
            if ( dia.getString("Dia").equals(String.valueOf(dias_carga.get(i)))){
                res=true;
            }
        }
        return res;
    }
    private boolean ValidacionCargaEstacion(JSONObject jsonObject) throws JSONException {
        boolean res = false;
        if (jsonObject.has("EstPermitida") && jsonObject.getString("EstPermitida").equals("0")){
            res = true;
        }
        if (jsonObject.getString("EstPermitida").equals(jsonObject.getString("EstLocal"))){
            res = true;
        }
        if (res){
            semana.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            semana.setText("");
            semana.setBackgroundResource(R.drawable.ok_vector);
        }else{
            semana.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            semana.setText("");
            semana.setBackgroundResource(R.drawable.cancel_vector);
        }
        return res;
    }
    private boolean ValidacionCargaHora(JSONObject jsonObject) throws JSONException {
        boolean res1=false,res2=false,res3=false;
        if (jsonObject.getString("hraini").equals("-1")&&jsonObject.getString("hrafin").equals("-1")){
            tvhora1.setText(R.string.HoraCero);
            res1=true;
        }else {
            String hora1=cgticket_obj.hora(jsonObject.getString("hraini")) + "-" +
                    cgticket_obj.hora(jsonObject.getString("hrafin"));
            tvhora1.setText(hora1);
            if (jsonObject.getInt("hraini") <= jsonObject.getInt("HoraActual") &&
                    jsonObject.getInt("HoraActual") <= jsonObject.getInt("hrafin")){
                res1 = true;
            }else{
                res1 = false;
            }
        }
        if (jsonObject.getString("hraini2").equals("-1")&&jsonObject.getString("hrafin2").equals("-1")){
            tvhora2.setText(R.string.HoraCero);
            res2=true;
        }else {
            String hora2=cgticket_obj.hora(jsonObject.getString("hraini2")) + "-" +
                    cgticket_obj.hora(jsonObject.getString("hrafin2"));
            tvhora2.setText(hora2);
            if (jsonObject.getInt("hraini2") <= jsonObject.getInt("HoraActual") &&
                    jsonObject.getInt("HoraActual") <= jsonObject.getInt("hrafin2")){
                res2 = true;
            }else{
                res2 = false;
            }
        }
        if (jsonObject.getString("hraini3").equals("-1")&&jsonObject.getString("hrafin3").equals("-1")){
            tvhora3.setText(R.string.HoraCero);
            res3=true;
        }else {
            String hora3 = cgticket_obj.hora(jsonObject.getString("hraini3")) + "-" +
                    cgticket_obj.hora(jsonObject.getString("hrafin3"));
            tvhora3.setText(hora3);
            if (jsonObject.getInt("hraini3") <= jsonObject.getInt("HoraActual") &&
                    jsonObject.getInt("HoraActual") <= jsonObject.getInt("hrafin3")){
                res3 = true;
            }else{
                res3 = false;
            }
        }
        if(res1 || res2 || res3){
            tvhora.setBackgroundResource(R.drawable.ok_vector);
            return true;
        }else{
            tvhora.setBackgroundResource(R.drawable.cancel_vector);
            return false;
        }
    }
    private boolean ValidacionCargaEstado(JSONObject jsonObject) throws JSONException {
        String texto="Habilitado";
        if (jsonObject.getString("Est").equals("1")){
            tvestado1.setBackgroundResource(R.drawable.ok_vector);
            tvestado2.setText(texto);
            return true;
        }else{
            switch (jsonObject.getString("Est")){
                case "2":
                    texto="Cargando";
                    break;
                case "3":
                    texto="Suspendido";
                    break;
                case "4":
                    texto="Uso Interno";
                    break;
                case "5":
                    texto="Verificcion Pendiente";
                    break;
                case "6":
                    texto="Baja Administrativa";
                    break;
            }
            tvestado1.setBackgroundResource(R.drawable.cancel_vector);
            tvestado2.setText(texto);
            return false;
        }
    }
    private void ValidacionCargaProducto (JSONObject jsonObject) throws JSONException {
        if (jsonObject.getInt("CodPrd")==0){
            tvproducto2.setText("Todos");
        }else{
            tvproducto2.setText(jsonObject.getString("Combustible"));
        }
    }


    @Override
    public void GetPumpPositionFinish(JSONObject output) {
        try {
            if (output.getInt(variables.CODE_ERROR)==0){
                ArrayList<String> data = (ArrayList<String>) output.get(variables.POSICIONES);
                ArrayAdapter NoCoreAdapter = new ArrayAdapter(getApplicationContext(),
                        R.layout.spinner_bombas_credito, data);
                spn_posicion.setAdapter(NoCoreAdapter);
                for (int i =0 ; i < data.size() ; i++) {
                    JSONObject Posicion = new JSONObject();
                    Posicion.put(variables.POSICION,data.get(i));
                    Logicos.put(Posicion);
                    Posiciones.put(variables.POSICIONES,Logicos);
                }
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + output.getString(variables.MESSAGE_ERROR));
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(output.getString(variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
    }

    @Override
    public void GetEstacionDataFinish(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt(Variables.CODE_ERROR)==0) {
                datos_domicilio = jsonObject;
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + jsonObject.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(jsonObject.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
    }

    @Override
    public void GetImpresoFinish(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt(Variables.CODE_ERROR)==0){
                PutData(Variables.KEY_IMPRESO, String.valueOf(jsonObject.getInt(Variables.KEY_IMPRESO)));
                if (GetData(variables.KEY_IMPRESO).equals("10")){
                    new UpdateCodcli(this, getApplicationContext(), new ControlGasListener() {
                        @Override
                        public void processFinish(JSONObject output){
                            try {
                                if (output.getInt(Variables.CODE_ERROR)==1){
                                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                            stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                                    new AlertDialog.Builder(Credito.this)
                                            .setTitle(R.string.error)
                                            .setIcon(icon)
                                            .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                            .setPositiveButton(R.string.btn_ok, null).show();
                                }
                            } catch (JSONException e) {
                                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                        stacktraceObj[2].getMethodName() + "|" + e);
                                new AlertDialog.Builder(Credito.this)
                                        .setTitle(R.string.error)
                                        .setIcon(icon)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok, null).show();
                                e.printStackTrace();
                            }
                        }
                    }).execute(
                            GetData(variables.KEY_CODCLI),
                            GetData(variables.KEY_CLIENTE_VEHICULO_NROVEH),
                            GetDataODM(),
                            GetData(variables.KEY_CLIENTE_VEHICULO_TAR),
                            GetTicketData(variables.KEY_TICKET_NROTRN));
                    PrintReceip();

                }
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + jsonObject.getString(variables.MESSAGE_ERROR));
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(jsonObject.getString(variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void UpdateNrotrnFinish(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt(Variables.CODE_ERROR)==0){
                /*impresion*/
                ExecutePrint executePrint = new ExecutePrint();
                executePrint.execute();
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + jsonObject.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setMessage(jsonObject.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void GetCustomerNipFinish(JSONObject res) {
        try {
            if (res.getInt(Variables.CODE_ERROR)==0){
                JSONObject jsonObject= new JSONObject();
                dataCustomerCG = new ArrayList<>((Collection<? extends DataCustomerCG>) res.get(Variables.DATA_CUSTOMER));
                if ( dataCustomerCG.size()>0) {
                    jsonObject.put("codcli", dataCustomerCG.get(0).codcli);
                    jsonObject.put("chofer", dataCustomerCG.get(0).rsp);
                    jsonObject.put("placa", dataCustomerCG.get(0).plc);
                    jsonObject.put("vehiculo", dataCustomerCG.get(0).den_vehicle);
                    jsonObject.put("tar", dataCustomerCG.get(0).tar);
                    jsonObject.put("nroveh", dataCustomerCG.get(0).nroveh);
                    jsonObject.put("cliente", dataCustomerCG.get(0).den);
                    jsonObject.put("rfc", dataCustomerCG.get(0).rfc);
                    jsonObject.put("nroeco",dataCustomerCG.get(0).nroeco);
                    jsonObject.put("tipval",dataCustomerCG.get(0).tipval);
                    jsonObject.put("tagadi", "");
                    FillCustomerData(jsonObject);
                    FillCustomerVehicleData(jsonObject);
                    CoreScreen();
                    CloseKeyboard();
                }else{
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage("No se encontraron datos de cliente con el parametro establecido.")
                            .setPositiveButton(R.string.btn_ok,null).show();
                }
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + res.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(res.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok,null).show();
            }
        } catch (ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException | JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
    }

    @Override
    public void GetCustomerNameFinish(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt(Variables.CODE_ERROR)==0){
                dataCustomerCG = new ArrayList<>((Collection<? extends DataCustomerCG>) jsonObject.get(Variables.DATA_CUSTOMER));
                mRVCustomerCG = (RecyclerView) findViewById(R.id.clientes_cg);
                mAdapter = new AdapterCustomerCG(Credito.this, dataCustomerCG);
                mRVCustomerCG.setAdapter(mAdapter);
                mRVCustomerCG.setLayoutManager(new LinearLayoutManager(Credito.this));
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + jsonObject.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(jsonObject.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok,null).show();
            }
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
    }

    @Override
    public void GetCustomerVehicleFinish(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt(Variables.CODE_ERROR)==0){

                List<DataCustomerCG> dataCustomerVehicleCG = new ArrayList<>(
                        (Collection<? extends DataCustomerCG>) jsonObject.get(Variables.DATA_CUSTOMER_VEHICLE));

                mAdapterVehicle = new AdapterCustomerVehicleCG(Credito.this,dataCustomerVehicleCG);
                mRVCustomerVehicleCG.setAdapter(mAdapterVehicle);
                mRVCustomerVehicleCG.setLayoutManager(new LinearLayoutManager(Credito.this));
                if (Boolean.parseBoolean(jsonObject.getString("Boolean"))){
                    etSearchCustomer.setText("");}
                viewFlipper.setDisplayedChild(4);
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + jsonObject.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(jsonObject.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok,null).show();
            }
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }

    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status,
                             final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                if (code == 0) {
                    try {
                        new PutImpreso(Credito.this, new ControlGasListener() {
                            @Override
                            public void processFinish(JSONObject output) {
                                try {
                                    if (output.getInt(Variables.CODE_ERROR)==0){
                                        CleanData();
                                    }else{
                                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                                stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                                        new AlertDialog.Builder(Credito.this)
                                                .setTitle(R.string.error)
                                                .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                                .setPositiveButton(R.string.btn_ok, null).show();
                                    }
                                } catch (JSONException | ClassNotFoundException | SQLException |
                                        InstantiationException | IllegalAccessException e) {
                                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                            stacktraceObj[2].getMethodName() + "|" + e);
                                    new AlertDialog.Builder(Credito.this)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok, null).show();
                                    e.printStackTrace();
                                }
                            }
                        }).execute(GetTicketData(variables.KEY_TICKET_NROTRN));
                    } catch ( JSONException e) {
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + e);
                        new AlertDialog.Builder(Credito.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok, null).show();
                        e.printStackTrace();
                    }
                }
                ShowMsg.showResult(code, makeErrorMessage(status), mContext);
                dispPrinterWarnings(status);
                updateButtonState(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }

    public class ExecutePrint extends AsyncTask <String, Void, JSONObject>{

        @Override
        protected void onPreExecute() {
            pdLoading = new ProgressDialog(Credito.this);
            pdLoading.setMessage("Imprimiendo..."); // Setting Message
            pdLoading.setTitle(marca); // Setting Title
            pdLoading.setIcon(icon);
            pdLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            pdLoading.show(); // Display Progress Dialog
            pdLoading.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                if (!runPrintReceiptSequence()){
                    updateButtonState(true);
                    if (pdLoading != null) {
                        pdLoading.dismiss();
                    }
                }
            } catch (SQLException | WriterException | InstantiationException | JSONException |
                    ClassNotFoundException | IllegalAccessException | Epos2Exception e) {
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + e);
                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok, null).show();
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (pdLoading!= null){
                pdLoading.dismiss();
            }
            super.onPostExecute(jsonObject);
        }
    }
}
