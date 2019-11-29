package com.epson.epos2_commbox;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.epson.epos2.ConnectionListener;
import com.epson.epos2.Log;
import com.epson.epos2.commbox.CommBox;
import com.epson.epos2.commbox.ReceiveListener;
import com.epson.epos2.commbox.SendMessageCallback;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 100;

    private Context mContext = null;
    private CommBox mCommBox = null;
    private ArrayAdapter<String> mAdapter = null;
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

        ListView list = (ListView) findViewById(R.id.lstReceiveMsg);
        mAdapter = new ArrayAdapter<String>(mContext, R.layout.receive_list_item);
        list.setAdapter(mAdapter);

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
                sendCommand();
                break;

            case R.id.btnClear:
                mAdapter.clear();
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

        if (!connectCommBox()) {
            return;
        }

        mBtnConnect.setEnabled(false);
    }

    private void disconnectProcess() {
        disconnectCommBox();

        finalizeObject();

        mBtnConnect.setEnabled(true);
    }

    private boolean initializeObject() {
        if (mCommBox != null) {
            finalizeObject();
        }

        try {
            mCommBox = new CommBox(mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "CommBox", mContext);

            return false;
        }

        mCommBox.setReceiveEventListener(mReceiveListener);
        mCommBox.setConnectionEventListener(mConnectionChangedEvent);

        return true;
    }

    private void finalizeObject() {
        if (mCommBox == null) {
            return;
        }

        mCommBox.setReceiveEventListener(null);
        mCommBox.setConnectionEventListener(null);

        mCommBox = null;
    }

    private boolean connectCommBox() {
        EditText edtTarget = (EditText)findViewById(R.id.edtTarget);
        EditText edtMyId = (EditText)findViewById(R.id.edtMyId);

        if (mCommBox == null) {
            return false;
        }

        try {
            mCommBox.connect(edtTarget.getText().toString(), CommBox.PARAM_DEFAULT, edtMyId.getText().toString());

            mIsConnect = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);

            finalizeObject();

            return false;
        }

        return true;
    }

    private void disconnectCommBox() {
        if (mCommBox == null) {
            return;
        }

        try {
            if(mIsConnect == true) {
                mCommBox.disconnect();
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

    private void sendCommand() {
        if (mCommBox == null) {
            return;
        }

        EditText edtMessage = (EditText)findViewById(R.id.edtMessage);
        EditText edtTargetId = (EditText)findViewById(R.id.edtTargetId);
        String message = edtMessage.getText().toString();
        String targetId = edtTargetId.getText().toString();

        try {
            mCommBox.sendMessage(message, targetId, mSendMessageListener);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "sendMessage", mContext);
        }
    };

    private  SendMessageCallback mSendMessageListener = new SendMessageCallback() {
        @Override
        public void onCommBoxSendMessage(CommBox commBoxObj, final int code, int count) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showResult(code, mContext);
                }
            });
        }
    };

    private ReceiveListener mReceiveListener = new ReceiveListener() {
        @Override
        public void onCommBoxReceive(CommBox commBoxObj, final String senderId, String receiverId, final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    mAdapter.add(String.format("From:%s %s", senderId, message));
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
