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

import static android.content.Context.WINDOW_SERVICE;

public class ClassImpresionSorteo extends AsyncTask<JSONObject,String,Boolean> implements ReceiveListener,SorteoListener{

    ProgressDialog pdLoading ;
    Activity activity;
    ImageButton btn;
    Context context;
    private Printer mPrinter = null;
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");

    int impreso1,impreso_calculado;
    cgticket tf = new cgticket();
    LogCE logCE = new LogCE();
    JSONObject jsonObjectError = new JSONObject();
    JSONObject js;

    public ClassImpresionSorteo(Activity activity, Context context, JSONObject jsonObject){
        this.activity=activity;
        this.context=context;
        //this.btn=btn;
        this.js=jsonObject;
        pdLoading = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //this method will be running on UI thread
        if (!js.has("validador")) {
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
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result.equals(true)) {
            if (pdLoading != null) {
                pdLoading.dismiss();
            }

        }
    }

    public void updateButtonState(boolean state) {
        //btn.setEnabled(state);
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
            try {
                logCE.EscirbirLog(activity,jsonObjectError.put("impresion_printer",e));
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
                textData.append("  ESTACION   :   " + js.getString("nombre_estacion")+"\n");
                textData.append("     FOLIO   :   " + js.getString("folio")+"\n");
                textData.append("     FECHA   :   " + js.getString("fecha_alta") +"\n");
                textData.append("     MONTO   :   " + js.getString("importe") +"\n");
                textData.append("  PRODUCTO   :   " + js.getString("producto") +"\n");

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
            try {
                logCE.EscirbirLog(activity,jsonObjectError.put("create_RecipeData",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            Log.w("Errorimprimir",e);
            ShowMsg.showException(e, method,activity);
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

            ShowMsg.showMsg(makeErrorMessage(status), activity);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                try {
                    logCE.EscirbirLog(activity,jsonObjectError.put("printer_disconnect",ex));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                // Do nothing
            }
            return false;
        }

        try {


            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            try {
                logCE.EscirbirLog(activity,jsonObjectError.put("sendData",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            ShowMsg.showException(e, "sendData", activity);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                try {
                    logCE.EscirbirLog(activity,jsonObjectError.put("printer_disconect",ex));
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
            DataBaseManager manager = new DataBaseManager(activity);
            String target = manager.target();//"BT:00:01:90:C6:81:22";
            //activity.Toast.makeText(context,target,Toast.LENGTH_LONG).show();
            mPrinter.connect(target ,Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            try {
                logCE.EscirbirLog(activity,jsonObjectError.put("printer_conect",e));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            Log.w("Errorimprimier",e);
            ShowMsg.showException(e, "connect", activity);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            try {
                logCE.EscirbirLog(activity,jsonObjectError.put("beginTransaction",e));
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
                    logCE.EscirbirLog(activity,jsonObjectError.put("printer_disconect",e));
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
            warningsMsg += activity.getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += activity.getString(R.string.handlingmsg_warn_battery_near_end);
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

        try {
            logCE.EscirbirLog(activity,jsonObjectError.put("printer",msg));
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
                logCE.EscirbirLog(activity,jsonObjectError.put("endTransaction",e));
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
                logCE.EscirbirLog(activity,jsonObjectError.put("disconnect",e));
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

    @Override
    public void processFinish3(String output) {
        try {
            Log.w("js1", output);
            js= new JSONObject(output);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, makeErrorMessage(status), activity);

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
