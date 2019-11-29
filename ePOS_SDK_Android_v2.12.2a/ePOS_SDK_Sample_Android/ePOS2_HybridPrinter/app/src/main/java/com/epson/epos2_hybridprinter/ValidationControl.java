package com.epson.epos2_hybridprinter;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.HybridPrinter;

public class ValidationControl extends SlipControl {
    public ValidationControl(Context context, ShowMsg showMessage, HybridPrinter hybridPrinter, String connectTarget) {
        super(context, showMessage, hybridPrinter, connectTarget);
    }

    @Override
    protected boolean addData() {

        boolean result = true;
        Date dateNow = new Date();

        if(mHybridPrinter == null) {
            return false;
        }

        try {
            mHybridPrinter.addTextSize(2, 2);
        }
        catch(Epos2Exception e) {
            mShowMessage.showExceptionOnMainThread(e, "addTextSize", mContext);
            return false;
        }

        if(result) {
            try {
                mHybridPrinter.addText("SAVINGS - DEPOSIT           $500.00\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addTextSize(1, 1);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addTextSize", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText("BALANCE: $ 3418.35\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            SimpleDateFormat formatDmyHm = new SimpleDateFormat("dd/MM/yy HH:mm",Locale.ENGLISH);
            String dmyHmString = formatDmyHm.format(dateNow);
            try {
                mHybridPrinter.addText(dmyHmString);
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(result) {
            try {
                mHybridPrinter.addText(" Branch: 32001  Teller: 10022\n");
            }
            catch(Epos2Exception e) {
                mShowMessage.showExceptionOnMainThread(e, "addText", mContext);
                result = false;
            }
        }

        if(!result) {
            clearData();
        }

        return result;
    }

    @Override
    protected boolean selectPaperType() {

        boolean result = true;

        if(mHybridPrinter == null) {
            return false;
        }

        try{
            mHybridPrinter.selectPaperType(HybridPrinter.PAPER_TYPE_VALIDATION);
        }
        catch(Epos2Exception e) {
            mShowMessage.showExceptionOnMainThread(e, "selectPaperType", mContext);
            result = false;
        }

        return result;
    }
}
