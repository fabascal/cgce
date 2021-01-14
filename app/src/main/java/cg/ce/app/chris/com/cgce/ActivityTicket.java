package cg.ce.app.chris.com.cgce;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.ControlGasListener;
import cg.ce.app.chris.com.cgce.ControlGas.GetEstacionData;
import cg.ce.app.chris.com.cgce.ControlGas.GetImpreso;
import cg.ce.app.chris.com.cgce.ControlGas.GetTPVs;
import cg.ce.app.chris.com.cgce.ControlGas.GetTicket;
import cg.ce.app.chris.com.cgce.ControlGas.GetVehicleData;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetEstacionDataListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetImpresoListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetVehicleDataListener;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.UpdateNrotrnListener;
import cg.ce.app.chris.com.cgce.ControlGas.PutImpreso;
import cg.ce.app.chris.com.cgce.ControlGas.UpdateNrotrn;
import cg.ce.app.chris.com.cgce.common.RequestPermission;
import cg.ce.app.chris.com.cgce.common.Variables;

public class ActivityTicket extends AppCompatActivity implements View.OnClickListener,
        com.epson.epos2.printer.ReceiveListener, GetImpresoListener, UpdateNrotrnListener,
        GetEstacionDataListener, GetVehicleDataListener {
    /*Elementos Graficos*/
    TextView nrotrn, prd, cant, precio, monto;
    ImageButton print;
    Spinner spn_metodo, spn_metodo_den;
    /*Elementos WiFi, Bluethoot y dispositivo*/
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    /*Objetos*/
    JSONObject ticket = new JSONObject();
    List tpv;
    /*Variables de actividad*/
    String event_ticket, evento_sorteo, tur, flag_brand;
    int tiptrn;
    Integer flag = 0;
    String event;
    boolean state_btn, state_error = false, flag_TicketImpreso;
    /*Elementos de formato*/
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    /*Elementos de SDK Epson*/
    private Printer mPrinter = null;
    private Context mContext = null;
    private static final int REQUEST_PERMISSION = 100;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds
    /*Elemento Progress para imprimir*/
    ProgressDialog pdLoading;
    Drawable image;
    LogCE logCE = new LogCE();
    Variables variables = new Variables();
    MacActivity mac = new MacActivity();
    RequestPermission requestPermission = new RequestPermission();
    JSONObject datos_domicilio;
    JSONObject vehiculo;
    Activity mActivity;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sensores.bluetooth();
        sensores.wifi(this, true);
        super.onCreate(savedInstanceState);
        OnCreateScreen();
        requestPermission.requestRuntimePermission(this);
        mContext = this;
        mActivity=this;
        nrotrn = findViewById(R.id.nrotrn);
        prd = findViewById(R.id.prd);
        cant = findViewById(R.id.cant);
        precio = findViewById(R.id.precio);
        monto = findViewById(R.id.monto);
        print = findViewById(R.id.print_ticket);
        spn_metodo = findViewById(R.id.spn_metedo);
        spn_metodo_den = findViewById(R.id.spn_metodo_den);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.mPagos_array, android.R.layout.simple_spinner_item);

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_tiptrn);
// Apply the adapter to the spinner
        spn_metodo.setAdapter(adapter);

        print.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle.getString("bomba") != null) {
            /*Funcion para obtener la data de la estacion, necesaria para el formato de impresion*/
            GetEstacionData getEstacionData = new GetEstacionData(this, getApplicationContext());
            getEstacionData.delegate = this;
            getEstacionData.execute();
            new GetTicket(this, new ControlGasListener() {
                @Override
                public void processFinish(JSONObject output) {
                    try {
                        if( output.getInt(Variables.CODE_ERROR)==0){
                            String textnrotrn = null;
                            ticket = output;

                            textnrotrn = ticket.getString(Variables.KEY_TICKET_NROTRN) + "0";
                            nrotrn.setText(textnrotrn);
                            prd.setText(ticket.getString(Variables.KEY_TICKET_PRODUCTO));
                            String textcant = "LTS " + formateador4.format(ticket.getDouble(
                                    Variables.KEY_TICKET_CANTIDAD));
                            cant.setText(textcant);
                            String textprecio = "$ "+ formateador2.format(ticket.getDouble(Variables.KEY_TICKET_PRECIO));
                            precio.setText(textprecio);
                            String textmonto = "$ " + formateador2.format
                                    (ticket.getDouble(Variables.KEY_TICKET_TOTAL));
                            monto.setText(textmonto);
                        }else{
                            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                    stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                            new AlertDialog.Builder(ActivityTicket.this)
                                    .setTitle(R.string.error)
                                    .setIcon(image)
                                    .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                    .setPositiveButton(R.string.btn_ok, null).show();
                        }
                    } catch (JSONException e) {
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + e);
                        new AlertDialog.Builder(ActivityTicket.this)
                                .setTitle(R.string.error)
                                .setIcon(image)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok, null).show();
                        e.printStackTrace();
                    }

                }
            }).execute(bundle.getString("bomba"),mac.getMacAddress(),"0");
        }

        spn_metodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                view_spn_metodo_den();
                try {
                    fill_spn_metodo_den();
                } catch (ClassNotFoundException | SQLException | InstantiationException |
                        JSONException | IllegalAccessException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    new AlertDialog.Builder(ActivityTicket.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
        /*Se inicializa el objeto impresora*/
        initializeObject();
        pdLoading = new ProgressDialog(ActivityTicket.this);

    }

    public void fill_spn_metodo_den() throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        if (spn_metodo.getSelectedItem().toString().equals("T. Credito") || spn_metodo.
                getSelectedItem().toString().equals("T. Debito")) {
            new GetTPVs(this, getApplicationContext(), new ControlGasListener() {
                @Override
                public void processFinish(JSONObject output) {
                    try {
                        if (output.getInt(Variables.CODE_ERROR)==0){
                            tpv = (List) output.get(Variables.TPV_LIST);
                            ArrayAdapter MonederoAdapter = new ArrayAdapter(getApplicationContext(),
                                    android.R.layout.simple_spinner_item, tpv);
                            MonederoAdapter.setDropDownViewResource(R.layout.spinner_tiptrn);
                            spn_metodo_den.setAdapter(MonederoAdapter);
                        }else{
                            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                    stacktraceObj[2].getMethodName() + "|" +
                                    output.getString(Variables.MESSAGE_ERROR));
                            new AlertDialog.Builder(ActivityTicket.this)
                                    .setTitle(R.string.error)
                                    .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                    .setPositiveButton(R.string.btn_ok, null).show();
                        }
                    } catch (JSONException e) {
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + e);
                        new AlertDialog.Builder(ActivityTicket.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok, null).show();
                        e.printStackTrace();
                    }
                }
            }).execute("1");

        } else if (spn_metodo.getSelectedItem().toString().equals("Monederos")) {
            tpv = null;
            ArrayAdapter MonederoAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, tpv);
            MonederoAdapter.setDropDownViewResource(R.layout.spinner_tiptrn);
            spn_metodo_den.setAdapter(MonederoAdapter);
        }

    }

    public void view_spn_metodo_den() {
        if (spn_metodo.getSelectedItem().toString().equals("T. Credito") || spn_metodo.
                getSelectedItem().toString().equals("T. Debito") || spn_metodo.
                getSelectedItem().toString().equals("Monederos")) {
            spn_metodo_den.setVisibility(View.VISIBLE);
        } else {
            spn_metodo_den.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.print_ticket:

                /*pdLoading = new ProgressDialog(ActivityTicket.this);
                pdLoading.setMessage("Imprimiendo..."); // Setting Message
                pdLoading.setTitle(flag_brand); // Setting Title
                pdLoading.setIcon(image);
                pdLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                pdLoading.show(); // Display Progress Dialog
                pdLoading.setCancelable(false);*/
                PrintReceip();
                /*new Thread(new Runnable() {
                    public void run() {

                        pdLoading.dismiss();
                    }
                }).start();*/

        }
    }
    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status,
                             final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                Log.w("Code", String.valueOf(code));
                if (code == 0) {
                    try {
                        new PutImpreso(ActivityTicket.this, new ControlGasListener() {
                            @Override
                            public void processFinish(JSONObject output) {
                                try {
                                    if ( output.getInt( Variables.CODE_ERROR )==1){

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).execute(ticket.getString(Variables.KEY_TICKET_NROTRN));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getApplicationContext(), VentaActivity.class);
                    startActivity(intent);
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
    private void updateButtonState(final boolean state) {
        state_btn = state;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                print.setEnabled(state_btn);
            }
        });
    }
    private void PrintReceip(){
        this.updateButtonState(false);
        if (spn_metodo.getSelectedItem().toString().equals("Efectivo")) {
            tur = "1|Efectivo";
            tiptrn = 49;
        } else if (spn_metodo.getSelectedItem().toString().equals("T. Credito")) {
            tur = "2|T. Credito" + "|" + spn_metodo_den.getSelectedItem().toString();
            tiptrn = 51;
        } else if (spn_metodo.getSelectedItem().toString().equals("T. Debito")) {
            tur = "3|T. Debito" + "|" + spn_metodo_den.getSelectedItem().toString();
            tiptrn = 51;
        } else if (spn_metodo.getSelectedItem().toString().equals("Anticipos")) {
            tur = "4|Anticipos";
            tiptrn = 50;
        } else if (spn_metodo.getSelectedItem().toString().equals("Combu-Vale")) {
            tur = "5|Combu-Vale";
            tiptrn = 50;
        } else if (spn_metodo.getSelectedItem().toString().equals("Monederos")) {
            tur = "6|Monederos" + "|" + spn_metodo_den.getSelectedItem().toString();
            tiptrn = 51;
        }
        try {
            ticket.put(variables.KEY_TIPTRN, tiptrn);
            ticket.put(variables.KEY_RUT, tur);
            /*Funcion para obtener la data del vehiculo, necesaria para el formato de impresion*/
            GetVehicleData getVehicleData = new GetVehicleData(this, getApplicationContext());
            getVehicleData.delegate= this;
            getVehicleData.execute(ticket.getString(Variables.KEY_TICKET_NROTRN),ticket.getString(Variables.KEY_TICKET_BOMBA));
            /*Funcion para obtener la validacion de impresion*/
            GetImpreso getImpreso = new GetImpreso(this, getApplicationContext());
            getImpreso.delegate=this;
            getImpreso.execute(ticket.getString(Variables.KEY_TICKET_NROTRN));

        } catch ( final JSONException e) {
            if (pdLoading != null) {
                pdLoading.dismiss();
            }
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(ActivityTicket.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            updateButtonState(true);
            e.printStackTrace();
        }
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

    public boolean createReceiptData() throws Epos2Exception, ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException, WriterException {
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.logo_impresion);
        switch (flag_brand) {
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
        String metodoPago = "";
        String titulo = "", folio_impreso = "", cliente = "", venta = "", tpv = "";
        System.out.println("metodopago GetImpresoFinish" + ticket);

        if (ticket.getInt(Variables.KEY_IMPRESO) == 10) {
            System.out.println("metodopago ticket nuevo");
            metodoPago = tur;
        }else {
            if (ticket.has(Variables.KEY_TICKET_CODCLI)) {
                System.out.println("metodopago tiene codcli");
                System.out.println("metodopago codcli"+ ticket.getInt(Variables.KEY_TICKET_CODCLI));
                if (ticket.getInt(Variables.KEY_TICKET_CODCLI) > 0) {
                    System.out.println("metodopago tiene codcli mayor a 0");
                    metodoPago = ticket.getString(Variables.KEY_TICKET_CLIENTE_TIPVAL_DEN);
                } else {
                    System.out.println("metodopago tiene codcli menor a 0");
                    if (ticket.has(Variables.KEY_RUT_CG)) {
                        metodoPago = ticket.getString(variables.KEY_RUT_CG);
                    }
                }
            }
        }
        System.out.println("metodopago"+metodoPago);
        if (ticket.getInt(Variables.KEY_IMPRESO) == 0 || ticket.getInt(Variables.KEY_IMPRESO) == 10) {
            titulo = "O R I G I N A L";
            folio_impreso = ticket.getString(variables.KEY_TICKET_NROTRN) + "0";
            if ( spn_metodo.getSelectedItem().toString().equals("T. Credito") ||
                    spn_metodo.getSelectedItem().toString().equals("T. Debito")){
                flag_TicketImpreso= false;
            }else {
                flag_TicketImpreso=true;
            }
            System.out.println("ticket validar" + ticket);
        } else if (ticket.getInt(variables.KEY_IMPRESO) == 1) {
            titulo = "C O P I A";
            folio_impreso = "C O P I A";

            flag_TicketImpreso = true;
        }

        Log.w("ticket", ticket.toString());
        if (ticket.getInt(variables.KEY_TICKET_CODCLI) != 0) {
            /*vehiculo = cg.get_vehiculo(mContext, ticket.getString(variables.KEY_TICKET_NROTRN), ticket.getString(variables.KEY_TICKET_BOMBA));*/
            cliente = ticket.getString(variables.KEY_TICKET_DENCLI);

        } else {
            /*vehiculo = cg.get_vehiculo(mContext, ticket.getString(variables.KEY_TICKET_NROTRN), ticket.getString(variables.KEY_TICKET_BOMBA));*/

            if (ticket.has("tpv")) {
                if (ticket.getJSONObject("tpv").has("nombre")) {
                    tpv = ticket.getJSONObject("tpv").getString("nombre");
                }
            }
            cliente = "Publico General";
            venta = "CONTADO" + " " + tpv;
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
        textData.append("\n");
        //textData.append("REPSOL"+"\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append("\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append(ticket.getString(variables.KEY_TICKET_CVEEST) + "\n");
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
        textData.append(cliente + "\n");
        textData.append(metodoPago + "\n");
        //textData.append("\n");
        Log.w("ticket", String.valueOf(ticket));
        if (ticket.has(Variables.KEY_TICKET_CODCLI)) {
            if(ticket.getInt(Variables.KEY_TICKET_CODCLI)>0) {
                if (vehiculo.has("rsp")) {
                    textData.append("Conductor     : " + vehiculo.getString("rsp") + "\n");
                }
                if (vehiculo.has("nroeco")) {
                    textData.append("No. Econ.     : " + vehiculo.getString("nroeco") + "\n");
                }
                if (vehiculo.has("placa")) {
                    textData.append("Placas        : " + vehiculo.getString("placa") + "\n");
                }
                if (vehiculo.has("ultodm")) {
                    textData.append("Kilometraje   : " + vehiculo.getString("ultodm") + "\n");
                }
                textData.append("------------------------------\n");
            }
        }
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("TICKET    : " + folio_impreso + "   BOMBA : " + ticket.getInt(variables.KEY_TICKET_BOMBA) + "\n");
        textData.append("FECHA: " + ticket.getString(variables.KEY_TICKET_FECHA) + "  HORA: " + ticket.getString(variables.KEY_TICKET_HORA) + "\n");
        textData.append("VENDEDOR  : " + String.valueOf(ticket.getString(variables.KEY_TICKET_DESPACHADOR)).toUpperCase() + "\n");
        textData.append("PRECIO    : $ " + String.format("%.2f", Double.valueOf(formateador2.format(ticket.getDouble(variables.KEY_TICKET_PRECIO)))) + "\n");
        textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(ticket.getDouble(variables.KEY_TICKET_CANTIDAD)))) + " LITROS " + String.valueOf(ticket.getString(variables.KEY_TICKET_PRODUCTO)).toUpperCase() + "\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("IMPORTE   : $ " + formateador2.format(ticket.getDouble(variables.KEY_TICKET_TOTAL)) + "\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append(letra.Convertir(String.valueOf(formateador21.format(ticket.getDouble(variables.KEY_TICKET_TOTAL))), true) + "\n");
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

        if (ticket.getInt(variables.KEY_IMPRESO) == 0 || ticket.getInt(variables.KEY_IMPRESO) == 10) {
            Bitmap qrrespol = null;
            QRCodeEncoder qrCodeEncoder1 = new QRCodeEncoder(repsolQR(ticket, datos_domicilio),
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
        Log.w("flag impreso", String.valueOf(flag_TicketImpreso));
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
            textData.append(ticket.getString(variables.KEY_TICKET_CVEEST) + "\n");
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
            textData.append("***** " + "C O P I A" + " *****" + "\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append("\n");
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            textData.append(cliente + "\n");
            textData.append(metodoPago + "\n");
            //textData.append("\n");
            Log.w("ticket", String.valueOf(ticket));
            if (ticket.has(Variables.KEY_TICKET_CODCLI)) {
                if(ticket.getInt(Variables.KEY_TICKET_CODCLI)>0) {
                    if (vehiculo.has("rsp")) {
                        textData.append("Conductor     : " + vehiculo.getString("rsp") + "\n");
                    }
                    if (vehiculo.has("nroeco")) {
                        textData.append("No. Econ.     : " + vehiculo.getString("nroeco") + "\n");
                    }
                    if (vehiculo.has("placa")) {
                        textData.append("Placas        : " + vehiculo.getString("placa") + "\n");
                    }
                    if (vehiculo.has("ultodm")) {
                        textData.append("Kilometraje   : " + vehiculo.getString("ultodm") + "\n");
                    }
                    textData.append("------------------------------\n");
                }
            }
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("TICKET    : " + "C O P I A" + "   BOMBA : " + String.valueOf(ticket.getInt(variables.KEY_TICKET_BOMBA)) + "\n");
            textData.append("FECHA: " + ticket.getString(variables.KEY_TICKET_FECHA) + "  HORA: " + ticket.getString(variables.KEY_TICKET_HORA) + "\n");
            textData.append("VENDEDOR  : " + String.valueOf(ticket.getString(variables.KEY_TICKET_DESPACHADOR)).toUpperCase() + "\n");
            textData.append("PRECIO    : $ " + String.format("%.2f", Double.valueOf(formateador2.format(ticket.getDouble(variables.KEY_TICKET_PRECIO)))) + "\n");
            textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(ticket.getDouble(variables.KEY_TICKET_CANTIDAD)))) + " LITROS " + String.valueOf(ticket.getString(variables.KEY_TICKET_PRODUCTO)).toUpperCase() + "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("IMPORTE   : $ " + formateador2.format(ticket.getDouble(variables.KEY_TICKET_TOTAL)) + "\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append(letra.Convertir(String.valueOf(formateador21.format(ticket.getDouble(variables.KEY_TICKET_TOTAL))), true) + "\n");
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
        if (ticket.getInt(variables.KEY_IMPRESO) == 0 || ticket.getInt(variables.KEY_IMPRESO) == 10) {
            if (sorteo > 0) {
                if (ticket.getDouble(variables.KEY_TICKET_TOTAL) >= 200) {
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
                    textData.append("                FECHA   :   " + ticket.getString(variables.KEY_TICKET_FECHA) + "\n");
                    textData.append("                MONTO   :   " + formateador2.format(ticket.getDouble(variables.KEY_TICKET_TOTAL)) + "\n");
                    textData.append("             PRODUCTO   :   " + ticket.getString(variables.KEY_TICKET_PRODUCTO) + "\n");
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
                    String qr_cadena = "URL: https://ganaconcombu.com?es=" + ticket.getString(variables.KEY_TICKET_CVEEST)
                            + "&fo=" + String.valueOf(folio_impreso) + "&fe=" + ticket.getString(variables.KEY_TICKET_FECHA)
                            + "&mo=" + formateador2.format(ticket.getDouble(variables.KEY_TICKET_TOTAL)) + "&pr=" + ticket.getString(variables.KEY_TICKET_CODPRD);
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

    public String repsolQR(JSONObject jsticket, JSONObject jsdomicilio) throws JSONException {
        Double ieps = jsticket.getDouble(variables.KEY_TICKET_IEPS);
        Double iva_factor = jsticket.getDouble(variables.KEY_TICKET_IVA);
        Double lts = jsticket.getDouble(variables.KEY_TICKET_CANTIDAD);
        Double pre = jsticket.getDouble(variables.KEY_TICKET_PRECIO);
        Double ivaentre = Double.valueOf("1" + iva_factor);
        Double precioneto = ((Double.valueOf(pre) - ieps) / ivaentre) * 100;
        Double iva = ((precioneto * lts) * iva_factor) / 100;
        Double subtotal = (precioneto + ieps) * lts;
        String precio_total = String.valueOf(precioneto + ieps);
        String substr = ".";
        String inicio = precio_total.substring(0, precio_total.indexOf(substr));
        String fin = precio_total.substring(precio_total.indexOf(substr) + substr.length());
        String precio_impresion = inicio + "." + fin.substring(0, 2);
        String qr = "COMBUGO|" + jsticket.getString(variables.KEY_TICKET_CVEEST) + "|" +
                jsdomicilio.getString("estacion") + "|" +
                jsticket.getString(variables.KEY_TICKET_FECHA) + "|" + jsticket.getString(variables.KEY_TICKET_HORA) +
                "|" + precio_impresion + "|" + formateador2.format(iva) + "|" +
                jsticket.getString(variables.KEY_TICKET_TOTAL) + "|" + jsticket.getString(variables.KEY_TICKET_NROTRN) + "|";
        return qr;
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
            state_error = true;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.CAMERA)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.BLUETOOTH)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }
    @SuppressLint("SourceLockedOrientationActivity")
    private void OnCreateScreen(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express")) {
            case "Combu-Express":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_ticket);
                flag_brand = "Combu-Express";
                image = getDrawable(R.drawable.logo_impresion);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_ticket_repsol);
                flag_brand = "Repsol";
                image = getDrawable(R.drawable.isologo_repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_ticket_ener);
                flag_brand = "Ener";
                image = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_ticket_total);
                flag_brand = "Total";
                image = getDrawable(R.drawable.total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void GetImpresoFinish(JSONObject output) {
        try {
            if (output.getInt(Variables.CODE_ERROR)==0){
                ticket.put(Variables.KEY_IMPRESO,output.getInt(Variables.KEY_IMPRESO));
                if (ticket.getInt(Variables.KEY_IMPRESO)==10){
                    UpdateNrotrn updateNrotrn = new UpdateNrotrn(this, getApplicationContext(), mac.getMacAddress(), "1");
                    updateNrotrn.delegate = this;
                    updateNrotrn.execute(ticket);
                }
                /*impresion*/
                ExecutePrint executePrint = new ExecutePrint();
                executePrint.execute();

            }else{
                updateButtonState(true);
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" +
                        output.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(ActivityTicket.this)
                        .setTitle(R.string.error)
                        .setMessage(output.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void UpdateNrotrnFinish(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt(Variables.CODE_ERROR)==1){
                updateButtonState(true);
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" +
                        jsonObject.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(ActivityTicket.this)
                        .setTitle(R.string.error)
                        .setMessage(jsonObject.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void GetEstacionDataFinish(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt(Variables.CODE_ERROR)==0) {
                datos_domicilio = jsonObject;
            }else{
                updateButtonState(true);
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" +
                        jsonObject.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(ActivityTicket.this)
                        .setTitle(R.string.error)
                        .setMessage(jsonObject.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void GetVehicleDataFinish(JSONObject jsonObject) {
        System.out.println("GetVehicleDataFinish"+jsonObject);
        try {
            if (jsonObject.getInt(Variables.CODE_ERROR)==0){
                vehiculo = jsonObject;
            }else{
                updateButtonState(true);
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" +
                        jsonObject.getString(Variables.MESSAGE_ERROR));
                new AlertDialog.Builder(ActivityTicket.this)
                        .setTitle(R.string.error)
                        .setMessage(jsonObject.getString(Variables.MESSAGE_ERROR))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public class  ExecutePrint extends AsyncTask<String,Void, JSONObject> {
        JSONObject result = new JSONObject();
        @Override
        protected void onPreExecute() {
            pdLoading = new ProgressDialog(ActivityTicket.this);
            pdLoading.setMessage("Imprimiendo..."); // Setting Message
            pdLoading.setTitle(flag_brand); // Setting Title
            pdLoading.setIcon(image);
            pdLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            pdLoading.show(); // Display Progress Dialog
            pdLoading.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                if (!runPrintReceiptSequence()) {
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
                new Thread(){
                    public void run(){
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(ActivityTicket.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok, null).show();
                            }
                        });
                    }
                }.start();
            }
            return result;
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

