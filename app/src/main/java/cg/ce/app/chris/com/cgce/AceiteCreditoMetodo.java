package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

public class AceiteCreditoMetodo extends AppCompatActivity implements View.OnClickListener {
    CardView CardViewRFID;
    Intent intent =null;
    String tipo_venta=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aceite_credito_metodo);
        CardViewRFID = (CardView) findViewById(R.id.CardViewRFID);
        CardViewRFID.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("tipo_venta")!= null)
        {
            tipo_venta=bundle.getString("tipo_venta");
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.CardViewRFID:
                intent=new Intent(this,AceiteCreditoMetodoNFC.class);
                break;
        }
        if (intent!=null){
            intent.putExtra("tipo_venta",tipo_venta);
            startActivity(intent);
        }
    }
}
