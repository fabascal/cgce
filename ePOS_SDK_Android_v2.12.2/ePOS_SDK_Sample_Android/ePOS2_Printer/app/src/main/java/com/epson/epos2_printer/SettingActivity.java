package com.epson.epos2_printer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.MaintenanceCounterListener;
import com.epson.epos2.printer.PrinterSettingListener;

import java.util.HashMap;

public class SettingActivity extends Activity implements View.OnClickListener, MaintenanceCounterListener, PrinterSettingListener{

    private Context mContext = null;
    private ShowMsg mShowMessage = null;
    private Printer mPrinter = null;
    private TextView mLabelPaperFeed = null;
    private TextView mLabelAutoCutter = null;

    private Spinner mSpnPrintSpeed = null;
    private Spinner mSpnPrintDensity = null;
    private Spinner mSpnPaperWidth = null;

    private ProgressDialog mProgressDialog = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mContext = this;
        mShowMessage = new ShowMsg();
        mProgressDialog = new ProgressDialog(this);
        int[] target = {
                R.id.btnGetAutoCutter,
                R.id.btnResetAutoCutter,
                R.id.btnGetPaperFeed,
                R.id.btnResetPaperFeed,
                R.id.btnGetPrintSpeed,
                R.id.btnGetPrintDensity,
                R.id.btnGetPaperWidth,
                R.id.btnSetPrinterSetting
        };

        for (int i = 0; i < target.length; i++) {
            Button button = (Button)findViewById(target[i]);
            button.setOnClickListener(this);
        }

        mLabelAutoCutter = (TextView)findViewById(R.id.labelAutoCutter);
        mLabelPaperFeed = (TextView)findViewById(R.id.labelPaperFeed);

        mSpnPrintSpeed = (Spinner)findViewById(R.id.spnPrintSpeed);
        ArrayAdapter<SpnModelsItem> printSpeedAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        printSpeedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_1), Printer.SETTING_PRINTSPEED_1));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_2), Printer.SETTING_PRINTSPEED_2));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_3), Printer.SETTING_PRINTSPEED_3));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_4), Printer.SETTING_PRINTSPEED_4));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_5), Printer.SETTING_PRINTSPEED_5));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_6), Printer.SETTING_PRINTSPEED_6));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_7), Printer.SETTING_PRINTSPEED_7));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_8), Printer.SETTING_PRINTSPEED_8));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_9), Printer.SETTING_PRINTSPEED_9));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_10), Printer.SETTING_PRINTSPEED_10));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_11), Printer.SETTING_PRINTSPEED_11));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_12), Printer.SETTING_PRINTSPEED_12));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_13), Printer.SETTING_PRINTSPEED_13));
        printSpeedAdapter.add(new SpnModelsItem(getString(R.string.printSpeed_14), Printer.SETTING_PRINTSPEED_14));
        mSpnPrintSpeed.setAdapter(printSpeedAdapter);
        mSpnPrintSpeed.setSelection(0);

        mSpnPrintDensity = (Spinner)findViewById(R.id.spnPrintDensity);
        ArrayAdapter<SpnModelsItem> printDensityAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        printDensityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_DIP), Printer.SETTING_PRINTDENSITY_DIP));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_70), Printer.SETTING_PRINTDENSITY_70));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_75), Printer.SETTING_PRINTDENSITY_75));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_80), Printer.SETTING_PRINTDENSITY_80));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_85), Printer.SETTING_PRINTDENSITY_85));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_90), Printer.SETTING_PRINTDENSITY_90));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_95), Printer.SETTING_PRINTDENSITY_95));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_100), Printer.SETTING_PRINTDENSITY_100));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_105), Printer.SETTING_PRINTDENSITY_105));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_110), Printer.SETTING_PRINTDENSITY_110));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_115), Printer.SETTING_PRINTDENSITY_115));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_120), Printer.SETTING_PRINTDENSITY_120));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_125), Printer.SETTING_PRINTDENSITY_125));
        printDensityAdapter.add(new SpnModelsItem(getString(R.string.printDensity_130), Printer.SETTING_PRINTDENSITY_130));
        mSpnPrintDensity.setAdapter(printDensityAdapter);
        mSpnPrintDensity.setSelection(0);

        mSpnPaperWidth = (Spinner)findViewById(R.id.spnPaperWidth);
        ArrayAdapter<SpnModelsItem> paperWidthAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        paperWidthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paperWidthAdapter.add(new SpnModelsItem(getString(R.string.paperWidth_58), Printer.SETTING_PAPERWIDTH_58_0));
        paperWidthAdapter.add(new SpnModelsItem(getString(R.string.paperWidth_60), Printer.SETTING_PAPERWIDTH_60_0));
        paperWidthAdapter.add(new SpnModelsItem(getString(R.string.paperWidth_80), Printer.SETTING_PAPERWIDTH_80_0));
        mSpnPaperWidth.setAdapter(paperWidthAdapter);
        mSpnPaperWidth.setSelection(0);

        if(!initializeObject()) {
            return;
        }

        if(!connectPrinter()) {
            return;
        }
    }

    public void onDestroy() {
        disconnectPrinter();
        finalizeObject();
        super.onDestroy();
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnGetAutoCutter:
                getAutoCutterCount();
                break;
            case R.id.btnResetAutoCutter:
                resetAutoCutterCount();
                break;
            case R.id.btnGetPaperFeed:
                getPaperFeedRow();
                break;
            case R.id.btnResetPaperFeed:
                resetPaperFeedRow();
                break;
            case R.id.btnGetPrintSpeed:
                getPrintSpeed();
                break;
            case R.id.btnGetPrintDensity:
                getPrintDensity();
                break;
            case R.id.btnGetPaperWidth:
                getPaperWidth();
                break;
            case R.id.btnSetPrinterSetting:
                setPrinterSetting();
                break;
            default:
                break;
        }
    }

// Maintenance Couter
    protected void getAutoCutterCount() {
        try {
            mPrinter.getMaintenanceCounter(Printer.PARAM_DEFAULT, Printer.MAINTENANCE_COUNTER_AUTOCUTTER, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "getMaintenanceCounter", mContext);
        }
    }

    protected void getPaperFeedRow() {
        try {
            mPrinter.getMaintenanceCounter(Printer.PARAM_DEFAULT, Printer.MAINTENANCE_COUNTER_PAPERFEED, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "getMaintenanceCounter", mContext);
        }
    }

    public void onGetMaintenanceCounter(final int code, final int type, final int value) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {

        if(code != Epos2CallbackCode.CODE_SUCCESS)
        {
            ShowMsg.showResult(code, "getMaintenanceCounter", mContext);
            return;
        }
        changeMaintenanceCounterLabel(type, value);
            }
        });
    }

    protected void changeMaintenanceCounterLabel(final int type, final int value) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {

                switch(type) {
                    case Printer.MAINTENANCE_COUNTER_AUTOCUTTER:
                        mLabelAutoCutter.setText(Integer.toString(value));
                        break;
                    case Printer.MAINTENANCE_COUNTER_PAPERFEED:
                        mLabelPaperFeed.setText(Integer.toString(value));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    protected void resetAutoCutterCount() {
        try {
            mPrinter.resetMaintenanceCounter(Printer.PARAM_DEFAULT, Printer.MAINTENANCE_COUNTER_AUTOCUTTER, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "resetMaintenanceCounter", mContext);
        }
    }

    protected void resetPaperFeedRow() {
        try {
            mPrinter.resetMaintenanceCounter(Printer.PARAM_DEFAULT, Printer.MAINTENANCE_COUNTER_PAPERFEED, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "resetMaintenanceCounter", mContext);
        }
    }

    public void onResetMaintenanceCounter(final int code, final int type) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {

                if(code !=Epos2CallbackCode.CODE_SUCCESS) {
                    ShowMsg.showResult(code, "resetMaintenanceCounter", mContext);
                }

                // When reset success, get current count to confirmation.
                switch(type) {
                    case Printer.MAINTENANCE_COUNTER_AUTOCUTTER:
                        getAutoCutterCount();
                        break;
                    case Printer.MAINTENANCE_COUNTER_PAPERFEED:
                        getPaperFeedRow();
                        break;
                    default:
                        break;
                }
            }
        });
    }

// Printer Setting
    protected void getPrintSpeed() {
        try {
            mPrinter.getPrinterSetting(Printer.PARAM_DEFAULT, Printer.SETTING_PRINTSPEED, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "getPrinterSetting", mContext);
        }
    }

    protected void getPrintDensity() {
        try {
            mPrinter.getPrinterSetting(Printer.PARAM_DEFAULT, Printer.SETTING_PRINTDENSITY, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "getPrinterSetting", mContext);
        }
    }

    protected void getPaperWidth() {
        try {
            mPrinter.getPrinterSetting(Printer.PARAM_DEFAULT, Printer.SETTING_PAPERWIDTH, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "getPrinterSetting", mContext);
        }
    }

    public void onGetPrinterSetting(final int code, final int type, final int value) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {

                if(code != Epos2CallbackCode.CODE_SUCCESS) {
                    ShowMsg.showResult(code, "getPrinterSetting", mContext);
                    return;
                }
                changePrinterSettingLabel(type, value);
            }
        });
    }

    protected void changePrinterSettingLabel(final int type, final int value) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {

                Spinner spinner = null;
                switch(type) {
                    case Printer.SETTING_PRINTSPEED:
                        spinner = mSpnPrintSpeed;
                        break;
                    case Printer.SETTING_PRINTDENSITY:
                        spinner = mSpnPrintDensity;
                        break;
                    case Printer.SETTING_PAPERWIDTH:
                        spinner = mSpnPaperWidth;
                        break;
                    default:
                        break;
                }

                if(spinner != null) {
                    SpinnerAdapter adapter = spinner.getAdapter();

                    for (int i = 0; i < adapter.getCount(); i++) {
                        SpnModelsItem item = (SpnModelsItem) (adapter.getItem(i));
                        int itemValue = (Integer) (item.getModelConstant());
                        if (value == itemValue) {
                            spinner.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    protected void setPrinterSetting() {
        HashMap settingList = new HashMap();
        settingList.put(Printer.SETTING_PRINTSPEED, getConstantValue(mSpnPrintSpeed));
        settingList.put(Printer.SETTING_PRINTDENSITY, getConstantValue(mSpnPrintDensity));
        settingList.put(Printer.SETTING_PAPERWIDTH, getConstantValue(mSpnPaperWidth));
        try {
            mPrinter.setPrinterSetting(Printer.PARAM_DEFAULT, settingList, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "setPrinterSetting", mContext);
            return;
        }

        // Show wating restart message. onSetPrinterSetting stop this message.
        showPrinterRestartMessage();
    }

    protected int getConstantValue(Spinner spinner) {
        int retValue = 0;
        if(spinner != null) {
            SpinnerAdapter adapter = spinner.getAdapter();
            SpnModelsItem item = (SpnModelsItem)spinner.getSelectedItem();
            retValue = (Integer) (item.getModelConstant());
        }
        return retValue;
    }

    public void onSetPrinterSetting(final int code) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {

                if(code != Epos2CallbackCode.CODE_SUCCESS) {
                    hidePrinterRestartMessage();
                    ShowMsg.showResult(code, "setPrinterSetting", mContext);
                    return;
                }

                new Thread(new Runnable() {
                    public void run() {
                        reconnectPrinter_And_GetPrinterSetting();
                    }
                }).start();
            }
        });
    }

    protected void reconnectPrinter_And_GetPrinterSetting() {
        // Reconect printer with 3 sec interval until 120 sec done.
        boolean ret = reconnect(120, 3);
        // Hide wating restart message
        hidePrinterRestartMessage();

        // If error, back connection setting view to confirm connection setting.
        if(!ret) {
            runOnUiThread(new Runnable() {
                public synchronized void run() {
                    String msg = String.format(
                            "\t%s\n\t%s\n\n\t%s\n\t%s\n",
                            mContext.getString(R.string.title_msg_result),
                            "ERR_CONNECT",
                            mContext.getString(R.string.title_msg_description),
                            "setPrinterSetting");
                ShowMsg.showMsg(msg, mContext);
                }
            });
            return;
        }

        // When restart success, get current setting to confirmation.
        try {
            mPrinter.getPrinterSetting(Printer.PARAM_DEFAULT, Printer.SETTING_PRINTSPEED, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "getPrinterSetting", mContext);
            return;
        }

        //Wait onGetPrinterSetting callback to avoid multiple API calling.
        try {
            Thread.sleep(200);
        }
        catch(InterruptedException e) {
        }

        try {
            mPrinter.getPrinterSetting(Printer.PARAM_DEFAULT, Printer.SETTING_PRINTDENSITY, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "getPrinterSetting", mContext);
            return;
        }

        //Wait onGetPrinterSetting callback to avoid multiple API calling.
        try {
            Thread.sleep(200);
        }
        catch(InterruptedException e) {
        }

        try {
            mPrinter.getPrinterSetting(Printer.PARAM_DEFAULT, Printer.SETTING_PAPERWIDTH, this);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "getPrinterSetting", mContext);
            return;
        }
    }

    protected boolean reconnect(int maxWait, int interval) {
        int timeout = 0;
        try {
            mPrinter.disconnect();
        }
        catch(Exception e) {
        }

        // Sleep 30 sec due to some printer do not available immediately after power on. see your printer's spec sheet.
        try {
            Thread.sleep(30000);
        }
        catch(InterruptedException e) {
        }

        timeout += 30;

        do {
            try {
                mPrinter.connect(MainActivity.mEditTarget.getText().toString(), interval*1000);
                break;
            }
            catch (Exception e) {
            }

            timeout += interval;    // Not correct measuring. When result is not TIMEOUT, wraptime do not equal interval.

            if(timeout > maxWait) {
                return false;
            }
        }while(true);

        return true;
    }

// Restarting Message
    protected void showPrinterRestartMessage(){
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("now printer restarting");
        mProgressDialog.show();
    }

    protected void hidePrinterRestartMessage(){
        mProgressDialog.dismiss();
    }

// Printer control
    private boolean initializeObject() {
        try {
            mPrinter = new Printer(((SpnModelsItem) MainActivity.mSpnSeries.getSelectedItem()).getModelConstant(),
                    ((SpnModelsItem) MainActivity.mSpnLang.getSelectedItem()).getModelConstant(),
                    mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }
        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter = null;
    }

    protected boolean connectPrinter(){
        if (mPrinter == null) {
            return false;
        }

        try {
            mPrinter.connect(MainActivity.mEditTarget.getText().toString(), Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }
        return true;
    }

    protected void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            runOnUiThread(new Runnable() {
                public synchronized void run() {
                    ShowMsg.showException(e, "disconnect", mContext);
                }
            });
        }
    }

}
