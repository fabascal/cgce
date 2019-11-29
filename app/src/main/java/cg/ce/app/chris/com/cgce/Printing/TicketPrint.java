package cg.ce.app.chris.com.cgce.Printing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cg.ce.app.chris.com.cgce.ActivityTicket;
import cg.ce.app.chris.com.cgce.Contents;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.Numero_a_Letra;
import cg.ce.app.chris.com.cgce.QRCodeEncoder;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.ShowMsg;
import cg.ce.app.chris.com.cgce.ValidacionFlotillero;
import cg.ce.app.chris.com.cgce.cgticket;

import static android.content.Context.WINDOW_SERVICE;

public class TicketPrint implements  com.epson.epos2.printer.ReceiveListener {
    JSONObject ticket ;
    Activity context;
    private Printer mPrinter = null;
    String event;
    cgticket tf = new cgticket();
    int impreso_calculado1;
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    Activity activity;

    public TicketPrint(ActivityTicket activityTicket, JSONObject ticket) throws JSONException {
        this.ticket=ticket;
        this.context=activityTicket;
        this.activity=activityTicket;
        ticket.put("impreso",tf.cant_impreso(this.activity.getApplicationContext(),ticket.getString("nrotrn")));
    }

    public String Print() throws JSONException {
        Log.w("ticket clase",ticket.toString());
        if(runPrintReceiptSequence()) {
            tf.actualizar_cant_impreso(activity.getApplicationContext(),ticket.getString("nrotrn"));
            return event;
        }else{
            return event;
        }
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
            mPrinter = new Printer( mPrinter.TM_M30, mPrinter.MODEL_ANK,activity);
        }
        catch (Exception e) {
            event = ShowMsg.showExceptionCE(e, "Printer", activity);
            return false;
        }
        mPrinter.setReceiveEventListener(this);
        return true;
    }

    //Ticket Repsol

    public boolean createReceiptData() {
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(this.activity.getResources(), R.drawable.logo_impresion);
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
            JSONObject datos_domicilio = tf.estacion_domicilio(activity);
            JSONObject vehiculo= new JSONObject();
            String titulo="",folio_impreso="",cliente="",venta="",tpv="";

            Log.w("ticket",ticket.getString("nrotrn"));
            if (ticket.getInt("impreso") == 0 || ticket.getInt("impreso")==10) {
                titulo = "O R I G I N A L";
                folio_impreso = ticket.getString("nrotrn")+"0";
            } else if(ticket.getInt("impreso")==1){
                titulo = "C O P I A";
                folio_impreso = "C O P I A";
            }
            Log.w("ticket",ticket.toString());
            if (ticket.getInt("codcli")!=0 ){
                vehiculo=tf.get_vehiculo(activity,ticket.getString("nrotrn"),ticket.getString("bomba"));
                cliente=ticket.getString("dencli");
                Log.w("vehiculo",vehiculo.toString());
                venta="CREDITO";
            }else{
                vehiculo=tf.get_vehiculo(activity,ticket.getString("nrotrn"),ticket.getString("bomba"));

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
            textData.append(ticket.getString("cveest")+"\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append(datos_domicilio.getString("estacion")+"\n");
            textData.append(datos_domicilio.getString("calle")+" "+datos_domicilio.getString("exterior")+" "+datos_domicilio.getString("interior")+"\n");
            textData.append("COL."+datos_domicilio.getString("colonia")+" C.P. "+datos_domicilio.getString("cp")+"\n");
            textData.append(datos_domicilio.getString("localidad")+", "+datos_domicilio.getString("municipio")+"\n");
            textData.append(datos_domicilio.getString("rfc")+"\n");
            //textData.append("PERMISO C.C. C.R.E.: "+datos_domicilio.getString("permiso")+"\n");
            textData.append("\n");
            textData.append("Regimen Fiscal"+"\n");
            textData.append(datos_domicilio.getString("regimen")+"\n");
            textData.append("\n");
            textData.append("Lugar de Expedicion"+"\n");
            textData.append(datos_domicilio.getString("municipio")+" "+datos_domicilio.getString("estado")+"\n");
            textData.append("\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("***** "+titulo+" *****"+"\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append("\n");
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
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
            textData.append("FECHA: " + ticket.getString("fecha") + "  HORA: "+ticket.getString("hora")+"\n");
            textData.append("VENDEDOR  : " + String.valueOf(ticket.getString("despachador")).toUpperCase() + "\n");
            textData.append("PRECIO    : $ " + String.format("%.2f",Double.valueOf(formateador2.format(ticket.getDouble("precio")))) + "\n");
            textData.append("VOLUMEN   : " + String.format("%.4f",Double.valueOf(formateador4.format(ticket.getDouble("cantidad")))) + " LITROS " + String.valueOf(ticket.getString("producto")).toUpperCase() + "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("IMPORTE   : $ " +formateador2.format(ticket.getDouble("total")) + "\n");
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
            textData.append("|      combuexpress.com.mx      |\n");
            textData.append("________________________________\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            //textData.append("POR DISPOSICION DEL SAT SI REQUIERE FACTURA DEBERA SOLICITARLA DENTRO DE LAS 24 HRS POSTERIORES AL DIA DE CONSUMO\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            if (ticket.getInt("impreso")==0 || ticket.getInt("impreso")==10) {
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
            textData.append("\n");
            /*Bitmap qrrespol=null;
            QRCodeEncoder qrCodeEncoder1 = new QRCodeEncoder(repsolQR(ticket,datos_domicilio),
                    null,
                    Contents.Type.TEXT,
                    BarcodeFormat.QR_CODE.toString(),
                    smallerDimension);
            try {
                qrrespol = qrCodeEncoder1.encodeAsBitmap();
            } catch (WriterException e) {
                e.printStackTrace();
            }
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addImage (qrrespol, 0, 0,
                    qrrespol.getWidth(),
                    qrrespol.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);

             */
            textData.append("\n");
            mPrinter.addText(textData.toString());
            ValidacionFlotillero vf = new ValidacionFlotillero();
            Integer sorteo= vf.validar_sorteo(context);
            String inicio = vf.sorteo_inicio(context);
            String fin = vf.sorteo_fin(context);

            if (ticket.getInt("impreso")==0 || ticket.getInt("impreso")==10){
                if (sorteo>0 ) {
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
                        textData.append("                FOLIO   :   " +  folio_impreso +"\n");
                        textData.append("                FECHA   :   " + ticket.getString("fecha") +"\n");
                        textData.append("                MONTO   :   " + formateador2.format(ticket.getDouble("total")) +"\n");
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
                        String qr_cadena="URL: https://ganaconcombu.com?es="+ ticket.getString("cveest")+"&fo="+ String.valueOf(folio_impreso)+"&fe="+ ticket.getString("fecha")+"&mo="+ formateador2.format(ticket.getDouble("total"))+"&pr="+ ticket.getString("codprd");
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
                        textData.append("PROMOCION : "+vf.sorteo_nombre(context)+"\n");
                        textData.append("VIGENCIA DEL "+inicio +" \n");
                        textData.append("AL "+fin+"\n");
                        textData.append("\n");
                        //textData.append("PROMOCION 2:\n");
                        //textData.append("VIGENCIA 00 de MES al 00 de MES,\n");
                        //textData.append("entrega del 50% de los incentivos\n");
                        //textData.append("00 de MES de 2019\n");
                    }
                }
            }


            mPrinter.addText(textData.toString());

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            event=e.toString();
            Log.w("Errorimprimir",e);
            event=ShowMsg.showExceptionCE(e, method,context);
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

        final PrinterStatusInfo status = mPrinter.getStatus();

        dispPrinterWarnings(status);

        if (!isPrintable(status)) {


            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {

                    ShowMsg.showMsg(makeErrorMessage(status), activity);
                }
            });

            //ShowMsg.showMsg(makeErrorMessage(status), context);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                event=ex.toString();
                // Do nothing
            }
            return false;
        }

        try {


            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            event=e.toString();
            ShowMsg.showException(e, "sendData", activity);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                event = ex.toString();
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
            DataBaseManager manager = new DataBaseManager(activity);
            String target = manager.target();//"BT:00:01:90:C6:81:22";
            //activity.Toast.makeText(context,target,Toast.LENGTH_LONG).show();

            mPrinter.connect(target ,Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            event = e.toString();
            Log.w("Errorimprimier",e);
            ShowMsg.showException(e, "connect", context);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            event= e.toString();
            ShowMsg.showException(e, "beginTransaction", activity);
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
               event=e.toString();
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
            warningsMsg += activity.getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += activity.getString(R.string.handlingmsg_warn_battery_near_end);
        }
        event=warningsMsg;
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
            msg += activity.getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += activity.getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += activity.getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += activity.getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += activity.getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += activity.getString(R.string.handlingmsg_err_autocutter);
            msg += activity.getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += activity.getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += activity.getString(R.string.handlingmsg_err_overheat);
                msg += activity.getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += activity.getString(R.string.handlingmsg_err_overheat);
                msg += activity.getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += activity.getString(R.string.handlingmsg_err_overheat);
                msg += activity.getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += activity.getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += activity.getString(R.string.handlingmsg_err_battery_real_end);
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
           event = e.toString();
            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    event=ShowMsg.showExceptionCE(e, "endTransaction", activity);
                }
            });
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            event = e.toString();
            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    event=ShowMsg.showExceptionCE(e, "disconnect", activity);
                }
            });
        }

        finalizeObject();
    }


    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                //ShowMsg.showResult(code, makeErrorMessage(status), context);

                dispPrinterWarnings(status);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }

    public String repsolQR (JSONObject jsticket, JSONObject jsdomicilio) throws JSONException {
        Double ieps = jsticket.getDouble("ieps");
        Double iva_factor = jsticket.getDouble("iva");
        Double lts = jsticket.getDouble("cantidad");
        Double pre = jsticket.getDouble("precio");
        Double ivaentre = Double.valueOf("1" + iva_factor);
        Log.w("PRE",String.valueOf(pre));
        Log.w("ieps",String.valueOf(ieps));
        Log.w("iva",String.valueOf(ivaentre));
        Double precioneto = ((Double.valueOf(pre) - ieps) / ivaentre) * 100;
        Double iva = ((precioneto * lts) * iva_factor) / 100;
        Double subtotal = (precioneto + ieps) * lts;
        Log.w("precioneto",String.valueOf(precioneto));
        Log.w("ieps",String.valueOf(ieps));
        String precio_total = String.valueOf(precioneto+ieps);
        Log.w("precio",precio_total);
        String substr = ".";
        Log.w("antes",precio_total.substring(0, precio_total.indexOf(substr)));
        Log.w("despued",precio_total.substring(precio_total.indexOf(substr) , 2));

        String inicio = precio_total.substring(0, precio_total.indexOf(substr));
        String fin = precio_total.substring(precio_total.indexOf(substr) + substr.length());
        String precio_impresion = inicio +"."+ fin.substring(0,2);
        String qr = "COMBUGO|"+jsticket.getString("cveest")+"|"+jsdomicilio.getString("estacion")+"|"+jsticket.getString("fecha")+"|"+jsticket.getString("hora")+"|"+precio_impresion+"|"+formateador2.format(iva)+"|"+jsticket.getString("total")+"|"+jsticket.getString("nrotrn")+"|";


        Log.w("QR %s",qr);
        return qr;
    }
}
