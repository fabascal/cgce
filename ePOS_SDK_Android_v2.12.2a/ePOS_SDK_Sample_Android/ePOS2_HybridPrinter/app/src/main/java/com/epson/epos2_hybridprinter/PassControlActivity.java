package com.epson.epos2_hybridprinter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class PassControlActivity extends Activity implements View.OnClickListener {
    private Context mContext = null;
    private Button mBtnStart = null;
    private CheckBox mCheckMicrRead = null;
    private CheckBox mChkEndorse = null;
    private CheckBox mChkSlip = null;
    private CheckBox mChkReceipt = null;
    private boolean mIsMicr = false;
    private boolean mIsEndorse = false;
    private boolean mIsSlip = false;
    private boolean mIsReceipt = false;
    private MicrControl mMicrControl = null;
    private EndorseControl mEndorseControl = null;
    private SlipControl mSlipControl = null;
    private ReceiptControl mReceiptControl = null;
    private ShowMsg mShowMessage = null;
    private EditText mEdtMicrData = null;
    private EditText mEdtWarnings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcontrol);

        mContext = this;
        mShowMessage = new ShowMsg();

        mBtnStart = (Button)findViewById(R.id.btnStart);
        mBtnStart.setOnClickListener(this);

        mCheckMicrRead = (CheckBox)findViewById(R.id.chkMicrRead);
        mChkEndorse = (CheckBox)findViewById(R.id.chkEndorse);
        mChkSlip = (CheckBox)findViewById(R.id.chkSlip);
        mChkReceipt = (CheckBox)findViewById(R.id.chkReceipt);

        mEdtMicrData = (EditText)findViewById(R.id.edtMicrData);
        mEdtWarnings = (EditText)findViewById(R.id.edtWarnings);

        mCheckMicrRead.setChecked(true);
        mCheckMicrRead.setEnabled(false);
        mChkEndorse.setChecked(true);
        mChkSlip.setChecked(true);
        mChkReceipt.setChecked(true);

        mEdtMicrData.setEnabled(false);
        mEdtWarnings.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                updateButtonState(false);

                mEdtMicrData.setText("");
                mEdtWarnings.setText("");

                mIsMicr = false;
                mIsEndorse = false;
                mIsSlip = false;
                mIsReceipt = false;

                if(mCheckMicrRead.isChecked()) {
                    mIsMicr = true;
                }

                if(mChkEndorse.isChecked()) {
                    mIsEndorse = true;
                }

                if(mChkSlip.isChecked()) {
                    mIsSlip = true;
                }

                if(mChkReceipt.isChecked()) {
                    mIsReceipt = true;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //1pass sequence
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
        mBtnStart.setEnabled(state);
        mChkEndorse.setEnabled(state);
        mChkSlip.setEnabled(state);
        mChkReceipt.setEnabled(state);
    }

    private void runPrintSequence(){
        int nextControl = DeviceControl.DEVICE_CONTROL_NONE;
        boolean result = true;

        mMicrControl = null;
        mEndorseControl = null;
        mSlipControl = null;
        mReceiptControl = null;

        if(mIsReceipt){
            mReceiptControl = new ReceiptControl(mContext, mShowMessage, MainActivity.mHybridPrinter, MainActivity.mConnectTarget);
            mReceiptControl.setNextControlType(nextControl);
            nextControl = DeviceControl.DEVICE_CONTROL_RECEIPT;
        }

        if(mIsSlip){
            mSlipControl = new SlipControl(mContext, mShowMessage, MainActivity.mHybridPrinter, MainActivity.mConnectTarget);
            mSlipControl.setNextControlType(nextControl);
            nextControl = DeviceControl.DEVICE_CONTROL_SLIP;
        }

        if(mIsEndorse){
            mEndorseControl = new EndorseControl(mContext, mShowMessage, MainActivity.mHybridPrinter, MainActivity.mConnectTarget);
            mEndorseControl.setNextControlType(nextControl);
            nextControl = DeviceControl.DEVICE_CONTROL_ENDORSE;
        }

        if(mIsMicr){
            mMicrControl = new MicrControl(mContext, mShowMessage, MainActivity.mHybridPrinter, MainActivity.mConnectTarget);
            mMicrControl.setFirstControlType(DeviceControl.DEVICE_CONTROL_MICR);
            mMicrControl.setNextControlType(nextControl);
        }

        if(mMicrControl != null){
            result = mMicrControl.startSequence();
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    mEdtMicrData.setText(mMicrControl.getMicrData());
                    mEdtWarnings.setText(mMicrControl.getWarningText());
                }
            });

        }

        if((mEndorseControl != null) && result){
            result = mEndorseControl.startSequence();
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    mEdtWarnings.setText(mEndorseControl.getWarningText());
                }
            });
        }

        if((mSlipControl != null) && result){
            result = mSlipControl.startSequence();
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    mEdtWarnings.setText(mSlipControl.getWarningText());
                }
            });
        }

        if((mReceiptControl != null) && result){
            result = mReceiptControl.startSequence();
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    mEdtWarnings.setText(mReceiptControl.getWarningText());
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
