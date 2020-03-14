package cg.ce.app.chris.com.cgce;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageButton;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by chris on 20/06/17.
 */

public class ClassImpresionAceite extends AsyncTask<JSONObject,String,Boolean> implements ReceiveListener {
    ProgressDialog pdLoading ;
    Activity activity;
    ImageButton btn;
    Context context;
    private Printer mPrinter = null;
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    JSONObject aceite;
    JSONArray items;
    int impreso;
    cgticket cia = new cgticket();

    public ClassImpresionAceite (Activity activity, Context context, ImageButton btn, JSONObject jsonObject) throws JSONException {
        this.activity=activity;
        this.context=context;
        this.btn=btn;
        //classImpresion = new ClassImpresionTicket(activity,context,btn,jsonObject);
        pdLoading = new ProgressDialog(activity);
        this.aceite=jsonObject;
        this.items=aceite.getJSONArray("items");
        if (aceite.has("impreso")) {
            try {
                this.impreso = aceite.getInt("impreso");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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

    @Override
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
    public void updateButtonState(boolean state) {
        btn.setEnabled(state);
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
    public boolean createReceiptData() {
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(activity.getResources(), R.drawable.isologo_repsol);
        StringBuilder textData = new StringBuilder();
        Numero_a_Letra letra = new Numero_a_Letra();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }


        try {
            ValidacionFlotillero vf = new ValidacionFlotillero();

            JSONObject datos_domicilio = cia.estacion_domicilio(context);
            JSONObject vehiculo= new JSONObject();
            if (aceite.optString("tag").length()>0 ) {
                vehiculo = vf.get_vehiculo(context, aceite.getString("tag"));
            }else{
                Log.w("null","no");
                vehiculo.put("rsp","Sin Chofer");
                vehiculo.put("nroeco","Sin No Economico");
                vehiculo.put("ultodm","Sin Odometro");
                vehiculo.put("placa","Sin Placa");
            }
            String titulo="",folio_impreso="",cliente="",venta="";
            //impreso=cia.cantimpresoaceite(context,aceite);
            //if (impreso == 0) {
            //    titulo = "O R I G I N A L";
            //    folio_impreso = aceite.getString("nota");
            //} else if(impreso==1){
            //    titulo = "C O P I A";
            //    folio_impreso = "C O P I A";
            //}
            titulo = "O R I G I N A L";
            if(aceite.has("rfc")){
                if (!aceite.getString("rfc").equals("AAAA000000AAA")){
                    cliente=aceite.getString("rfc");
                    venta="VENTA ACEITE CREDITO";
                }else{
                    cliente="Publico General";
                    venta="VENTA ACEITE CONTADO";
                }
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
            textData.append("" + titulo + "\n");
            textData.append(venta+"\n");

            textData.append("FECHA: " + aceite.getString("fecha")+"\n");
            textData.append(datos_domicilio.getString("regimen")+"\n");
            textData.append("\n");
            textData.append("LUGAR DE EXPEDICION:\n");
            textData.append("ESTACION: "+datos_domicilio.getString("estacion")+" "+datos_domicilio.getString("cveest")+"\n");
            textData.append(datos_domicilio.getString("calle")+" "+datos_domicilio.getString("exterior")+" "+datos_domicilio.getString("interior")+", "+datos_domicilio.getString("colonia")+", "+datos_domicilio.getString("cp")+"\n");
            textData.append(datos_domicilio.getString("localidad")+", "+datos_domicilio.getString("municipio")+", "+datos_domicilio.getString("estado")+", "+datos_domicilio.getString("pais")+"\n");
            textData.append("RFC "+datos_domicilio.getString("rfc")+" TEL."+datos_domicilio.getString("telefono")+"\n");
            //textData.append("------------------------------\n");
            if (aceite.has("cliente")) {
                textData.append(aceite.getString("cliente") + "-" + aceite.getString("rfc") + "\n");
            }
            //textData.append("\n");
            if(aceite.has("codcli")) {
                textData.append("Conductor     : "+vehiculo.getString("rsp")+"\n");
                textData.append("No. Econ.     : "+vehiculo.getString("nroeco")+"\n");
                textData.append("Placas        : "+vehiculo.getString("placa")+"\n");
                textData.append("Kilometraje   : "+aceite.getString("odm")+"\n");
                textData.append("------------------------------\n");
            }
            textData.append("\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("TICKET    : N/A" );//+  String.valueOf(aceite.getInt("nota")) + "\n");
            textData.append("\n");
            textData.append("VENDEDOR  : " + String.valueOf(cia.nombre_depsachador(context)).toUpperCase() + "\n");
            textData.append("PIEZAS    : " + aceite.getString("qty") + "\n");
            textData.append("IMPORTE   : " + aceite.getString("total") + "\n");
            textData.append(letra.Convertir(String.valueOf(aceite.getDouble("total_print")),true)+"\n");
            textData.append("\n");
            textData.append("PRODUCTOS\n");
            textData.append("================================================\n");
            textData.append("\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            method = "addText";
            Log.w("json",aceite.toString());

            for (int i = 0; i < items.length(); i++) {
                JSONObject jo = items.getJSONObject(i);
                String cantidad = String.valueOf(1);
                String producto = jo.getString("descripcion").toUpperCase();
                String precio = String.valueOf(jo.getDouble("precio"));
                textData.append(cantidad +" "+producto +" "+precio);

                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
                textData.append("\n");
            }
            textData.append("\n");
            textData.append("================================================\n");
            textData.append("GRACIAS POR SU PREFERENCIA!!! \n");
            textData.append("\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
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
                // Do nothing
            }
            return false;
        }

        try {
            //createReceiptData();
            mPrinter.sendData(Printer.PARAM_DEFAULT);
           // cia.updateaceiteimpresion(context,aceite);
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
            if (target.equals("Sin impresora ")){
                //Toast.makeText(context,target,Toast.LENGTH_LONG).show();
            }else {
                mPrinter.connect(target, Printer.PARAM_DEFAULT);
            }
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
}
