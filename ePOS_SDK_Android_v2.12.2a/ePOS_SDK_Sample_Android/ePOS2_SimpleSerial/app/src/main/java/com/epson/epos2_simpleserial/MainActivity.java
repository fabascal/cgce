package com.epson.epos2_simpleserial;

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
import com.epson.epos2.simpleserial.ReceiveListener;
import com.epson.epos2.simpleserial.SimpleSerial;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 100;

    private Context mContext = null;
    private SimpleSerial mSimpleSerial = null;
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
            R.id.btnClear,
            R.id.btnSend
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

            case R.id.btnSend:
                onSendCommand();
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

        if (!connectSimpleSerial()) {
            return;
        }
        mBtnConnect.setEnabled(false);
    }

    private void disconnectProcess() {
        disconnectSimpleSerial();

        finalizeObject();

        mBtnConnect.setEnabled(true);
    }

    private boolean initializeObject() {
        if (mSimpleSerial != null) {
            finalizeObject();
        }

        try {
            mSimpleSerial = new SimpleSerial(mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "SimpleSerial", mContext);

            return false;
        }

        mSimpleSerial.setReceiveEventListener(mReceiveEvent);
        mSimpleSerial.setConnectionEventListener(mConnectionChangedEvent);

        return true;
    }

    private void finalizeObject() {
        if (mSimpleSerial == null) {
            return;
        }

        mSimpleSerial.setReceiveEventListener(null);
        mSimpleSerial.setConnectionEventListener(null);

        mSimpleSerial = null;
    }

    private boolean connectSimpleSerial() {
        EditText edtTarget = (EditText)findViewById(R.id.edtTarget);

        if (mSimpleSerial == null) {
            return false;
        }

        try {
            mSimpleSerial.connect(edtTarget.getText().toString(), SimpleSerial.PARAM_DEFAULT);

            mIsConnect = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);

            finalizeObject();

            return false;
        }

        return true;
    }

    private void disconnectSimpleSerial() {
        if (mSimpleSerial == null) {
            return;
        }

        try {
            if(mIsConnect == true) {
                mSimpleSerial.disconnect();
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

    private void onSendCommand() {
        EditText edtSendData = (EditText)findViewById(R.id.edtSendData);
        String text = edtSendData.getText().toString();
        ArrayList<Byte> dataBuffer = new ArrayList<Byte>();
        Character enableNumber[] = {
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
            0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x61, 0x62, 0x63, 0x64, 0x65, 0x66
        };
        int start = 0;

        if (mSimpleSerial == null) {
            return;
        }

        edtSendData.setText("");

        for (int index = 0; index < text.length(); index++) {
            char charcter = text.charAt(index);

            if (charcter == 0x20 || index == text.length() - 1) {
                boolean numFlag = false;
                String target = "";
                if (charcter == 0x20) {
                    target = text.substring(start, index);
                }
                else {
                    target = text.substring(start, text.length());
                }
                start = index + 1;

                if (target.length() <= 2) {
                    for (int i = 0; i < target.length(); i++) {
                        char chkChar = target.charAt(i);
                        if (Arrays.asList(enableNumber).contains(chkChar)) {
                            numFlag = true;
                        }
                        else {
                            numFlag = false;
                            break;
                        }
                    }
                }
                if (numFlag) {
                    dataBuffer.add((byte)(Short.parseShort(target, 16) & 0xFF));
                }
                else {
                    byte [] bytes = null;
                    try {
                        bytes = target.getBytes("UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        ShowMsg.showException(e, "getBytes", mContext);
                    }

                    for (int j = 0; j < bytes.length; j++) {
                        dataBuffer.add(bytes[j]);
                    }
                }
            }
        }

        byte[] sendData = new byte[dataBuffer.size()];
        for (int n = 0; n < sendData.length; n++) {
            sendData[n] = dataBuffer.get(n);
            String displayStr = Integer.toString(dataBuffer.get(n), 16);
            if (displayStr.length() == 1) {
                displayStr = "0" + displayStr;
            }
            edtSendData.append(displayStr);
            edtSendData.append(" ");
        }

        if (sendData.length > 0) {
            try {
                mSimpleSerial.sendCommand(sendData);
            }
            catch (Exception e) {
                ShowMsg.showException(e, "sendCommand", mContext);
            }
        }
    }

    protected String getBinaryString(byte[] data) {
        int counter = 0;
        StringBuffer buffer = new StringBuffer();
        for (counter = 0; counter < data.length; counter++) {
            buffer.append(String.format("%02x ", data[counter]));
        }
        return buffer.toString();
    }

    private  ReceiveListener mReceiveEvent = new ReceiveListener() {
        @Override
        public void onSimpleSerialReceive(SimpleSerial serialObj, final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    if (data != null) {
                        TextView txtReceiveData = (TextView)findViewById(R.id.txtReceiveData);
                        txtReceiveData.append(getBinaryString(data));
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

