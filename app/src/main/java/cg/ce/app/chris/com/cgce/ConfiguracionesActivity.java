package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfiguracionesActivity extends AppCompatActivity implements View.OnClickListener{
    Button btn_odbc;
    EditText et_ip, et_puerto,et_bd,et_userbd,et_passbd,et_bd_cg,et_integra;
    JSONObject cursor=null;
    ValidateTablet tablet = new ValidateTablet();

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuraciones);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        et_ip = (EditText)findViewById(R.id.et_ip);
        et_puerto = (EditText)findViewById(R.id.et_puerto);
        et_integra = (EditText)findViewById(R.id.et_integra);
        et_bd = (EditText)findViewById(R.id.et_bd);
        et_bd_cg = (EditText)findViewById(R.id.et_bd_cg);
        et_userbd = (EditText)findViewById(R.id.et_userbd);
        et_passbd = (EditText)findViewById(R.id.et_passbd);
        btn_odbc = (Button)findViewById(R.id.btn_odbc);
        btn_odbc.setOnClickListener(this);

        Intent intent = new Intent();
        Bundle extras = intent.getExtras();

        DataBaseManager manager = new DataBaseManager(this);
        cursor = manager.cargarcursorodbc2();
        String direccion=null;
        String puerto=null;
        String user=null;
        String base=null;
        String pass = null;
        String base_cg=null;
        String integra=null;
        try {
            direccion = cursor.getString("ip");
            puerto = cursor.getString("puerto");
            user = cursor.getString("userdb");
            base = cursor.getString("db");
            pass = cursor.getString("passdb");
            base_cg = cursor.getString("db_cg");
            integra = cursor.getString("integra");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        et_ip.setText(direccion);
        et_puerto.setText(puerto);
        et_userbd.setText(user);
        et_bd_cg.setText(base_cg);
        et_bd.setText(base);
        et_passbd.setText(pass);
        et_integra.setText(integra);
    }

    @Override
    public void onClick(View view) {
        DataBaseManager manager = new DataBaseManager(this);
        switch (view.getId()) {
            case R.id.btn_odbc:
                String direccion=et_ip.getText().toString();
                String puerto=et_puerto.getText().toString();
                String bd=et_bd.getText().toString();
                String bd_cg=et_bd_cg.getText().toString();
                String user=et_userbd.getText().toString();
                String pass=et_passbd.getText().toString();
                String integra=et_integra.getText().toString();
                long dato = manager.contarodbc();
                if (dato==1){
                    manager.actualizar(direccion,integra,puerto,bd,bd_cg,user,pass);
                    Toast.makeText(getApplicationContext(),"Actualizado",Toast.LENGTH_SHORT).show();
                }else {
                    manager.insertar(direccion,integra,puerto,bd,bd_cg,user,pass);
                    Toast.makeText(getApplicationContext(),"Insertado",Toast.LENGTH_SHORT).show();
                }

        }
    }
}
