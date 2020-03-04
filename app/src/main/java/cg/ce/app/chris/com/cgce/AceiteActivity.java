package cg.ce.app.chris.com.cgce;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.JsonReader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cg.ce.app.chris.com.cgce.socket.SGPMGateway;
import cg.ce.app.chris.com.cgce.webservice.AceiteInsertaWS;


public class AceiteActivity extends AppCompatActivity implements View.OnClickListener, SorteoListener {
    ImageButton imgbtnscan,btn_imprimir;
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
    private RecyclerView.Adapter adapter;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aceite);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Log.w("Tableta","es Tableta");
            IsTablet=true;
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            IsTablet=false;
            Log.w("Tableta","no es Tableta");
        }
        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("bomba")!= null)
        {
            bomba=bundle.getString("bomba");
        }

        scananim(this);
        recyclerView = (RecyclerView) findViewById(R.id.rvaceite);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(getApplicationContext(), R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        recyclerView.addItemDecoration(horizontalDecoration);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            IsTable=true;
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            IsTable=false;
        }
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
        try {
            jsAceiteTicket.put("cveest",cg.get_cveest(this));
            jsAceiteTicket.put("corte",cg.get_corte(this));
            jsAceiteTicket.put("nip",cg.nip_desp(this));
            jsAceiteTicket.put("despachador",cg.nombre_depsachador(this));
            jsAceiteTicket.put("fecha", df.format(Calendar.getInstance().getTime()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        imgbtnscan = (ImageButton) findViewById(R.id.imgbtnscan);
        //imgbtnscan.setOnClickListener(AceiteActivity.this);
        tvname = (TextView) findViewById(R.id.tvname);
        tvprecio = (TextView) findViewById(R.id.tvprecio);
        etprecio =(TextView) findViewById(R.id.etprecio);
        img_aceite = (ImageView) findViewById(R.id.img_aceite);
        btn_imprimir = (ImageButton)findViewById(R.id.btn_imprimir);
        total = findViewById(R.id.total);
        qty = findViewById(R.id.qty);
        btn_imprimir.setOnClickListener(this);

        if(bundle.getString("tipo_venta")!= null)
        {
            try {
                jsonenviaproducto.put("tipo_venta",bundle.getString("tipo_venta"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    private void scananim(final Activity activity){
        LinearLayout imageView = (LinearLayout) findViewById(R.id.image);
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
                if (IsTable) {
                    scanIntegrator.addExtra("SCAN_CAMERA_ID", 1);
                    scanIntegrator.setOrientationLocked(true);
                }
                scanIntegrator.initiateScan();
                return false;
            }
        });
    }
    private void loadData(JSONObject js) throws JSONException {
        jsAceitesList = new JSONObject();
        jsAceitesList.put("descripcion",js.getString("descripcion"));
        jsAceitesList.put("precio",js.getDouble("precio"));
        jsAceitesList.put("codprd", js.getString("codprd"));
        items.put(jsAceitesList);
        jsAceiteTicket.put("items",items);
        Log.w("list", String.valueOf(jsAceitesList));
        Log.w("ticket",String.valueOf(jsAceiteTicket));
        JSONArray array = jsAceiteTicket.getJSONArray("items");
        for (int i = 0; i < array.length(); i++){

            JSONObject jo = array.getJSONObject(i);

            AceiteList aceites = new AceiteList(R.drawable.aceite_logo,jo.getString("descripcion"),jo.getDouble("precio"));
            aceiteLists.add(aceites);

        }
        adapter = new AceiteAdapterRV(aceiteLists, getApplicationContext());
        recyclerView.setAdapter(adapter);
        total();

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
            } catch (SQLException e ) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
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
                System.out.println(jsAceiteTicket);

                JSONArray array = null;
                try {
                    array = jsAceiteTicket.getJSONArray("items");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jo = array.getJSONObject(i);
                        callSGPM(jo,VENTASECOS);
                        ticketPrint = cg.consulta_servicio(this,bomba);
                        System.out.println(ticketPrint);
                        //new ClassImpresionAceite(AceiteActivity.this,getApplicationContext(),this.btn_imprimir,jo).execute();
                        System.out.println(jo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                /*try {
                    new ClassImpresionAceite(AceiteActivity.this,getApplicationContext(),this.btn_imprimir,jsAceiteTicket).execute();
                } catch (JSONException e) {
                    Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                try {
                    SendDataWebService(jsAceiteTicket);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                Intent intent = new Intent(AceiteActivity.this,VentaActivity.class);
                startActivity(intent);
                break;*/

        }
    }
    //Funcion que execute web service de envio de data
    public void SendDataWebService(JSONObject js) throws JSONException, ExecutionException, InterruptedException {
        Log.w("Json",js.toString());
        AceiteInsertaWS aceiteInsertaWS = new AceiteInsertaWS(js,this);
        aceiteInsertaWS.delegate=this;
        aceiteInsertaWS.execute();

    }

    public String callSGPM(JSONObject js, String method) throws JSONException, ExecutionException, InterruptedException {
        JSONObject cursor = null;
        String message=method+"|10|1|"+js.getString("codprd")+"|1";
        DataBaseManager manager = new DataBaseManager(getApplicationContext());
        cursor = manager.cargarcursorodbc2();
        cursor.put("port", 9770);
        cursor.put("message",message);
        SGPMGateway sgmp = new SGPMGateway(cursor);
        return sgmp.execute(cursor).get();
    }



    public void total() throws JSONException {
        Double grantotal =0.0;
        int cantidad = 0;
        JSONArray array = jsAceiteTicket.getJSONArray("items");
        for (int i = 0; i<array.length(); i++){
            JSONObject o = array.getJSONObject(i);

            grantotal += o.getDouble("precio");
            cantidad += 1;
        }
        jsAceiteTicket.put("total",decimalFormat.format(grantotal));
        jsAceiteTicket.put("total_print",grantotal);
        jsAceiteTicket.put("qty",cantidad);
        total.setText(String.valueOf(decimalFormat.format(grantotal)));
        qty.setText( String.valueOf(cantidad));

    }

    @Override
    public void processFinish3(String output) {
        Log.w("Respuesta Aceites",output);

    }
}
