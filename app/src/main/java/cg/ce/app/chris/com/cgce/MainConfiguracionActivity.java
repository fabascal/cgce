package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainConfiguracionActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_odbc, btn_mac, btn_razonsocial,btn_marca, btn_vale;
    ValidateTablet tablet = new ValidateTablet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_configuracion);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        btn_odbc = (Button) findViewById(R.id.btn_odbc);
        btn_mac = (Button) findViewById(R.id.btn_mac);
        btn_razonsocial = (Button) findViewById(R.id.btn_razonsocial);
        btn_marca = (Button) findViewById(R.id.btn_marca);
        btn_vale = (Button) findViewById(R.id.btn_vale);
        btn_odbc.setOnClickListener(this);
        btn_mac.setOnClickListener(this);
        btn_razonsocial.setOnClickListener(this);
        btn_marca.setOnClickListener(this);
        btn_vale.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent=null ;
        switch (view.getId()) {
            case R.id.btn_odbc:
                intent = new Intent(MainConfiguracionActivity.this, ConfiguracionesActivity.class);
                break;
            case R.id.btn_mac:
                intent = new Intent(MainConfiguracionActivity.this, MacActivity.class);
                break;
            case R.id.btn_razonsocial:
                intent = new Intent(MainConfiguracionActivity.this, Razon_Social.class);
                break;
            case R.id.btn_marca:
                intent = new Intent(MainConfiguracionActivity.this, MarcaActivity.class);
                break;
            case R.id.btn_vale:
                intent = new Intent(MainConfiguracionActivity.this, ConfiguracionValeActivity.class);
                break;
        }
        if (intent!=null){
            startActivity(intent);
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MainConfiguracionActivity.this,Splashscreen.class);
        startActivity(intent);
    }
}
