package com.epson.epos2_cashchanger;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.cashchanger.CashChanger;

import static com.epson.epos2_cashchanger.MainActivity.finalizeObject;
import static com.epson.epos2_cashchanger.MainActivity.initializeObject;
import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class ConnectFragment extends CashChangerFragment implements View.OnClickListener {

    private Button mBtnConnect = null;
    private EditText mTextTarget = null;
    private TextView mTextCashChanger = null;

    public static ConnectFragment newInstance() {
        return new ConnectFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);
        mBtnConnect = rootView.findViewById(R.id.button_connect);
        Button btn_disconnect = rootView.findViewById(R.id.button_disconnect);
        Button btn_clear = rootView.findViewById(R.id.button_clear);
        mTextTarget = rootView.findViewById(R.id.editText_target);
        mTextCashChanger = rootView.findViewById(R.id.textView_connectText);
        mBtnConnect.setOnClickListener(this);
        btn_disconnect.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        super.setTextView(mTextCashChanger);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            super.setTextView(mTextCashChanger);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_connect:
                connectProcess();
                break;
            case R.id.button_disconnect:
                disconnectProcess();
                break;
            case R.id.button_clear:
                onClearCashChanger();
                break;
            case R.id.editText_target:

                break;
            default:
                // Do nothing
                break;
        }
    }

    private void connectProcess() {
        mBtnConnect.setEnabled(false);

        if (!initializeObject()) {
            mBtnConnect.setEnabled(true);
            return;
        }
        super.setUserVisibleHint(true);
        if (!connectCashChanger()) {
            mBtnConnect.setEnabled(true);
            return;
        }
    }

    private void disconnectProcess() {
        disconnectCashChanger();

        finalizeObject();

        mBtnConnect.setEnabled(true);
    }

    private boolean connectCashChanger() {

        if (mCashChanger == null) {
            return false;
        }

        try {
            mCashChanger.connect(mTextTarget.getText().toString(), CashChanger.PARAM_DEFAULT);
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        return true;
    }

    private void disconnectCashChanger() {
        if (mCashChanger == null) {
            return;
        }

        try {
            mCashChanger.disconnect();
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "disconnect", mContext);
        }
    }

    private void onClearCashChanger() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                mTextCashChanger.setText("");
            }
        });
    }
}
