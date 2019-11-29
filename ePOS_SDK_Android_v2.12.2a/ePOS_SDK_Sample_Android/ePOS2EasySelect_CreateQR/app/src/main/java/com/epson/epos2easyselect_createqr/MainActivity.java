package com.epson.epos2easyselect_createqr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.epson.easyselect.EasySelect;
import com.epson.easyselect.EasySelectDeviceType;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.Log;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.epson.epos2easyselect_createqr.common.Utility;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, ReceiveListener {

    private static final int REQUEST_PERMISSION = 100;

    // --------------------------------------------------------------------------------------------

    private Context mContext = null;
    private Spinner mSpnPrinterName = null;
    private Printer mPrinter = null;
    private int mDeviceType = EasySelectDeviceType.TCP;
    private String mPrinterName = null;
    private String mTarget = null;
    private String mInterfaceType = null;
    private String mAddress = null;

    // --------------------------------------------------------------------------------------------
    // print data
    private static final String PRINT_TEXT_DEVICE = "Device:";
    private static final String PRINT_TEXT_INTERFACE = "Interface:";
    private static final String PRINT_TEXT_ADDRESS = "Address:";

    // --------------------------------------------------------------------------------------------

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Runtime permission
        requestRuntimePermission();

        mContext = this;

        int[] target = {
                R.id.btnDiscovery,
                R.id.btnCreateQrCode
        };

        for (int i = 0; i < target.length; i++) {
            Button button = (Button) findViewById(target[i]);
            button.setOnClickListener(this);
        }

        //Create PrinterName Spinner
        mSpnPrinterName = (Spinner) findViewById(R.id.spnPrinterName);
        ArrayAdapter<String> printerNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        printerNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        printerNameAdapter.add(getString(R.string.printername_m10));
        printerNameAdapter.add(getString(R.string.printername_m30));
        printerNameAdapter.add(getString(R.string.printername_p20));
        printerNameAdapter.add(getString(R.string.printername_p60ii));
        printerNameAdapter.add(getString(R.string.printername_p80));
        printerNameAdapter.add(getString(R.string.printername_t88v));
        printerNameAdapter.add(getString(R.string.printername_t88vi));
        printerNameAdapter.add(getString(R.string.printername_h6000v));
        mSpnPrinterName.setAdapter(printerNameAdapter);
        mSpnPrinterName.setSelection(0);

        try {
            Log.setLogSettings(mContext, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 1, Log.LOGLEVEL_LOW);
        } catch (Exception e) {
            ShowMsg.showException(e, "setLogSettings", mContext);
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onActivityResult
     */
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (data != null && resultCode == RESULT_OK) {

            String target = data.getStringExtra(getString(R.string.title_target));
            String interfaceType = data.getStringExtra(getString(R.string.title_interface));
            String address = data.getStringExtra(getString(R.string.title_address));
            //Data for create QRcode
            mDeviceType = data.getIntExtra(getString(R.string.title_devicetype), EasySelectDeviceType.TCP);

            if (target != null) {
                //View
                TextView mTxtTarget = (TextView) findViewById(R.id.txtTarget);
                mTxtTarget.setText(target);
                //Data for create QRcode
                mTarget = target;
            }
            if (interfaceType != null) {
                //View
                TextView mTxtInterface = (TextView) findViewById(R.id.txtInterface);
                mTxtInterface.setText(interfaceType);
                //Data for create QRcode
                mInterfaceType = interfaceType;
            }
            if (address != null) {
                //View
                TextView mTxtAddress = (TextView) findViewById(R.id.txtAddress);
                mTxtAddress.setText(address);
                //Data for create QRcode
                mAddress = address;
            }
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onClick
     *
     * @param v View
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnDiscovery:
                Intent intent = new Intent(this, DiscoveryActivity.class);
                startActivityForResult(intent, 0);
                break;

            case R.id.btnCreateQrCode:
                mPrinterName = (String) mSpnPrinterName.getSelectedItem();
                updateButtonState(false);
                if (!runPrintQRCodeSequence()) {
                    updateButtonState(true);
                }
                break;

            default:
                // Do nothing
                break;
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Run Print QRCode Sequence
     */
    private boolean runPrintQRCodeSequence() {

        if (!initializeObject()) {
            return false;
        }

        if (!createQrCodeData()) {
            finalizeObject();
            return false;
        }

        if (!printData()) {
            finalizeObject();
            return false;
        }

        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Create QRCode data
     */
    private boolean createQrCodeData() {
        String method = "";

        if (mPrinter == null) {
            return false;
        }

        try {
            // header
            method = "makeHeaderText";
            if (!makeHeaderText()) {
                return false;
            }

            // QR code
            method = "makeQrCode";
            if (!makeQrCode()) {
                return false;
            }

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);

        } catch (Exception e) {
            ShowMsg.showException(e, method, mContext);
            return false;
        }

        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * make print header text
     *
     * @return boolean
     */
    private boolean makeHeaderText() {
        String method = "";

        if (mPrinter == null) {
            return false;
        }

        StringBuilder textData = new StringBuilder();

        try {
            // header
            // device name
            method = "addText";
            textData.append(PRINT_TEXT_DEVICE);
            textData.append(mPrinterName);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            // interface
            method = "addText";
            textData.append(PRINT_TEXT_INTERFACE);
            textData.append(mInterfaceType);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            // MAC address
            method = "addText";
            textData.append(PRINT_TEXT_ADDRESS);
            textData.append(mAddress);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addFeedLine";
            mPrinter.addFeedLine(2);

        } catch (Epos2Exception e) {
            ShowMsg.showException(e, method, mContext);
            return false;
        }

        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * make body text
     *
     * @return boolean
     */
    private boolean makeQrCode() {
        String method = "";

        if (mPrinter == null) {
            return false;
        }

        String qrCode = new String();
        // QR code size
        final int qrcodeWidth = 5;
        final int qrcodeHeight = 5;

        try {
            EasySelect easySelect = new EasySelect();

            // create QR code data from EasySelect library
            method = "createQR";
            qrCode = easySelect.createQR(mPrinterName,
                    mDeviceType,
                    mAddress);
            if (null == qrCode) {
                Exception e = new Exception(method);
                ShowMsg.showException(e, method, mContext);
                return false;
            }

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            // QR Code
            method = "addSymbol";
            mPrinter.addSymbol(qrCode,
                    Printer.SYMBOL_QRCODE_MODEL_2,
                    Printer.LEVEL_L,
                    qrcodeWidth,
                    qrcodeHeight,
                    0);

            // feed & cut
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

        } catch (Epos2Exception e) {
            ShowMsg.showException(e, method, mContext);
            return false;
        }

        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Print data
     *
     * @return boolean result
     */
    private boolean printData() {
        String msg = "";

        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            ShowMsg.showMsg(makeErrorMessage(status), mContext);
            try {
                mPrinter.disconnect();
            } catch (Epos2Exception ex) {
                // Do nothing
            }
            finalizeObject();
            return false;
        }

        try {
            msg = "sendData";
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            ShowMsg.showException(e, msg, mContext);
            disconnectPrinter();
            return false;
        }

        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Initialize printer object
     *
     * @return boolean result
     */
    private boolean initializeObject() {
        try {
            mPrinter = new Printer(Utility.convertPrinterNameToPrinterSeries(mPrinterName),
                    Printer.MODEL_ANK,
                    mContext);
        } catch (Exception e) {
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Finalize printer object
     */
    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Connect printer
     *
     * @return boolean result
     */
    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            mPrinter.connect(mTarget, Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        } catch (Exception e) {
            ShowMsg.showException(e, "beginTransaction", mContext);
        }

        if (!isBeginTransaction) {
            try {
                mPrinter.disconnect();
            } catch (Epos2Exception e) {
                return false;
            }
        }

        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Disconnect printer
     */
    private void disconnectPrinter() {
        String method = "";

        if (mPrinter == null) {
            return;
        }

        try {
            method = "endTransaction";
            mPrinter.endTransaction();
        } catch (final Exception e) {
            final String errorMethod = method;
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, errorMethod, mContext);
                }
            });
        }

        try {
            method = "disconnect";
            mPrinter.disconnect();
        } catch (final Exception e) {
            final String errorMethod = method;
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, errorMethod, mContext);
                }
            });
        }

        finalizeObject();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Disconnect printer
     *
     * @param status PrinterStatusInfo
     * @return boolean result
     */
    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        } else if (status.getOnline() == Printer.FALSE) {
            return false;
        } else {
            //print available
        }

        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Make error message
     *
     * @param status PrinterStatusInfo
     * @return String error message
     */
    private String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += getString(R.string.handlingmsg_err_autocutter);
            msg += getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Display warnings
     *
     * @param status PrinterStatusInfo
     */
    private void dispPrinterWarnings(PrinterStatusInfo status) {
        EditText edtWarnings = (EditText) findViewById(R.id.edtWarnings);
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
        }

        edtWarnings.setText(warningsMsg);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Update button state
     *
     * @param state boolean
     */
    private void updateButtonState(boolean state) {
        Button btnReceipt = (Button) findViewById(R.id.btnCreateQrCode);
        btnReceipt.setEnabled(state);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Listener Registration Method
     *
     * @param printerObj Printer object
     * @param code       result
     * @param status     Printer status
     * @param printJobId JobId
     */
    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, makeErrorMessage(status), mContext);

                dispPrinterWarnings(status);

                updateButtonState(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }

    // --------------------------------------------------------------------------------------------

    /**
     * requestRuntimePermission
     */
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

    // --------------------------------------------------------------------------------------------

    /**
     * onRequestPermissionsResult
     *
     * @param requestCode The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
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