package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.JsonReader;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
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
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import cg.ce.app.chris.com.cgce.common.Variables;
import cg.ce.app.chris.com.cgce.dialogos.close_credito;
import cg.ce.app.chris.com.cgce.dialogos.fab_contado;

public class Credito extends AppCompatActivity implements View.OnClickListener, com.epson.epos2.printer.ReceiveListener {
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
    CardView CardViewRFID, CardViewNIP, CardViewNOMBRE;
    Boolean hasMetodo = false, hasCliente = false, state_btn, flag_TicketImpreso = false;
    ViewFlipper viewFlipper;
    final static int MIN_SEARCH=3;
    EditText etSearchCustomer, et_clientecg;
    TextView tvvehiculo_cliente, tvvehiculo_rfc, tvvehiculo_codcli,lunes,martes,miercoles,jueves,viernes,sabado,domingo;
    /*elementos de activity_credito_impresion*/
    TextView tv_cliente,tv_rfc,tv_codcli,tv_placa,tv_vehiculo,tv_chofer,semana,tvhora,tvhora1,tvhora2,tvhora3;
    TextView tvestado1, tvestado2, tvproducto1, tvproducto2;
    EditText et_odm;
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
        if (!IsTablet){
            try {
                onCreateNFC();
            } catch (ClassNotFoundException | SQLException | InstantiationException |
                    IllegalAccessException | JSONException e) {
                logCE.EscirbirLog2(getApplicationContext(),"Credito_onCreate - " + e);
                new AlertDialog.Builder(Credito.this)
                        .setIcon(icon)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                e.printStackTrace();
            }
        }
        mContext = this;
        viewFlipper = findViewById(R.id.viewFlipper);
        CardViewRFID = findViewById(R.id.CardViewRFID);
        CardViewNIP = findViewById(R.id.CardViewNIP);
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
        spn_posicion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    CoreScreen();
                } catch (JSONException | InstantiationException | SQLException |
                        IllegalAccessException | ClassNotFoundException e) {
                    logCE.EscirbirLog2(getApplicationContext(),"Credito_OnCreate - " + e);
                    new AlertDialog.Builder(Credito.this)
                            .setIcon(icon)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
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
                    logCE.EscirbirLog2(getApplicationContext(),"Credito_onTextChanged - " + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
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
        try {
            if (pdLoading != null){
                pdLoading.dismiss();
            }
            pdLoading = new ProgressDialog(Credito.this);
            pdLoading.setMessage("Actualizando..."); // Setting Message
            pdLoading.setTitle(marca); // Setting Title
            pdLoading.setIcon(icon);
            pdLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            pdLoading.show(); // Display Progress Dialog
            pdLoading.setCancelable(false);
            UpdateCodcli();
            PutTicketData();
            pdLoading.dismiss();
            if (Integer.parseInt(GetData(variables.KEY_ULT_NROTRN))<Integer.parseInt(GetTicketData(variables.KEY_TICKET_NROTRN))){
                pdLoading = new ProgressDialog(Credito.this);
                pdLoading.setMessage("Imprimiendo..."); // Setting Message
                pdLoading.setTitle(marca); // Setting Title
                pdLoading.setIcon(icon);
                pdLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                pdLoading.show(); // Display Progress Dialog
                pdLoading.setCancelable(false);
                new Thread(new Runnable() {
                    public void run() {
                        PrintReceip();
                        pdLoading.dismiss();
                    }
                }).start();
            }else{

                new AlertDialog.Builder(Credito.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(R.string.EsperaServicio)
                        .setPositiveButton(R.string.btn_ok,null).show();
            }
        } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException e) {
            logCE.EscirbirLog2(getApplicationContext(),"Credito_BtnCredito - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
    }
    private void UpdateCodcli() throws JSONException, ClassNotFoundException, SQLException,
            InstantiationException, IllegalAccessException {
        PutTicketData();
        int impreso = cgticket_obj.cant_impreso(this, GetTicketData(variables.KEY_TICKET_NROTRN));
        if (impreso == 10 ) {
            cgticket_obj.update_codcli(getApplicationContext(),GetTicketData(variables.KEY_TICKET_NROTRN),
                    GetData(variables.KEY_CODCLI),GetTicketData(variables.KEY_CLIENTE_VEHICULO_NROVEH),
                    GetDataODM(),GetData(variables.KEY_CLIENTE_VEHICULO_TAR));
        }
    }
    private void PrintReceip()  {
        this.updateButtonState(false);
        try {
            int impreso = cgticket_obj.cant_impreso(this, GetTicketData(variables.KEY_TICKET_NROTRN));
            if (impreso == 10 ){
                runOnUiThread(new Runnable() {
                    public synchronized void run() {
                        JSONArray Validar = null;
                        try {
                            Validar = Posiciones.getJSONArray(variables.POSICIONES);
                            int index = spn_posicion.getSelectedItemPosition();
                            cgticket_obj.guardarnrotrn(getApplicationContext(),
                                    Validar.getJSONObject(index).getJSONObject(variables.KEY_TICKET),2);
                        } catch (JSONException | ClassNotFoundException | SQLException |
                                InstantiationException | IllegalAccessException e) {
                            logCE.EscirbirLog2(getApplicationContext(),"Credito_PrintReceip - " + e);
                            new AlertDialog.Builder(Credito.this)
                                    .setTitle(R.string.error)
                                    .setIcon(icon)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                            e.printStackTrace();
                        }
                    }
                });
            }
            if (!runPrintReceiptSequence()){
                updateButtonState(true);
                if (pdLoading != null) {
                    pdLoading.dismiss();
                }
            }
        } catch (SQLException | IllegalAccessException | InstantiationException |
                ClassNotFoundException | JSONException | WriterException | Epos2Exception e) {
            logCE.EscirbirLog2(getApplicationContext(),"Credito_PrintReceip - " + e);
            runOnUiThread(new Runnable() {
                public synchronized void run() {
                    new AlertDialog.Builder(Credito.this)
                            .setIcon(icon)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    updateButtonState(true);
                }
            });
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
                break;
            case "Repsol":
                setTheme(R.style.AppThemeCreditoRepsol);
                setContentView(R.layout.activity_credito_repsol);
                icon = getDrawable(R.drawable.isologo_repsol);
                break;
            case "Ener":
                setTheme(R.style.AppThemeCreditoEner);
                setContentView(R.layout.activity_credito_ener);
                icon = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.AppThemeCreditoTotal);
                setContentView(R.layout.activity_credito_total);
                icon = getDrawable(R.drawable.total);
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
        ResultSet rs;
        Cursor c;
        Connection connect;
        PreparedStatement stmt;
        MacActivity mac = new MacActivity();
        String query = "select p.numero_logico as logico from posicion as p \n" +
                "left outer join dispensario as disp on disp.id=p.id_dispensario\n" +
                "left outer join corte as c on c.id_dispensario=disp.id\n" +
                "left outer join dispositivos as dispo on dispo.id=c.id_dispositivo\n" +
                "where c.status =0 and dispo.mac_adr='"+mac.getMacAddress()+"'";
        try {
            DataBaseCG gc = new DataBaseCG();
            connect = gc.odbc_cecg_app(getApplicationContext());
            stmt = connect.prepareStatement(query);
            rs = stmt.executeQuery();

            ArrayList<String> data = new ArrayList<String>();
            while (rs.next()) {
                JSONObject Posicion = new JSONObject();
                String id = rs.getString("logico");
                data.add(id);
                Posicion.put(variables.POSICION,rs.getInt("logico"));
                Logicos.put(Posicion);
                Posiciones.put(variables.POSICIONES,Logicos);
            }
            String[] array = data.toArray(new String[0]);
            ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,
                    R.layout.spinner_bombas_credito, data);
            connect.close();
            spn_posicion.setAdapter(NoCoreAdapter);
        } catch (SQLException | IllegalAccessException | ClassNotFoundException | InstantiationException | JSONException e) {
            logCE.EscirbirLog2(getApplicationContext(),"Credito_FillPosicion - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
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
        * 6.-
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
                btn_print.setVisibility(View.GONE);
            }
        }
        else if(hasMetodo && !hasCliente){
            switch (Validar.getJSONObject(spn_posicion.getSelectedItemPosition()).getString(variables.METODO)){
                case "Rfid":
                    btn_print.setVisibility(View.GONE);
                    viewFlipper.setDisplayedChild(1);
                    break;
                case "Nip":
                    btn_print.setVisibility(View.GONE);
                    viewFlipper.setDisplayedChild(2);
                    break;
                case "Nombre":
                    btn_print.setVisibility(View.GONE);
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
            }
        }else if(!hasMetodo) {
            btn_print.setVisibility(View.GONE);
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
                    logCE.EscirbirLog2(getApplicationContext(),"Credito_onClick - " + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
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
                    logCE.EscirbirLog2(getApplicationContext(),"Credito_onClick - " + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
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
                    logCE.EscirbirLog2(getApplicationContext(),"Credito_onClick - " + e);
                    new AlertDialog.Builder(Credito.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }
                break;
        }
    }
    public void SearchCustomerNip(View v){
        try {
            PutData(variables.KEY_ULT_NROTRN, validacionFlotillero.validar_utlimo_nrotrn(this,
                    spn_posicion.getSelectedItem().toString()));
            JSONObject jsonObject= new JSONObject();
            PutData(variables.KEY_TAG,et_clientecg.getText().toString());
            dataCustomerCG = new ArrayList<>(cgticket_obj.GetCustomerNipCG(
                    this,GetData(variables.KEY_TAG)));
            if ( dataCustomerCG.size()>0) {
                jsonObject.put("codcli", dataCustomerCG.get(0).codcli);
                jsonObject.put("chofer", dataCustomerCG.get(0).rsp);
                jsonObject.put("placa", dataCustomerCG.get(0).plc);
                jsonObject.put("vehiculo", dataCustomerCG.get(0).den_vehicle);
                jsonObject.put("tar", dataCustomerCG.get(0).tar);
                jsonObject.put("nroveh", dataCustomerCG.get(0).nroveh);
                jsonObject.put("cliente", dataCustomerCG.get(0).den);
                jsonObject.put("rfc", dataCustomerCG.get(0).rfc);
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
        } catch (ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException | JSONException e) {
            logCE.EscirbirLog2(getApplicationContext(),"Credito_SearchCustomerNip - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
    }
    public void SearchCustomerName(View v){
        try {
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
                dataCustomerCG = new ArrayList<>(cgticket_obj.getCustomerCG(
                        this, etSearchCustomer.getText().toString()));
                mRVCustomerCG = (RecyclerView) findViewById(R.id.clientes_cg);
                mAdapter = new AdapterCustomerCG(Credito.this, dataCustomerCG);
                mRVCustomerCG.setAdapter(mAdapter);
                mRVCustomerCG.setLayoutManager(new LinearLayoutManager(Credito.this));
            }
        } catch (ClassNotFoundException | SQLException | InstantiationException | JSONException |
                IllegalAccessException e) {
            logCE.EscirbirLog2(getApplicationContext(),"Credito_SearchCustomerName - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
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
            List<DataCustomerCG> dataCustomerVehicleCG = new ArrayList<>(cgticket_obj.getCustomerVehicleCG(
                    this,js.getString("codcli")));
            mAdapterVehicle = new AdapterCustomerVehicleCG(Credito.this,dataCustomerVehicleCG);
            mRVCustomerVehicleCG.setAdapter(mAdapterVehicle);
            mRVCustomerVehicleCG.setLayoutManager(new LinearLayoutManager(Credito.this));
            if (First){
            etSearchCustomer.setText("");}
            viewFlipper.setDisplayedChild(4);
        } catch (JSONException | InstantiationException | SQLException | IllegalAccessException |
                ClassNotFoundException e) {
            logCE.EscirbirLog2(getApplicationContext(),"Credito_AdapterClickCustomerCG - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
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
                                    logCE.EscirbirLog2(getApplicationContext(),"Credito_AdapterClickCustomerVehicleCG - " + e);
                                    new AlertDialog.Builder(Credito.this)
                                            .setTitle(R.string.error)
                                            .setIcon(icon)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
                                    e.printStackTrace();
                                }
                            }
                        }).show();

            }else {
                PutData(variables.KEY_ULT_NROTRN, validacionFlotillero.validar_utlimo_nrotrn(this,
                        spn_posicion.getSelectedItem().toString()));
                FillScreenFinalData();
                viewFlipper.setDisplayedChild(5);
                btn_print.setVisibility(View.VISIBLE);
            }
        } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException e) {
            logCE.EscirbirLog2(getApplicationContext(),"Credito_AdapterClickCustomerVehicleCG - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
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
        ValidacionCarga();
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
    }
    private void FillCustomerVehicleData(JSONObject data) throws JSONException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_TAR,data.getString("tar"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_DEN,data.getString("vehiculo"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_NROVEH,data.getString("nroveh"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_TAGADI, data.getString("tagadi"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_PLACA, ValidateOptionalData(data,"placa"));
        Validar.getJSONObject(index).put(variables.KEY_CLIENTE_VEHICULO_CHOFER, ValidateOptionalData(data,"chofer"));
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
    private void PutTicketData() throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        JSONArray Validar = Posiciones.getJSONArray(variables.POSICIONES);
        int index = spn_posicion.getSelectedItemPosition();
        Validar.getJSONObject(index).put(variables.KEY_TICKET,cgticket_obj.consulta_servicio(this,
                spn_posicion.getSelectedItem().toString()));
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
                                    logCE.EscirbirLog2(getApplicationContext(),"Credito_MethodCleanData - " + e);
                                    new AlertDialog.Builder(Credito.this)
                                            .setIcon(icon)
                                            .setTitle(R.string.error)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok,null).show();
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
            logCE.EscirbirLog2(getApplicationContext(),"Credito_initializeObject - " + e);
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
        JSONObject datos_domicilio = cgticket_obj.estacion_domicilio(mContext);
        JSONObject vehiculo = new JSONObject();
        String titulo = "", folio_impreso = "", cliente = "", venta = "", tpv = "";
        String metodoPago = "";
        UpdateTicketData(variables.KEY_IMPRESO, String.valueOf(cgticket_obj.cant_impreso(getApplicationContext(),
                GetTicketData(variables.KEY_TICKET_NROTRN))));
        if (Integer.parseInt(GetTicketData(variables.KEY_IMPRESO)) == 0 || Integer.parseInt(GetTicketData(variables.KEY_IMPRESO)) == 10) {
            titulo = "O R I G I N A L";
            metodoPago = CalculateMetoPago(GetTicketData(variables.KEY_TICKET_CLIENTE_TIPVAL));
            folio_impreso = GetTicketData(variables.KEY_TICKET_NROTRN) + "0";

        } else if (Integer.parseInt(GetTicketData(variables.KEY_IMPRESO)) == 1) {
            titulo = "C O P I A";
            folio_impreso = "C O P I A";
            metodoPago = cgticket_obj.get_rut(mContext, Posiciones.getJSONArray(variables.POSICIONES)
                    .getJSONObject(spn_posicion.getSelectedItemPosition()).getJSONObject(variables.KEY_TICKET));
        }
        vehiculo = cgticket_obj.get_vehiculo(mContext, GetTicketData(variables.KEY_TICKET_NROTRN), GetData(variables.POSICION));
        venta = CalculateVenta(GetTicketData(variables.KEY_TICKET_CLIENTE_TIPVAL));
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
        if (HasData(variables.KEY_CODCLI)) {
            if (vehiculo.has("rsp")) {
                textData.append("Conductor     : " + vehiculo.getString("rsp") + "\n");
            }
            if (vehiculo.has("nroeco")) {
                textData.append("No. Econ.     : " + vehiculo.getString("nroeco") + "\n");
            }
            if (vehiculo.has("placa")) {
                textData.append("Placas        : " + vehiculo.getString("placa") + "\n");
            }
            if (HasData(variables.KEY_ODM)) {
                textData.append("Kilometraje   : " + GetDataODM() + "\n");
            }
            textData.append("------------------------------\n");
        }
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("TICKET    : " + folio_impreso + "   BOMBA : " + String.valueOf(GetData(variables.POSICION)) + "\n");
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
        textData.append("|TRAMITE SU FACTURA POR INTERNET|\n");
        textData.append("|      combuexpress.com.mx      |\n");
        textData.append("________________________________\n");
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());

        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());

        if (Integer.parseInt(GetTicketData(variables.KEY_IMPRESO)) == 0 || Integer.parseInt(GetTicketData(variables.KEY_IMPRESO)) == 10) {
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
            textData.append(datos_domicilio.getString("calle") + " " + datos_domicilio.getString("exterior") + " " + datos_domicilio.getString("interior") + "\n");
            textData.append("COL." + datos_domicilio.getString("colonia") + " C.P. " + datos_domicilio.getString("cp") + "\n");
            textData.append(datos_domicilio.getString("localidad") + ", " + datos_domicilio.getString("municipio") + "\n");
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
            //textData.append("\n");
            if (HasData(variables.KEY_CODCLI)) {
                if (vehiculo.has("rsp")) {
                    textData.append("Conductor     : " + vehiculo.getString("rsp") + "\n");
                }
                if (vehiculo.has("nroeco")) {
                    textData.append("No. Econ.     : " + vehiculo.getString("nroeco") + "\n");
                }
                if (vehiculo.has("placa")) {
                    textData.append("Placas        : " + vehiculo.getString("placa") + "\n");
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
            textData.append("|TRAMITE SU FACTURA POR INTERNET|\n");
            textData.append("|      combuexpress.com.mx      |\n");
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
        if (Integer.parseInt(GetTicketData(variables.KEY_IMPRESO)) == 0 || Integer.parseInt(GetTicketData(variables.KEY_IMPRESO)) == 10) {
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
            logCE.EscirbirLog2(getApplicationContext(),"Credito_printData - " + e);
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
            logCE.EscirbirLog2(getApplicationContext(),"Credito_connectPrinter - " + e);
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
    @Override
    public void onPtrReceive(final Printer printerObj, final int code,
                             final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                Log.w("Code", String.valueOf(code));
                if (code == 0) {
                    try {
                        cgticket_obj.actualizar_cant_impreso(mContext, GetTicketData(variables.KEY_TICKET_NROTRN));
                        CleanData();
                    } catch (ClassNotFoundException | JSONException | InstantiationException |
                            IllegalAccessException | SQLException e) {
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
                                logCE.EscirbirLog2(getApplicationContext(),"Credito_disconnectPrinter - " + e);
                                ShowMsg.showException(e, "disconnect", mContext);
                            }
                        });
                        break;
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            logCE.EscirbirLog2(getApplicationContext(),"Credito_disconnectPrinter - " + e);
                            ShowMsg.showException(e, "disconnect", mContext);
                        }
                    });
                    break;
                }
            }
        }
        mPrinter.clearCommandBuffer();
    }
    /*public void ImpresionContado(View view){
        ValidacionCarga();
    }*/
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
            logCE.EscirbirLog2(getApplicationContext(),"Credito_ImpresionContado - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
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
            logCE.EscirbirLog2(getApplicationContext(),"Credito_onNewIntent - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setIcon(icon)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
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
                    logCE.EscirbirLog2(getApplicationContext(),"Credito_buildTagViews - " + e);
                    new AlertDialog.Builder(Credito.this)
                            .setIcon(icon)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
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
        try {
            PutData(variables.KEY_ULT_NROTRN, "1");//validacionFlotillero.validar_utlimo_nrotrn(this,
                    //spn_posicion.getSelectedItem().toString()));
            JSONObject jsonObject= new JSONObject();
            dataCustomerCG = new ArrayList<>(cgticket_obj.GetCustomerNipCG(
                    this,tag));
            if ( dataCustomerCG.size()>0) {
                jsonObject.put("codcli", dataCustomerCG.get(0).codcli);
                jsonObject.put("chofer", dataCustomerCG.get(0).rsp);
                jsonObject.put("placa", dataCustomerCG.get(0).plc);
                jsonObject.put("vehiculo", dataCustomerCG.get(0).den_vehicle);
                jsonObject.put("tar", dataCustomerCG.get(0).tar);
                jsonObject.put("nroveh", dataCustomerCG.get(0).nroveh);
                jsonObject.put("cliente", dataCustomerCG.get(0).den);
                jsonObject.put("rfc", dataCustomerCG.get(0).rfc);
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
        } catch (ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException | JSONException e) {
            logCE.EscirbirLog2(getApplicationContext(),"Credito_FillCustomerNFC - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
    }
    /*Validaciones del flotillero*/
    private Boolean ValidacionCarga()  {
        boolean resdia = true, resestacion = true, reshora = true, resestado = true;
        try {
            JSONObject dia= new JSONObject();
            if(GetData(variables.METODO).equals(variables.KEY_NOMBRE)){
                dia = validacionFlotillero.validar_dia(mContext, GetData(variables.KEY_CLIENTE_VEHICULO_TAR),
                        GetData(variables.METODO));
            }else {
                dia = validacionFlotillero.validar_dia(mContext, GetData(variables.KEY_TAG),
                        GetData(variables.METODO));
            }
            ArrayList<Integer> dias_carga = (ArrayList<Integer>) dia.get("Array");
            if (!ValidacionCargaDia(dias_carga, dia)){
                resdia = false;
            }
            if (!ValidacionCargaEstacion(dia)){
                resestacion = false;
            }
            if(!ValidacionCargaHora(dia)){
                reshora = false;
            }
            if (!ValidacionCargaEstado(dia)){
                resestado = false;
            }
            ValidacionCargaProducto(dia);
            if (!resdia || !resestacion || !reshora || !resestado ){
                return false;
            }else {
                return true;
            }
        } catch (ClassNotFoundException | JSONException | SQLException | InstantiationException |
                IllegalAccessException e) {
            logCE.EscirbirLog2(getApplicationContext(),"Credito_ValidacionCarga - " + e);
            new AlertDialog.Builder(Credito.this)
                    .setIcon(icon)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
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
}
