package cg.ce.app.chris.com.cgce;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cg.ce.app.chris.com.cgce.webservice.sorteo;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by chris on 23/05/17.
 */

public class ClassImpresionTicket extends AsyncTask<JSONObject,String,Boolean> implements ReceiveListener,SorteoListener {
    ProgressDialog pdLoading ;
    Activity activity;
    ImageButton btn;
    Context context;
    private Printer mPrinter = null;
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    JSONObject ticket ;
    int impreso1,impreso_calculado;
    cgticket tf = new cgticket();
    LogCE logCE = new LogCE();
    JSONObject jsonObjectError = new JSONObject();
    JSONObject js;




    public ClassImpresionTicket (Activity activity, Context context, ImageButton btn, JSONObject jsonObject){

        this.activity=activity;
        this.context=context;
        this.btn=btn;
        //classImpresion = new ClassImpresionTicket(activity,context,btn,jsonObject);
        this.ticket=jsonObject;
        if (!ticket.has("validador")) {
            pdLoading = new ProgressDialog(activity);
        }
        try {
            impreso1= jsonObject.getInt("impreso");
        } catch (JSONException e) {

            try {
                logCE.EscirbirLog(context,jsonObjectError.put("impresion",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        try {
            if(jsonObject.getString("tipo_venta").equals("1") || jsonObject.getString("tipo_venta").equals("3")){
                impreso_calculado=tf.cant_impreso(context,ticket.getString("nrotrn"));
            }else{
                impreso_calculado=0;
            }
        } catch (JSONException e) {
            try {
                logCE.EscirbirLog(context,jsonObjectError.put("impresion",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            if (impreso_calculado==0 && ticket.getDouble("total")>=200) {
                //sorteo();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }





        //this method will be running on UI thread
        if (!ticket.has("validador")) {
            pdLoading.setIndeterminate(true);
            pdLoading.setCancelable(false);
            pdLoading.setTitle("Combu-Express");
            pdLoading.setMessage("Imprimiendo ...");
            pdLoading.show();
        }
        updateButtonState(false);
    }
    @Override
    protected Boolean doInBackground(JSONObject... jsonObjects) {
        if(runPrintReceiptSequence()) {
            SystemClock.sleep(2000);
            return true;
        }else{
            return false;
        }
    }
    @Override
    protected void onPostExecute(Boolean result) {
        if (result.equals(true)){
            try {
                tf.actualizar_cant_impreso(context,ticket.getString("nrotrn"));
            } catch (JSONException e) {
                try {
                    logCE.EscirbirLog(context,jsonObjectError.put("impresion",e));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        if(!ticket.has("validador")) {
            if (pdLoading != null) {
                pdLoading.dismiss();
            }
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
            try {
                logCE.EscirbirLog(context,jsonObjectError.put("impresion_printer",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            ShowMsg.showException(e, "Printer", activity);
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }
    public boolean createReceiptData() {
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo_impresion);
        StringBuilder textData = new StringBuilder();
        Numero_a_Letra letra = new Numero_a_Letra();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;
        Point point = new Point();
        WindowManager manager = (WindowManager) this.activity.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? 380 : 380;

        if (mPrinter == null) {
            return false;
        }


        try {
            JSONObject datos_domicilio = tf.estacion_domicilio(context);
            JSONObject vehiculo= new JSONObject();
            String titulo="",folio_impreso="",cliente="",venta="",tpv="";

            Log.w("ticket",ticket.getString("nrotrn"));
            impreso1=tf.cant_impreso(context,ticket.getString("nrotrn"));
            if (impreso_calculado == 0) {
                titulo = "O R I G I N A L";
                folio_impreso = ticket.getString("nrotrn")+"0";
            } else if(impreso_calculado==1){
                titulo = "C O P I A";
                folio_impreso = "C O P I A";
            }
            Log.w("ticket",ticket.toString());
            if (ticket.getInt("codcli")!=0 ){
                vehiculo=tf.get_vehiculo(context,ticket.getString("nrotrn"),ticket.getString("bomba"));
                cliente=ticket.getString("dencli");
                Log.w("vehiculo",vehiculo.toString());
                venta="CREDITO";
            }else{
                vehiculo=tf.get_vehiculo(context,ticket.getString("nrotrn"),ticket.getString("bomba"));

                if (ticket.has("tpv")) {
                    if (ticket.getJSONObject("tpv").has("nombre")) {
                        tpv = ticket.getJSONObject("tpv").getString("nombre");
                    }
                }
                cliente="Publico General";
                venta="CONTADO" +" "+tpv;
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
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            //method = "addFeedLine";
            //mPrinter.addFeedLine(1);
            Log.w("hora",ticket.getString("hora"));
            textData.append("" + titulo + "\n");
            textData.append(venta+"\n");
            textData.append("FECHA: " + ticket.getString("fecha") + "  HORA: "+ticket.getString("hora")+"\n");
            textData.append(datos_domicilio.getString("regimen")+"\n");
            textData.append("\n");
            textData.append("LUGAR DE EXPEDICION:\n");
            textData.append("ESTACION: "+datos_domicilio.getString("estacion")+" "+ticket.getString("cveest")+"\n");
            textData.append(datos_domicilio.getString("calle")+" "+datos_domicilio.getString("exterior")+" "+datos_domicilio.getString("interior")+", "+datos_domicilio.getString("colonia")+", "+datos_domicilio.getString("cp")+"\n");
            textData.append(datos_domicilio.getString("localidad")+", "+datos_domicilio.getString("municipio")+", "+datos_domicilio.getString("estado")+", "+datos_domicilio.getString("pais")+"\n");
            textData.append("RFC "+datos_domicilio.getString("rfc")+" TEL."+datos_domicilio.getString("telefono")+"\n");
            //textData.append("------------------------------\n");
            textData.append(cliente+"\n");
            //textData.append("\n");
            if(ticket.has("codcli")) {
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
            textData.append("VENDEDOR  : " + String.valueOf(ticket.getString("despachador")).toUpperCase() + "\n");
            textData.append("PRECIO    : $ " + String.valueOf(formateador2.format(ticket.getDouble("precio"))) + "\n");
            textData.append("VOLUMEN   : " + String.valueOf(formateador4.format(ticket.getDouble("cantidad"))) + " LITROS " + String.valueOf(ticket.getString("producto")).toUpperCase() + "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("IMPORTE   : $ " + String.valueOf(formateador2.format(ticket.getDouble("total"))) + "\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append(letra.Convertir(String.valueOf(formateador21.format(ticket.getDouble("total"))),true)+"\n");
            //textData.append("\n");
            textData.append("\n");
            textData.append("------------------------------\n");
            textData.append("NOMBRE Y FIRMA CONDUCTOR\n");
            textData.append("________________________________\n");
            textData.append("|TRAMITE SU FACTURA POR INTERNET|\n");
            textData.append("|    www.combuexpress.com.mx    |\n");
            textData.append("________________________________\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("POR DISPOSICION DEL SAT SI REQUIERE FACTURA DEBERA SOLICITARLA DENTRO DE LAS 24 HRS POSTERIORES AL DIA DE CONSUMO\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            if (impreso_calculado==0) {
                method = "addBarcode";
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                mPrinter.addBarcode(String.valueOf(folio_impreso),
                        Printer.BARCODE_CODE39,
                        Printer.HRI_BELOW,
                        Printer.FONT_A,
                        barcodeWidth,
                        barcodeHeight);
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            }
            /*
            Agregar la funcion de los sorteos
             */
            ValidacionFlotillero vf = new ValidacionFlotillero();
            Integer sorteo= vf.validar_sorteo(context);
            if (sorteo>0) {
                if (ticket.getDouble("total")>=200){
                    Bitmap logoviaje = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ganaconcombu);
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
                    textData.append("             ESTACION   :   " + datos_domicilio.getString("estacion")+"\n");
                    textData.append("                FOLIO   :   " + ticket.getString("nrotrn")+"\n");
                    textData.append("                FECHA   :   " + ticket.getString("fecha") +"\n");
                    textData.append("                MONTO   :   " + String.valueOf(Double.valueOf(formateador2.format(ticket.getDouble("total")))) +"\n");
                    textData.append("             PRODUCTO   :   " + ticket.getString("producto") +"\n");
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

                    Bitmap qrcfdi=null;
                    String qr_cadena="URL: https://ganaconcombu.com?es="+ ticket.getString("cveest")+"&fo="+ String.valueOf(folio_impreso)+"&fe="+ ticket.getString("fecha")+"&mo="+ String.valueOf(Double.valueOf(formateador2.format(ticket.getDouble("total"))))+"&pr="+ ticket.getString("codprd");
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
                    mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                    mPrinter.addImage (qrcfdi, 0, 0,
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
                    textData.append("PROMOCION 1:\n");
                    textData.append("VIGENCIA 00 de MES al 00 de MES,\n");
                    textData.append("entrega del 50% de los incentivos\n");
                    textData.append("00 de MES de 2019\n");
                    textData.append("\n");
                    textData.append("PROMOCION 2:\n");
                    textData.append("VIGENCIA 00 de MES al 00 de MES,\n");
                    textData.append("entrega del 50% de los incentivos\n");
                    textData.append("00 de MES de 2019\n");
                }
            }

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            try {
                logCE.EscirbirLog(context,jsonObjectError.put("create_RecipeData",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            Log.w("Errorimprimir",e);
            //ShowMsg.showException(e, method,context);
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
                try {
                    logCE.EscirbirLog(context,jsonObjectError.put("printer_disconnect",ex));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                // Do nothing
            }
            return false;
        }

        try {
            if (ticket.has("tipo_venta")){
                /*
                tipo de venta 2 es credito, aqui se valida el tipo de venta para imprimir copia
                 */
                if(ticket.getInt("tipo_venta")==2) {
                    impreso_calculado=1;
                    //Log.w("impreso_calculado", String.valueOf(impreso_calculado));
                    createReceiptData();
                }
            }

            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            try {
                logCE.EscirbirLog(context,jsonObjectError.put("sendData",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            ShowMsg.showException(e, "sendData", activity);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                try {
                    logCE.EscirbirLog(context,jsonObjectError.put("printer_disconect",ex));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
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
            try {
                logCE.EscirbirLog(context,jsonObjectError.put("printer_conect",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            Log.w("Errorimprimier",e);
//            ShowMsg.showException(e, "connect", context);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            try {
                logCE.EscirbirLog(context,jsonObjectError.put("beginTransaction",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            ShowMsg.showException(e, "beginTransaction", activity);
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                try {
                    logCE.EscirbirLog(context,jsonObjectError.put("printer_disconect",e));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
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

        try {
            logCE.EscirbirLog(context,jsonObjectError.put("printer",msg));
        } catch (JSONException e1) {
            e1.printStackTrace();
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
            try {
                logCE.EscirbirLog(context,jsonObjectError.put("endTransaction",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
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
            try {
                logCE.EscirbirLog(context,jsonObjectError.put("disconnect",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
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

    public void sorteo() throws JSONException {
        sorteo s =new sorteo(context,ticket);
        s.delegate= this;
        s.execute();
    }


    @Override
    public void processFinish3(String output) {
        try {
            js = new JSONObject(output);
            Log.w("js_sorteo",js.toString());
            if (js.has("error")){
                if (js.getInt("error")!=0){
                    new ClassImpresionSorteo(activity,context,js).execute();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
