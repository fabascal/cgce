package cg.ce.app.chris.com.cgce;


import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import org.json.JSONObject;

/**
 * Created by chris on 31/01/17.
 */

public class Cfdi_impresion implements ReceiveListener{

    Activity activity;
    JSONObject ticket = new JSONObject();
    private Printer  mPrinter = null;
    Context mContext;

    public Boolean print_cfdi (Context con){
        //Toast.makeText(con,"algo2",Toast.LENGTH_LONG).show();
        mContext=con;
        runPrintReceiptSequence();
       return true;
    }

    private boolean initializeObject() {

        try {
            mPrinter = new Printer( mPrinter.TM_M30, mPrinter.MODEL_ANK,mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Printer", activity);
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }
    //public static Context getContext(){
    //    mContext=getContext();
    //    return mContext;
    //}


    // este es el objeto a llamar, se debera pasar un json

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
    private void dispPrinterWarnings(PrinterStatusInfo status) {
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += mContext.getResources().getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += mContext.getResources().getString(R.string.handlingmsg_warn_battery_near_end);
        }

        Toast.makeText(mContext,warningsMsg.toString(),Toast.LENGTH_LONG).show();
    }
    private boolean printData() {
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
    private String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_autocutter);
            msg += mContext.getResources().getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += mContext.getResources().getString(R.string.handlingmsg_err_overheat);
                msg += mContext.getResources().getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += mContext.getResources().getString(R.string.handlingmsg_err_overheat);
                msg += mContext.getResources().getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += mContext.getResources().getString(R.string.handlingmsg_err_overheat);
                msg += mContext.getResources().getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += mContext.getResources().getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += mContext.getResources().getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }
    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        else {
            ;//print available
        }

        return true;
    }
    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            DataBaseManager manager = new DataBaseManager(mContext);
            String target = manager.target();
            Toast.makeText(mContext,target,Toast.LENGTH_LONG).show();
            mPrinter.connect(target ,Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", activity);
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
    private boolean createReceiptData() {
        String method = "";
        //Bitmap logoData = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo_impresion);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        String a ="O R I G I N A L";
        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            //method = "addImage";
            //mPrinter.addImage(logoData, 0, 0,
            //        logoData.getWidth(),
            //        logoData.getHeight(),
            //        Printer.COLOR_1,
            //        Printer.MODE_MONO,
            //        Printer.HALFTONE_DITHER,
            //        Printer.PARAM_DEFAULT,
            //        Printer.COMPRESS_AUTO);

            //method = "addFeedLine";
            //mPrinter.addFeedLine(1);
            textData.append(""+a+"\n");
            textData.append("CONTADO\n");
            textData.append("FECHA: ");//+ticket.getString("fecha")+" Hora: 01:15\n");
            //textData.append("COMBU-EXPRESS, S.A. DE C.V. E.S.7846\n");
            //textData.append("Rancho Contento E.S.7846\n");
            textData.append("REGIMEN GENERAL DE LEY, PERSONAS MORALES\n");
            textData.append("\n");
            textData.append("LUGAR DE EXPEDICION:\n");
            textData.append("ESTACION: RANCHO CONTENTO E.S.7846\n");
            textData.append("CARRETERA GUADALAJARA-NOGALES NO.6755\n");
            textData.append("SAN JUAN DE OCOTAN,ZAPOPAN,JAL 45010\n");
            textData.append("RFC CEX-980921-3U5 TEL.(0133)3682-1040\n");
            //textData.append("------------------------------\n");
            textData.append("Publico en General\n");
            //textData.append("\n");
            textData.append("Conductor     : SIN CONDUCTOR\n");
            textData.append("No. Econ.     :\n");
            textData.append("Placas        :\n");
            textData.append("Kilometraje   :0\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("TICKET    :"+String.valueOf(ticket.getInt("folio"))+"   BOMBA: "+String.valueOf(ticket.getInt("bomba"))+"\n");
            textData.append("VENDEDOR  :"+String.valueOf(ticket.getString("despachador")).toUpperCase()+"\n");
            textData.append("PRECIO    : $ "+String.valueOf(ticket.getDouble("precio"))+"\n");
            textData.append("VOLUMEN   :"+String.valueOf(ticket.getDouble("cantidad"))+" LITROS "+String.valueOf(ticket.getString("producto")).toUpperCase()+"\n");
            textData.append("IMPORTE   : $ "+String.valueOf(ticket.getDouble("total"))+"\n");
            textData.append("(quinientos treinta pesos con treinta y ocho centavo(s) 38/100 M.N.\n");
            //textData.append("\n");
            textData.append("\n");
            textData.append("------------------------------\n");
            textData.append("NOMBRE CONDUCTOR\n");
            //textData.append("\n");
            //textData.append("\n");
            textData.append("\n");
            textData.append("______________________________\n");
            textData.append("F I R M A\n");
            textData.append("________________________________\n");
            textData.append("|       AHORRE TIEMPO !!!       |\n");
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

            method = "addBarcode";
            mPrinter.addBarcode(String.valueOf(ticket.getInt("folio")),
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            ShowMsg.showException(e, method, activity);
            return false;
        }

        textData = null;

        return true;
    }
    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private void disconnectPrinter() {
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

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, makeErrorMessage(status), activity);

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
}
