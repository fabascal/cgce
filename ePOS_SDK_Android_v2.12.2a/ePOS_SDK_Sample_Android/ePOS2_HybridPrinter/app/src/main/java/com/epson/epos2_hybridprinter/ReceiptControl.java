package com.epson.epos2_hybridprinter;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.HybridPrinter;
import com.epson.epos2.Epos2CallbackCode;

public class ReceiptControl extends DeviceControl {
    public ReceiptControl(Context context, ShowMsg showMessage, HybridPrinter hybridPrinter, String connectTarget) {
        super(context, showMessage, hybridPrinter, connectTarget);
    }

    @Override
    protected boolean addData() {

        boolean result = true;
        Date dateNow = new Date();

        if(!super.addData()) {
            result = false;
        }


        SimpleDateFormat formatDmyHms = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        String dmyHmsString = formatDmyHms.format(dateNow);

        if(result) {
            try {
                mHybridPrinter.addFeedLine(3);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addFeedLine", mContext);
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
                mHybridPrinter.addTextAlign(HybridPrinter.ALIGN_CENTER);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addTextAlign", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addLogo(48, 48);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addLogo", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addTextStyle(HybridPrinter.FALSE, HybridPrinter.FALSE, HybridPrinter.TRUE, HybridPrinter.COLOR_1);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addTextStyle", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("( 555 ) 555 - 5555\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("SHIOJIRI BEACH, 12\n\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("TAX INCLUDE\n\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addTextAlign(HybridPrinter.ALIGN_LEFT);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addTextAlign", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addTextStyle(HybridPrinter.FALSE, HybridPrinter.FALSE, HybridPrinter.FALSE, HybridPrinter.COLOR_1);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addTextStyle", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("#ORD 24 -REG 02- ");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText(dmyHmsString);
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
                mHybridPrinter.addText("QTY ITEM                         TOTAL\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("  1 LRG ORANGE                    2.25\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("  4 YASHI NUGGETS                 2.40\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("  1 PREMIUM YASHI SALAD           3.50\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("  3 YASHI BURGER                 10.50\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("  1 LRG YASHI FRENCH FRIES        1.75\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("  1 SMILE                         0.00\n\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("SUB TOTAL                        20.40\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("TAKE OUT TAX                      3.06\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("                                  ----\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("                                 23.46\n\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("CASH TENDERED                    24.00\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("CHANGE                             .54\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("        THANK YOU PLEASE CALL AGAIN   \n\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("------------------------------------------\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addFeedLine(2);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addFeedLine", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addCut(HybridPrinter.CUT_NO_FEED);
            }
            catch(Epos2Exception e) {

                mShowMessage.showExceptionOnMainThread(e, "addCut", mContext);
                result = false;
            }
        }

        if(!result) {
            clearData();
        }

        return true;
    }

    @Override
    protected void clearData() {
        if(mHybridPrinter == null) {
            return ;
        }

        mHybridPrinter.clearCommandBuffer();
    }

    @Override
    protected boolean sendData() {

        boolean result = true;

        if(!super.sendData()) {
            result = false;
        }

        if(result) {
            waitOnHybdReceiveStart();

            try{
                mHybridPrinter.sendData(HybridPrinter.PARAM_DEFAULT);
            }
            catch(Epos2Exception e){
                signalOnHybdReceive();
                mShowMessage.showExceptionOnMainThread(e, "sendData", mContext);
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
    protected boolean selectPaperType() {

        boolean result = true;

        if(!super.selectPaperType()) {
            result = false;
        }

        if(result) {
            try{
                mHybridPrinter.selectPaperType(HybridPrinter.PAPER_TYPE_RECEIPT);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "selectPaperType", mContext);
                result = false;
            }
        }

        return result;
    }
}
