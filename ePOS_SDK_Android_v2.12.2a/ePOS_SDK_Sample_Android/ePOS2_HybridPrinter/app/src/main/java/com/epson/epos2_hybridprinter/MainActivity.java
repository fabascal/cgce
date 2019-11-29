package com.epson.epos2_hybridprinter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.epson.epos2.Log;
import com.epson.epos2.printer.HybridPrinter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 100;

    private Context mContext = null;
    private EditText mEditTarget = null;
    private Spinner mSpnLang = null;
    private int mLang = HybridPrinter.MODEL_ANK;
    private int mHybridPrinterLang = HybridPrinter.MODEL_ANK;
    public static HybridPrinter  mHybridPrinter = null;
    public static String mConnectTarget = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();

        mContext = this;

        int[] target = {
            R.id.btnDiscovery,
            R.id.btnOnePassControl,
            R.id.btnValidationControl
        };

        for (int i = 0; i < target.length; i++) {
            Button button = (Button)findViewById(target[i]);
            button.setOnClickListener(this);
        }

        mSpnLang = (Spinner)findViewById(R.id.spnLang);
        ArrayAdapter<SpnModelsItem> langAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_ank), HybridPrinter.MODEL_ANK));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_japanese), HybridPrinter.MODEL_JAPANESE));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_chinese), HybridPrinter.MODEL_CHINESE));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_taiwan), HybridPrinter.MODEL_TAIWAN));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_korean), HybridPrinter.MODEL_KOREAN));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_thai), HybridPrinter.MODEL_THAI));
        langAdapter.add(new SpnModelsItem(getString(R.string.lang_southasia), HybridPrinter.MODEL_SOUTHASIA));
        mSpnLang.setAdapter(langAdapter);
        mSpnLang.setSelection(0);

        mEditTarget = (EditText)findViewById(R.id.edtTarget);

        try {
            Log.setLogSettings(mContext, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 1, Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "setLogSettings", mContext);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            String target = data.getStringExtra(getString(R.string.title_target));
            if (target != null) {
                EditText mEdtTarget = (EditText)findViewById(R.id.edtTarget);
                mEdtTarget.setText(target);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        mLang = ((SpnModelsItem) mSpnLang.getSelectedItem()).getModelConstant();
        mConnectTarget = mEditTarget.getText().toString();

        switch (v.getId()) {
            case R.id.btnDiscovery:
                intent = new Intent(this, DiscoveryActivity.class);
                startActivityForResult(intent, 0);
                break;

            case R.id.btnOnePassControl:
                intent = new Intent(this, PassControlActivity.class);
                if(initializeObject()){
                    startActivityForResult(intent, 0);
                }
                break;

            case R.id.btnValidationControl:
                intent = new Intent(this, ValidationControlActivity.class);
                if(initializeObject()){
                    startActivityForResult(intent, 0);
                }
                break;

            default:
                // Do nothing
                break;
        }
    }

    private boolean initializeObject() {
        try {
            if(mHybridPrinter == null){
                mHybridPrinter = new HybridPrinter(mLang, mContext);
                mHybridPrinterLang = mLang;
            }
            else{
                if(mLang != mHybridPrinterLang){
                    finalizeObject();
                    mHybridPrinter = new HybridPrinter(mLang, mContext);
                    mHybridPrinterLang = mLang;
                }
            }

        }
        catch (Exception e) {
            ShowMsg.showException(e, "HybridPrinter", mContext);
            return false;
        }

        return true;
    }

    private void finalizeObject() {
        if (mHybridPrinter == null) {
            return;
        }

        mHybridPrinter.clearCommandBuffer();

        mHybridPrinter.setReceiveEventListener(null);

        mHybridPrinter = null;
    }

    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> requestPermissions = new ArrayList<>();

        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
            if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }
}
