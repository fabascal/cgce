package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cg.ce.app.chris.com.cgce.ControlGas.GetAllCustomerValeConfig;
import cg.ce.app.chris.com.cgce.ControlGas.Listeners.GetCustomerValeListener;
import cg.ce.app.chris.com.cgce.common.Variables;
import cg.ce.app.chris.com.cgce.dialogos.RefreshCustomerVale;

public class ConfiguracionValeActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, GetCustomerValeListener {

    ValidateTablet tablet = new ValidateTablet();
    LogCE logCE = new LogCE();

    private final String TITLE = "Cliente debito";
    private final String BASE_URL = "http://combuexpress.mx/bajio_demo/";
    private final String BASE_URL_EXT = ".php";
    private final String REPSOL_FILE = "tramavales";
    private final String COMBU_FILE = "tramavales2";
    
    Spinner spn_file;
    Button save;
    ImageButton refresh;
    TextView codcli, dencli;
    
    JSONObject data = new JSONObject();

    RefreshCustomerVale dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_vale);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(this);
        spn_file = (Spinner) findViewById(R.id.spn_file);
        spn_file.setOnItemSelectedListener(this);
        refresh = (ImageButton) findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        codcli = (TextView) findViewById(R.id.codcli);
        dencli = (TextView) findViewById(R.id.dencli);
        List<String> Files = new ArrayList<String>();
        Files.add(REPSOL_FILE);
        Files.add(COMBU_FILE);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, Files);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_file.setAdapter(dataAdapter);
        ShowDataPreference();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ConfiguracionValeActivity.this,MainConfiguracionActivity.class);
        startActivity(intent);
    }
    
    private void ShowDataPreference(){
        SharedPreferences sharedPreferences = getSharedPreferences("Vale", Context.MODE_PRIVATE);
        codcli.setText(sharedPreferences.getString(getResources().getString(R.string.ValeCodcli),""));
        dencli.setText(sharedPreferences.getString(getResources().getString(R.string.ValeDencli),""));
        int spn_position = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.ValeFileID),"0"));
        spn_file.setSelection(spn_position);
    }
    private void SaveDataPreference() throws JSONException {
        Log.w("data",String.valueOf(data));
        SharedPreferences sharedPreferences = getSharedPreferences("Vale", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getResources().getString(R.string.ValeFileID),data.getString(getResources().getString(R.string.ValeFileID)));
        editor.putString(getResources().getString(R.string.ValeFile),data.getString(getResources().getString(R.string.ValeFile)));
        editor.putString(getResources().getString(R.string.ValeCodcli),codcli.getText().toString());
        editor.putString(getResources().getString(R.string.ValeDencli),dencli.getText().toString());
        editor.commit();
        ShowDataPreference();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case (R.id.save):
                try {
                    SaveDataPreference();
                    String e = "Datos guardados exitosamente.";
                    new AlertDialog.Builder(ConfiguracionValeActivity.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                } catch (JSONException e) {
                    StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
                    logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                            stacktraceObj[2].getMethodName() + "|" + e);
                    new AlertDialog.Builder(ConfiguracionValeActivity.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok, null).show();
                    e.printStackTrace();
                }
                break;
            case (R.id.refresh):
                GetAllCustomerValeConfig getAllCustomerValeConfig = new GetAllCustomerValeConfig(this);
                getAllCustomerValeConfig.delegate = this;
                getAllCustomerValeConfig.execute();
                break;
        }
        
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            data.put(getResources().getString(R.string.ValeFileID),position);
            String url = BASE_URL + parent.getItemAtPosition(position).toString() + BASE_URL_EXT;
            data.put(getResources().getString(R.string.ValeFile), url);
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(ConfiguracionValeActivity.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void GetCustomerValeFinish(JSONObject jsonObject) {
        try {
            List<DataCustomerCG> dataCustomerCG;
            dataCustomerCG = new ArrayList<>((Collection<? extends DataCustomerCG>) jsonObject.get(Variables.DATA_CUSTOMER));
            dialogFragment = RefreshCustomerVale.newInstance(TITLE, dataCustomerCG);
            dialogFragment.show(getFragmentManager(), "dialog");
        } catch (JSONException e) {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            logCE.EscirbirLog2(getApplicationContext(),getLocalClassName() + "|" +
                    stacktraceObj[2].getMethodName() + "|" + e);
            new AlertDialog.Builder(ConfiguracionValeActivity.this)
                    .setTitle(R.string.error)
                    .setMessage(String.valueOf(e))
                    .setPositiveButton(R.string.btn_ok, null).show();
            e.printStackTrace();
        }

    }
    public void AdapterClickCustomerVale(String codcli_data, String den_data){
        codcli.setText(codcli_data);
        dencli.setText(den_data);
        dialogFragment.dismiss();
    }
}