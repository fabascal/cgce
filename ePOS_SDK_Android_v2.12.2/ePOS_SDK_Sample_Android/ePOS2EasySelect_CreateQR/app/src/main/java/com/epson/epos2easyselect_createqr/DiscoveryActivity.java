package com.epson.epos2easyselect_createqr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2easyselect_createqr.common.Utility;

import java.util.ArrayList;
import java.util.HashMap;

public class DiscoveryActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Context mContext = null;
    private ArrayList<HashMap<String, Object>> mPrinterList = null;
    private SimpleAdapter mPrinterListAdapter = null;
    private FilterOption mFilterOption = null;

    // --------------------------------------------------------------------------------------------

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        mContext = this;

        Button button = (Button) findViewById(R.id.btnRestart);
        button.setOnClickListener(this);

        mPrinterList = new ArrayList<HashMap<String, Object>>();
        mPrinterListAdapter = new SimpleAdapter(this, mPrinterList, R.layout.list_at,
                new String[]{"PrinterName", "Target"},
                new int[]{R.id.PrinterName, R.id.Target});
        ListView list = (ListView) findViewById(R.id.lstReceiveData);
        list.setAdapter(mPrinterListAdapter);
        list.setOnItemClickListener(this);

        mFilterOption = new FilterOption();
        mFilterOption.setDeviceType(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NONE);
        try {
            Discovery.start(this, mFilterOption, mDiscoveryListener);
        } catch (Exception e) {
            ShowMsg.showException(e, "start", mContext);
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        while (true) {
            try {
                Discovery.stop();
                break;
            } catch (Epos2Exception e) {
                if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                    break;
                }
            }
        }

        mFilterOption = null;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onClick
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRestart:
                restartDiscovery();
                break;

            default:
                // Do nothing
                break;
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * On Item click
     *
     * @param parent   AdapterView<?>
     * @param view     clicked view
     * @param position position
     * @param id       id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();

        HashMap<String, Object> item = mPrinterList.get(position);
        intent.putExtra(getString(R.string.title_target), ((DeviceInfo) item.get("DeviceInfo")).getTarget());
        intent.putExtra(getString(R.string.title_interface),
                Utility.getInterfaceStringFromEposConnectionType(Utility.convertEpos2DeviceInfoToEposEasySelectDeviceType((DeviceInfo) item.get("DeviceInfo"))));
        intent.putExtra(getString(R.string.title_address),
                Utility.getAddressFromEpos2DeviceInfo((DeviceInfo) item.get("DeviceInfo")));
        intent.putExtra(getString(R.string.title_devicetype), Utility.convertEpos2DeviceInfoToEposEasySelectDeviceType((DeviceInfo) item.get("DeviceInfo")));

        setResult(RESULT_OK, intent);

        finish();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Restart discovery
     */
    private void restartDiscovery() {
        while (true) {
            try {
                Discovery.stop();
                break;
            } catch (Epos2Exception e) {
                if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                    ShowMsg.showException(e, "stop", mContext);
                    return;
                }
            }
        }

        mPrinterList.clear();
        mPrinterListAdapter.notifyDataSetChanged();

        try {
            Discovery.start(this, mFilterOption, mDiscoveryListener);
        } catch (Exception e) {
            ShowMsg.showException(e, "start", mContext);
        }
    }

    // --------------------------------------------------------------------------------------------
    /**
     * Listener Registration Method
     *
     * @param deviceInfo DeviceInfo object
     */
    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put("PrinterName", deviceInfo.getDeviceName());
                    item.put("Target", deviceInfo.getTarget());
                    item.put("DeviceInfo", deviceInfo);
                    mPrinterList.add(item);
                    mPrinterListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

}
