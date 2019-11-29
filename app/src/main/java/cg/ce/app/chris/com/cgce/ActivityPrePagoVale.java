package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityPrePagoVale extends AppCompatActivity implements View.OnClickListener,ValeResultListener {
    ImageButton imgbtnscan,imbtn_ticket;
    JSONObject vale_envia= new JSONObject();
    JSONObject vale= new JSONObject();
    TextView tvlabelcliente,tvcliente,tvlabelrfc,tvrfc,tvlabeldenominacion,tvdenominacion;
    ImageView imagen;
    cgticket cg = new cgticket();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_pago_vale);
        imgbtnscan = (ImageButton)findViewById(R.id.imgbtnscan);
        imgbtnscan.setOnClickListener(this);
        imbtn_ticket =(ImageButton)findViewById(R.id.imbtn_ticket);
        imbtn_ticket.setOnClickListener(this);
        tvlabelcliente = (TextView) findViewById(R.id.tvlabelcliente);
        tvcliente = (TextView) findViewById(R.id.tvcliente);
        tvlabelrfc = (TextView) findViewById(R.id.tvlabelrfc);
        tvrfc = (TextView) findViewById(R.id.tvrfc);
        tvlabeldenominacion = (TextView) findViewById(R.id.tvlabeldenominacion);
        tvdenominacion = (TextView) findViewById(R.id.tvdenominacion);
        imagen = (ImageView) findViewById(R.id.imagen);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgbtnscan:
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            String a ="FORMAT: " + scanFormat;
            String b="CONTENT: " + scanContent;
            try {
                vale_envia.put("serie",scanContent.substring(0,3));
                vale_envia.put("codigo",scanContent.substring(3));
                vale_envia.put("cveest",cg.get_cveest(this));
                vale_envia.put("id_despachador",cg.nip_desp(this));
                vale_envia.put("despachador",cg.nombre_depsachador(this));
                if(vale_envia.getString("serie").equals("BAJ")) {
                    vale_envia.put("user", "bajio-consumo");
                }else{
                    vale_envia.put("user","rancho-consumo");
                }
                Log.w("json",vale_envia.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            WSVale wsvale = new WSVale(ActivityPrePagoVale.this,vale_envia);
            wsvale.delegate = this;
            wsvale.execute();
        }else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    @Override
    public void processFinishVale(String output) {
        try {
            vale = new JSONObject(output);
            Log.w("json_res",vale.toString());
            if (vale.has("mensaje")){
                if (vale.getString("mensaje").equals("1")){
                    imgbtnscan.setVisibility(View.GONE);
                    imagen.setVisibility(View.VISIBLE);
                    imbtn_ticket.setVisibility(View.VISIBLE);
                    tvlabelcliente.setVisibility(View.VISIBLE);
                    tvcliente.setVisibility(View.VISIBLE);
                    tvcliente.setText(vale.getString("nombre"));
                    tvlabelrfc.setVisibility(View.VISIBLE);
                    tvrfc.setVisibility(View.VISIBLE);
                    tvrfc.setText(vale.getString("rfc"));
                    tvlabeldenominacion.setVisibility(View.VISIBLE);
                    tvdenominacion.setVisibility(View.VISIBLE);
                    tvdenominacion.setText(vale.getString("denominacion"));
                }else{
                    Toast.makeText(this,vale.getString("mensaje"),Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ActivityPrePagoVale.this,VentaActivity.class);
                    startActivity(intent);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
