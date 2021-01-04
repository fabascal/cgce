package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import cg.ce.app.chris.com.cgce.ControlGas.Listeners.ControlGasListener;
import cg.ce.app.chris.com.cgce.ControlGas.UpdateCFDi;
import cg.ce.app.chris.com.cgce.Facturacion.Utils.JsonFromString;
import cg.ce.app.chris.com.cgce.common.Variables;


public class EmisionCfdi extends AppCompatActivity implements View.OnClickListener, CfdiResultListener, ReceiveListener {
    TextView tvrazon, tvrfc, tvdomicilio, tvcolonia, tvestado, tvciudad, tvcp, tvfolio, tvvendedor,
            tvprecio, tvvolumen, tvimporte, tvmetodo, tvcuenta, tvproducto;
    Button btnwebservice;
    JSONObject cfdienvio = new JSONObject();
    JSONObject Productos = new JSONObject();
    JSONObject ProductoArray = new JSONObject();
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
    JSONObject cursor ;
    Drawable icon;
    JsonFromString json = new JsonFromString();
    /*Elemento Progress para imprimir*/
    ProgressDialog pdLoading;
    boolean state_btn;
    Integer flag = 0;
    JSONObject ticket;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds


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
            Log.w("log", String.valueOf(cfdienvio.getJSONObject("data").getJSONArray("productos").get(0)));
            Productos = new JSONObject(String.valueOf(cfdienvio.getJSONObject("data").getJSONArray("productos").get(0)));
            Log.w("json", String.valueOf(cfdienvio));
            tvfolio.setText("Folio                   : " + Productos.getInt("nrotrn")+"0");
            tvvendedor.setText("Despachador    : " + cfdienvio.getJSONObject("data").getString("despachador"));
            tvprecio.setText("Precio                : " + formateador2.format(Productos.getDouble("precio_unitario")));
            tvvolumen.setText("Cantidad            : " + formateador4.format(Productos.getDouble("cantidad")));
            tvimporte.setText("Importe              : " + formateador2.format(Productos.getDouble("importe")));
            tvrazon.setText("Razon          : " + cfdienvio.getString("razon_social"));
            tvrfc.setText("R.F.C.           : " + cfdienvio.getString("RFC"));
            tvdomicilio.setText("Calle             : " + cfdienvio.getString("calle") + cfdienvio.getString("exterior") +  cfdienvio.getString("interior"));
            tvcolonia.setText("Colonia        : " + cfdienvio.getString("colonia"));
            tvestado.setText("Estado         : " + cfdienvio.getString("estado"));
            tvciudad.setText("Municipio    : " + cfdienvio.getString("municipio"));
            tvcp.setText("C.P.               : " + cfdienvio.getString("cp"));
            tvmetodo.setText("Metodo        : " + cfdienvio.getJSONObject("data").getString("MetodoPagoDescripcion"));
            tvmetodo.setText("Uso CFDi      : " + cfdienvio.getJSONObject("data").getString("UsoCFDiDescripcion"));
            tvproducto.setText("Producto           : " + Productos.getString("descripcion"));
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
            e.printStackTrace();
        }
        btnwebservice = (Button) findViewById(R.id.btnwebservice);
        btnwebservice.setOnClickListener(this);
        /*Se inicializa el objeto impresora*/
        initializeObject();
        pdLoading = new ProgressDialog(EmisionCfdi.this);
    }
    public void PrintCFDi(JSONObject js){
        this.updateButtonState(false);
        ExecutePrint executePrint = new ExecutePrint(js);
        executePrint.execute();
    }
    private void updateButtonState(final boolean state) {
        state_btn = state;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnwebservice.setEnabled(state_btn);
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

    private boolean createReceiptData() throws JSONException, ClassNotFoundException, SQLException,
            InstantiationException, IllegalAccessException, WriterException, Epos2Exception {
        Numero_a_Letra letra = new Numero_a_Letra();
        JSONObject datos_domicilio = new JSONObject();

        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_impresion);
        switch (Bandera) {
            case "Combu-Express":
                logoData = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_impresion);
                break;
            case "Repsol":
                logoData = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_impresion_repsol);
                break;
            case "Ener":
                logoData = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_impresion_ener);
                break;
            case "Total":
                logoData = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_impresion_total);
                break;
        }
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;
        Bitmap qrcfdi=null;

        //Generar QR
        //Find screen size
        WindowManager manager = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? 300 : 300;
        String qr_cadena = null;

        ProductoArray = ticket.getJSONObject("productos");
        cgticket cg = new cgticket();
        datos_domicilio = cg.estacion_domicilio(getApplicationContext());
        qr_cadena = "?re"+ String.valueOf(ticket.getString("erfc")).toUpperCase()+
                "&rr"+ String.valueOf(ticket.getString("rrfc")).toUpperCase()+
                "&tt"+ String.valueOf(ticket.getString("total")).toUpperCase()+
                "&rr"+ String.valueOf(ticket.getString("UUID"));
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qr_cadena,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                smallerDimension);

        qrcfdi = qrCodeEncoder.encodeAsBitmap();


        if (mPrinter == null) {
            return false;
        }

        String a ="O R I G I N A L";

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
        method = "addTextAlign";
        mPrinter.addTextAlign(Printer.ALIGN_LEFT);
        //method = "addFeedLine";
        //mPrinter.addFeedLine(1);
        textData.append("\n");
        if (ticket.has("enombre")) {
            textData.append("" + ticket.getString("enombre") + "\n");
        }
        if (ticket.has("erfc")) {
            textData.append("" + ticket.getString("erfc") + "\n");
        }
        if (ticket.has("numfactura")) {
            textData.append("FACTURA: " + String.valueOf(ticket.getString("numfactura")).toUpperCase() + "\n");
        }
        if ( ticket.has("FechaTimbrado")) {
            textData.append("FECHA TIMBRADO: " + ticket.getString("FechaTimbrado") + "\n");
        }
        if ( ticket.has("fecha_emision")) {
            textData.append("FECHA EMISION: " + ticket.getString("fecha_emision") + "\n");
        }
        if ( ticket.has("calle_emisor") || ticket.has("numext_emisor")) {
            textData.append("CALLE: " + ticket.getString("calle_emisor") + " " + ticket.getString("numext_emisor") + "\n");
        }
        if ( ticket.has("colonia_emisor")) {
            textData.append("COLONIA: " + ticket.getString("colonia_emisor") + "\n");
        }
        if ( ticket.has("municipio_emisor") || ticket.has("cp_emisor")) {
            textData.append("" + ticket.getString("municipio_emisor") + " " + ticket.getString("estado_emisor") + " C.P." + ticket.getString("cp_emisor") + "\n");
        }
        if ( ticket.has("pais_emisor")) {
            textData.append("PAIS: " + ticket.getString("pais_emisor") + "\n");
        }
        textData.append("\n");
        textData.append("==========================================\n");
        if (ticket.has("cveest") || ticket.has("estacion")) {
            textData.append("ESTACION: " + ticket.getString("estacion") + " " + ticket.getString("cveest") + "\n");
        }
        if ( datos_domicilio.has("calle") || datos_domicilio.has("exterior") || datos_domicilio.has("interior") || datos_domicilio.has("colonia") || datos_domicilio.has("cp")) {
            textData.append(datos_domicilio.getString("calle") + " " + datos_domicilio.getString("exterior") + " " + datos_domicilio.getString("interior") + ", " + datos_domicilio.getString("colonia") + ", " + datos_domicilio.getString("cp") + "\n");
        }
        if ( datos_domicilio.has("localidad") || datos_domicilio.has("municipio") || datos_domicilio.has("estado") || datos_domicilio.has("pais")) {
            textData.append(datos_domicilio.getString("localidad") + ", " + datos_domicilio.getString("municipio") + ", " + datos_domicilio.getString("estado") + ", " + datos_domicilio.getString("pais") + "\n");
        }
        if ( datos_domicilio.has("rfc") || datos_domicilio.has("telefono")) {
            textData.append("RFC " + datos_domicilio.getString("rfc") + " TEL." + datos_domicilio.getString("telefono") + "\n");
        }
        if ( datos_domicilio.has("regimen")) {
            textData.append(datos_domicilio.getString("regimen") + "\n");
        }
        textData.append("==========================================\n");
        if (ticket.has("rnombre")) {
            textData.append("Cliente: " + String.valueOf(ticket.getString("rnombre")).toUpperCase() + "\n");
        }
        if (ticket.has("rrfc")) {
            textData.append("R.F.C.: " + String.valueOf(ticket.getString("rrfc")).toUpperCase() + "\n");
        }
        if ( ticket.has("rdomicilio")) {
            textData.append("Calle: " + String.valueOf(ticket.getString("rdomicilio")).toUpperCase() + "\n");
        }
        if (ticket.has("rcolonia")) {
            textData.append("Domicilio: " + String.valueOf(ticket.getString("rcolonia")).toUpperCase() + "\n");
        }
        if (ticket.has("restado")) {
            textData.append("Estado: " + String.valueOf(ticket.getString("restado")).toUpperCase() + "\n");
        }
        if (ticket.has("rmunicipio")) {
            textData.append("Ciudad: " + String.valueOf(ticket.getString("rmunicipio")).toUpperCase() + "\n");
        }
        if (ticket.has("rcp")) {
            textData.append("C.P.: " + String.valueOf(ticket.getString("rcp")).toUpperCase() + "\n");
        }

        textData.append("==========================================\n");
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());

        //textData.append("TICKET    :"+String.valueOf(cfdienvio.getInt("ticket"))+"   BOMBA: "+String.valueOf(cfdienvio.getInt("bomba"))+"\n");
        if ( ticket.has("fecha_ticket")) {
            textData.append("FECHA: " + ticket.getString("fecha_ticket") + "\n");
        }
        if (ticket.has("despachador")) {
            textData.append("VENDEDOR    : " + String.valueOf(ticket.getString("despachador")).toUpperCase() + "\n");
        }
        if (ProductoArray.has("unitario")) {
            textData.append("PRECIO         : $ " + String.valueOf(formateador2.format(ProductoArray.getDouble("unitario"))) + "\n");
        }
        if ( ProductoArray.has("cantidad") ) {
            textData.append("VOLUMEN        : " + String.valueOf(formateador4.format(ProductoArray.getDouble("cantidad"))) + " LITROS " + "\n");
        }
        if(ProductoArray.has("descripcion")){
            textData.append("PRODUCTO       : "+String.valueOf(ProductoArray.getString("descripcion")).toUpperCase() + "\n");
        }
        if ( ProductoArray.has("noidentificacion") ) {
            textData.append("IDENTIFICACION : " + String.valueOf(ProductoArray.getString("noidentificacion")).toUpperCase() + "\n");
        }
        if ( ProductoArray.has("claveunidad") || ProductoArray.has("unidad_medida")) {
            textData.append("UNIDAD SAT     : " + String.valueOf(ProductoArray.getString("claveunidad")).toUpperCase() + " - " + String.valueOf(ProductoArray.getString("unidad_medida")).toUpperCase() + "\n");
        }
        if ( ProductoArray.has("subtotal")) {
            textData.append("SUBTOTAL       : $ " + String.valueOf(formateador2.format(ProductoArray.getDouble("subtotal"))) + "\n");
        }
        if ( ProductoArray.has("siniva")) {
            textData.append("IVA            : $ " + String.valueOf(formateador2.format(ProductoArray.getDouble("siniva"))) + "\n");
        }
        if (ticket.has("total")){
            textData.append("IMPORTE        : $ " + String.valueOf(formateador2.format(ticket.getDouble("total"))) + "\n");
            textData.append(letra.Convertir(String.valueOf(formateador21.format(ticket.getDouble("total"))),true)+"\n");
        }
        textData.append("==========================================\n");
        if ( ticket.has("comentario")) {
            textData.append("COMENTARIO : " + cfdienvio.getString("comentario") + "\n");
        }
        textData.append("==========================================\n");
        textData.append("ESTE DOCUMENTO ES UNA REPRESENTACION IMPRESA DE UN CFDI\n");
        if ( ticket.has("certificado_emisor")) {
            textData.append("CERTIFICADO EMISOR: "+ ticket.getString("certificado_emisor")+"\n");
        }
        if ( ticket.has("UUID")) {
            textData.append("FOLIO FISCAL " + String.valueOf(ticket.getString("UUID")) + "\n");
        }
        if (ticket.has("noCertificadoSAT")) {
            textData.append("CERTIFICADO SAT " + String.valueOf(ticket.getString("noCertificadoSAT")) + "\n");
        }
        if ( ticket.has("FechaTimbrado")) {
            textData.append("FECHA DE CERTIFICACION " + String.valueOf(ticket.getString("FechaTimbrado")) + "\n");
        }
        textData.append("\n");
        textData.append("SELLO DIGITAL DEL CFDI\n");
        if ( ticket.has("selloCFD")) {
            textData.append(String.valueOf(ticket.getString("selloCFD")) + "\n");
        }
        textData.append("SELLO DEL SAT\n");
        if (ticket.has("selloSAT")) {
            textData.append(String.valueOf(ticket.getString("selloSAT")) + "\n");
        }
        textData.append("CADENA ORIGINAL DEL COMPLEMENTO DE CERTIFICACION DIGITAL DEL SAT\n");
        if (ticket.has("version") ||
                ticket.has("UUID") ||
                ticket.has("FechaTimbrado") ||
                ticket.has("selloCFD")) {
            textData.append("|" + String.valueOf(ticket.getString("version")) +
                    "|" + String.valueOf(ticket.getString("UUID")) +
                    "|" + String.valueOf(ticket.getString("FechaTimbrado")) +
                    "|" + String.valueOf(ticket.getString("selloCFD")) + "|" + "\n");
        }
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("\n");
        method = "addTextAlign";
        mPrinter.addTextAlign(Printer.ALIGN_CENTER);
        method = "addImage";
        mPrinter.addImage (qrcfdi, 0, 0,
                qrcfdi.getWidth(),
                qrcfdi.getHeight(),
                Printer.COLOR_1,
                Printer.MODE_MONO,
                Printer.HALFTONE_DITHER,
                Printer.PARAM_DEFAULT,
                Printer.COMPRESS_AUTO);
        textData.append("\n");
        method = "addTextAlign";
        mPrinter.addTextAlign(Printer.ALIGN_LEFT);
        textData.append("¡¡¡GRACIAS POR SU PREFERENCIA!!!\n");
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
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
    public class ExecutePrint extends AsyncTask<String, Void, JSONObject> {

        public ExecutePrint(JSONObject js){
            ticket = js;
        }
        @Override
        protected void onPreExecute() {
            pdLoading = new ProgressDialog(EmisionCfdi.this);
            pdLoading.setMessage("Imprimiendo..."); // Setting Message
            pdLoading.setTitle(Bandera); // Setting Title
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
                runOnUiThread(new Runnable() {
                    @Override
                    public synchronized void run() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(EmisionCfdi.this)
                                        .setTitle(R.string.error)
                                        .setMessage(String.valueOf(e))
                                        .setPositiveButton(R.string.btn_ok, null).show();
                            }
                        }).start();
                    }
                });
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
    @Override
    public void onClick(View view) {
        cgticket cgticket_obj = new cgticket();
        JSONObject validar=null;
        try {
            Log.w("cliente1",String.valueOf(Productos.getInt("cg_cliente")));
            if (Productos.getInt("cg_cliente")!=0){
                validar=cgticket_obj.consulta_credito(getApplicationContext(),Productos.getInt("cg_cliente"));
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
                CFDiTimbre timbre = new CFDiTimbre(EmisionCfdi.this, cfdienvio);
                timbre.delegate = this;
                timbre.execute(getIntegra());
                //String  res ="2030|18909|01||E07846|30257940|2017/02/01|2|1|16.530000686645508|0.0|100.0|MWCONRANC1199888|1199888|OD9yhPFD81BvVG+L8TX9gzrkaK4hQj5k+Zz3GiZbq1dao4PXRfIAjtI1NikihhEmrDPAJdKZO5nVI5YXK37eQGeWM1PPyY4bdYGQbF1Qfy/YkEkCJ567DndhwQPuBuo1E6xseFLi7GHJCKUrDndgm4tGzg3UU9geQzXAlJe1EAWkkhQhomkJ+K3gwx5GLCa4MHHuJ/qWVqj3u2pup2vjAUHH7PyYjQ45y6x0HWOYDQCAR38Qc8jRx6YvCIU5MRwNUzMGqnthAZX09W3zo2PWzoZp8XmkshUP1JLQzGJ4eOQg1UEHsdEtFhOPQsO8hN9/8/ZGNJXWfBrCl0E2aKNklA==|2017-02-01T17:54:20|d1c13433-1402-4ce2-b9a8-16f3c5f7f6ae|00001000000301634628|1.0|PgsPnqjYsKg1KAWGFLghg+3+hsNmkRf+MOhdzZmi5sS5AxnbrvZVR4TBM6DvtQQSzMFW/ZCHbYiYt4rfB2Gojg9xavBmE/8zi6CI8ziuuu23l8w5tGh+BYnqK58vsRN15xqp7LJ2G9WjPms4d2KLcOgaRe47/KRNRVcmXesUEFI=";
                break;
        }
    }
    public String getIntegra(){
        DataBaseManager manager = new DataBaseManager(getApplicationContext());
        cursor = manager.cargarcursorodbc2();
        String integra=null;
        try {
            integra = cursor.getString("integra");
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(EmisionCfdi.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
        return integra;
    }
    @Override
    public void processFinish(JSONObject output){
        try {
            if (output.getString("error").equals("1")) {
                JSONObject js_productos = json.strtojson(output.getString("productos"), "|");
                output.remove("productos");
                output.put("productos", js_productos);
                PrintCFDi(output);
            }else{
                StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                        stacktraceObj[2].getMethodName() + "|" + output.getString("mensaje"));
                new AlertDialog.Builder(EmisionCfdi.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(String.valueOf(output.getString("mensaje")))
                        .setPositiveButton(R.string.btn_ok, null).show();
            }
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(EmisionCfdi.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
        Log.w("processFinish",String.valueOf(output));
    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.ContentMainSearch);
                setContentView(R.layout.activity_emision_cfdi);
                Bandera="Combu-Express";
                icon = getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainSearchRepsol);
                setContentView(R.layout.activity_emision_cfdi);
                Bandera = "Repsol";
                icon = getDrawable(R.drawable.repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainSearchEner);
                setContentView(R.layout.activity_emision_cfdi);
                Bandera = "Ener";
                icon = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainSearchTotal);
                setContentView(R.layout.activity_emision_cfdi);
                Bandera = "Total";
                icon = getDrawable(R.drawable.logo_impresion_total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status,
                             final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                if (code == 0) {
                    //Funcion para marcar el servicio en Control-Gas como facturado
                    try {
                        new UpdateCFDi(EmisionCfdi.this, getApplicationContext(), new ControlGasListener() {
                            @Override
                            public void processFinish(JSONObject output) {
                                try {
                                    if (output.getInt(Variables.CODE_ERROR) == 0) {
                                        Intent intent = new Intent(EmisionCfdi.this, VentaActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                                stacktraceObj[2].getMethodName() + "|" + output.getString(Variables.MESSAGE_ERROR));
                                        new AlertDialog.Builder(EmisionCfdi.this)
                                                .setTitle(R.string.error)
                                                .setIcon(icon)
                                                .setMessage(output.getString(Variables.MESSAGE_ERROR))
                                                .setPositiveButton(R.string.btn_ok, null).show();
                                    }
                                }catch (JSONException e) {
                                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                            stacktraceObj[2].getMethodName() + "|" + e);
                                    new AlertDialog.Builder(EmisionCfdi.this)
                                            .setTitle(R.string.error)
                                            .setIcon(icon)
                                            .setMessage(String.valueOf(e))
                                            .setPositiveButton(R.string.btn_ok, null).show();
                                    e.printStackTrace();

                                }
                            }
                        }).execute(ticket.getString("id_factura"),ticket.getString("UUID"),ticket.getString("rrfc"),cfdienvio.getString("nrotrn"));
                    } catch (JSONException e) {
                        StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                        logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                                stacktraceObj[2].getMethodName() + "|" + e);
                        new AlertDialog.Builder(EmisionCfdi.this)
                                .setTitle(R.string.error)
                                .setIcon(icon)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok, null).show();
                        e.printStackTrace();
                    }
                }else{
                    ShowMsg.showResult(code, makeErrorMessage(status), mContext);
                    dispPrinterWarnings(status);
                }
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
}
