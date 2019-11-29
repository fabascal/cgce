package com.epson.epos2_discovery;

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
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.Log;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 100;

    private Context mContext = null;
    private ArrayList<HashMap<String, String>> mPrinterList = null;
    private SimpleAdapter mPrinterListAdapter = null;
    private Spinner mSpnPortType = null;
    private Spinner mSpnModel = null;
    private Spinner mSpnFilter = null;
    private Spinner mSpnType = null;
    private EditText mEdtBroadCast = null;
    private Button mBtnStart = null;
    private Button mBtnStop = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();

        mContext = this;

        int[] target = {
            R.id.btnStart,
            R.id.btnStop
        };

        for (int i = 0; i < target.length; i++) {
            Button button = (Button)findViewById(target[i]);
            button.setOnClickListener(this);
        }

        mPrinterList = new ArrayList<HashMap<String, String>>();
        mPrinterListAdapter = new SimpleAdapter(this, mPrinterList, R.layout.list_at,
                                                new String[] { "PrinterName", "Target" },
                                                new int[] { R.id.PrinterName, R.id.Target });
        ListView list = (ListView)findViewById(R.id.lstReceiveData);
        list.setAdapter(mPrinterListAdapter);

        mSpnPortType = (Spinner)findViewById(R.id.spnPortType);
        ArrayAdapter<SpnModelsItem> portTypeAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        portTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        portTypeAdapter.add(new SpnModelsItem("PORTTYPE_ALL", Discovery.PORTTYPE_ALL));
        portTypeAdapter.add(new SpnModelsItem("PORTTYPE_TCP", Discovery.PORTTYPE_TCP));
        portTypeAdapter.add(new SpnModelsItem("PORTTYPE_BLUETOOTH", Discovery.PORTTYPE_BLUETOOTH));
        portTypeAdapter.add(new SpnModelsItem("PORTTYPE_USB", Discovery.PORTTYPE_USB));
        mSpnPortType.setAdapter(portTypeAdapter);
        mSpnPortType.setSelection(0);

        mSpnModel = (Spinner)findViewById(R.id.spnDeviceModel);
        ArrayAdapter<SpnModelsItem> modelAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelAdapter.add(new SpnModelsItem("MODEL_ALL", Discovery.MODEL_ALL));
        mSpnModel.setAdapter(modelAdapter);
        mSpnModel.setSelection(0);

        mSpnFilter = (Spinner)findViewById(R.id.spnEpsonFilter);
        ArrayAdapter<SpnModelsItem> filterAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterAdapter.add(new SpnModelsItem("FILTER_NAME", Discovery.FILTER_NAME));
        filterAdapter.add(new SpnModelsItem("FILTER_NONE", Discovery.FILTER_NONE));
        mSpnFilter.setAdapter(filterAdapter);
        mSpnFilter.setSelection(0);

        mSpnType = (Spinner)findViewById(R.id.spnDeviceType);
        ArrayAdapter<SpnModelsItem> typeAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeAdapter.add(new SpnModelsItem("TYPE_ALL", Discovery.TYPE_ALL));
        mSpnType.setAdapter(typeAdapter);
        mSpnType.setSelection(0);

        mEdtBroadCast = (EditText)findViewById(R.id.edtSubnetMask);

        mBtnStart = (Button)findViewById(R.id.btnStart);
        mBtnStop = (Button)findViewById(R.id.btnStop);

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
            case R.id.btnStart:
                startDiscovery();
                break;

            case R.id.btnStop:
                stopDiscovery();
                break;

            default:
                //Do nothing
                break;
        }
    }

    private  void startDiscovery() {
        FilterOption filterOption = null;

        mPrinterList.clear();
        mPrinterListAdapter.notifyDataSetChanged();

        filterOption = new FilterOption();
        filterOption.setPortType(((SpnModelsItem) mSpnPortType.getSelectedItem()).getModelConstant());
        filterOption.setBroadcast(mEdtBroadCast.getText().toString());
        filterOption.setDeviceModel(((SpnModelsItem)mSpnModel.getSelectedItem()).getModelConstant());
        filterOption.setEpsonFilter(((SpnModelsItem)mSpnFilter.getSelectedItem()).getModelConstant());
        filterOption.setDeviceType(((SpnModelsItem)mSpnType.getSelectedItem()).getModelConstant());

        try {
            Discovery.start(mContext, filterOption, mDiscoveryListener);

            mBtnStart.setEnabled(false);
            mBtnStop.setEnabled(true);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "start", mContext);
        }
    }

    private void stopDiscovery() {
        try {
            Discovery.stop();

            mBtnStart.setEnabled(true);
            mBtnStop.setEnabled(false);
        }
        catch (Epos2Exception e) {
            if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                ShowMsg.showException(e, "stop", mContext);
            }
        }
    }

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("PrinterName", deviceInfo.getDeviceName());
                    item.put("Target", deviceInfo.getTarget());
                    mPrinterList.add(item);
                    mPrinterListAdapter.notifyDataSetChanged();
                }
            });
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