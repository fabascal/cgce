package cg.ce.app.chris.com.cgce.dialogos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cg.ce.app.chris.com.cgce.ActivityTicket;
import cg.ce.app.chris.com.cgce.ClassImpresionTicket;
import cg.ce.app.chris.com.cgce.Contents;
import cg.ce.app.chris.com.cgce.Credito;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.LogCE;
import cg.ce.app.chris.com.cgce.Numero_a_Letra;
import cg.ce.app.chris.com.cgce.Printing.TicketPrint;
import cg.ce.app.chris.com.cgce.QRCodeEncoder;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.RfidCredito;
import cg.ce.app.chris.com.cgce.ShowMsg;
import cg.ce.app.chris.com.cgce.ValidacionFlotillero;
import cg.ce.app.chris.com.cgce.VentaActivity;
import cg.ce.app.chris.com.cgce.cgticket;
import cg.ce.app.chris.com.cgce.common.RequestPermission;
import cg.ce.app.chris.com.cgce.common.Variables;

import static android.content.Context.WINDOW_SERVICE;


/**
 * Created by chris on 17/08/17.
 */

public class fab_contado extends DialogFragment implements View.OnClickListener, ReceiveListener {

    Spinner mSpinner;
    ImageButton imgbtn_cancel,imgbtn_imprimir;
    private static ArrayList<String>bombas;
    LinearLayout root;
    JSONObject ticket = null;
    String rut;
    int tiptrn;
    cgticket cg = new cgticket();
    Spinner spn_metodo,spn_metodo_den;
    List tpv ;
    String marca;
    Drawable icon;
    ImageView ivicon, dispensario;
    Variables variables = new Variables();
    boolean FlagImpreso=false, FlagReimpreso=false, state_btn;
    /*Elementos de SDK Epson*/
    private Printer mPrinter = null;
    private Context mContext = null;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds
    /*Elemento Progress para imprimir*/
    ProgressDialog pdLoading;
    Drawable image;
    LogCE logCE = new LogCE();
    RequestPermission requestPermission = new RequestPermission();
    Integer flag = 0;
    cgticket cgticket = new cgticket();
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    Activity mActivity;


    public static fab_contado newInstance(String title, ArrayList<String> data){
        fab_contado fragment = new fab_contado();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        bombas=data;

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        root = (LinearLayout) inflater.inflate(R.layout.dialog_fab_contado, null);
        mContext = this.getActivity();
        /*Se inicializa el objeto impresora*/
        initializeObject();
        pdLoading = new ProgressDialog(getActivity());
        spn_metodo = root.findViewById(R.id.spn_metedo);
        spn_metodo_den = root.findViewById(R.id.spn_metodo_den);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(root.getContext(),
                R.array.mPagos_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_tiptrn);
// Apply the adapter to the spinner
        spn_metodo.setAdapter(adapter);
        mSpinner=(Spinner)root.findViewById(R.id.spn_fab_contado);
        ArrayAdapter NoCoreAdapter = new ArrayAdapter(root.getContext(),
                R.layout.spinner_bombas_dialog, bombas);
        mSpinner.setAdapter(NoCoreAdapter);
        imgbtn_cancel = (ImageButton) root.findViewById(R.id.imgbtn_cancel);
        imgbtn_cancel.setOnClickListener(this);
        imgbtn_imprimir = (ImageButton) root.findViewById(R.id.imgbtn_imprimir);
        imgbtn_imprimir.setOnClickListener(this);
        ivicon = (ImageView) root.findViewById(R.id.icon);
        dispensario = (ImageView) root.findViewById(R.id.dispensario);
        BrandSharedPreferences();

        spn_metodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                view_spn_metodo_den();
                try {
                    fill_spn_metodo_den();
                } catch (ClassNotFoundException | SQLException | java.lang.InstantiationException |
                        JSONException | IllegalAccessException e) {
                    new android.support.v7.app.AlertDialog.Builder(fab_contado.this.getActivity())
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new AlertDialog.Builder(getActivity())
                    .setView(root)
                    .setIcon(icon)
                    .create();
        }

        return null;
    }

    public void fill_spn_metodo_den() throws ClassNotFoundException, SQLException,
            java.lang.InstantiationException, JSONException, IllegalAccessException {
        if (spn_metodo.getSelectedItem().toString().equals("T. Credito") || spn_metodo.
                getSelectedItem().toString().equals("T. Debito")){
            tpv = cg.getTPVs(getActivity(), "1");
            ArrayAdapter MonederoAdapter = new ArrayAdapter(getActivity(),
                    android.R.layout.simple_spinner_item, tpv);
            MonederoAdapter.setDropDownViewResource(R.layout.spinner_tiptrn);
            spn_metodo_den.setAdapter(MonederoAdapter);
        }else if(spn_metodo.getSelectedItem().toString().equals("Monederos")){
            tpv = cg.getTPVs(getActivity(), "0");
            ArrayAdapter MonederoAdapter = new ArrayAdapter(getActivity(),
                    android.R.layout.simple_spinner_item, tpv);
            MonederoAdapter.setDropDownViewResource(R.layout.spinner_tiptrn);
            spn_metodo_den.setAdapter(MonederoAdapter);
        }
    }

    public void view_spn_metodo_den(){
        if (spn_metodo.getSelectedItem().toString().equals("T. Credito") || spn_metodo.
                getSelectedItem().toString().equals("T. Debito") || spn_metodo.
                getSelectedItem().toString().equals("Monederos") ){
            spn_metodo_den.setVisibility(View.VISIBLE);
        }else{
            spn_metodo_den.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgbtn_imprimir:
                LogCE logCE = new LogCE();
                try {
                    pdLoading = new ProgressDialog(getActivity());
                    pdLoading.setMessage("Imprimiendo..."); // Setting Message
                    pdLoading.setTitle(marca); // Setting Title
                    pdLoading.setIcon(icon);
                    pdLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                    pdLoading.show(); // Display Progress Dialog
                    pdLoading.setCancelable(false);
                    ticket = cgticket.consulta_servicio(mContext, String.valueOf(mSpinner.getSelectedItem()));
                    Log.w("Ticket rut", String.valueOf(ticket));
                    Integer impreso=cgticket.cant_impreso(getActivity(),ticket.getString(variables.KEY_TICKET_NROTRN));
                    ticket.put(variables.KEY_IMPRESO,impreso);
                    new Thread(new Runnable() {
                        public void run() {
                            PrintReceip();
                            pdLoading.dismiss();
                        }
                    }).start();
                } catch (SQLException | IllegalAccessException | java.lang.InstantiationException |
                        ClassNotFoundException | JSONException | SocketException e) {
                    logCE.EscirbirLog2(mContext,"ActivityTicket_PrintReceip - " + e);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new android.support.v7.app.AlertDialog.Builder((Activity) mContext)
                                    .setTitle(R.string.error)
                                    .setIcon(icon)
                                    .setMessage(String.valueOf(e))
                                    .setPositiveButton(R.string.btn_ok,null).show();
                        }
                    });
                    Log.w("Error",String.valueOf(e));

                }
                break;
            case R.id.imgbtn_cancel:
                getDialog().dismiss();
                break;
        }
    }
    private void PrintReceip(){

        this.updateButtonState(false);
        //se valida el metodo de pago
        if (spn_metodo.getSelectedItem().toString().equals("Efectivo")){
            rut="1|Efectivo";
            tiptrn=49;
        }else if(spn_metodo.getSelectedItem().toString().equals("T. Credito")){
            rut="2|T. Credito" + "|" + spn_metodo_den.getSelectedItem().toString();
            FlagReimpreso=true;
            tiptrn=51;
        }else if(spn_metodo.getSelectedItem().toString().equals("T. Debito")){
            rut="3|T. Debito" + "|" + spn_metodo_den.getSelectedItem().toString();;
            FlagReimpreso=true;
            tiptrn=51;
        }else if(spn_metodo.getSelectedItem().toString().equals("Anticipos")){
            rut="4|Anticipos";
            tiptrn=50;
        }else if(spn_metodo.getSelectedItem().toString().equals("Combu-Vale")){
            rut="5|Combu-Vale";
            tiptrn=50;
        }
        try {
            ticket.put(variables.KEY_TIPTRN,tiptrn);
            int impreso = cg.cant_impreso(mContext, ticket.getString(variables.KEY_TICKET_NROTRN));
            if (impreso==1){
                FlagImpreso = true;
                FlagReimpreso = true;
            }
            if (!FlagImpreso){
                if (impreso==10) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ticket.put(variables.KEY_RUT,rut);
                                cg.guardarnrotrn(getActivity(), ticket, 1);
                            } catch (ClassNotFoundException | SQLException |
                                    java.lang.InstantiationException | IllegalAccessException |
                                    JSONException | SocketException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
            if (!runPrintReceiptSequence()) {
                updateButtonState(true);
                if (pdLoading != null) {
                    pdLoading.dismiss();
                }

            }

        } catch ( final ClassNotFoundException | SQLException | java.lang.InstantiationException |
                IllegalAccessException | JSONException | Epos2Exception | WriterException e) {
            if (pdLoading != null) {
                pdLoading.dismiss();
            }
            logCE.EscirbirLog2(mContext,"ActivityTicket_PrintReceip - " + e);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new android.support.v7.app.AlertDialog.Builder((Activity) mContext)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                }
            });
            e.printStackTrace();
        }
    }
    private boolean runPrintReceiptSequence() throws SQLException, WriterException,
            java.lang.InstantiationException, JSONException, ClassNotFoundException, IllegalAccessException,
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
            java.lang.InstantiationException, JSONException, IllegalAccessException, WriterException {
        Log.w("FlagImpreso",String.valueOf(FlagImpreso));
        Log.w("FlagReimpreso",String.valueOf(FlagReimpreso));
        Log.w("Ticket",String.valueOf(ticket));
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(((Activity)mContext).getResources(), R.drawable.logo_impresion);
        switch (marca) {
            case "Combu-Express":
                logoData = BitmapFactory.decodeResource(((Activity)mContext).getResources(), R.drawable.logo_impresion);
                break;
            case "Repsol":
                logoData = BitmapFactory.decodeResource(((Activity)mContext).getResources(), R.drawable.logo_impresion_repsol);
                break;
            case "Ener":
                logoData = BitmapFactory.decodeResource(((Activity)mContext).getResources(), R.drawable.logo_impresion_ener);
                break;
            case "Total":
                logoData = BitmapFactory.decodeResource(((Activity)mContext).getResources(), R.drawable.logo_impresion_total);
                break;
        }
        StringBuilder textData = new StringBuilder();
        Numero_a_Letra letra = new Numero_a_Letra();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;
        Point point = new Point();
        WindowManager manager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? 380 : 380;
        if (mPrinter == null) {
            return false;
        }
        JSONObject datos_domicilio = cgticket.estacion_domicilio(mContext);
        JSONObject vehiculo = new JSONObject();
        String titulo = "", folio_impreso = "", cliente = "", venta = "", tpv = "";
        String metodoPago = "";

        if (!FlagImpreso) {
            titulo = "O R I G I N A L";
            metodoPago = rut ;
            folio_impreso = ticket.getString(variables.KEY_TICKET_NROTRN) + "0";

        } else if (FlagImpreso) {
            titulo = "C O P I A";
            folio_impreso = "C O P I A";
            if (ticket.has(variables.KEY_RUT)) {
                metodoPago = ticket.getString(variables.KEY_RUT);
            }
        }
        vehiculo = cgticket.get_vehiculo(mContext, ticket.getString(variables.KEY_TICKET_NROTRN), String.valueOf(mSpinner.getSelectedItem()));
        venta = CalculateVenta(ticket.getString(variables.KEY_TICKET_CLIENTE_TIPVAL));
        method = "addTextAlign";
        mPrinter.addTextAlign(Printer.ALIGN_CENTER);
        method = "addImage";
        if (!FlagImpreso){
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
            if (ticket.has(variables.KEY_TICKET_DENCLI)) {
                textData.append(ticket.getString(variables.KEY_TICKET_DENCLI));
            }
            textData.append(cliente + "\n");
            textData.append(metodoPago + "\n");
            if (ticket.has(variables.KEY_CODCLI)) {
                if (vehiculo.has("rsp")) {
                    textData.append("Conductor     : " + vehiculo.getString("rsp") + "\n");
                }
                if (vehiculo.has("nroeco")) {
                    textData.append("No. Econ.     : " + vehiculo.getString("nroeco") + "\n");
                }
                if (vehiculo.has("placa")) {
                    textData.append("Placas        : " + vehiculo.getString("placa") + "\n");
                }
                if (ticket.has(variables.KEY_ODM)) {
                    textData.append("Kilometraje   : " + ticket.getString(variables.KEY_ODM) + "\n");
                }
                textData.append("------------------------------\n");
            }
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("TICKET    : " + folio_impreso + "   BOMBA : " + String.valueOf(mSpinner.getSelectedItem()) + "\n");
            textData.append("FECHA: " + ticket.getString(variables.KEY_TICKET_FECHA) + "  HORA: " + ticket.getString(variables.KEY_TICKET_HORA) + "\n");
            textData.append("VENDEDOR  : " + String.valueOf(ticket.getString(variables.KEY_TICKET_DESPACHADOR)).toUpperCase() + "\n");
            textData.append("PRECIO    : $ " + String.format("%.2f", Double.parseDouble(formateador2.format(Double.parseDouble(ticket.getString(variables.KEY_TICKET_PRECIO))))) + "\n");
            textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(Double.parseDouble(ticket.getString(variables.KEY_TICKET_CANTIDAD))))) + " LITROS " + String.valueOf(ticket.getString(variables.KEY_TICKET_PRODUCTO)).toUpperCase() + "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("IMPORTE   : $ " + formateador2.format(Double.parseDouble(ticket.getString(variables.KEY_TICKET_TOTAL))) + "\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append(letra.Convertir(String.valueOf(formateador21.format(Double.parseDouble(ticket.getString(variables.KEY_TICKET_TOTAL)))), true) + "\n");
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
            Bitmap qrrespol = null;
            QRCodeEncoder qrCodeEncoder1 = new QRCodeEncoder(repsolQR(datos_domicilio),
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
            textData.append("\n");
            mPrinter.addText(textData.toString());
            mPrinter.addCut(Printer.CUT_FEED);
        }
        ValidacionFlotillero vf = new ValidacionFlotillero();
        Integer sorteo = vf.validar_sorteo(mContext);
        String inicio = vf.sorteo_inicio(mContext);
        String fin = vf.sorteo_fin(mContext);
        /*funcion para imprimir copias*/
        if (FlagReimpreso){
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
            if (ticket.has(variables.KEY_TICKET_DENCLI)) {
                textData.append(ticket.getString(variables.KEY_TICKET_DENCLI));
            }
            textData.append(cliente + "\n");
            textData.append(metodoPago + "\n");
            //textData.append("\n");
            if (ticket.has(variables.KEY_CODCLI)) {
                if (vehiculo.has("rsp")) {
                    textData.append("Conductor     : " + vehiculo.getString("rsp") + "\n");
                }
                if (vehiculo.has("nroeco")) {
                    textData.append("No. Econ.     : " + vehiculo.getString("nroeco") + "\n");
                }
                if (vehiculo.has("placa")) {
                    textData.append("Placas        : " + vehiculo.getString("placa") + "\n");
                }
                if (ticket.has(variables.KEY_ODM)) {
                    textData.append("Kilometraje   : " + ticket.getString(variables.KEY_ODM) + "\n");
                }
                textData.append("------------------------------\n");
            }
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("TICKET    : " + "C O P I A" + "   BOMBA : " + ticket.getString(variables.KEY_TICKET_BOMBA) + "\n");
            textData.append("FECHA: " + ticket.getString(variables.KEY_TICKET_FECHA) + "  HORA: " + ticket.getString(variables.KEY_TICKET_HORA) + "\n");
            textData.append("VENDEDOR  : " + String.valueOf(ticket.getString(variables.KEY_TICKET_DESPACHADOR)).toUpperCase() + "\n");
            textData.append("PRECIO    : $ " + String.format("%.2f", Double.valueOf(formateador2.format(Double.parseDouble(ticket.getString(variables.KEY_TICKET_PRECIO))))) + "\n");
            textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(Double.parseDouble(ticket.getString(variables.KEY_TICKET_CANTIDAD))))) + " LITROS " + String.valueOf(ticket.getString(variables.KEY_TICKET_PRODUCTO)).toUpperCase() + "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("IMPORTE   : $ " + formateador2.format(Double.parseDouble(ticket.getString(variables.KEY_TICKET_TOTAL))) + "\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            textData.append(letra.Convertir(String.valueOf(formateador21.format(Double.parseDouble(ticket.getString(variables.KEY_TICKET_TOTAL)))), true) + "\n");
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
            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
            textData = null;
        }
        /*funcion para los sorteos*/
        if (Integer.parseInt(ticket.getString(variables.KEY_IMPRESO)) == 0 || Integer.parseInt(ticket.getString(variables.KEY_IMPRESO)) == 10) {
            if (sorteo > 0) {
                if (Double.parseDouble(ticket.getString(variables.KEY_TICKET_TOTAL)) >= 200) {
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
                    textData.append("                MONTO   :   " + formateador2.format(ticket.getString(variables.KEY_TICKET_TOTAL)) + "\n");
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
                            + "&fo=" + String.valueOf(folio_impreso) + "&fe=" +
                            ticket.getString(variables.KEY_TICKET_FECHA) + "&mo=" + formateador2.format(ticket.getString(variables.KEY_TICKET_TOTAL))
                            + "&pr=" + ticket.getString(variables.KEY_TICKET_ID_PRODUCTO);
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
                method = "addCut";
                mPrinter.addCut(Printer.CUT_FEED);
                textData = null;
            }
        }
        /*mPrinter.addText(textData.toString());*/

        return true;
    }
    public String repsolQR(JSONObject jsdomicilio) throws JSONException {
        Double ieps = Double.parseDouble(ticket.getString(variables.KEY_TICKET_IEPS));
        Double iva_factor = Double.parseDouble(ticket.getString(variables.KEY_TICKET_IVA));
        Double lts = Double.parseDouble(ticket.getString(variables.KEY_TICKET_CANTIDAD));
        Double pre = Double.parseDouble(ticket.getString(variables.KEY_TICKET_PRECIO));
        Double ivaentre = Double.valueOf("1" + iva_factor);
        Double precioneto = ((Double.valueOf(pre) - ieps) / ivaentre) * 100;
        Double iva = ((precioneto * lts) * iva_factor) / 100;
        Double subtotal = (precioneto + ieps) * lts;
        String precio_total = String.valueOf(precioneto + ieps);
        String substr = ".";
        String inicio = precio_total.substring(0, precio_total.indexOf(substr));
        String fin = precio_total.substring(precio_total.indexOf(substr) + substr.length());
        String precio_impresion = inicio + "." + fin.substring(0, 2);
        String qr = "COMBUGO|" + ticket.getString(variables.KEY_TICKET_CVEEST) + "|" +
                jsdomicilio.getString("estacion") + "|" + ticket.getString(variables.KEY_TICKET_FECHA) +
                "|" + ticket.getString(variables.KEY_TICKET_HORA) + "|" + precio_impresion + "|" +
                formateador2.format(iva) + "|" + ticket.getString(variables.KEY_TICKET_TOTAL) + "|" +
                ticket.getString(variables.KEY_TICKET_NROTRN) + "|";
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
            logCE.EscirbirLog2(mContext,"Credito_printData - " + e);
            ShowMsg.showException(e, "sendData", mContext);
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
            logCE.EscirbirLog2(mContext,"Credito_connectPrinter - " + e);
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }
        return true;
    }
    private String CalculateMetoPago(String data){
        String res = "Cliente Contado";
        switch (data){
            case "3":
                res = "Cliente Credito";
                break;
            case "4":
                res = "Cliente Debito";
                break;
            case "0":
                res = "Cliente Contado";
                break;
        }
        return res;
    }
    private String CalculateVenta(String data){
        String res = "Contado";
        switch (data){
            case "3":
                res = "Credito";
                break;
            case "4":
                res = "Debito";
                break;
            case "0":
                res = "Contado";
                break;
        }
        return res;
    }
    private boolean initializeObject() {
        try {
            mPrinter = new Printer(mPrinter.TM_M30, mPrinter.MODEL_ANK, mContext);
        } catch (Exception e) {
            logCE.EscirbirLog2(mContext,"ActivityTicket_initializeObject - " + e);
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }
        mPrinter.setReceiveEventListener(this);
        return true;
    }

    @Override
    public void onPtrReceive(final Printer printerObj, int code, final PrinterStatusInfo status,
                             final String printJobId) {
        Log.w("Code", String.valueOf(code));
        if (code == 0) {
            try {
                cg.actualizar_cant_impreso(mContext,
                        ticket.getString(variables.KEY_TICKET_NROTRN));
            } catch (ClassNotFoundException | JSONException | java.lang.InstantiationException |
                    IllegalAccessException | SQLException e) {
                e.printStackTrace();
            }
            getDialog().dismiss();
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
    private void updateButtonState(final boolean state) {
        state_btn = state;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgbtn_imprimir.setEnabled(state_btn);
            }
        });

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
                        logCE.EscirbirLog2(mContext,"ActivityTicket_disconnectPrinter - " + e);
                        ShowMsg.showException(e, "disconnect", mContext);
                        break;
                    }
                } else {
                    logCE.EscirbirLog2(mContext,"ActivityTicket_disconnectPrinter - " + e);
                    ShowMsg.showException(e, "disconnect", mContext);

                    break;
                }
            }
        }

        mPrinter.clearCommandBuffer();
    }

    public void BrandSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Brand", Context.MODE_PRIVATE);
        marca=sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express");
        switch (Objects.requireNonNull(sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express"))) {
            case "Combu-Express":
                dispensario.setImageDrawable(getActivity().getDrawable(R.drawable.dispensario));
                ivicon.setImageDrawable(getActivity().getDrawable(R.drawable.combuito));
                imgbtn_imprimir.setImageResource(R.drawable.printbtn_pressed);
                icon = getActivity().getDrawable(R.drawable.combuito);
                break;
            case "Repsol":
                dispensario.setImageDrawable(getActivity().getDrawable(R.drawable.dispensariorespol));
                ivicon.setImageDrawable(getActivity().getDrawable(R.drawable.isologo_repsol));
                imgbtn_imprimir.setImageResource(R.drawable.printbtn_pressed_repsol);
                icon = getActivity().getDrawable(R.drawable.isologo_repsol);
                break;
            case "Ener":
                dispensario.setImageDrawable(getActivity().getDrawable(R.drawable.dispensarioener));
                ivicon.setImageDrawable(getActivity().getDrawable(R.drawable.logo_impresion_ener));
                imgbtn_imprimir.setImageResource(R.drawable.printbtn_pressed_ener);
                icon = getActivity().getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                dispensario.setImageDrawable(getActivity().getDrawable(R.drawable.dispensariototal));
                ivicon.setImageDrawable(getActivity().getDrawable(R.drawable.total));
                imgbtn_imprimir.setImageResource(R.drawable.printbtn_pressed_total);
                icon = getActivity().getDrawable(R.drawable.total);
                break;
        }
    }
}
