package com.epson.epos2easyselect_parse;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.epson.easyselect.EasySelect;
import com.epson.easyselect.EasySelectDeviceType;
import com.epson.easyselect.EasySelectInfo;
import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.Log;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.epson.epos2easyselect_parse.camera.CameraController;
import com.epson.epos2easyselect_parse.camera.CameraPreviewCallback;
import com.epson.epos2easyselect_parse.common.BroadcastCallback;
import com.epson.epos2easyselect_parse.common.BroadcastManager;
import com.epson.epos2easyselect_parse.common.CustomProgressDialog;
import com.epson.epos2easyselect_parse.common.MessageBox;
import com.epson.epos2easyselect_parse.common.Utility;
import com.epson.epos2easyselect_parse.common.bluetooth.BluetoothController;
import com.epson.epos2easyselect_parse.common.bluetooth.IntentListBluetooth;
import com.epson.epos2easyselect_parse.common.wifi.IntentListWiFi;
import com.epson.epos2easyselect_parse.common.wifi.WiFiController;
import com.epson.epos2easyselect_parse.nfc.NFCController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity
        implements OnClickListener, BroadcastCallback, CameraPreviewCallback, ReceiveListener {
    // --------------------------------------------------------------------------------------------
    private static final String LI_LABEL = "li_label";
    private static final String LI_VALUE = "li_value";

    private static final int ACTION_NONE = 0;
    private static final int ACTION_CONNECT = 1;
    private static final int ACTION_PRINT = 2;

    private static final int WAIT_WIFI_SCAN = 2000;

    private static final int RESULT_SUCCESS = 0;
    private static final int RESULT_ERROR_CONNECT = 1;
    private static final int RESULT_UNKNOWN = 2;

    private static final int REQUEST_PERMISSION = 100;

    // --------------------------------------------------------------------------------------------
    // View
    private Button mPrintButton = null;
    private ImageView mNfcImage = null;
    private LinearLayout mNfcSettingsLayout = null;
    private Button mNfcSettingsButton = null;
    private TextView mNfcSettingsText = null;
    private TextView mConnectingText = null;

    private Thread mOpenThread = null;
    private Thread mPrintThread = null;

    // --------------------------------------------------------------------------------------------
    private EasySelect mEasySelect = null;       // EasySelect Library
    private EasySelectInfo mEasySelectInfo = null;
    private ArrayList<EasySelectInfo> mEasySelectInfoArray = null;

    private NFCController mNfcCtr = null;
    private CameraController mCameraCtr = null;

    private BarcodeManager mBarcodeManager = null;

    private WiFiController mWifiController = null;
    private BluetoothController mBluetoothController = null;

    private BroadcastManager mBroadcastManager = null;

    private boolean mConnecting = false;

    private boolean mConnectedPrinter = false;

    private int mAction = ACTION_NONE;

    private boolean mIsCreatePreview = false;

    private Handler mHandler = null;

    private Printer mPrinter = null;

    private int mPrinterSeries = Printer.TM_T88;

    private String mTargetText = null;

    // --------------------------------------------------------------------------------------------
    // print text
    private static final String PRINT_LINE = "--------------------";
    private static final String PRINT_TITLE = "Sample Print";
    private static final String PRINT_WIFI_ADDRESS = "Network Address:";
    private static final String PRINT_BLUETOOTH_ADDRESS = "Bluetooth Address:";
    private static final String PRINT_MESSAGE = "Print successfully!!";
    private CustomProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Runtime permission
        requestRuntimePermission();

        // View
        mNfcImage = (ImageView) findViewById(R.id.QP_img_NFC);

        mPrintButton = (Button) findViewById(R.id.QP_btn_Print);
        mNfcSettingsLayout = (LinearLayout) findViewById(R.id.quickPairing_nfc_settings_LinearLayout);
        mNfcSettingsText = (TextView) findViewById(R.id.QP_msg_NfcInformation);
        mNfcSettingsButton = (Button) findViewById(R.id.QP_btn_NfcSetting);

        mConnectingText = (TextView) findViewById(R.id.QP_msg_Information);

        mPrintButton.setEnabled(false);

        // Button
        registClickListener();

        setSelectPrinterInfo("", -1, "");

        mEasySelect = new EasySelect();

        mWifiController = new WiFiController(this);
        mBluetoothController = new BluetoothController();

        mBroadcastManager = new BroadcastManager();

        mHandler = new Handler();

        // interface enable
        checkInterface(EasySelectDeviceType.BLUETOOTH);
        checkInterface(EasySelectDeviceType.TCP);

        // initialize NFC & Camera
        initNfc();
        initCamera();

        try {
            Log.setLogSettings(this, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 1, Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            //Do nothig
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onResume
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcCtr.isSuport()) {
            if (mNfcCtr.isEnabled()) {
                mNfcSettingsLayout.setVisibility(View.INVISIBLE);

                mNfcCtr.enableDispatch(this, true);
                mNfcImage.setVisibility(View.VISIBLE);

            } else {
                // disable NFC
                mNfcSettingsText
                        .setText(getString(R.string.QP_msg_NfcDisabled));
                mNfcSettingsButton.setEnabled(true);
                mNfcSettingsLayout.setVisibility(View.VISIBLE);
                mNfcImage.setVisibility(View.INVISIBLE);
            }
        }

        if (mConnectedPrinter) {
            callConnectPrinter();
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onPause
     */
    @Override
    protected void onPause() {
        if (mNfcCtr.isSuport()) {
            mNfcCtr.enableDispatch(this, false);
        }

        if (null != mOpenThread) {
            mOpenThread.interrupt();
        }

        if (null != mPrintThread) {
            mPrintThread.interrupt();
        }

        if (mConnectedPrinter) {
            mConnectedPrinter = false;
        }

        super.onPause();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onStop
     */
    @Override
    protected void onStop() {
        mPrintButton.setEnabled(false);
        mConnectingText.setVisibility(View.INVISIBLE);

        setSelectPrinterInfo("", -1, "");

        waitScan(false);

        releaseCameraPreview();
        mIsCreatePreview = false;

        super.onStop();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onDestroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            case R.id.QP_btn_NfcSetting:
                runNfcSetting();
                break;

            case R.id.QP_btn_Print:
                runPrintSample();
                break;

            default:
                break;
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onWindowFocusChanged
     *
     * @param hasFocus HasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (null == mCameraCtr) {
            return;
        }

        if (!mCameraCtr.isSuport()) {
            return;
        }

        if (hasFocus) {
            if (!mIsCreatePreview) {
                createCameraPreview();
                mIsCreatePreview = true;
            } else {
                mCameraCtr.startCameraPreview();
                mCameraCtr.waitScanPreview(false);
            }

        } else {
            mCameraCtr.waitScanPreview(true);
            mCameraCtr.stopCameraPreview();
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onKeyDown
     *
     * @param keyCode KeyCode
     * @param event   KeyEvent
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            releaseCameraPreview();
        }

        return super.onKeyDown(keyCode, event);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onNewIntent
     *
     * @param intent Intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        // event frow : onPause -> onNewIntent -> onResume

        // read NFC, stop preview scan
        Tag tag = mNfcCtr.scanNfc(intent);
        if (null == tag) {
            // not NFC tag
            waitScan(false);
            return;
        }

        // stop the scan of the camera / NFC.
        waitScan(true);

        // parse NFC Tag
        ArrayList<EasySelectInfo> easySelectInfoList = mEasySelect.parseNFC(
                tag, EasySelect.PARSE_NFC_TIMEOUT_DEFAULT);

        try {
            int size = easySelectInfoList.size();

            if (1 == size) {
                // Single record
                mEasySelectInfo = easySelectInfoList.get(0);

                if ((null == mEasySelectInfo.printerName)
                        || mEasySelectInfo.printerName.equals("")) {
                    // Please specify the printer name of the use printers.
                    mEasySelectInfo.printerName = "TM-T88V";
                }

                if ((null == mEasySelectInfo.macAddress)
                        || mEasySelectInfo.macAddress.equals("")) {
                    // When a communication error occurred, macAddress is empty string.
                    mEasySelectInfo = null;
                    waitScan(false);
                    return;
                }

                callConnectPrinter();

            } else if (1 < size) {
                // Multiple records
                mEasySelectInfoArray = easySelectInfoList;
                mEasySelectInfo = easySelectInfoList.get(0);

                callConnectPrinterEx();

            } else {
                // other NFC

                waitScan(false);
            }

        } catch (Exception e) {
            // other NFC

            waitScan(false);
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * onTouchEvent
     *
     * @param event MotionEvent
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            if (null != mCameraCtr) {
                // If does not support the AF, the focus is set by the tap.
                mCameraCtr.setFocus();
            }
        }

        return super.onTouchEvent(event);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * BloaccastReceiver onReceive
     *
     * @param context Context
     * @param intent  Intent
     */
    @Override
    public void broadcastCallback(Context context, Intent intent) {
        if (intent.getAction().equals(
                WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int status = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);

            // enable Wi-Fi
            if (WifiManager.WIFI_STATE_ENABLED == status) {
                mBroadcastManager.unregisterFilter(this);
                mBroadcastManager.unregistCallback();

                // example wait wi-fi connect
                // if changed enabled, can not connect immediately.
                try {
                    Thread.sleep(WAIT_WIFI_SCAN);
                } catch (InterruptedException e) {
                    // nothing
                }

                if (ACTION_CONNECT == mAction) {
                    callConnectPrinter();

                } else if (ACTION_PRINT == mAction) {
                    printDemo();
                } else {
                    // nothing
                }

                mAction = ACTION_NONE;
            }

        } else if (intent.getAction().equals(
                BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.STATE_OFF);

            // enable Bluetooth
            if (BluetoothAdapter.STATE_ON == status) {
                mBroadcastManager.unregisterFilter(this);
                mBroadcastManager.unregistCallback();

                if (ACTION_CONNECT == mAction) {
                    callConnectPrinter();

                } else if (ACTION_PRINT == mAction) {
                    printDemo();

                } else {
                    // nothing
                }

                mAction = ACTION_NONE;
            }
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Regist ClickListener
     */
    private void registClickListener() {
        int[] clickTarget = {R.id.QP_btn_NfcSetting, R.id.QP_btn_Print};

        for (int target : clickTarget) {
            Button button = (Button) findViewById(target);

            button.setOnClickListener(this);
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * NFC setting
     */
    private void runNfcSetting() {
        if (mNfcCtr.isSuport()) {
            mNfcCtr.showNfcSetting(MainActivity.this);
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * print sample
     */
    private void runPrintSample() {
        printDemo();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Initalize NFC
     */
    private void initNfc() {
        mNfcCtr = new NFCController(this);

        if (!mNfcCtr.isSuport()) {
            // not support NFC
            mNfcSettingsText.setText(getString(R.string.QP_msg_NfcNoSupport));

            mNfcSettingsButton.setVisibility(View.INVISIBLE);
            mNfcSettingsLayout.setVisibility(View.VISIBLE);
            mNfcImage.setVisibility(View.INVISIBLE);

            return;
        }

        Intent intent = new Intent(this, this.getClass())
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        IntentFilter actionNdef = IntentFilter.create(
                NfcAdapter.ACTION_NDEF_DISCOVERED, "*/*");
        IntentFilter[] filters = new IntentFilter[]{actionNdef};

        mNfcCtr.setPendingIntent(pendingIntent);
        mNfcCtr.setNfcfilter(filters);
        mNfcCtr.setTechLists(null); // does not limit the type of tag
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Initialize Camera
     */
    private void initCamera() {
        mCameraCtr = new CameraController(this);

        if (!mCameraCtr.isSuport()) {
            // this device is not support camera
            return;
        }

        mCameraCtr.setCameraPreviewCallback(this);

        // analyze barcdode
        mBarcodeManager = new BarcodeManager();

        BarcodeManager.BARCODE_TYPE hints[] = {BarcodeManager.BARCODE_TYPE.QR_CODE}; // only qr code

        mBarcodeManager.setDecodHints(hints);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Start camera preview
     */
    public void createCameraPreview() {
        // If you do not a layout after the final, you can not get the size.
        // ex.match_parent
        FrameLayout frame = (FrameLayout) findViewById(R.id.QP_Layout_CameraPreview);

        mCameraCtr.startPreview(frame);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Stop camera preview
     */
    public void releaseCameraPreview() {
        mCameraCtr.stopPreview();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * CameraPreviewCallback
     *
     * @param data        picture of camera
     * @param previewSize preview size
     * @param frameWidth  frame width
     * @param frameHeigth frame height
     * @param offsetX     ofset of frame X
     * @param offsetY     ofset of frame Y
     * @return boolean result
     */
    public boolean cameraPreviewCallback(byte[] data, Point previewSize,
                                         int frameWidth, int frameHeigth, int offsetX, int offsetY) {
        boolean result = false;

        if (null == data) {
            return false;
        }

        // decode
        result = mBarcodeManager.decode(data, previewSize, frameWidth,
                frameHeigth, offsetX, offsetY);
        if (!result) {
            return false;
        }

        // parse QR code
        mEasySelectInfo = mEasySelect
                .parseQR(mBarcodeManager.getStringResult());

        if (null != mEasySelectInfo) {
            mCameraCtr.setFrameColor(Color.GREEN);

            callConnectPrinter();

            // wait preview
            result = true;

        } else {
            // not support QR Code
            result = false;
        }

        return result;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Stop scan NFC / camera
     *
     * @param wait true:stop false:restart
     */
    private void waitScan(boolean wait) {
        // stop the scan of the camera / NFC.
        // NFC
        mNfcCtr.waitScanNfc(wait);

        // camera
        mCameraCtr.waitScanPreview(wait);
        if (!wait) {
            mCameraCtr.setFrameColor(Color.RED);
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Call connect printer method
     */
    private void callConnectPrinter() {
        mPrintButton.setEnabled(false);

        if (!checkInterface(mEasySelectInfo.deviceType)) {
            mAction = ACTION_CONNECT;
            return;
        }

        if (mConnecting) {
            return;
        }

        //Show progressDialog
        mProgressDialog = new CustomProgressDialog(MainActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        mConnecting = true;

        // select printer info
        setSelectPrinterInfo(mEasySelectInfo.printerName,
                mEasySelectInfo.deviceType, mEasySelectInfo.macAddress);

        // Connect printer
        connectSelectPrinter();

        mConnectedPrinter = true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * call connect printer method( Bluetooth and TCP )
     */
    private void callConnectPrinterEx() {
        if (!checkInterface()) {
            waitScan(false);
            return;
        }

        mPrintButton.setEnabled(false);

        if (mConnecting) {
            return;
        }

        //Show progressDialog
        mProgressDialog = new CustomProgressDialog(MainActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        mConnecting = true;

        connectSelectPrinterEx();

    }

    // --------------------------------------------------------------------------------------------

    /**
     * print demo
     */
    private void printDemo() {
        if (!checkInterface(mEasySelectInfo.deviceType)) {
            mAction = ACTION_PRINT;
            return;
        }

        mConnecting = true;

        //Show progressDialog
        mProgressDialog = new CustomProgressDialog(MainActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        updateButtonState(false);

        if (!runPrintSequence()) {
            // close progress dialog
            if (null != mProgressDialog) {
                mProgressDialog.dismiss();
            }
            updateButtonState(true);
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Set select printer information
     *
     * @param printerName   Printer name
     * @param interfaceType Interface type
     * @param macAddress    MAC address
     */
    private void setSelectPrinterInfo(String printerName, int interfaceType,
                                      String macAddress) {
        ListView list = (ListView) findViewById(R.id.QP_List_TargetInfo);
        SimpleAdapter adapter = null;
        ArrayList<HashMap<String, String>> printerInfo = new ArrayList<HashMap<String, String>>();
        String label = null;
        String value = null;
        HashMap<String, String> item = new HashMap<String, String>();

        adapter = new SimpleAdapter(this, printerInfo,
                R.layout.listitem_horizontal_layout, new String[]{LI_LABEL,
                LI_VALUE}, new int[]{R.id.listitem_hl_label,
                R.id.listitem_hl_value});

        // printer name
        label = getString(R.string.QP_Item_PrinterInfo_PrinterName);
        item.put(LI_LABEL, label);
        item.put(LI_VALUE, printerName);
        printerInfo.add(item);

        // interface
        item = new HashMap<String, String>();

        label = getString(R.string.QP_Item_PrinterInfo_Interface);

        switch (interfaceType) {
            case EasySelectDeviceType.TCP:
                value = getString(R.string.QP_Item_Interface_Network);
                break;
            case EasySelectDeviceType.BLUETOOTH:
                value = getString(R.string.QP_Item_Interface_Bluetooth);
                break;
            default:
                value = "";
                break;
        }

        item.put(LI_LABEL, label);
        item.put(LI_VALUE, value);

        printerInfo.add(item);

        // mac address
        item = new HashMap<String, String>();

        label = getString(R.string.QP_Item_PrinterInfo_MacAddress);
        item.put(LI_LABEL, label);
        item.put(LI_VALUE, macAddress);
        printerInfo.add(item);

        list.setAdapter(adapter);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Connect to selected printer
     */
    private void connectSelectPrinter() {
        mPrintButton.setEnabled(false);
        mConnectingText.setVisibility(View.VISIBLE);

        // get PrinterSeries
        mPrinterSeries = Utility.convertPrinterNameToPrinterSeries(mEasySelectInfo.printerName);

        // Create target string
        mTargetText = Utility.convertEasySelectInfoToTargetString(mEasySelectInfo.deviceType, mEasySelectInfo.macAddress);

        if ((null == mOpenThread) || !mOpenThread.isAlive()) {
            mOpenThread = new Thread(new Runnable() {
                // --------------------------------------------------------------------------------
                @Override
                public void run() {
                    int retval = RESULT_UNKNOWN;

                    if (!initializeObject()) {
                        retval = RESULT_ERROR_CONNECT;
                    }
                    if (!(RESULT_ERROR_CONNECT == retval) && !connectPrinter()) {
                        retval = RESULT_ERROR_CONNECT;
                    }
                    if (!(RESULT_ERROR_CONNECT == retval)) {
                        mConnectedPrinter = true;
                        retval = RESULT_SUCCESS;
                        disconnectPrinterNonTransaction();
                    }

                    final int finalVal = retval;

                    runOnUiThread(new Runnable() {
                        // --------------------------------------------------------------------
                        @Override
                        public void run() {
                            if (RESULT_SUCCESS == finalVal) {
                                mPrintButton.setEnabled(true);
                                mConnecting = false;
                            } else {
                                showErrorMessage(getString(R.string.QP_msg_ErrorPrinterConnect));
                                mConnecting = false;
                            }

                            mConnectingText.setVisibility(View.INVISIBLE);
                            // close progress dialog
                            if (null != mProgressDialog) {
                                mProgressDialog.dismiss();
                            }
                        }
                    });

                    // restart NFC / Camera preview
                    waitScan(false);
                }
            });

            mOpenThread.start();

        } else {
            if (null != mProgressDialog) {
                mProgressDialog.dismiss();
            }
            showErrorMessage(getString(R.string.QP_msg_ErrorPrinterConnect));
            mConnecting = false;
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Connect to selected printer ( Bluetooth and TCP )
     */
    private void connectSelectPrinterEx() {
        mPrintButton.setEnabled(false);
        mConnectingText.setVisibility(View.VISIBLE);

        // get PrinterSeries
        mPrinterSeries = Utility.convertPrinterNameToPrinterSeries(mEasySelectInfo.printerName);

        if ((null == mOpenThread) || !mOpenThread.isAlive()) {
            mOpenThread = new Thread(new Runnable() {
                // --------------------------------------------------------------------------------
                @Override
                public void run() {
                    int retval = RESULT_ERROR_CONNECT;

                    if (!initializeObject()) {
                        mConnectedPrinter = false;
                    } else {
                        for (EasySelectInfo info : mEasySelectInfoArray) {

                            if (EasySelectDeviceType.BLUETOOTH == info.deviceType) {
                                // Bluetooth
                                if (!mBluetoothController.isSuport()
                                        || !mBluetoothController.isEnabled()) {
                                    continue;
                                }
                            } else if (EasySelectDeviceType.TCP == info.deviceType) {
                                // TCP
                                if (!mWifiController.isSuport()
                                        || !mWifiController.isEnabled()) {
                                    continue;
                                }
                            }

                            // Please specify the printer name of the use printers.
                            // When a communication error occurred, macAddress is empty string.
                            /*
                             * if( (null == info.printerName) || info.printerName.equals("") ) {
                             * info.printerName = "TM-T88V";
                             * }
                             *
                             * if( (null == info.macAddress) || info.macAddress.equals("") ) {
                             * continue;
                             * }
                             */

                            mEasySelectInfo = info;

                            mHandler.post(new Runnable() {
                                public void run() {
                                    setSelectPrinterInfo(
                                            mEasySelectInfo.printerName,
                                            mEasySelectInfo.deviceType,
                                            mEasySelectInfo.macAddress);
                                }
                            });

                            // Create target string
                            mTargetText = Utility.convertEasySelectInfoToTargetString(mEasySelectInfo.deviceType, mEasySelectInfo.macAddress);

                            if (!connectPrinter()) {
                                // next
                                mConnectedPrinter = false;
                            } else {
                                mConnectedPrinter = true;
                                retval = RESULT_SUCCESS;
                                disconnectPrinterNonTransaction();
                                break;
                            }
                        }
                    }

                    if (!mConnectedPrinter) {
                        retval = RESULT_ERROR_CONNECT;
                    }

                    final int finalval = retval;

                    runOnUiThread(new Runnable() {
                        // --------------------------------------------------------------------
                        @Override
                        public void run() {
                            if (RESULT_SUCCESS == finalval) {
                                mPrintButton.setEnabled(true);
                                mConnecting = false;
                            } else {
                                showErrorMessage(getString(R.string.QP_msg_ErrorPrinterConnect));
                                mConnecting = false;
                            }
                            // close progress dialog
                            if (null != mProgressDialog) {
                                mProgressDialog.dismiss();
                            }

                            mConnectingText.setVisibility(View.INVISIBLE);

                        }
                    });

                    // restart NFC / Camera preview
                    waitScan(false);
                }
            });

            mOpenThread.start();

        } else {
            if (null != mProgressDialog) {
                mProgressDialog.dismiss();
            }
            showErrorMessage(getString(R.string.QP_msg_ErrorPrinterConnect));
            mConnecting = false;
        }
    }

    // --------------------------------------------------------------------------------------------

    /**
     * check interface
     *
     * @param interfaceType Interface type
     * @return boolean Result
     */
    private boolean checkInterface(int interfaceType) {
        boolean result = true;

        if (EasySelectDeviceType.TCP == interfaceType) {
            if (!mWifiController.isSuport()) {
                result = false;

            } else {
                // Wi-Fi
                if (!mWifiController.isEnabled()) {
                    msgWiFiEnabled();
                    result = false;
                }
            }
        } else if (EasySelectDeviceType.BLUETOOTH == interfaceType) {
            if (!mBluetoothController.isSuport()) {
                result = false;

            } else {
                // Bluetooth
                if (!mBluetoothController.isEnabled()) {
                    msgBluetoothEnabled();
                    result = false;
                }
            }
        }

        return result;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * check interface ( Bluetooth and Wi-Fi )
     */
    private boolean checkInterface() {
        // Bluetooth
        if (mBluetoothController.isEnabled()) {
            return true;
        }

        // Wi-Fi
        if (mWifiController.isEnabled()) {
            return true;
        }

        // Disable
        showErrorMessage(getString(R.string.CP_Msg_Interface_EnableRetry));

        return false;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * message box enable Wi-Fi
     */
    private void msgWiFiEnabled() {
        MessageBox msgBox = new MessageBox(this) {
            // ------------------------------------------------------------------------------------
            @Override
            protected void onButtonClick(DialogInterface dialog, int which) {
                switch (which) {
                    case (DialogInterface.BUTTON_POSITIVE):
                        // enable Wi-Fi
                        IntentFilter broadcastFilter = new IntentFilter();
                        for (IntentListWiFi i : IntentListWiFi.values()) {
                            broadcastFilter.addAction(i.getAction());
                        }

                        mBroadcastManager.registFilter(MainActivity.this,
                                broadcastFilter);
                        mBroadcastManager.registCallback(MainActivity.this);

                        mWifiController.setEnabled(true);
                        break;

                    default:
                        waitScan(false);
                        break;
                }
            }
        };

        msgBox.intMessageBox(null, getString(R.string.CP_Msg_TurnOnWiFi),
                getString(R.string.dialog_btn_yes),
                getString(R.string.dialog_btn_no), null);
        msgBox.show();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * message box enable Bluetooth
     */
    private void msgBluetoothEnabled() {
        MessageBox msgBox = new MessageBox(this) {
            // ------------------------------------------------------------------------------------
            @Override
            protected void onButtonClick(DialogInterface dialog, int which) {
                switch (which) {
                    case (DialogInterface.BUTTON_POSITIVE):
                        // enable Bluetooth
                        IntentFilter broadcastFilter = new IntentFilter();
                        for (IntentListBluetooth i : IntentListBluetooth.values()) {
                            broadcastFilter.addAction(i.getAction());
                        }

                        mBroadcastManager.registFilter(MainActivity.this,
                                broadcastFilter);
                        mBroadcastManager.registCallback(MainActivity.this);

                        mBluetoothController.setEnabled(null, true);
                        break;

                    default:
                        waitScan(false);
                        break;
                }
            }
        };

        msgBox.intMessageBox(null, getString(R.string.CP_Msg_TurnOnBluetooth),
                getString(R.string.dialog_btn_yes),
                getString(R.string.dialog_btn_no), null);
        msgBox.show();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * show error message
     *
     * @param message error message
     */
    private void showErrorMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageBox msgBox = new MessageBox(MainActivity.this) {
                    // ----------------------------------------------------------------------------
                    @Override
                    protected void onButtonClick(DialogInterface dialog,
                                                 int which) {
                        // nothing
                    }
                };

                msgBox.intMessageBox(getString(R.string.dialog_title_error),
                        message, getString(R.string.dialog_btn_ok), null, null);
                msgBox.show();
            }
        });
    }

    // --------------------------------------------------------------------------------------------

    /**
     * update ButtonState
     *
     * @param state buttonState
     */
    private void updateButtonState(boolean state) {
        Button btnPrint = (Button) findViewById(R.id.QP_btn_Print);
        btnPrint.setEnabled(state);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Run PrintSequence
     */
    private boolean runPrintSequence() {

        final boolean[] finalVal = {true};

        //Create and start print thread
        if ((null == mPrintThread) || !mPrintThread.isAlive()) {
            mPrintThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean result = true;
                    if (!initializeObject()) {
                        result = false;
                    }
                    if (result && !createData()) {
                        finalizeObject();
                        result = false;
                    }
                    if (result && !printData()) {
                        finalizeObject();
                        result = false;
                    }
                    final boolean retVal = result;
                    finalVal[0] = result;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!retVal) {
                                // close progress dialog
                                if (null != mProgressDialog) {
                                    mProgressDialog.dismiss();
                                }
                                showErrorMessage(getString(R.string.QP_msg_PrintError));
                            }
                            mConnecting = false;
                            mPrintButton.setEnabled(true);
                        }
                    });
                }
            });
        }
        mPrintThread.start();

        return finalVal[0];
    }

    /**
     * Initialize Printer object
     */
    private boolean initializeObject() {
        try {
            mPrinter = new Printer(mPrinterSeries, Printer.MODEL_ANK, getApplicationContext());
        } catch (Exception e) {
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }

    /**
     * Finalize Printer object
     */
    private void finalizeObject() {
        if (null == mPrinter) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    /**
     * Create print sample data
     */
    private boolean createData() {

        if (null == mPrinter) {
            return false;
        }
        if (!makeHeaderText()) {
            return false;
        }
        if (!makeBodyText()) {
            return false;
        }

        return true;
    }

    /**
     * Make print header text
     *
     * @return boolean
     */
    private boolean makeHeaderText() {
        if (null == mPrinter) {
            return false;
        }

        StringBuilder textData = new StringBuilder();

        try {
            textData.append(PRINT_LINE);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(1);

            textData.append(PRINT_TITLE);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(1);

            textData.append(PRINT_LINE);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(2);

        } catch (Epos2Exception e) {
            return false;
        }

        return true;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Make body text
     *
     * @return boolean
     */
    private boolean makeBodyText() {
        if (null == mPrinter) {
            return false;
        }

        StringBuilder textData = new StringBuilder();


        try {
            textData.append(mEasySelectInfo.printerName);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(1);

            // port type
            switch (mEasySelectInfo.deviceType) {
                case EasySelectDeviceType.TCP:
                    textData.append(PRINT_WIFI_ADDRESS);
                    break;
                case EasySelectDeviceType.BLUETOOTH:
                    textData.append(PRINT_BLUETOOTH_ADDRESS);
                    break;
                default:
                    break;
            }

            textData.append(mEasySelectInfo.macAddress);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(5);

            textData.append(mEasySelectInfo.macAddress);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addText(PRINT_MESSAGE);
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(2);

            mPrinter.addCut(Printer.CUT_FEED);

        } catch (Epos2Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Print data
     */
    private boolean printData() {

        if (null == mPrinter) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        if (!isPrintable(status)) {
            try {
                mPrinter.disconnect();
            } catch (Epos2Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            updateButtonState(true);
            disconnectPrinter();
            return false;
        }

        return true;
    }

    /**
     * Connect Printer for print sample
     */
    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (null == mPrinter) {
            return false;
        }
        if (null == mTargetText) {
            return false;
        }

        try {
            mPrinter.connect(mTargetText, Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        } catch (Exception e) {
            //Do nothig
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

    /**
     * End transaction and disconnect printer
     */
    private void disconnectPrinter() {

        if (null == mPrinter) {
            return;
        }

        try {
            mPrinter.endTransaction();
        } catch (final Epos2Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    showErrorMessage(getString(R.string.QP_msg_ErrorPrinterConnect));
                }
            });
        }

        try {
            mPrinter.disconnect();
        } catch (final Epos2Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    showErrorMessage(getString(R.string.QP_msg_ErrorPrinterConnect));
                }
            });
        }

        finalizeObject();
    }

    /**
     * Disconnect Printer(Transaction was not started)
     */
    private void disconnectPrinterNonTransaction() {

        if (null == mPrinter) {
            return;
        }

        try {
            mPrinter.disconnect();
        } catch (final Epos2Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    showErrorMessage(getString(R.string.QP_msg_ErrorPrinterConnect));
                }
            });
        }

        finalizeObject();
    }

    /**
     * Check printer status
     *
     * @param status Printer status
     */
    private boolean isPrintable(PrinterStatusInfo status) {
        if (null == status) {
            return false;
        }

        if (Printer.FALSE == status.getConnection()) {
            return false;
        } else if (Printer.FALSE == status.getOnline()) {
            return false;
        } else {
            //print available
        }

        return true;
    }

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
                if (!(Epos2CallbackCode.CODE_SUCCESS == code)) {
                    showErrorMessage(getString(R.string.QP_msg_PrintError));
                }
                // close progress dialog
                if (null != mProgressDialog) {
                    mProgressDialog.dismiss();
                }
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

        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> requestPermissions = new ArrayList<>();

        if (permissionCamera == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.CAMERA);
        }
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
            if (permissions[i].equals(Manifest.permission.CAMERA)
                    && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(permissions[i]);
            }
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
