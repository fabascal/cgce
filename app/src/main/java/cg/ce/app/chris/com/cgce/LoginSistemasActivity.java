package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class LoginSistemasActivity extends AppCompatActivity implements View.OnClickListener {
    EditText et_user_sistemas, et_pass_sistemas;
    ImageButton btn_login;
    ValidateTablet tablet = new ValidateTablet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sistemas);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        et_user_sistemas = (EditText)findViewById(R.id.et_user_sistemas);
        et_pass_sistemas = (EditText)findViewById(R.id.et_pass_sistemas);
        btn_login = (ImageButton) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("msg")!= null)
        {
            Toast.makeText(this,bundle.getString("msg"),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                String email = et_user_sistemas.getText().toString();
                String password = et_pass_sistemas.getText().toString();
                String user="Sistemas";
                String pass="qwerty";

                if (email.equals(user) && password.equals(pass) ){
                    Intent inento = new Intent(LoginSistemasActivity.this,MainConfiguracionActivity.class);
                    startActivity(inento);
                }else {
                    Toast.makeText(getApplicationContext(),"ERROR!! Usuario y/o contraseña incorrecta.",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
