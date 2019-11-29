package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class CreditoMetodo extends AppCompatActivity implements View.OnClickListener{
    CardView qr,rfid;
    String bomba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credito_metodo);
        qr = (CardView) findViewById(R.id.CardViewQR);
        rfid = (CardView) findViewById(R.id.CardViewRFID);
        qr.setOnClickListener(this);
        rfid.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("bomba")!= null)
        {
            bomba=bundle.getString("bomba");
        }

    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.CardViewRFID:
                //Toast.makeText(this,bomba,Toast.LENGTH_SHORT).show();
                intent = new Intent(CreditoMetodo.this, RfidCredito.class);
                Log.w("bomba",bomba);
                intent.putExtra("bomba", bomba);
                break;
            case R.id.CardViewQR:
                Toast.makeText(this,"QR",Toast.LENGTH_SHORT).show();
                break;
        }
        if (intent!=null){
            intent.putExtra("bomba", bomba);
            startActivity(intent);
        }
    }
}
