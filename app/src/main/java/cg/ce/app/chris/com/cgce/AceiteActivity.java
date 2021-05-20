package cg.ce.app.chris.com.cgce;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.JsonReader;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cg.ce.app.chris.com.cgce.common.QrString;
import cg.ce.app.chris.com.cgce.common.RecyclerEntityAceite;
import cg.ce.app.chris.com.cgce.common.Variables;
import cg.ce.app.chris.com.cgce.dialogos.AceiteCantidad;
import cg.ce.app.chris.com.cgce.listeners.StringListener;
import cg.ce.app.chris.com.cgce.socket.SGPMGateway;


public class AceiteActivity extends AppCompatActivity implements View.OnClickListener,
        StringListener, ReceiveListener,AceiteCantidad.AceiteCantidadListener {
    Integer flag=0,flag_Gateway_printer=0;
    private static final int REQUEST_PERMISSION = 100;
    private static final int DISCONNECT_INTERVAL = 500;//millseconds
    private Context mContext = null;
    ImageButton imgbtnscan,btn_imprimir,btn_vender;
    TextView tvname,tvprecio,etprecio;
    JSONObject jsonenviaproducto = new JSONObject();
    String estacion;
    ImageView img_aceite;
    private final int DURACION_SPLASH_5 = 5000; // 5 segundos
    cgticket ticket = new cgticket();
    boolean IsTable=false;
    JSONObject jsAceiteTicket = new JSONObject();
    JSONObject jsAceitesList;
    JSONArray items = new JSONArray();
    private RecyclerView recyclerView;
    AceiteAdapterRV adapter;
    private List<AceiteList> aceiteLists;
    cgticket cg = new cgticket();
    String barcode;
    TextView total,qty;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    DecimalFormat decimalFormat = new DecimalFormat("$ #,###.00",symbols);
    final static String VENTASECOS="ventaSecos";
    JSONObject ticketPrint = new JSONObject();
    String bomba;
    boolean IsTablet = false;
    ValidateTablet tablet = new ValidateTablet();
    LinearLayout imageView;
    ProgressDialog progress;
    private Printer mPrinter = null;
    Drawable icon, logo;
    LogCE logCE = new LogCE();
    RelativeLayout main;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        main = findViewById(R.id.activity_aceite);
        /*Funcion para obtener el tamaño del dispositivo y orientar la pantalla*/
        ScreenDevice();
        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("bomba")!= null)
        {
            bomba=bundle.getString("bomba");
        }
        requestRuntimePermission();
        mContext = this;
        scananim(this);
        recyclerView = (RecyclerView) findViewById(R.id.rvaceite);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(getApplicationContext(), R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        recyclerView.addItemDecoration(horizontalDecoration);
        FillGenerals();

        imgbtnscan = (ImageButton) findViewById(R.id.imgbtnscan);
        //imgbtnscan.setOnClickListener(AceiteActivity.this);
        tvname = (TextView) findViewById(R.id.tvname);
        tvprecio = (TextView) findViewById(R.id.tvprecio);
        etprecio =(TextView) findViewById(R.id.etprecio);
        img_aceite = (ImageView) findViewById(R.id.img_aceite);
        btn_imprimir = (ImageButton)findViewById(R.id.btn_imprimir);
        btn_vender = (ImageButton)findViewById(R.id.btn_vender);
        total = findViewById(R.id.total);
        qty = findViewById(R.id.qty);
        btn_imprimir.setOnClickListener(this);
        btn_vender.setOnClickListener(this);
        if(bundle.getString("tipo_venta")!= null)
        {
            try {
                jsonenviaproducto.put("tipo_venta",bundle.getString("tipo_venta"));
            } catch (JSONException e) {
                new AlertDialog.Builder(AceiteActivity.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                e.printStackTrace();
            }
        }
        /*Se inicializa el objeto impresora*/
        initializeObject();
        /*Borrar un aceite*/
        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private Drawable deleteIcon = ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_delete);
            private final ColorDrawable background = new ColorDrawable(Color.RED);

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final AceiteList entity = adapter.getEntity(viewHolder.getAdapterPosition());
                adapter.removeItem(viewHolder.getAdapterPosition());
                try {
                    JSONArray array_validar = jsAceiteTicket.getJSONArray("items");
                    for (int j =0; j < array_validar.length();j++) {
                        JSONObject jsvalidar = array_validar.getJSONObject(j);
                        if (jsvalidar.getString("codprd").equals(String.valueOf(entity.getCodprd()))){
                            jsAceiteTicket.getJSONArray("items").remove(j);
                        }
                    }
                    total();
                    Snackbar snackbar = Snackbar.make(main, "Producto eliminado", Snackbar.LENGTH_LONG)
                            .setAction("DESHACER", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.w("entity", entity.toString());
                                    adapter.undoDelete(entity, position);
                                    UndoDeleteProduct(entity);
                                }
                            });
                    snackbar.show();

                } catch (JSONException e) {
                    new AlertDialog.Builder(AceiteActivity.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }

            }
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;

                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

                if (dX > 0) {
                    int iconLeft = itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth();
                    int iconRight = itemView.getLeft() + iconMargin;

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
                } else if (dX < 0) {
                    int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else {
                    background.setBounds(0, 0, 0, 0);
                }
                ColorFilter filter = new LightingColorFilter(Color.BLACK, Color.BLACK);
                deleteIcon.setColorFilter(filter);
                background.draw(c);
                deleteIcon.draw(c);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }
    public void UndoDeleteProduct(AceiteList list){
        JSONObject res = new JSONObject();
        try {
            res.put("codprd",list.getCodprd());
            res.put("descripcion",list.getDescripcion());
            res.put("precio",list.getPrecio());
            res.put("codext",list.getCodext());
            loadData(res);
            total();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    @SuppressLint("SourceLockedOrientationActivity")
    private void ScreenDevice(){
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            IsTablet=true;
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            IsTablet=false;
        }
    }

    private void FillGenerals () {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
        try {
            jsAceiteTicket.put("cveest",cg.get_cveest(this));
            jsAceiteTicket.put("corte",cg.get_corte(this));
            jsAceiteTicket.put("nip",cg.nip_desp(this));
            jsAceiteTicket.put("despachador",cg.nombre_depsachador(this));
            jsAceiteTicket.put("fecha", df.format(Calendar.getInstance().getTime()));
        } catch (JSONException | ClassNotFoundException | SQLException | InstantiationException |
                IllegalAccessException | SocketException e) {
            new AlertDialog.Builder(AceiteActivity.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
    }

    private void scananim(final Activity activity){
        imageView = (LinearLayout) findViewById(R.id.image);
        final View bar = findViewById(R.id.bar);
        final Animation animation = AnimationUtils.loadAnimation(AceiteActivity.this, R.anim.animationscan);
        bar.setVisibility(View.VISIBLE);
        bar.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });


        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(activity);
                scanIntegrator.setPrompt("Escanear producto");
                Log.w("Tableta", String.valueOf(IsTable));
                if (IsTable) {
                    scanIntegrator.addExtra("SCAN_CAMERA_ID", 1);
                    scanIntegrator.setOrientationLocked(true);
                }else{
                    scanIntegrator.addExtra("SCAN_CAMERA_ID", 0);
                    scanIntegrator.setCaptureActivity(ScanActivityPortrait.class);
                    scanIntegrator.setOrientationLocked(false);
                }
                scanIntegrator.setBeepEnabled(true);
                scanIntegrator.initiateScan();
                return false;
            }
        });
    }
    private void loadData(JSONObject js) throws JSONException {
        int flag = 0 ;
        int flag_producto = 0;
        int cant_nueva =1;
        if (jsAceiteTicket.has("items")){
            flag_producto = jsAceiteTicket.getJSONArray("items").length();
            JSONArray array_validar = jsAceiteTicket.getJSONArray("items");
            for (int j =0; j < array_validar.length();j++){
                JSONObject jsvalidar = array_validar.getJSONObject(j);
                if (jsvalidar.getString("codprd").equals(js.getString("codprd"))){
                    int pieza_ant = Integer.parseInt(jsAceiteTicket.getJSONArray("items").getJSONObject(j).getString("cantidad"));
                    if (js.has("cantidad_nueva")){
                        cant_nueva=js.getInt("cantidad_nueva");
                    }else{
                        cant_nueva = pieza_ant + 1 ;
                    }
                    jsAceiteTicket.getJSONArray("items").getJSONObject(j).put("cantidad",String.valueOf(cant_nueva));
                    flag=1;
                }
            }
        }

        if (flag==0){
            if (flag_producto < 9) {
                jsAceitesList = new JSONObject();
                jsAceitesList.put("descripcion", js.getString("descripcion"));
                jsAceitesList.put("precio", js.getDouble("precio"));
                jsAceitesList.put("codprd", js.getString("codprd"));
                jsAceitesList.put("codext", js.getString("codext"));
                jsAceitesList.put("cantidad", "1");
                items.put(jsAceitesList);
                jsAceiteTicket.put("items", items);
            }else{
                String e = "No puedes vender mas de 9 productos distintos.";
                new AlertDialog.Builder(AceiteActivity.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
            }
        }

        JSONArray array = jsAceiteTicket.getJSONArray("items");
        aceiteLists.clear();
        for (int i = 0; i < array.length(); i++){

            JSONObject jo = array.getJSONObject(i);

            AceiteList aceites = new AceiteList(R.drawable.aceite_logo,
                    jo.getString("descripcion"),
                    jo.getDouble("precio"),
                    jo.getInt("cantidad"),
                    jo.getInt("codprd"),
                    jo.getString("codext"));
            aceiteLists.add(aceites);

        }
        adapter = new AceiteAdapterRV(aceiteLists, getApplicationContext(), AceiteActivity.this);
        Log.w("Adapter",String.valueOf(adapter.getItemCount()));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        total();
    }


    @Override
    protected void onDestroy() {

        finalizeObject();

        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        JSONObject res ;
        if (scanningResult != null && resultCode==RESULT_OK) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            try {
                res= cg.busca_producto(this,scanContent);
                aceiteLists = new ArrayList<>();
                if (res.has("error")){
                    Toast.makeText(this,res.getString("error"),Toast.LENGTH_LONG).show();
                }else {
                    loadData(res);
                }
            } catch (SQLException | JSONException | IllegalAccessException | InstantiationException | ClassNotFoundException e ) {
                new AlertDialog.Builder(AceiteActivity.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setIcon(icon)
                        .setPositiveButton(R.string.btn_ok,null).show();
                e.printStackTrace();
            }
        }else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No se recibio informacion del escaner!", Toast.LENGTH_LONG);
            toast.show();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_imprimir:

                updateButtonState(false);
                if (!runPrintReceiptSequence()) {
                    updateButtonState(true);
                }
                break;
            case R.id.btn_vender:
                try {
                    callSGPM(jsAceiteTicket, VENTASECOS, bomba);
                } catch (JSONException | ExecutionException | InterruptedException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(AceiteActivity.this)
                            .setTitle(R.string.error)
                            .setIcon(icon)
                            .setMessage(e.toString() )
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
                break;
        }
    }



    public String callSGPM(JSONObject js, String method, String bomba) throws JSONException,
            ExecutionException, InterruptedException {

        JSONObject cursor = null;
        JSONArray array = jsAceiteTicket.getJSONArray("items");
        String producto_trama = "";
        int resta = 0;

        for (int i = 0; i < array.length(); i++) {
            String pipe = "";
            if (array.length() > 1) {
                pipe = "|";
                resta = 1;
            }
            JSONObject o = array.getJSONObject(i);
            producto_trama += o.getString("codext") + "|" + o.getString("cantidad") + pipe;
        }
        String message = method + "|10|" + bomba + "|" + producto_trama.substring(0, producto_trama.length() - resta);
        DataBaseManager manager = new DataBaseManager(getApplicationContext());
        cursor = manager.cargarcursorodbc2();
        cursor.put("port", 9770);
        cursor.put("message", message);
        SGPMGateway sgmp = new SGPMGateway(AceiteActivity.this,getApplicationContext(),cursor);
        sgmp.delegate = this;
        return sgmp.execute(cursor).get();
    }
    /*funcion para esconder y mostrar elementos en funcion del ciclo de vida
    * si la bandera esta en 0 no hay venta, bandera en 1 ya se vendio y esta listo el proceso de impresion*/
    public void sale_print(Integer flag_sale_print){
        Log.w("flag_sale_print",String.valueOf(flag_sale_print));
    }

    @Override
    public void applyTexts(Integer codprd, Integer cantidad) {

        Log.w("applytexts", String.valueOf(codprd));
        Log.w("js applytexts",String.valueOf(jsAceiteTicket));
        try{
            JSONObject js = new JSONObject();
            js.put("codprd",codprd);
            js.put("cantidad_nueva",cantidad);
            loadData(js);
        } catch (JSONException e) {
            new AlertDialog.Builder(AceiteActivity.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(e.toString() )
                    .setPositiveButton(R.string.btn_ok, null).show();
        }
    }

    public void total() throws JSONException {
        Double grantotal =0.0;
        int cantidad = 0;
        JSONArray array = jsAceiteTicket.getJSONArray("items");
        for (int i = 0; i<array.length(); i++){
            JSONObject o = array.getJSONObject(i);

            grantotal += o.getDouble("precio") * o.getInt("cantidad");
            cantidad += o.getInt("cantidad");
        }
        jsAceiteTicket.put("total",decimalFormat.format(grantotal));
        jsAceiteTicket.put("total_print",grantotal);
        jsAceiteTicket.put("qty",cantidad);
        total.setText(String.valueOf(decimalFormat.format(grantotal)));
        qty.setText( String.valueOf(cantidad));

    }

    @Override
    public void processFinish(String output)  {
        if (!output.substring(5,6).equals("1")) {
            progress.dismiss();
            new AlertDialog.Builder(AceiteActivity.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(output)
                    .setPositiveButton(R.string.btn_ok, null).show();
        }else{

            try {
                ticketPrint=ticket.consulta_servicio_aceite(getApplicationContext(),bomba);
                System.out.println(ticketPrint);
                String e= "Servicio registrado con el numero " + ticketPrint.getString("nrotrn")
                        + " por un total de $" + ticketPrint.getString("total");
                new AlertDialog.Builder(AceiteActivity.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok, null).show();
                flag_Gateway_printer=1;
                btn_imprimir.setEnabled(true);
                btn_vender.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                        String dialog = "No se puede modificar las cantidades ya vendidas.";
                        new AlertDialog.Builder(AceiteActivity.this)
                                .setTitle(R.string.error)
                                .setIcon(icon)
                                .setMessage(dialog)
                                .setPositiveButton(R.string.btn_ok, null).show();
                        return true;
                    }
                });



            } catch (SQLException | IllegalAccessException | InstantiationException |
                    ClassNotFoundException | JSONException | SocketException e) {
                new AlertDialog.Builder(AceiteActivity.this)
                        .setTitle(R.string.error)
                        .setIcon(icon)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok, null).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (flag_Gateway_printer==1){
            String dialog = "No se puede regresar a la pantalla anterior sin imprimir.";
            new AlertDialog.Builder(AceiteActivity.this)
                    .setTitle(R.string.error)
                    .setIcon(icon)
                    .setMessage(dialog)
                    .setPositiveButton(R.string.btn_ok, null).show();
        }else {
            super.onBackPressed();
        }
    }

    private boolean initializeObject() {
        try {
            mPrinter = new Printer(mPrinter.TM_M30, mPrinter.MODEL_ANK, mContext);
        }
        catch (Exception e) {
            flag=1;
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }
        mPrinter.setReceiveEventListener(this);
        return true;
    }
    private void updateButtonState(boolean state) {
       btn_imprimir.setEnabled(state);
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private boolean runPrintReceiptSequence() {
        if (!createReceiptData()) {
            flag=1;
            return false;
        }
        if (!printData()) {
            flag=1;
            return false;
        }

        return true;
    }
    public boolean createReceiptData() {
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;
        Point point = new Point();
        WindowManager manager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? 380 : 380;
        //Tenemos que crear busqueda de servicio para aceites
        String method = "";
        Bitmap logoData = ((BitmapDrawable) logo).getBitmap();
        StringBuilder textData = new StringBuilder();
        Numero_a_Letra letra = new Numero_a_Letra();
        if (mPrinter == null) {
            return false;
        }
        try {

            ValidacionFlotillero vf = new ValidacionFlotillero();

            JSONObject datos_domicilio = ticket.estacion_domicilio(getApplicationContext());
            JSONObject vehiculo = new JSONObject();
            if (ticketPrint.optString("tag").length() > 0) {
                vehiculo = vf.get_vehiculo(getApplicationContext(), ticketPrint.getString("tag"));
            } else {
                Log.w("null", "no");
                vehiculo.put("rsp", "Sin Chofer");
                vehiculo.put("nroeco", "Sin No Economico");
                vehiculo.put("ultodm", "Sin Odometro");
                vehiculo.put("placa", "Sin Placa");
            }
            String titulo = "", folio_impreso = "", cliente = "", venta = "";
            titulo = "O R I G I N A L";
            if (ticketPrint.has("rfc")) {
                if (!ticketPrint.getString("rfc").equals("AAAA000000AAA")) {
                    cliente = ticketPrint.getString("rfc");
                    venta = "VENTA ACEITE CREDITO";
                } else {
                    cliente = "Publico General";
                    venta = "VENTA ACEITE CONTADO";
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
            textData.append(venta + "\n");
            textData.append("FECHA: " + ticketPrint.getString("fecha") + "\n");
            textData.append(datos_domicilio.getString("regimen") + "\n");
            textData.append("\n");
            textData.append("LUGAR DE EXPEDICION:\n");
            textData.append("ESTACION: " + datos_domicilio.getString("estacion") + " " + datos_domicilio.getString("cveest") + "\n");
            textData.append(datos_domicilio.getString("calle") + " " + datos_domicilio.getString("exterior") + " " + datos_domicilio.getString("interior") + ", " + datos_domicilio.getString("colonia") + ", " + datos_domicilio.getString("cp") + "\n");
            textData.append(datos_domicilio.getString("localidad") + ", " + datos_domicilio.getString("municipio") + ", " + datos_domicilio.getString("estado") + ", " + datos_domicilio.getString("pais") + "\n");
            textData.append("RFC " + datos_domicilio.getString("rfc") + " TEL." + datos_domicilio.getString("telefono") + "\n");
            //textData.append("------------------------------\n");
            if (ticketPrint.has("cliente")) {
                textData.append(ticketPrint.getString("cliente") + "-" + ticketPrint.getString("rfc") + "\n");
            }
            //textData.append("\n");
            if (ticketPrint.has("codcli")) {
                textData.append("Conductor     : " + vehiculo.getString("rsp") + "\n");
                textData.append("No. Econ.     : " + vehiculo.getString("nroeco") + "\n");
                textData.append("Placas        : " + vehiculo.getString("placa") + "\n");
                textData.append("Kilometraje   : " + ticketPrint.getString("odm") + "\n");
                textData.append("------------------------------\n");
            }
            textData.append("\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            textData.append("TICKET    : " + String.valueOf(ticketPrint.getInt("nrotrn")) + "\n");
            textData.append("VENDEDOR  : " + String.valueOf(ticket.nombre_depsachador(getApplicationContext())).toUpperCase() + "\n");
            textData.append("PIEZAS    : " + ticketPrint.getString("qty") + "\n");
            textData.append("IMPORTE   : " + ticketPrint.getString("total") + "\n");
            textData.append(letra.Convertir(String.valueOf(ticketPrint.getDouble("total")), true) + "\n");
            textData.append("\n");
            textData.append("CANT      DESCRIPCION           PRECIO   IMPORTE\n");
            textData.append("================================================\n");
            textData.append("\n");
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.TRUE, mPrinter.PARAM_DEFAULT);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextStyle(mPrinter.PARAM_DEFAULT, mPrinter.PARAM_DEFAULT, mPrinter.FALSE, mPrinter.PARAM_DEFAULT);
            method = "addText";
            for (int i = 0; i < items.length(); i++) {
                JSONObject jo = items.getJSONObject(i);
                String cantidad = jo.getString("cantidad");
                String producto = fillblank(cantidad.length(), 7) + jo.getString("descripcion").toUpperCase();
                String precio = fillblank(cantidad.length() + producto.length(), 33) + String.valueOf(jo.getDouble("precio"));
                String importe = fillblank(cantidad.length() + producto.length() + precio.length(), 43) + String.valueOf(jo.getInt("cantidad") * jo.getDouble("precio"));
                textData.append(cantidad + producto + precio + importe);

                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
                textData.append("\n");
            }
            textData.append("\n");
            textData.append("================================================\n");
            textData.append("GRACIAS POR SU PREFERENCIA!!! \n");
            textData.append("\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            Bitmap qrimage=null;
            QrString qr = new QrString();
            QRCodeEncoder qrCodeEncoder1 = new QRCodeEncoder(qr.Aceiteqrstring(ticketPrint,datos_domicilio),
                    null,
                    Contents.Type.TEXT,
                    BarcodeFormat.QR_CODE.toString(),
                    smallerDimension);
            try {
                qrimage = qrCodeEncoder1.encodeAsBitmap();
            } catch (WriterException e) {
                e.printStackTrace();
            }
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addImage (qrimage, 0, 0,
                    qrimage.getWidth(),
                    qrimage.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);
            textData.append("\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addCut(Printer.CUT_FEED);

        }
        catch (Exception e ){
            flag=1;
            mPrinter.clearCommandBuffer();
            ShowMsg.showException(e, method, this);
            textData = null;
            return false;
        }
        textData = null;
        return true;
    }
    public String fillblank(int data, int margen){
        int espacios = 0;
        String result="";
        if (margen > data) {
            espacios = margen - data;
        }
        for (int i = 0 ; i<espacios; i++){
            result += " ";
        }
        return result;
    }
    private boolean printData() {
        if (mPrinter == null) {
            flag=1;
            return false;
        }

        if (!connectPrinter()) {
            flag=1;
            mPrinter.clearCommandBuffer();
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            flag=1;
            mPrinter.clearCommandBuffer();
            ShowMsg.showException(e, "sendData", mContext);
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
    private boolean connectPrinter() {
        if (mPrinter == null) {
            return false;
        }
        try {
            DataBaseManager manager = new DataBaseManager(this);
            String target = manager.target();
            mPrinter.connect(target, Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            flag=1;
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        return true;
    }

    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> requestPermissions = new ArrayList<>();

        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
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
                if (e instanceof Epos2Exception) {
                    //Note: If printer is processing such as printing and so on, the disconnect API returns ERR_PROCESSING.
                    if (((Epos2Exception) e).getErrorStatus() == Epos2Exception.ERR_PROCESSING) {
                        try {
                            Thread.sleep(DISCONNECT_INTERVAL);
                        } catch (Exception ex) {
                        }
                    }else{
                        runOnUiThread(new Runnable() {
                            public synchronized void run() {
                                ShowMsg.showException(e, "disconnect", mContext);
                            }
                        });
                        break;
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            ShowMsg.showException(e, "disconnect", mContext);
                        }
                    });
                    break;
                }
            }
        }

        mPrinter.clearCommandBuffer();
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

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {

                if (code==0){
                    Intent intent = new Intent(getApplicationContext(),VentaActivity.class);
                    startActivity(intent);
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
        });
    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_aceite);
                icon = getDrawable(R.drawable.combuito);
                logo = getDrawable(R.drawable.logo_impresion);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_aceite_repsol);
                icon = getDrawable(R.drawable.isologo_repsol);
                logo = getDrawable(R.drawable.logo_impresion_repsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_aceite_ener);
                icon = getDrawable(R.drawable.logo_impresion_ener);
                logo = getDrawable(R.drawable.logo_impresion_ener);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_aceite_total);
                icon = getDrawable(R.drawable.total);
                logo = getDrawable(R.drawable.logo_impresion_total);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            IsTable = true;
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            IsTable = false;
        }
    }
}
