package com.epson.epos2_hybridprinter;

import android.content.Context;
import android.content.res.Resources;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.HybridPrinter;
import com.epson.epos2.printer.HybridPrinterStatusInfo;
import com.epson.epos2.printer.HybdStatusChangeListener;
import com.epson.epos2.printer.HybdReceiveListener;
import com.epson.epos2.Epos2CallbackCode;

public class DeviceControl implements HybdStatusChangeListener, HybdReceiveListener {
    public static final int DEVICE_CONTROL_NONE = 0;
    public static final int DEVICE_CONTROL_RECEIPT = 1;
    public static final int DEVICE_CONTROL_SLIP = 2;
    public static final int DEVICE_CONTROL_ENDORSE = 3;
    public static final int DEVICE_CONTROL_VALIDATION = 4;
    public static final int DEVICE_CONTROL_MICR = 5;

    protected HybridPrinter  mHybridPrinter = null;
    protected int mFirstControlType = DEVICE_CONTROL_NONE;
    protected int mNextControlType = DEVICE_CONTROL_NONE;
    protected String mConnectTarget = "";
    protected String mWarningText_ = "";
    protected Context mContext = null;
    protected Resources mRes = null;
    protected int mErrorCodeOnReceive = Epos2CallbackCode.CODE_SUCCESS;
    protected ShowMsg mShowMessage = null;

    public DeviceControl(Context context, ShowMsg showMessage, HybridPrinter hybridPrinter, String connectTarget){
        if(context != null){
            mHybridPrinter = hybridPrinter;
            mContext = context;
            mRes = context.getResources();
            mShowMessage = showMessage;
        }

        if(mHybridPrinter != null){
            mConnectTarget = connectTarget;
            mWarningText_ = "";
            mErrorCodeOnReceive = Epos2CallbackCode.CODE_SUCCESS;
        }
        mFirstControlType = DEVICE_CONTROL_NONE;
        mNextControlType = DEVICE_CONTROL_NONE;
    }

    public void setFirstControlType(int controlType) {
        mFirstControlType = controlType;
    }

    public void setNextControlType(int controlType) {
        mNextControlType = controlType;
    }

    public boolean checkErrorStatus() {

        boolean result = true;

        mWarningText_ = "";
        HybridPrinterStatusInfo status = null;

        status = mHybridPrinter.getStatus();
        mWarningText_ = makeWarningsMessage(status);

        if (!isPrintable(status)) {
            mShowMessage.showMsgOnMainThread(makeErrorMessage(status),mContext);
            result = false;
        }

        return result;
    }

    public String getWarningText() {
        return mWarningText_;
    }

    public boolean startSequence()
    {
        boolean result = true;
        mWarningText_ = "";

        if (mHybridPrinter == null) {
            return false;
        }

        mHybridPrinter.setStatusChangeEventListener(this);
        mHybridPrinter.setReceiveEventListener(this);

        if(mFirstControlType != DEVICE_CONTROL_NONE) {
            if (!connectHybridPrinter()) {
                endSequence();
                return false;
            }
        }

        if(result) {
            if (!selectPaperType()) {
                result = false;
            }
        }

        if(result) {
            if (!insertPaper()) {
                result = false;
            }
        }

        if(result) {
            if (!addData()) {
                result = false;
            }
        }

        if(result) {
            if (!sendData()) {
                result = false;
            }
            clearData();
        }

        if(result) {
            if(mNextControlType == DEVICE_CONTROL_NONE) {
                if (!ejectPaper()) {
                    result = false;
                }
                disconnectHybridPrinter();
            }
            else if(mNextControlType == DEVICE_CONTROL_RECEIPT) {
                if (!ejectPaper()) {
                    result = false;
                }
            }
            else{
            }
        }

        if(!result) {
            disconnectHybridPrinter();
        }

        endSequence();

        return result;
    }

    protected void endSequence(){
        mHybridPrinter.setStatusChangeEventListener(null);
        mHybridPrinter.setReceiveEventListener(null);
    }

    protected boolean addData() {

        boolean result = true;

        if(mHybridPrinter == null){
            result = false;
        }

        return result;
    }

    protected void clearData() {
    }

    protected boolean connectHybridPrinter() {

        boolean result = true;

        if (mHybridPrinter == null) {
            return false;
        }


        try{
            mHybridPrinter.connect(mConnectTarget, HybridPrinter.PARAM_DEFAULT);
        }
        catch(Epos2Exception e){
            mShowMessage.showExceptionOnMainThread(e, "connect", mContext);
            result = false;
        }

        if(result){
            try{
                mHybridPrinter.beginTransaction();
            }
            catch(Epos2Exception e){
                mShowMessage.showExceptionOnMainThread(e, "beginTransaction", mContext);
                result = false;
            }
        }

        if(result){
            try{
                mHybridPrinter.startMonitor();
            }
            catch(Epos2Exception e){
                mShowMessage.showExceptionOnMainThread(e, "startMonitor", mContext);
                result = false;
            }
        }

        if(!result){
            disconnectHybridPrinter();
        }

        return result;
    }

    protected void disconnectHybridPrinter() {

        if (mHybridPrinter == null) {
            return;
        }

        try{
            mHybridPrinter.endTransaction();
        }
        catch(Epos2Exception e){
        }

        try{
            mHybridPrinter.stopMonitor();
        }
        catch(Epos2Exception e){
        }

        try{
            mHybridPrinter.disconnect();
        }
        catch(Epos2Exception e){
        }
    }

    protected boolean isPrintable(HybridPrinterStatusInfo status) {

        boolean result = true;

        if (status == null) {
            return false;
        }

        if (status.getConnection() == HybridPrinter.FALSE) {
            result = false;
        }
        else if (status.getOnline() == HybridPrinter.FALSE) {
            result = false;
        }
        else {
            ;//print available
        }

        return result;
    }

    protected boolean selectPaperType() {

        boolean result = true;

        if (mHybridPrinter == null) {
            result = false;
        }

        return result;
    }

    protected boolean sendData() {

        boolean result = true;

        if (mHybridPrinter == null) {
            result = false;
        }

        return result;
    }

    protected boolean insertPaper() {

        boolean result = true;

        if(mHybridPrinter == null){
            return false;
        }

        if(!checkErrorStatus()){
            result = false;
        }

        return result;
    }

    protected boolean ejectPaper() {

        boolean result = true;

        if(mHybridPrinter == null){
            result = false;
        }

        return result;
    }

    public void onHybdStatusChange(HybridPrinter hybridPrinterObj, int eventType) {
        switch(eventType)
        {
            case HybridPrinter.EVENT_INSERTION_WAIT_MICR:
                mShowMessage.showMsgOnMainThread("Please insert the check", mContext);
                break;

            case HybridPrinter.EVENT_INSERTION_WAIT_SLIP:
            case HybridPrinter.EVENT_INSERTION_WAIT_VALIDATION:
                mShowMessage.showMsgOnMainThread("Please insert the paper", mContext);
                break;

            case HybridPrinter.EVENT_REMOVAL_WAIT_PAPER:
                mShowMessage.showMsgOnMainThread("Please remove the paper", mContext);
                break;

            case HybridPrinter.EVENT_SLIP_PAPER_EMPTY:
            case HybridPrinter.EVENT_REMOVAL_WAIT_NONE:
            case HybridPrinter.EVENT_POWER_OFF:
                signalStatuschange();
                break;

            default:
                break;
        }
    }

    protected void  waitOnHybdReceiveStart() {
        ESemaphore.semaphoreStart(ESemaphore.ESEMAPHORE_ONRECEIVE);
    }

    protected int  waitOnHybdReceive() {
        mErrorCodeOnReceive = Epos2CallbackCode.CODE_SUCCESS;

        ESemaphore.semaphoreWait(ESemaphore.ESEMAPHORE_ONRECEIVE);

        return mErrorCodeOnReceive;
    }

    protected void signalOnHybdReceive() {
        ESemaphore.semaphoreSignal(ESemaphore.ESEMAPHORE_ONRECEIVE);
    }

    protected void  waitOnStatuschangeStart() {
        ESemaphore.semaphoreStart(ESemaphore.ESEMAPHORE_STATUSCHANGE);
    }

    protected void  waitOnStatuschange() {
        ESemaphore.semaphoreWait(ESemaphore.ESEMAPHORE_STATUSCHANGE);
    }

    protected void  signalStatuschange() {
        ESemaphore.semaphoreSignal(ESemaphore.ESEMAPHORE_STATUSCHANGE);
    }

    protected String makeWarningsMessage(HybridPrinterStatusInfo status) {
        String warningsMsg = "";

        if (status == null) {
            return warningsMsg;
        }

        if (status.getPaper() == HybridPrinter.PAPER_NEAR_END) {
            warningsMsg += mRes.getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        return warningsMsg;
    }

    protected String makeErrorMessage(HybridPrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == HybridPrinter.FALSE) {
            msg += mRes.getString(R.string.handlingmsg_err_offline);
        }

        if (status.getConnection() == HybridPrinter.FALSE) {
            msg += mRes.getString(R.string.handlingmsg_err_no_response);
        }

        if (status.getCoverOpen() == HybridPrinter.TRUE) {
            msg += mRes.getString(R.string.handlingmsg_err_cover_open);
        }

        if (status.getPaper() == HybridPrinter.PAPER_EMPTY) {
            msg += mRes.getString(R.string.handlingmsg_err_receipt_end);
        }

        if (status.getPaperFeed() == HybridPrinter.TRUE || status.getPanelSwitch() == HybridPrinter.SWITCH_ON) {
            msg += mRes.getString(R.string.handlingmsg_err_paper_feed);
        }

        if (status.getErrorStatus() == HybridPrinter.MECHANICAL_ERR || status.getErrorStatus() == HybridPrinter.AUTOCUTTER_ERR) {
            msg += mRes.getString(R.string.handlingmsg_err_autocutter);
            msg += mRes.getString(R.string.handlingmsg_err_need_recover);
        }

        if (status.getErrorStatus() == HybridPrinter.UNRECOVER_ERR) {
            msg += mRes.getString(R.string.handlingmsg_err_unrecover);
        }

        if (status.getErrorStatus() == HybridPrinter.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == HybridPrinter.HEAD_OVERHEAT) {
                msg += mRes.getString(R.string.handlingmsg_err_overheat);
                msg += mRes.getString(R.string.handlingmsg_err_head);
            }

            if (status.getAutoRecoverError() == HybridPrinter.MOTOR_OVERHEAT) {
                msg += mRes.getString(R.string.handlingmsg_err_overheat);
                msg += mRes.getString(R.string.handlingmsg_err_motor);
            }

            if (status.getAutoRecoverError() == HybridPrinter.BATTERY_OVERHEAT) {
                msg += mRes.getString(R.string.handlingmsg_err_overheat);
                msg += mRes.getString(R.string.handlingmsg_err_battery);
            }

            if (status.getAutoRecoverError() == HybridPrinter.WRONG_PAPER) {
                msg += mRes.getString(R.string.handlingmsg_err_wrong_paper);
            }
        }

        return msg;
    }

    @Override
    public void onHybdReceive (final HybridPrinter hybridPrinterObj, final int method, final int code,
                        final String micrData, final HybridPrinterStatusInfo status) {
        if(code != Epos2CallbackCode.CODE_SUCCESS) {
            mShowMessage.showResultOnMainThread(code, makeErrorMessage(status), mContext);
        }

        mErrorCodeOnReceive = code;

        signalOnHybdReceive();
    }
}
