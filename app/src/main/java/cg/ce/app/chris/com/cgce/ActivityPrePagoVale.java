package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cg.ce.app.chris.com.cgce.listeners.ValesListener;
import cg.ce.app.chris.com.cgce.webservice.AceiteInsertaWS;
import cg.ce.app.chris.com.cgce.webservice.ValesWS;

public class ActivityPrePagoVale extends AppCompatActivity implements ValesListener {
    ImageButton imgbtnscan,btn_imprimir;
    cgticket cg = new cgticket();
    ValidateTablet tablet = new ValidateTablet();
    boolean IsTable=false;
    private List<ValesList> valesLists;
    JSONObject jsValesList;
    JSONArray items = new JSONArray();
    JSONObject jsValeTicket = new JSONObject();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    DecimalFormat decimalFormat = new DecimalFormat("$ #,###.00",symbols);
    TextView total,qty;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_pago_vale);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            IsTable=true;
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            IsTable=false;
        }
        scananim(this);
        recyclerView = (RecyclerView) findViewById(R.id.rvvale);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(getApplicationContext(), R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        recyclerView.addItemDecoration(horizontalDecoration);
    }

    private void scananim(final Activity activity){
        LinearLayout imageView = (LinearLayout) findViewById(R.id.image);
        final View bar = findViewById(R.id.bar);
        final Animation animation = AnimationUtils.loadAnimation(ActivityPrePagoVale.this, R.anim.animationscan);
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
                }
                scanIntegrator.initiateScan();
                return false;
            }
        });
    }

    private void loadData(JSONObject js) throws JSONException {
        jsValesList = new JSONObject();
        jsValesList.put("descripcion",js.getString("descripcion"));
        jsValesList.put("precio",js.getDouble("precio"));
        items.put(jsValesList);
        jsValesList.put("items",items);
        JSONArray array = jsValeTicket.getJSONArray("items");
        for (int i = 0; i < array.length(); i++){

            JSONObject jo = array.getJSONObject(i);

            ValesList valesList = new ValesList(R.drawable.aceite_logo,R.drawable.aceite_logo,jo.getString("descripcion"),jo.getDouble("precio"));
            valesLists.add(valesList);

        }
        adapter = new ValesAdapterRV(valesLists, getApplicationContext());
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
                /*Funcion para descargar el vale*/
                res = cg.busca_producto(this,scanContent);
                valesLists = new ArrayList<>();
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

    //Funcion que execute web service de envio de data
    public void SendDataWebService(JSONObject js) throws JSONException, ExecutionException, InterruptedException {
        Log.w("Json",js.toString());
        ValesWS valesWS = new ValesWS(js,this);
        valesWS.delegate=this;
        valesWS.execute();
    }

    public void total() throws JSONException {
        Double grantotal =0.0;
        int cantidad = 0;
        JSONArray array = jsValeTicket.getJSONArray("items");
        for (int i = 0; i<array.length(); i++){
            JSONObject o = array.getJSONObject(i);

            grantotal += o.getDouble("precio");
            cantidad += 1;
        }
        jsValeTicket.put("total",decimalFormat.format(grantotal));
        jsValeTicket.put("total_print",grantotal);
        jsValeTicket.put("qty",cantidad);
        total.setText(String.valueOf(decimalFormat.format(grantotal)));
        qty.setText( String.valueOf(cantidad));

    }

    @Override
    public void processFinish(String output) {
        Log.w("Respuesta Vales",output);
    }
}
