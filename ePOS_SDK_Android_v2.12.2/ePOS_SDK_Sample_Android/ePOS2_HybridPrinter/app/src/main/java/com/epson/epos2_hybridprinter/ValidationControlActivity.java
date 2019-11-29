package com.epson.epos2_hybridprinter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ValidationControlActivity extends Activity implements View.OnClickListener {
    private Context mContext = null;
    private ShowMsg mShowMessage = null;
    private ValidationControl mValidationControl = null;

    private Button mBtnValidation = null;
    private EditText mEdtWarnings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validationcontrol);

        mContext = this;
        mShowMessage = new ShowMsg();

        mBtnValidation = (Button)findViewById(R.id.btnPrintValidation);
        mEdtWarnings = (EditText)findViewById(R.id.edtWarnings);
        mBtnValidation.setOnClickListener(this);

        mEdtWarnings.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPrintValidation:

                updateButtonState(false);

                mEdtWarnings.setText("");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //validation sequence
                        runPrintSequence();
                    }
                }).start();
                break;

            default:
                // Do nothing
                break;
        }
    }

    private void updateButtonState(boolean state) {
        mBtnValidation.setEnabled(state);
    }

    private void runPrintSequence() {
        boolean result = true;

        mValidationControl = new ValidationControl(mContext, mShowMessage, MainActivity.mHybridPrinter, MainActivity.mConnectTarget);

        if(mValidationControl != null) {
            mValidationControl.setFirstControlType(DeviceControl.DEVICE_CONTROL_VALIDATION);
            mValidationControl.setNextControlType(DeviceControl.DEVICE_CONTROL_NONE);
            result = mValidationControl.startSequence();
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    mEdtWarnings.setText(mValidationControl.getWarningText());
                }
            });

        }

        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                updateButtonState(true);
            }
        });
    }
}
