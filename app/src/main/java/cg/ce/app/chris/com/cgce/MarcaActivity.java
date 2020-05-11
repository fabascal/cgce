package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;

import java.sql.SQLException;

public class MarcaActivity extends AppCompatActivity implements View.OnClickListener {

    ValidateTablet tablet = new ValidateTablet();
    TextView bandera,url;
    Button btn_actualizar;
    cgticket cg = new cgticket();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marca);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        bandera = (TextView) findViewById(R.id.nombre);
        url = ( TextView ) findViewById(R.id.url);
        btn_actualizar = ( Button ) findViewById(R.id.btn_actualizar);
        btn_actualizar.setOnClickListener(this);
        try {
            bandera.setText(cg.nombrebandera(this));
        } catch (ClassNotFoundException | SQLException | InstantiationException | JSONException | IllegalAccessException e) {
            new AlertDialog.Builder(MarcaActivity.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
        //url.setText(cg.urltimbre(this));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_actualizar:
                Intent intent = new Intent(this,MarcaUpdateActivity.class);
                startActivity(intent);
                break;
        }

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MarcaActivity.this,MainConfiguracionActivity.class);
        startActivity(intent);
    }
}
