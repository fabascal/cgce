package com.epson.epos2_otherperipheral;

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
import com.epson.epos2.otherperipheral.OtherPeripheral;
import com.epson.epos2.otherperipheral.ReceiveListener;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 100;

    private Context mContext = null;
    private OtherPeripheral mOther = null;
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
                R.id.btnSendData,
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

            case R.id.btnSendData:
                onSendData();
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

        if (!connectOtherPeripheral()) {
            return;
        }

        mBtnConnect.setEnabled(false);
    }

    private void disconnectProcess() {
        disconnectOtherPeripheral();

        finalizeObject();

        mBtnConnect.setEnabled(true);
    }

    private boolean initializeObject() {
        if (mOther != null) {
            finalizeObject();
        }

        try {
            mOther = new OtherPeripheral(mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "OtherPeripheral", mContext);
            return false;
        }

        mOther.setReceiveEventListener(mReceiveEvent);
        mOther.setConnectionEventListener(mConnectionChangedEvent);

        return true;
    }

    private void finalizeObject() {
        if (mOther == null) {
            return;
        }

        mOther.setReceiveEventListener(null);
        mOther.setConnectionEventListener(null);

        mOther = null;

        return;
    }

    private boolean connectOtherPeripheral() {
        EditText edtTarget = (EditText)findViewById(R.id.edtTarget);

        if (mOther == null) {
            return false;
        }

        try {
            mOther.connect(edtTarget.getText().toString(), OtherPeripheral.PARAM_DEFAULT);

            mIsConnect = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);

            finalizeObject();

            return false;
        }
        return true;
    }

    private void disconnectOtherPeripheral() {
        if (mOther == null) {
            return;
        }

        try {
            if(mIsConnect == true) {
                mOther.disconnect();
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

    private void onSendData() {
        if(mOther == null) {
            return;
        }

        EditText edtMethodName = (EditText)findViewById(R.id.edtMethodName);
        EditText edtData = (EditText)findViewById(R.id.edtSendData);
        try {
            mOther.sendData(edtMethodName.getText().toString(), edtData.getText().toString());

        }
        catch (Exception e) {
            ShowMsg.showException(e, "sendData", mContext);
        }

    }

    private ReceiveListener mReceiveEvent = new ReceiveListener() {
        @Override
        public void onOtherReceive(OtherPeripheral otherObj, final String eventName, final String data) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    TextView txtReceiveData = (TextView)findViewById(R.id.txtReceiveData);

                    String receiveText = "";
                    receiveText += "OnReceive:\n";
                    receiveText += "  eventName:" + eventName + "\n";
                    receiveText += "  data:" + data + "\n";

                    txtReceiveData.setText(receiveText);
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
