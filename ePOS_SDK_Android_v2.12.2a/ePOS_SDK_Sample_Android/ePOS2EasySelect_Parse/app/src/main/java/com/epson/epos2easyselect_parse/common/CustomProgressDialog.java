package com.epson.epos2easyselect_parse.common;

import android.app.Dialog;
import android.content.Context;

import com.epson.epos2easyselect_parse.R;

// ------------------------------------------------------------------------------------------------
public class CustomProgressDialog extends Dialog {
    // --------------------------------------------------------------------------------------------

    /**
     * custom progress dialog
     *
     * @param context
     */
    public CustomProgressDialog(
            Context context) {
        super(context, R.style.CustomProgressDialog);
        setContentView(R.layout.custom_progress_dialog);
    }
}
