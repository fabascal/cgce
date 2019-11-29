package com.epson.epos2_linedisplay;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.epson.epos2.Log;
import com.epson.epos2.linedisplay.LineDisplay;
import com.epson.epos2.linedisplay.ReceiveListener;
import com.epson.epos2.Epos2Exception;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, ReceiveListener {

    private static final int REQUEST_PERMISSION = 100;

    private Context mContext = null;
    private LineDisplay mLineDisplay = null;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();

        mContext = this;

        Button btnDisplayText = (Button)findViewById(R.id.btnDisplay);
        btnDisplayText.setOnClickListener(this);

        try {
            Log.setLogSettings(mContext, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 1, Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "setLogSettings", mContext);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDisplay:
                updateButtonState(false);
                if (!runLineDisplaySequence()) {
                    updateButtonState(true);
                }
                break;

            default:
                // Do nothing
                break;
        }
    }

    private boolean runLineDisplaySequence() {
        if (!initializeObject()) {
            return false;
        }

        if (!createDisplayData()) {
            finalizeObject();
            return false;
        }

        if (!connectDisplay()) {
            finalizeObject();
            return false;
        }

        try {
            mLineDisplay.sendData();
        }
        catch (Exception e) {
            ShowMsg.showException(e, "sendData", mContext);
            disconnectDisplay();
            return false;
        }

        return true;
    }

    private boolean initializeObject() {
        try {
            mLineDisplay = new LineDisplay(LineDisplay.DM_D30, mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "LineDisplay", mContext);

            return false;
        }

        mLineDisplay.setReceiveEventListener(this);

        return true;
    }

    private void finalizeObject() {
        if (mLineDisplay == null) {
            return;
        }

        mLineDisplay.clearCommandBuffer();

        mLineDisplay.setReceiveEventListener(null);

        mLineDisplay = null;
    }

    private boolean connectDisplay() {
        EditText edtTarget = (EditText)findViewById(R.id.edtTarget);

        if (mLineDisplay == null) {
            return false;
        }

        try {
            mLineDisplay.connect(edtTarget.getText().toString(), LineDisplay.PARAM_DEFAULT);
        }
        catch (Epos2Exception e) {
            ShowMsg.showException(e, "connect", mContext);

            return false;
        }

        return true;
    }

    private void disconnectDisplay() {
        if (mLineDisplay == null) {
            return;
        }

        try {
            mLineDisplay.disconnect();
        }
        catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "disconnect", mContext);
                }
            });
        }

        finalizeObject();
    }

    private boolean createDisplayData() {
        String method = "";
        EditText editText = (EditText)findViewById(R.id.edtText);
        ToggleButton toggleBlink = (ToggleButton)findViewById(R.id.toggleBlink);

        if (mLineDisplay == null) {
            return false;
        }

        try {
            method = "addInitialize";
            mLineDisplay.addInitialize();

            method = "addSetCursorPosition";
            mLineDisplay.addSetCursorPosition(1, 1);

            if (toggleBlink.isChecked()) {
                method = "addSetBlink";
                mLineDisplay.addSetBlink(1000);
            }

            method = "addText";
            mLineDisplay.addText(editText.getText().toString());
        }
        catch (Exception e) {
            ShowMsg.showException(e, method, mContext);
            return false;
        }

        return true;
    }

    private void updateButtonState(boolean state) {
        Button btnDisplay = (Button)findViewById(R.id.btnDisplay);
        btnDisplay.setEnabled(state);
    }

    @Override
    public void onDispReceive(final LineDisplay displayObj, final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, mContext);

                updateButtonState(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectDisplay();
                    }
                }).start();
            }
        });
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
