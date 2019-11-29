package com.epson.epos2_hybridprinter;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.HybridPrinter;
import com.epson.epos2.Epos2CallbackCode;

public class SlipControl extends DeviceControl {
    public SlipControl(Context context, ShowMsg showMessage, HybridPrinter hybridPrinter, String connectTarget) {
        super(context, showMessage, hybridPrinter, connectTarget);
    }

    @Override
    protected boolean addData()
    {

        boolean result = true;
        Date dateNow = new Date();

        if(!super.addData()) {
            result = false;
        }

        if(result) {
            try {
                mHybridPrinter.addPageBegin();
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addPageBegin", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addTextFont(HybridPrinter.FONT_B);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addTextFont", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addPageDirection(HybridPrinter.DIRECTION_BOTTOM_TO_TOP);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addPageDirection", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addPageArea(188,0,340,687);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addPageArea", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addPagePosition(300, 55);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addPagePosition", mContext);
                result = false;
            }
        }

        if(result) {
            SimpleDateFormat formatMdy = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);
            String mdyString =formatMdy.format(dateNow);
            try {
                mHybridPrinter.addText(mdyString);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addFeedLine(1);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addFeedLine", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addPagePosition(10, 125);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addPagePosition", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("CASH                                         150.00\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addPagePosition(10, 170);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addPagePosition", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("One hundred fifty and zero cents\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addPageEnd();
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addPageEnd", mContext);
                result = false;
            }
        }

        if(!result) {
            clearData();
        }

        return result;
    }

    @Override
    protected void clearData() {
        if(mHybridPrinter == null) {
            return ;
        }

        mHybridPrinter.clearCommandBuffer();
    }

    @Override
    protected boolean sendData()
    {

        boolean result = true;

        if(!super.sendData()) {
            result = false;
        }

        if(result) {
            waitOnHybdReceiveStart();

            try{
                mHybridPrinter.sendData(HybridPrinter.PARAM_DEFAULT);
            }
            catch(Epos2Exception e) {
                signalOnHybdReceive();
                mShowMessage.showExceptionOnMainThread(e, "sendData", mContext);
                result = false;
            }
        }

        if(result) {
            if (waitOnHybdReceive() != Epos2CallbackCode.CODE_SUCCESS) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean insertPaper() {

        boolean result = true;

        if(!super.insertPaper()) {
            result = false;
        }
        if(result) {
            if(mFirstControlType != DEVICE_CONTROL_NONE)
            {
                waitOnHybdReceiveStart();

                try{
                    mHybridPrinter.waitInsertion(HybridPrinter.PARAM_DEFAULT);
                }
                catch(Epos2Exception e) {
                    signalOnHybdReceive();
                    mShowMessage.showExceptionOnMainThread(e, "waitInsertion", mContext);
                    result = false;
                }
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
    protected boolean selectPaperType() {

        boolean result = true;

        if(!super.selectPaperType()) {
            result = false;
        }

        if(result) {
            try {
                mHybridPrinter.selectPaperType(HybridPrinter.PAPER_TYPE_SLIP);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "selectPaperType", mContext);
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

            try {
                mHybridPrinter.ejectPaper();
            }
            catch(Epos2Exception e) {
                signalOnHybdReceive();
                mShowMessage.showExceptionOnMainThread(e, "ejectPaper", mContext);
                result = false;
            }
        }

        if(result) {
            if (waitOnHybdReceive() != Epos2CallbackCode.CODE_SUCCESS) {
                 return false;
            }
        }

        if(result) {
            waitOnStatuschangeStart();
            waitOnStatuschange();
        }

        return result;
    }
}
