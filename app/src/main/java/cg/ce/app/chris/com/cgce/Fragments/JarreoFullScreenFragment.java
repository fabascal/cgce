package cg.ce.app.chris.com.cgce.Fragments;


import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import cg.ce.app.chris.com.cgce.ActivityTicket;
import cg.ce.app.chris.com.cgce.ContadoActivity;
import cg.ce.app.chris.com.cgce.Contents;
import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.DataBaseManager;
import cg.ce.app.chris.com.cgce.LogCE;
import cg.ce.app.chris.com.cgce.Numero_a_Letra;
import cg.ce.app.chris.com.cgce.QRCodeEncoder;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.ShowMsg;
import cg.ce.app.chris.com.cgce.ValidacionFlotillero;
import cg.ce.app.chris.com.cgce.ValidateTablet;
import cg.ce.app.chris.com.cgce.VentaActivity;
import cg.ce.app.chris.com.cgce.cgticket;
import cg.ce.app.chris.com.cgce.common.Variables;

import static android.content.Context.WINDOW_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class JarreoFullScreenFragment extends DialogFragment implements com.epson.epos2.printer.ReceiveListener{

    Toolbar toolbar;
    private final static String QUERY="select numero_logico as logico from posicion ";
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;
    Spinner spn_tiptrn, spn_fab_contado;
    ImageButton btn_print;
    cgticket cgticket = new cgticket();
    ValidateTablet tablet = new ValidateTablet();
    ImageView dispensario, jarreo;
    Variables var = new Variables();
    Drawable icon;
    LogCE logCE = new LogCE();
    Context mContext;
    boolean state_btn;
    /*Elementos de formato*/
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    DecimalFormat formateador21 = new DecimalFormat("###,###.##");
    /*Elementos de SDK Epson*/
    private Printer mPrinter = null;
    private static final int REQUEST_PERMISSION = 100;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds
    /*Elemento Progress para imprimir*/
    ProgressDialog pdLoading;
    String flag_brand, tiptrnSelect;
    JSONObject ticket = new JSONObject();

    public JarreoFullScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        mContext = getActivity().getApplicationContext();
        initializeObject();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_jarreo_full_screen, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.jarreo);
        spn_tiptrn = view.findViewById(R.id.spn_tiptrn);
        spn_fab_contado = view.findViewById(R.id.spn_fab_contado);
        btn_print = view.findViewById(R.id.btn_print);
        dispensario = view.findViewById(R.id.dispensario);
        jarreo = view.findViewById(R.id.jarreo);
        ElementBrandSharedPreferences();
        try {
            fillPosicion();
            fillTiptrn();
        } catch (SQLException | JSONException e) {
            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_LONG).show();
            logCE.EscirbirLog2(getActivity().getApplicationContext(),"JarreoFullScreenFragment_onCreateView - " + e);
            new AlertDialog.Builder(getActivity().getApplicationContext())
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tiptrnSelect = spn_tiptrn.getSelectedItem().toString();
                String posicion = spn_fab_contado.getSelectedItem().toString();
                String nrotrn;
                /*Iniciamos variable en autojarreo que es el numero 65*/
                String tiptrn = "65";
                if (tiptrnSelect.equals("Jarreo")){
                    tiptrn = "74";
                }
                try {
                    pdLoading = new ProgressDialog(getActivity());
                    pdLoading.setMessage("Imprimiendo..."); // Setting Message
                    pdLoading.setTitle(flag_brand); // Setting Title
                    pdLoading.setIcon(icon);
                    pdLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                    pdLoading.show(); // Display Progress Dialog
                    pdLoading.setCancelable(false);

                    ticket = cgticket.consulta_servicio(getActivity(),posicion);
                    nrotrn = ticket.getString(Variables.KEY_TICKET_NROTRN);
                    Log.w("nrotrn",nrotrn);
                    cgticket.setTipTrn(getActivity(),tiptrn,nrotrn);

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                PrintReceip();
                            } catch (IllegalAccessException | java.lang.InstantiationException |
                                    JSONException | SQLException | Epos2Exception | WriterException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            pdLoading.dismiss();
                        }
                    }).start();
                } catch (SQLException | IllegalAccessException | java.lang.InstantiationException |
                        ClassNotFoundException | JSONException | SocketException e) {
                    logCE.EscirbirLog2(getActivity().getApplicationContext(),"JarreoFullScreenFragment_onCreateView - " + e);
                    new AlertDialog.Builder(getActivity().getApplicationContext())
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(),tiptrn +"|"+posicion,Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }
    private void PrintReceip() throws IllegalAccessException, java.lang.InstantiationException,
            JSONException, SQLException, Epos2Exception, WriterException, ClassNotFoundException {
        this.updateButtonState(false);
        if (!runPrintReceiptSequence()) {
            updateButtonState(true);
            if (pdLoading != null) {
                pdLoading.dismiss();
            }
        }
    }
    private boolean runPrintReceiptSequence() throws SQLException, WriterException,
            java.lang.InstantiationException, JSONException, ClassNotFoundException, IllegalAccessException, Epos2Exception {
        if (!createReceiptData()) {
            return false;
        }
        if (!printData()) {
            return false;
        }
        return true;
    }
    public boolean createReceiptData() throws Epos2Exception, ClassNotFoundException, SQLException,
            java.lang.InstantiationException, JSONException, IllegalAccessException {
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
        WindowManager manager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
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
        mPrinter.addTextAlign(Printer.ALIGN_CENTER);
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
        textData.append(ticket.getString(var.KEY_TICKET_CVEEST) + "\n");
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
        textData.append("Lugar de Expedicion" + "\n");
        textData.append(datos_domicilio.getString("municipio") + " " + datos_domicilio.getString("estado") + "\n");
        textData.append("\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("***** " + tiptrnSelect + " *****" + "\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append("\n");
        mPrinter.addTextAlign(Printer.ALIGN_LEFT);
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("TICKET    : " + folio_impreso + "   BOMBA : " + ticket.getInt(var.KEY_TICKET_BOMBA) + "\n");
        textData.append("FECHA: " + ticket.getString(var.KEY_TICKET_FECHA) + "  HORA: " + ticket.getString(var.KEY_TICKET_HORA) + "\n");
        textData.append("VENDEDOR  : " + String.valueOf(ticket.getString(var.KEY_TICKET_DESPACHADOR)).toUpperCase() + "\n");
        textData.append("PRECIO    : $ " + String.format("%.2f", Double.valueOf(formateador2.format(ticket.getDouble(var.KEY_TICKET_PRECIO)))) + "\n");
        textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(ticket.getDouble(var.KEY_TICKET_CANTIDAD)))) + " LITROS " + String.valueOf(ticket.getString(var.KEY_TICKET_PRODUCTO)).toUpperCase() + "\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("IMPORTE   : $ " + formateador2.format(ticket.getDouble(var.KEY_TICKET_TOTAL)) + "\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append(letra.Convertir(String.valueOf(formateador21.format(ticket.getDouble(var.KEY_TICKET_TOTAL))), true) + "\n");
        //textData.append("\n");
        textData.append("\n");
        textData.append("------------------------------\n");
        textData.append("NOMBRE Y FIRMA USUARIO\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("\n");
        mPrinter.addText(textData.toString());
        mPrinter.addCut(Printer.CUT_FEED);
        mPrinter.addTextAlign(Printer.ALIGN_CENTER);
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
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append(ticket.getString(var.KEY_TICKET_CVEEST) + "\n");
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
        textData.append("Lugar de Expedicion" + "\n");
        textData.append(datos_domicilio.getString("municipio") + " " + datos_domicilio.getString("estado") + "\n");
        textData.append("\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("***** " + tiptrnSelect + " *****" + "\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append("\n");
        mPrinter.addTextAlign(Printer.ALIGN_LEFT);
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("TICKET    : " + folio_impreso + "   BOMBA : " + ticket.getInt(var.KEY_TICKET_BOMBA) + "\n");
        textData.append("FECHA: " + ticket.getString(var.KEY_TICKET_FECHA) + "  HORA: " + ticket.getString(var.KEY_TICKET_HORA) + "\n");
        textData.append("VENDEDOR  : " + String.valueOf(ticket.getString(var.KEY_TICKET_DESPACHADOR)).toUpperCase() + "\n");
        textData.append("PRECIO    : $ " + String.format("%.2f", Double.valueOf(formateador2.format(ticket.getDouble(var.KEY_TICKET_PRECIO)))) + "\n");
        textData.append("VOLUMEN   : " + String.format("%.4f", Double.valueOf(formateador4.format(ticket.getDouble(var.KEY_TICKET_CANTIDAD)))) + " LITROS " + String.valueOf(ticket.getString(var.KEY_TICKET_PRODUCTO)).toUpperCase() + "\n");
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        textData.append("IMPORTE   : $ " + formateador2.format(ticket.getDouble(var.KEY_TICKET_TOTAL)) + "\n");
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());
        mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
        textData.append(letra.Convertir(String.valueOf(formateador21.format(ticket.getDouble(var.KEY_TICKET_TOTAL))), true) + "\n");
        //textData.append("\n");
        textData.append("\n");
        textData.append("------------------------------\n");
        textData.append("NOMBRE Y FIRMA USUARIO\n");
        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());

        method = "addText";
        mPrinter.addText(textData.toString());
        textData.delete(0, textData.length());

        textData.append("\n");
        mPrinter.addText(textData.toString());

        mPrinter.addCut(Printer.CUT_FEED);

        return true;
    }
    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            mPrinter.clearCommandBuffer();
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            if (pdLoading != null) {
                pdLoading.dismiss();
            }
            mPrinter.clearCommandBuffer();
            logCE.EscirbirLog2(mContext,"ActivityTicket_printData - " + e);
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

            logCE.EscirbirLog2(mContext,"ActivityTicket_connectPrinter - " + e);
            getActivity().runOnUiThread(new Runnable() {
                public synchronized void run() {
                    ShowMsg.showException(e, "connect", mContext);
                }
            });

            return false;
        }
        return true;
    }
    public void fillPosicion() throws SQLException, JSONException {
        DataBaseCG gc = new DataBaseCG();
        connect = gc.control_gas(getActivity());
        stmt = connect.prepareStatement(QUERY);
        rs = stmt.executeQuery();

        ArrayList<String> data = new ArrayList<String>();
        while (rs.next()) {
            String id = rs.getString("logico");
            data.add(id);
        }
        String[] array = data.toArray(new String[0]);
        ArrayAdapter NoCoreAdapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_bombas, data);
        connect.close();
        spn_fab_contado.setAdapter(NoCoreAdapter);
    }
    public void fillTiptrn(){
        ArrayList<String> data = new ArrayList<String>();
        data.add("Jarreo");
        data.add("AutoJarreo");
        ArrayAdapter NoCoreAdapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_tiptrn, data);
        spn_tiptrn.setAdapter(NoCoreAdapter);
    }

    public void ElementBrandSharedPreferences(){
        Drawable tempimage;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")) {
            case "Combu-Express":
                dispensario.setImageResource(R.drawable.dispensario);
                jarreo.setImageResource(R.drawable.jarreocombu);
                tempimage  = getResources().getDrawable(R.drawable.printbtn_pressed);
                btn_print.setImageDrawable(tempimage);
                toolbar.setTitleTextColor(getResources().getColor(R.color.Negro));
                break;
            case "Repsol":
                dispensario.setImageResource(R.drawable.dispensariorespol);
                jarreo.setImageResource(R.drawable.jarreorepsol);
                tempimage  = getResources().getDrawable(R.drawable.printbtn_pressed_repsol);
                btn_print.setImageDrawable(tempimage);
                toolbar.setTitleTextColor(getResources().getColor(R.color.Blanco));
                break;
            case "Ener":
                dispensario.setImageResource(R.drawable.dispensarioener);
                jarreo.setImageResource(R.drawable.jarreoener);
                tempimage  = getResources().getDrawable(R.drawable.printbtn_pressed_ener);
                btn_print.setImageDrawable(tempimage);
                toolbar.setTitleTextColor(getResources().getColor(R.color.Blanco));
                break;
            case "Total":
                dispensario.setImageResource(R.drawable.dispensariototal);
                jarreo.setImageResource(R.drawable.jarreototal);
                tempimage  = getResources().getDrawable(R.drawable.printbtn_pressed_total);
                btn_print.setImageDrawable(tempimage);
                toolbar.setTitleTextColor(getResources().getColor(R.color.Blanco));
                break;
        }
    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (Objects.requireNonNull(sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express"))){
            case "Combu-Express":
                setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme);
                icon = getActivity().getDrawable(R.drawable.combuito);
                flag_brand = "Combu-Express";
                break;
            case "Repsol":
                setStyle(DialogFragment.STYLE_NORMAL,R.style.ContentMainRepsol);
                icon = getActivity().getDrawable(R.drawable.isologo_repsol);
                flag_brand = "Repsol";
                break;
            case "Ener":
                setStyle(DialogFragment.STYLE_NORMAL,R.style.ContentMainEner);
                icon = getActivity().getDrawable(R.drawable.logo_impresion_ener);
                flag_brand = "Ener";
                break;
            case "Total":
                setStyle(DialogFragment.STYLE_NORMAL,R.style.ContentMainTotal);
                icon = getActivity().getDrawable(R.drawable.total);
                flag_brand = "Total";
                break;
        }
        if (tablet.esTablet(getActivity().getApplicationContext())){
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status,
                             final String printJobId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                Log.w("Code", String.valueOf(code));
                if (code == 0) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), VentaActivity.class);
                    startActivity(intent);
                }else{
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(makeErrorMessage(status))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    logCE.EscirbirLog2(mContext,"ContadoActivity_OnCreate - " + makeErrorMessage(status));
                    /*ShowMsg.showResult(code, makeErrorMessage(status), mContext);*/
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
                btn_print.setEnabled(state_btn);
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
                        getActivity().runOnUiThread(new Runnable() {
                            public synchronized void run() {
                                logCE.EscirbirLog2(mContext,"ActivityTicket_disconnectPrinter - " + e);
                                ShowMsg.showException(e, "disconnect", mContext);
                            }
                        });
                        break;
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            logCE.EscirbirLog2(mContext,"ActivityTicket_disconnectPrinter - " + e);
                            ShowMsg.showException(e, "disconnect", mContext);
                        }
                    });
                    break;
                }
            }
        }

        mPrinter.clearCommandBuffer();
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
}
