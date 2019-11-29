package com.epson.epos2_keyboard;

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
import com.epson.epos2.keyboard.KeyPressListener;
import com.epson.epos2.keyboard.Keyboard;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 100;

    private Context mContext = null;
    private Keyboard mKeyboard = null;
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

        if (!connectKeyboard()) {
            return;
        }

        mBtnConnect.setEnabled(false);
    }

    private void disconnectProcess() {
        disconnectKeyboard();

        finalizeObject();

        mBtnConnect.setEnabled(true);
    }

    private boolean initializeObject() {
        if (mKeyboard != null) {
            finalizeObject();
        }

        try {
            mKeyboard = new Keyboard(mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Keyboard", mContext);
            return false;
        }

        mKeyboard.setKeyPressEventListener(mKeyPressEvent);
        mKeyboard.setConnectionEventListener(mConnectionChangedEvent);

        return true;
    }

    private void finalizeObject() {
        if (mKeyboard == null) {
            return;
        }

        mKeyboard.setKeyPressEventListener(null);
        mKeyboard.setConnectionEventListener(null);

        mKeyboard = null;

        return;
    }

    private boolean connectKeyboard() {
        EditText edtTarget = (EditText)findViewById(R.id.edtTarget);

        if (mKeyboard == null) {
            return false;
        }

        try {
            mKeyboard.connect(edtTarget.getText().toString(), Keyboard.PARAM_DEFAULT);

            mIsConnect = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);

            finalizeObject();

            return false;
        }
        return true;
    }

    private void disconnectKeyboard() {
        if (mKeyboard == null) {
            return;
        }

        try {
            if(mIsConnect == true) {
                mKeyboard.disconnect();
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

    private KeyPressListener mKeyPressEvent = new KeyPressListener() {
        @Override
        public void onKbdKeyPress(Keyboard keyboardObj, final int keyCode, final String ascii) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    if (keyCode != 0) {
                        TextView txtReceiveData = (TextView)findViewById(R.id.txtReceiveData);
                        txtReceiveData.append(ascii);
                    }
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
