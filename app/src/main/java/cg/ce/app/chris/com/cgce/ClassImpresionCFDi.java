package cg.ce.app.chris.com.cgce;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Button;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by chris on 29/06/17.
 */

public class ClassImpresionCFDi extends AsyncTask<JSONObject,String,Boolean> implements ReceiveListener {
    ProgressDialog pdLoading ;
    Activity activity;
    Button btn;
    Context context;
    private Printer mPrinter = null;
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    JSONObject cfdienvio = new JSONObject();
    cgticket cg = new cgticket();

    public ClassImpresionCFDi(Activity activity, Context context, Button btn, JSONObject jsonObject){
        this.activity=activity;
        this.context=context;
        this.btn=btn;
        //classImpresion = new ClassImpresionTicket(activity,context,btn,jsonObject);
        pdLoading = new ProgressDialog(activity);
        this.cfdienvio=jsonObject;
        Log.w("json",cfdienvio.toString());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //this method will be running on UI thread
        pdLoading.setIndeterminate(true);
        pdLoading.setCancelable(false);
        pdLoading.setTitle("Combu-Express");
        pdLoading.setMessage("Imprimiendo ...");
        pdLoading.show();
        updateButtonState(false);
    }
    @Override
    protected Boolean doInBackground(JSONObject... jsonObjects) {
        runPrintReceiptSequence();
        return true;
    }
    @Override
    protected void onPostExecute(Boolean result) {
        if(pdLoading != null){
            pdLoading.dismiss();
        }
        updateButtonState(true);
    }
    public void updateButtonState(boolean state) {
        btn.setEnabled(state);
    }
    public boolean runPrintReceiptSequence() {
        if (!initializeObject()) {
            return false;
        }
        if (!createReceiptData()) {
            finalizeObject();
            return false;
        }
        if (!printData()) {
            finalizeObject();
            return false;
        }
        return true;
    }
    public boolean initializeObject() {
        try {
            mPrinter = new Printer( mPrinter.TM_M30, mPrinter.MODEL_ANK,context);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Printer", activity);
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }
    private boolean createReceiptData() {
        Numero_a_Letra letra = new Numero_a_Letra();
        JSONObject datos_domicilio = new JSONObject();
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_impresion);
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
        try {
            datos_domicilio = cg.estacion_domicilio(context);
            qr_cadena = "?re"+ String.valueOf(cfdienvio.getString("rfc_emisor")).toUpperCase()+
                    "&rr"+ String.valueOf(cfdienvio.getString("rfc")).toUpperCase()+
                    "&tt"+ String.valueOf(cfdienvio.getString("importe")).toUpperCase()+
                    "&rr"+ String.valueOf(cfdienvio.getString("uuid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qr_cadena,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                smallerDimension);
        try {
            qrcfdi = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }

        if (mPrinter == null) {
            return false;
        }

        String a ="O R I G I N A L";
        try {
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
            if (cfdienvio.has("nombre_emisor")) {
                textData.append("" + cfdienvio.getString("nombre_emisor") + "\n");
            }
            if (cfdienvio.has("rfc_emisor")) {
                textData.append("" + cfdienvio.getString("rfc_emisor") + "\n");
            }
            if (cfdienvio.has("folio")) {
                textData.append("FACTURA: " + String.valueOf(cfdienvio.getString("folio")).toUpperCase() + "\n");
            }
            if ( cfdienvio.has("fecha_timbre")) {
                textData.append("FECHA: " + cfdienvio.getString("fecha_timbre") + "\n");
            }
            if ( cfdienvio.has("calle_emisor") || cfdienvio.has("numext_emisor")) {
                textData.append("CALLE: " + cfdienvio.getString("calle_emisor") + " " + cfdienvio.getString("numext_emisor") + "\n");
            }
            if ( cfdienvio.has("colonia_emisor")) {
                textData.append("COLONIA: " + cfdienvio.getString("colonia_emisor") + "\n");
            }
            if ( cfdienvio.has("fecha_timbre")) {
                textData.append("FECHA: " + cfdienvio.getString("fecha_timbre") + "\n");
            }
            if ( cfdienvio.has("municipio_emisor") || cfdienvio.has("cp_emisor")) {
                textData.append("" + cfdienvio.getString("municipio_emisor") + " " + cfdienvio.getString("estado_emisor") + " C.P." + cfdienvio.getString("cp_emisor") + "\n");
            }
            if ( cfdienvio.has("pais_emisor")) {
                textData.append("PAIS: " + cfdienvio.getString("pais_emisor") + "\n");
            }
            textData.append("\n");
            textData.append("==========================================\n");
            if (cfdienvio.has("cveest") || datos_domicilio.has("estacion")) {
                textData.append("ESTACION: " + datos_domicilio.getString("estacion") + " " + cfdienvio.getString("cveest") + "\n");
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
            if (cfdienvio.has("cliente")) {
                textData.append("Cliente: " + String.valueOf(cfdienvio.getString("cliente")).toUpperCase() + "\n");
            }
            if (cfdienvio.has("rfc")) {
                textData.append("R.F.C.: " + String.valueOf(cfdienvio.getString("rfc")).toUpperCase() + "\n");
            }
            if ( cfdienvio.has("domicilio")) {
                textData.append("Calle: " + String.valueOf(cfdienvio.getString("domicilio")).toUpperCase() + "\n");
            }
            if (cfdienvio.has("colonia")) {
                textData.append("Colonia: " + String.valueOf(cfdienvio.getString("colonia")).toUpperCase() + "\n");
            }
            if (cfdienvio.has("estado")) {
                textData.append("Estado: " + String.valueOf(cfdienvio.getString("estado")).toUpperCase() + "\n");
            }
            if (cfdienvio.has("municipio")) {
                textData.append("Ciudad: " + String.valueOf(cfdienvio.getString("municipio")).toUpperCase() + "\n");
            }
            if (cfdienvio.has("cp")) {
                textData.append("C.P.: " + String.valueOf(cfdienvio.getString("cp")).toUpperCase() + "\n");
            }

            textData.append("==========================================\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            //textData.append("TICKET    :"+String.valueOf(cfdienvio.getInt("ticket"))+"   BOMBA: "+String.valueOf(cfdienvio.getInt("bomba"))+"\n");
            if ( cfdienvio.has("fecha_ticket")) {
                textData.append("FECHA: " + cfdienvio.getString("fecha_ticket") + "\n");
            }
            if (cfdienvio.has("despachador")) {
                textData.append("VENDEDOR    : " + String.valueOf(cfdienvio.getString("despachador")).toUpperCase() + "\n");
            }
            if (cfdienvio.has("preunitario")) {
                textData.append("PRECIO      : $ " + String.valueOf(formateador2.format(cfdienvio.getDouble("preunitario"))) + "\n");
            }
            if ( cfdienvio.has("cantidad") || cfdienvio.has("producto")) {
                textData.append("VOLUMEN     : " + String.valueOf(formateador4.format(cfdienvio.getDouble("cantidad"))) + " LITROS " + String.valueOf(cfdienvio.getString("producto")).toUpperCase() + "\n");
            }
            if ( cfdienvio.has("claveprodserv") || cfdienvio.has("descripcion")) {
                textData.append("PRODUCTO SAT: " + String.valueOf(cfdienvio.getString("claveprodserv")).toUpperCase() + " - " + String.valueOf(cfdienvio.getString("descripcion")).toUpperCase() + "\n");
            }
            if ( cfdienvio.has("claveunidad") || cfdienvio.has("unidad_medida")) {
                textData.append("UNIDAD SAT  : " + String.valueOf(cfdienvio.getString("claveunidad")).toUpperCase() + " - " + String.valueOf(cfdienvio.getString("unidad_medida")).toUpperCase() + "\n");
            }
            if ( cfdienvio.has("subtotal")) {
                textData.append("SUBTOTAL    : $ " + String.valueOf(formateador2.format(cfdienvio.getDouble("subtotal"))) + "\n");
                //textData.append(letra.Convertir(String.valueOf(formateador21.format(cfdienvio.getDouble("subtotal"))),true)+"\n");
            }
            if ( cfdienvio.has("iva")) {
                textData.append("IVA         : $ " + String.valueOf(formateador2.format(cfdienvio.getDouble("iva"))) + "\n");
                //textData.append(letra.Convertir(String.valueOf(formateador21.format(cfdienvio.getDouble("iva"))),true)+"\n");
            }
            if ( cfdienvio.has("importe")) {
                textData.append("IMPORTE     : $ " + String.valueOf(formateador2.format(cfdienvio.getDouble("importe"))) + "\n");
                textData.append(letra.Convertir(String.valueOf(formateador21.format(cfdienvio.getDouble("importe"))),true)+"\n");
            }
            textData.append("==========================================\n");
            if ( cfdienvio.has("comentario")) {
                textData.append("COMENTARIO : " + cfdienvio.getString("comentario") + "\n");
            }
            textData.append("==========================================\n");
            textData.append("ESTE DOCUMENTO ES UNA REPRESENTACION IMPRESA DE UN CFDI\n");
            textData.append("CERTIFICADO EMISOR:\n");
            if ( cfdienvio.has("uuid")) {
                textData.append("FOLIO FISCAL" + String.valueOf(cfdienvio.getString("uuid")) + "\n");
            }
            if (cfdienvio.has("certificado_sat")) {
                textData.append("CERTIFICADO SAT" + String.valueOf(cfdienvio.getString("certificado_sat")) + "\n");
            }
            if ( cfdienvio.has("fecha_timbre")) {
                textData.append("FECHA DE CERTIFICACION" + String.valueOf(cfdienvio.getString("fecha_timbre")) + "\n");
            }
            textData.append("\n");
            textData.append("SELLO DIGITAL DEL CFDI\n");
            if ( cfdienvio.has("sello_cfd")) {
                textData.append(String.valueOf(cfdienvio.getString("sello_cfd")) + "\n");
            }
            textData.append("SELLO DEL SAT\n");
            if (cfdienvio.has("sello_sat")) {
                textData.append(String.valueOf(cfdienvio.getString("sello_sat")) + "\n");
            }
            textData.append("CADENA ORIGINAL DEL COMPLEMENTO DE CERTIFICACION DIGITAL DEL SAT\n");
            if (cfdienvio.has("version") || cfdienvio.has("uuid") || cfdienvio.has("fecha_timbre") || cfdienvio.has("sello_cfd")) {
                textData.append("|" + String.valueOf(cfdienvio.getString("version")) +
                        "|" + String.valueOf(cfdienvio.getString("uuid")) +
                        "|" + String.valueOf(cfdienvio.getString("fecha_timbre")) +
                        "|" + String.valueOf(cfdienvio.getString("sello_cfd")) + "|" + "\n");
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
        }
        catch (Exception e) {
            Log.w("error_imprimir",e);
            //ShowMsg.showException(e, method, context);
            return false;
        }

        textData = null;

        return true;
    }
    public void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }
    public boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            ShowMsg.showMsg(makeErrorMessage(status), activity);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            //createReceiptData();
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "sendData", activity);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }
    public boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            DataBaseManager manager = new DataBaseManager(context);
            String target = manager.target();//"BT:00:01:90:C6:81:22";
            //activity.Toast.makeText(context,target,Toast.LENGTH_LONG).show();
            mPrinter.connect(target ,Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            Log.w("Errorimprimier",e);
//            ShowMsg.showException(e, "connect", context);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "beginTransaction", activity);
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }

        }

        return true;
    }
    public void dispPrinterWarnings(PrinterStatusInfo status) {
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += context.getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += context.getString(R.string.handlingmsg_warn_battery_near_end);
        }
        Log.w("Errorimprimir",warningsMsg.toString());
        //Toast.makeText(context,warningsMsg.toString(),Toast.LENGTH_LONG).show();
    }
    public boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        return true;
    }
    public String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += context.getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += context.getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += context.getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += context.getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += context.getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += context.getString(R.string.handlingmsg_err_autocutter);
            msg += context.getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += context.getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += context.getString(R.string.handlingmsg_err_overheat);
                msg += context.getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += context.getString(R.string.handlingmsg_err_overheat);
                msg += context.getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += context.getString(R.string.handlingmsg_err_overheat);
                msg += context.getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += context.getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += context.getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }
    public void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "endTransaction", activity);
                }
            });
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "disconnect", activity);
                }
            });
        }

        finalizeObject();
    }

    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                //ShowMsg.showResult(code, makeErrorMessage(status), context);

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
}
