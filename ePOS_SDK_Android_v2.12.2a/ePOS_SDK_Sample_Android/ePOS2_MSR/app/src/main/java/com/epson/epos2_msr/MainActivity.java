package com.epson.epos2_msr;

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

import com.epson.epos2.ConnectionListener;
import com.epson.epos2.Log;
import com.epson.epos2.msr.Data;
import com.epson.epos2.msr.DataListener;
import com.epson.epos2.msr.Msr;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 100;

    private Context mContext = null;
    private Msr mMsr = null;
    private boolean mIsConnect = false;
    private Button mBtnConnect = null;

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
                TextView txtReceiveData = (TextView)findViewById(R.id.txtReceiveData);
                txtReceiveData.setText("");
                break;

            default:
                // Do nothing
                break;
        }
    }

    private  void connectProcess() {
        if (!initializeObject()) {
            return;
        }

        if (!connectMSR()) {
            return;
        }

        mBtnConnect.setEnabled(false);
    }

    private void disconnectProcess() {
        disconnectMSR();

        finalizeObject();

        mBtnConnect.setEnabled(true);
    }

    private boolean initializeObject() {
        if (mMsr != null) {
            finalizeObject();
        }

        try {
            mMsr = new Msr(mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Msr", mContext);
            return false;
        }

        mMsr.setDataEventListener(mDataEvent);
        mMsr.setConnectionEventListener(mConnectionChangedEvent);

        return true;
    }

    private void finalizeObject() {
        if (mMsr == null) {
            return;
        }

        mMsr.setDataEventListener(null);
        mMsr.setConnectionEventListener(null);

        mMsr = null;

        return;
    }

    private boolean connectMSR() {
        EditText edtTarget = (EditText)findViewById(R.id.edtTarget);

        if (mMsr == null) {
            return false;
        }

        try {
            mMsr.connect(edtTarget.getText().toString(), Msr.PARAM_DEFAULT);

            mIsConnect = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);

            finalizeObject();

            return false;
        }
        return true;
    }

    private void disconnectMSR() {
        if (mMsr == null) {
            return;
        }

        try {
            if(mIsConnect == true) {
                mMsr.disconnect();
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

    private DataListener mDataEvent = new DataListener() {
        @Override
        public void onData(Msr msrObj, final Data data) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    TextView txtReceiveData = (TextView)findViewById(R.id.txtReceiveData);
                    String dataText ="";
                    dataText += "OnData:\n";
                    dataText += "  Track1:" + data.getTrack1() + "\n";
                    dataText += "  Track2:" + data.getTrack2() + "\n";
                    dataText += "  Track4:" + data.getTrack4() + "\n";
                    dataText += "  AccountNumber:" + data.getAccountNumber() + "\n";
                    dataText += "  ExpirationData:" + data.getExpirationData() + "\n";
                    dataText += "  Surname:" + data.getSurname() + "\n";
                    dataText += "  FirstName:" + data.getFirstName() + "\n";
                    dataText += "  MiddleInitial:" + data.getMiddleInitial() + "\n";
                    dataText += "  Title:" + data.getTitle() + "\n";
                    dataText += "  ServiceCode:" + data.getServiceCode() + "\n";
                    dataText += "  Track1_dd:" + data.getTrack1_dd() + "\n";
                    dataText += "  Track2_dd:" + data.getTrack2_dd() + "\n";
                    txtReceiveData.setText(dataText);
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

        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }
}
