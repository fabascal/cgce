package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

public class AceiteVenta extends AppCompatActivity implements View.OnClickListener{
    CardView cardViewContado,cardViewCredito;
    ValidateTablet tablet = new ValidateTablet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aceite_venta);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        cardViewContado = (CardView) findViewById(R.id.CardViewContado);
        cardViewContado.setOnClickListener(this);
        cardViewCredito = (CardView) findViewById(R.id.CardViewCredito);
        cardViewCredito.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent=null;
        String tipo_venta="0";
        switch (view.getId()) {
            case R.id.CardViewContado:
                tipo_venta="1";
                intent = new Intent(this, AceiteActivity.class);
                break;
            case R.id.CardViewCredito:
                tipo_venta="2";
                intent = new Intent(this, AceiteCreditoMetodo.class);
                break;
        }
        if (intent!=null){
            intent.putExtra("tipo_venta",tipo_venta);
            startActivity(intent);
        }
    }
}
