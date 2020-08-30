package cg.ce.app.chris.com.cgce;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

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
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cg.ce.app.chris.com.cgce.Printing.TicketPrint;

public class ActivityTicket extends AppCompatActivity implements View.OnClickListener, com.epson.epos2.printer.ReceiveListener {
    /*Elementos Graficos*/
    TextView nrotrn, prd, cant, precio, monto;
    ImageButton print;
    Spinner spn_metodo, spn_metodo_den;
    /*Elementos WiFi, Bluethoot y dispositivo*/
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    /*Objetos*/
    JSONObject ticket = new JSONObject();
    cgticket cg = new cgticket();
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


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sensores.bluetooth();
        sensores.wifi(this, true);
        super.onCreate(savedInstanceState);
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
        requestRuntimePermission();
        mContext = this;
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
            try {
                ticket = cg.consulta_servicio(this, bundle.getString("bomba"));
                ticket.put("nip", cg.nip_desp(getApplicationContext()));
                nrotrn.setText(ticket.getString("nrotrn") + "0");
                prd.setText(ticket.getString("producto"));
                cant.setText("LTS " + String.valueOf(formateador4.format(ticket.getDouble("cantidad"))));
                precio.setText("$ " + String.format("%.2f", Double.valueOf(String.valueOf(formateador2.format(ticket.getDouble("precio"))))));
                monto.setText("$ " + String.valueOf(formateador2.format(ticket.getDouble("total"))));

            } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException | JSONException e) {
                new AlertDialog.Builder(ActivityTicket.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok, null).show();
                logCE.EscirbirLog2(getApplicationContext(),"ActivityTicket_OnCreate - " + e);
                e.printStackTrace();
            }
        }

        spn_metodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                view_spn_metodo_den();
                try {
                    fill_spn_metodo_den();
                } catch (ClassNotFoundException | SQLException | InstantiationException | JSONException | IllegalAccessException e) {
                    new AlertDialog.Builder(ActivityTicket.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    logCE.EscirbirLog2(getApplicationContext(),"ActivityTicket_OnCreate - " + e);
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
            tpv = cg.getTPVs(this, "1");
            ArrayAdapter MonederoAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, tpv);
            MonederoAdapter.setDropDownViewResource(R.layout.spinner_tiptrn);
            spn_metodo_den.setAdapter(MonederoAdapter);
        } else if (spn_metodo.getSelectedItem().toString().equals("Monederos")) {
            tpv = cg.getTPVs(this, "0");
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

                pdLoading = new ProgressDialog(ActivityTicket.this);
                pdLoading.setMessage("Imprimiendo..."); // Setting Message
                pdLoading.setTitle(flag_brand); // Setting Title
                pdLoading.setIcon(image);
                pdLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                pdLoading.show(); // Display Progress Dialog
                pdLoading.setCancelable(false);

                new Thread(new Runnable() {
                    public void run() {
                        PrintReceip();
                        pdLoading.dismiss();
                    }
                }).start();

        }
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {

                if (code == 0) {
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
            logCE.EscirbirLog2(getApplicationContext(),"ActivityTicket_initializeObject - " + e);
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
            ticket.put("rut", tur);
            ticket.put("tiptrn", tiptrn);
            int impreso = cg.cant_impreso(getApplicationContext(), ticket.getString("nrotrn"));
            if (impreso == 10) {
                Log.w("ticket con tiptrn", ticket.toString());
                runOnUiThread(new Runnable() {
                    public synchronized void run() {
                        try {
                            cg.guardarnrotrn(getApplicationContext(), ticket, 1);
                        } catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException | JSONException e) {
                            new AlertDialog.Builder(ActivityTicket.this)
                                    .setTitle(R.string.error)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok, null).show();
                            e.printStackTrace();
                        }
                    }
                });

            }
            if (!runPrintReceiptSequence()) {
                updateButtonState(true);
                if (pdLoading != null) {
                    pdLoading.dismiss();
                }
                if (!state_error) {
                    cg.actualizar_cant_impreso(getApplicationContext(), ticket.getString("nrotrn"));
                    Intent intent = new Intent(ActivityTicket.this, VentaActivity.class);
                    startActivity(intent);
                }
            }

        } catch ( final ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException | JSONException | Epos2Exception | WriterException e) {
            if (pdLoading != null) {
                pdLoading.dismiss();
            }
            logCE.EscirbirLog2(getApplicationContext(),"ActivityTicket_PrintReceip - " + e);
            runOnUiThread(new Runnable() {
                public synchronized void run() {
                    new AlertDialog.Builder(ActivityTicket.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    updateButtonState(true);
                }
            });
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
        JSONObject datos_domicilio = cg.estacion_domicilio(mContext);
        JSONObject vehiculo = new JSONObject();
        String titulo = "", folio_impreso = "", cliente = "", venta = "", tpv = "";
        Log.w("ticket", ticket.getString("nrotrn"));
        String metodoPago = "";
        ticket.put("impreso", cg.cant_impreso(getApplicationContext(), ticket.getString("nrotrn")));
        if (ticket.getInt("impreso") == 0 || ticket.getInt("impreso") == 10) {
            titulo = "O R I G I N A L";
            metodoPago = ticket.getString("rut");
            folio_impreso = ticket.getString("nrotrn") + "0";
            if ( spn_metodo.getSelectedItem().toString().equals("T. Credito") ||
                    spn_metodo.getSelectedItem().toString().equals("T. Debito")){
                flag_TicketImpreso= false;
            }else {
                flag_TicketImpreso=true;
            }

        } else if (ticket.getInt("impreso") == 1) {
            titulo = "C O P I A";
            folio_impreso = "C O P I A";
            metodoPago = cg.get_rut(mContext, ticket);
            flag_TicketImpreso = true;
        }
        Log.w("ticket", ticket.toString());
        if (ticket.getInt("codcli") != 0) {
            vehiculo = cg.get_vehiculo(mContext, ticket.getString("nrotrn"), ticket.getString("bomba"));
            cliente = ticket.getString("dencli");
            Log.w("vehiculo", vehiculo.toString());
            venta = "CREDITO";
        } else {
            vehiculo = cg.get_vehiculo(mContext, ticket.getString("nrotrn"), ticket.getString("bomba"));

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
        textData.append(ticket.getString("cveest") + "\n");
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
        textData.append(cliente + "\n");
        textData.append(metodoPago + "\n");
        //textData.append("\n");
        if (ticket.has("codcli")) {
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
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("TICKET    : " + folio_impreso + "   BOMBA : " + String.valueOf(ticket.getInt("bomba")) + "\n");
        textData.append("FECHA: " + ticket.getString("fecha") + "  HORA: " + ticket.getString("hora") + "\n");
        textData.append("VENDEDOR  : " + String.valueOf(ticket.getString("despachador")).toUpperCase() + "\n");
        textData.append("PRECIO    : $ " + String.format("%.2f", Double.valueOf(formateador2.format(ticket.getDouble("precio")))) + "\n");
        textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(ticket.getDouble("cantidad")))) + " LITROS " + String.valueOf(ticket.getString("producto")).toUpperCase() + "\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("IMPORTE   : $ " + formateador2.format(ticket.getDouble("total")) + "\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append(letra.Convertir(String.valueOf(formateador21.format(ticket.getDouble("total"))), true) + "\n");
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

        if (ticket.getInt("impreso") == 0 || ticket.getInt("impreso") == 10) {
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
            textData.append(ticket.getString("cveest") + "\n");
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
            textData.append(cliente + "\n");
            textData.append(metodoPago + "\n");
            //textData.append("\n");
            if (ticket.has("codcli")) {
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
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("TICKET    : " + "C O P I A" + "   BOMBA : " + String.valueOf(ticket.getInt("bomba")) + "\n");
            textData.append("FECHA: " + ticket.getString("fecha") + "  HORA: " + ticket.getString("hora") + "\n");
            textData.append("VENDEDOR  : " + String.valueOf(ticket.getString("despachador")).toUpperCase() + "\n");
            textData.append("PRECIO    : $ " + String.format("%.2f", Double.valueOf(formateador2.format(ticket.getDouble("precio")))) + "\n");
            textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(ticket.getDouble("cantidad")))) + " LITROS " + String.valueOf(ticket.getString("producto")).toUpperCase() + "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("IMPORTE   : $ " + formateador2.format(ticket.getDouble("total")) + "\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append(letra.Convertir(String.valueOf(formateador21.format(ticket.getDouble("total"))), true) + "\n");
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
        if (ticket.getInt("impreso") == 0 || ticket.getInt("impreso") == 10) {
            if (sorteo > 0) {
                if (ticket.getDouble("total") >= 200) {
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
                    textData.append("                FECHA   :   " + ticket.getString("fecha") + "\n");
                    textData.append("                MONTO   :   " + formateador2.format(ticket.getDouble("total")) + "\n");
                    textData.append("             PRODUCTO   :   " + ticket.getString("producto") + "\n");
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
                    String qr_cadena = "URL: https://ganaconcombu.com?es=" + ticket.getString("cveest") + "&fo=" + String.valueOf(folio_impreso) + "&fe=" + ticket.getString("fecha") + "&mo=" + formateador2.format(ticket.getDouble("total")) + "&pr=" + ticket.getString("codprd");
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
        Double ieps = jsticket.getDouble("ieps");
        Double iva_factor = jsticket.getDouble("iva");
        Double lts = jsticket.getDouble("cantidad");
        Double pre = jsticket.getDouble("precio");
        Double ivaentre = Double.valueOf("1" + iva_factor);
        Double precioneto = ((Double.valueOf(pre) - ieps) / ivaentre) * 100;
        Double iva = ((precioneto * lts) * iva_factor) / 100;
        Double subtotal = (precioneto + ieps) * lts;
        String precio_total = String.valueOf(precioneto + ieps);
        String substr = ".";
        String inicio = precio_total.substring(0, precio_total.indexOf(substr));
        String fin = precio_total.substring(precio_total.indexOf(substr) + substr.length());
        String precio_impresion = inicio + "." + fin.substring(0, 2);
        String qr = "COMBUGO|" + jsticket.getString("cveest") + "|" + jsdomicilio.getString("estacion") + "|" + jsticket.getString("fecha") + "|" + jsticket.getString("hora") + "|" + precio_impresion + "|" + formateador2.format(iva) + "|" + jsticket.getString("total") + "|" + jsticket.getString("nrotrn") + "|";
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
            logCE.EscirbirLog2(getApplicationContext(),"ActivityTicket_printData - " + e);
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
            logCE.EscirbirLog2(getApplicationContext(),"ActivityTicket_connectPrinter - " + e);
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
                                logCE.EscirbirLog2(getApplicationContext(),"ActivityTicket_disconnectPrinter - " + e);
                                ShowMsg.showException(e, "disconnect", mContext);
                            }
                        });
                        break;
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            logCE.EscirbirLog2(getApplicationContext(),"ActivityTicket_disconnectPrinter - " + e);
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


    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> requestPermissions = new ArrayList<>();

        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
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
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }
}

