package com.epson.epos2_barcodescanner;

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
import android.widget.TextView;

import com.epson.epos2.Log;
import com.epson.epos2.barcodescanner.BarcodeScanner;
import com.epson.epos2.barcodescanner.ScanListener;
import com.epson.epos2.ConnectionListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 100;

    private Context mContext = null;
    private BarcodeScanner mBarcodeScanner = null;
    private boolean mIsConnect = false;
    private Button  mBtnConnect = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();

        mContext = this;

        int[] target = {
            R.id.btnConnect,
            R.id.btnDisconnect,
            R.id.btnClear
        };

        for (int i = 0; i < target.length; i++) {
            Button button = (Button)findViewById(target[i]);
            button.setOnClickListener(this);
        }

        mBtnConnect = (Button)findViewById(R.id.btnConnect);

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
            case R.id.btnConnect:
                connectProcess();
                break;

            case R.id.btnDisconnect:
                disconnectProcess();
                break;

            case R.id.btnClear:
                TextView txtScanData = (TextView)findViewById(R.id.txtScanData);
                txtScanData.setText("");
                break;

            default:
                // Do nothing
                break;
        }
    }

    private void connectProcess() {
        if (!initializeObject()) {
            return;
        }

        if (!connectScanner()) {
            return;
        }

        mBtnConnect.setEnabled(false);
    }

    private void disconnectProcess() {
        disconnectScanner();

        finalizeObject();

        mBtnConnect.setEnabled(true);
    }

    private boolean initializeObject() {
        if (mBarcodeScanner != null) {
            finalizeObject();
        }

        try {
            mBarcodeScanner = new BarcodeScanner(mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "BarcodeScanner", mContext);

            return false;
        }

        mBarcodeScanner.setScanEventListener(mScanEvent);
        mBarcodeScanner.setConnectionEventListener(mConnectionChangedEvent);

        return true;
    }

    private void finalizeObject() {
        if (mBarcodeScanner == null) {
            return;
        }

        mBarcodeScanner.setScanEventListener(null);
        mBarcodeScanner.setConnectionEventListener(null);

        mBarcodeScanner = null;
    }

    private boolean connectScanner() {
        EditText edtTarget = (EditText)findViewById(R.id.edtTarget);

        if (mBarcodeScanner == null) {
            return false;
        }

        try {
            mBarcodeScanner.connect(edtTarget.getText().toString(), BarcodeScanner.PARAM_DEFAULT);

            mIsConnect = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);

            finalizeObject();

            return false;
        }
        return true;
    }

    private void disconnectScanner() {
        if (mBarcodeScanner == null) {
            return;
        }

        try {
            if(mIsConnect == true) {
                mBarcodeScanner.disconnect();
            }
            else {
                return;
            }
        }
        catch (Exception e) {
            mIsConnect = false;

            ShowMsg.showException(e, "disconnect", mContext);
        }
    }

    private  ScanListener mScanEvent = new ScanListener() {
        @Override
        public void onScanData(BarcodeScanner scannerObj, final String input) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    TextView txtScanData = (TextView)findViewById(R.id.txtScanData);
                    txtScanData.append(input);
                }
            });
        }
    };

    private ConnectionListener mConnectionChangedEvent = new ConnectionListener() {
        @Override
        public void onConnection(Object deviceObj, int eventType) {
            if(eventType == EVENT_DISCONNECT) {
                mIsConnect = false;
            }
            else {
                //Do each process.
            }
        }
    };

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
