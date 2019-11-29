package com.epson.epos2_hybridprinter;

import android.content.Context;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.HybridPrinter;
import com.epson.epos2.printer.HybridPrinterStatusInfo;
import com.epson.epos2.Epos2CallbackCode;

public class MicrControl extends DeviceControl {
    protected String mMicrData = "";

    public MicrControl(Context context, ShowMsg showMessage, HybridPrinter hybridPrinter, String connectTarget) {
        super(context, showMessage, hybridPrinter, connectTarget);
    }

    public String getMicrData() {
        return mMicrData;
    }

    @Override
    protected boolean insertPaper() {

        boolean result = true;

        if(!super.insertPaper()) {
            result = false;
        }


        if(result) {
            mMicrData = "";

            waitOnHybdReceiveStart();

            try{
                mHybridPrinter.readMicrData(HybridPrinter.PARAM_DEFAULT, HybridPrinter.PARAM_DEFAULT);
            }
            catch(Epos2Exception e){
                mShowMessage.showExceptionOnMainThread(e, "readMicrData", mContext);
                result = false;
            }
        }

        if(result) {
            if (waitOnHybdReceive() != Epos2CallbackCode.CODE_SUCCESS) {
                result = false;
            }
        }

        return result;

    }

    @Override
    protected boolean ejectPaper() {

        boolean result = true;

        if(!super.ejectPaper()) {
            result = false;
        }

        if(result) {
            waitOnHybdReceiveStart();

            try{
                mHybridPrinter.ejectPaper();
            }
            catch(Epos2Exception e){
                signalOnHybdReceive();
                mShowMessage.showExceptionOnMainThread(e, "ejectPaper", mContext);
                result = false;
            }
        }

        if(result) {
            if (waitOnHybdReceive() != Epos2CallbackCode.CODE_SUCCESS) {
                result = false;
            }
        }

        if(result) {
            waitOnStatuschangeStart();
            waitOnStatuschange();
        }

        return result;
    }

    @Override
    public void onHybdReceive (final HybridPrinter hybridPrinterObj, final int method, final int code,
                               final String micrData, final HybridPrinterStatusInfo status) {
        if(method == HybridPrinter.METHOD_READMICRDATA){
            mMicrData = micrData;
        }
        super.onHybdReceive(hybridPrinterObj, method, code, micrData, status);
    }
}
