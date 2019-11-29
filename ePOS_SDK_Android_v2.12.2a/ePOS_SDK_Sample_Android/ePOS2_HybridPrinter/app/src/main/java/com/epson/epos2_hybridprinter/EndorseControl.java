package com.epson.epos2_hybridprinter;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.HybridPrinter;

public class EndorseControl extends SlipControl {
    public EndorseControl(Context context, ShowMsg showMessage, HybridPrinter hybridPrinter, String connectTarget) {
        super(context, showMessage, hybridPrinter, connectTarget);
    }

    @Override
    protected boolean addData()
    {

        boolean result = true;
        Date dateNow = new Date();

        if(mHybridPrinter == null) {
            return false;
        }

        try {
            mHybridPrinter.addText("FOR DEPOSIT ONLY FLESH*MART #5521\n");
        }
        catch(Epos2Exception e) {
            mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
            result = false;
        }


        if(result) {
            try {
                mHybridPrinter.addText("TE#01 TR#8009 OP00000001  TIM");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            SimpleDateFormat formatKm = new SimpleDateFormat("kk:mm");
            String kmString = formatKm.format(dateNow);
            try {
                mHybridPrinter.addText(kmString);
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
                mHybridPrinter.addText(" H ID#                    ");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            SimpleDateFormat formatDmy = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
            String dmyString = formatDmy.format(dateNow);
            try {
                mHybridPrinter.addText(dmyString);
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

        if(!result) {
            clearData();
        }

        return result;
    }

    @Override
    protected boolean selectPaperType()
    {

        boolean result = true;

        if(mHybridPrinter == null) {
            return false;
        }

        try {
            mHybridPrinter.selectPaperType(HybridPrinter.PAPER_TYPE_ENDORSE);
        }
        catch(Epos2Exception e) {
            mShowMessage.showExceptionOnMainThread(e, "selectPaperType", mContext);
            result = false;
        }

        return result;
    }
}
