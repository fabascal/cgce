package cg.ce.app.chris.com.cgce;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;

import java.sql.SQLException;

public class MarcaUpdateActivity extends FragmentActivity {

    Spinner bandera;
    EditText url;
    Button actualizar;
    ValidateTablet tablet = new ValidateTablet();
    cgticket cg = new cgticket();
    CheckBox checkBox;
    LinearLayout ll;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marca_update);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        bandera = (Spinner) findViewById(R.id.bandera);
        ArrayAdapter NoCoreAdapter = null;
        try {
            NoCoreAdapter = new ArrayAdapter(this,
                    R.layout.spinner_banderas, cg.banderas(getApplicationContext()));
        } catch (ClassNotFoundException | SQLException | InstantiationException | JSONException | IllegalAccessException e) {
            new AlertDialog.Builder(MarcaUpdateActivity.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok,null).show();
            e.printStackTrace();
        }
        bandera.setAdapter(NoCoreAdapter);
        url = (EditText) findViewById(R.id.url);
        actualizar = (Button) findViewById(R.id.btn_actualizar);
        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                validamos datos
                 */
                if (validateurl(url.getText().toString())){
                    /*
                lo primero que debemos realizar es bloquear el boton
                 */
                    actualizar.setClickable(false);
                /*
                antes de actualizar se tiene que limpiar la bandera anterior
                para esto llamamos la funcion limpiarbandera de la clase cgticket
                */
                    try {
                        cg.limpiarbandera(getApplicationContext());
                         /*
                obtenemos el id de la bandera selecionada para establecer como defecto
                y actualizamos la base con al funcion insertarbandera de la clase cgticket
                 */
                        cg.insertarbandera(getApplicationContext(),bandera.getSelectedItem().toString());
                    /*
                    validamos si se requiere actualizar la url del web service para timbrar y la actualizamos
                     */
                        if(checkBox.isChecked()) {
                            Log.w("check","ok");
                            cg.insertarurl(getApplicationContext(), bandera.getSelectedItem().toString(), url.getText().toString());
                        }
                    } catch (ClassNotFoundException | SQLException | InstantiationException | JSONException | IllegalAccessException e) {
                        new AlertDialog.Builder(MarcaUpdateActivity.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                        e.printStackTrace();
                    }

                /*
                Finalizamos regresando a la clase principal de marca
                 */
                    Intent intent = new Intent(getApplicationContext(),MarcaActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Valida la URL de facturacion!!!", Toast.LENGTH_LONG);
                    toast.show();
                    url.requestFocus();
                    actualizar.setClickable(true);
                }

            }
        });
        ll = (LinearLayout)findViewById(R.id.linearLayout);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked()){
                    ll.setVisibility(View.VISIBLE);
                }else{
                    ll.setVisibility(View.GONE);
                }
            }
        });

    }

    public boolean validateurl (String url){
        //boolean res= false;
        if (checkBox.isChecked()){
            return Patterns.WEB_URL.matcher(url).matches();
        }else{
            return true;
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MarcaUpdateActivity.this,MarcaActivity.class);
        startActivity(intent);
    }

}
