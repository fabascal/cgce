package cg.ce.app.chris.com.cgce.Printing;

import android.app.Activity;
import android.content.Context;
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
import cg.ce.app.chris.com.cgce.cgticket;

import static android.content.Context.WINDOW_SERVICE;

public class SorteoPrint implements  com.epson.epos2.printer.ReceiveListener{
    JSONObject js ;
    Context context;
    private Printer mPrinter = null;
    String event;
    cgticket tf = new cgticket();
    int impreso_calculado1;
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    Activity activity;



    public SorteoPrint(ActivityTicket activityTicket, JSONObject ticket) throws JSONException {
        this.js=ticket;
        this.context=context;
        this.activity=activityTicket;

    }
    public String Print(){
        if(runPrintReceiptSequence()) {
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
    public boolean createReceiptData() {
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(activity.getResources(), R.drawable.isologo_repsol);
        StringBuilder textData = new StringBuilder();
        Numero_a_Letra letra = new Numero_a_Letra();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;
        Point point = new Point();
        WindowManager manager = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? 380 : 380;

        if (mPrinter == null) {
            return false;
        }

        try {
            if (js.getString("error").equals("1")) {
                Bitmap logoviaje = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logosorteo);


                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                method = "addImage";
                mPrinter.addImage(logoviaje, 0, 0,
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


                textData.append("EL VIAJE DE TU VIDA\n");
                mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
                mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);


                mPrinter.addTextAlign(Printer.ALIGN_LEFT);
                textData.append("     ESTACION   :   " + js.getString("nombre_estacion")+"\n");
                textData.append("        FOLIO   :   " + js.getString("folio")+"\n");
                textData.append("        FECHA   :   " + js.getString("fecha_alta") +"\n");
                textData.append("        MONTO   :   " + js.getString("importe") +"\n");
                textData.append("     PRODUCTO   :   " + js.getString("producto") +"\n");

                textData.append("\n");
                textData.append("\n");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                textData.append("INGRESA A :\n");
                textData.append("www.ganaconrepsol.com\n");
                textData.append("LLENA LA FORMA\n");
                textData.append("Y GANA CON REPSOL\n");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
                //QR

                Bitmap qrcfdi=null;
                String qr_cadena="URL: https://ganaconrepsol.com/pages/registro.html?es="+ js.getString("nombre_estacion")+"&fo="+ js.getString("folio")+"&fe="+ js.getString("fecha_alta")+"&mo="+ js.getString("importe")+"&pr="+ js.getString("producto");
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
                textData.append("VIGENCIA 1 de septiembre al 15 de octubre,\n");
                textData.append("entrega del 50% de los incentivos\n");
                textData.append("25 de octubre de 2019\n");
                textData.append("\n");
                textData.append("PROMOCION 2:\n");
                textData.append("VIGENCIA 16 de octubre al 30 de noviembre,\n");
                textData.append("entrega del 50% de los incentivos\n");
                textData.append("13 de diciembre de 2019\n");
                mPrinter.addText(textData.toString());
            }
            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            event=e.toString();
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
//            ShowMsg.showException(e, "connect", context);
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

    @Override
    public void onPtrReceive(Printer printer, int i, PrinterStatusInfo printerStatusInfo, String s) {

    }
}
