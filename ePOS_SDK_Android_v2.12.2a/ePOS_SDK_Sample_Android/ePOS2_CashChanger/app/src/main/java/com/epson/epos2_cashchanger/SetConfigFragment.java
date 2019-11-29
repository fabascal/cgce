package com.epson.epos2_cashchanger;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.cashchanger.CashChanger;
import com.epson.epos2.cashchanger.ConfigChangeListener;

import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class SetConfigFragment extends CashChangerFragment implements View.OnClickListener, ConfigChangeListener {

    EditText mTextCoins = null;
    EditText mTextBills = null;
    Spinner mSpinner = null;
    TextView mTextCashChanger = null;

    public static SetConfigFragment newInstance() {
        return new SetConfigFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_setconfig, container, false);
        Button btn_setConfig = rootView.findViewById(R.id.button_setCountMode);
        Button btn_setLeftCash = rootView.findViewById(R.id.button_setLeftCash);
        Button btn_clear = rootView.findViewById(R.id.button_clear);
        mTextCoins = rootView.findViewById(R.id.editText_coins);
        mTextBills = rootView.findViewById(R.id.editText_bills);
        mSpinner = rootView.findViewById(R.id.spinner_countMode);
        mTextCashChanger = rootView.findViewById(R.id.textView_setConfigText);

        mSpinner = rootView.findViewById(R.id.spinner_countMode);
        ArrayAdapter<SpnItems> endModeAdapter = new ArrayAdapter<SpnItems>(getActivity(), android.R.layout.simple_spinner_item);
        endModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endModeAdapter.add(new SpnItems(getString(R.string.countMode_manualInput), CashChanger.COUNT_MODE_MANUAL_INPUT));
        endModeAdapter.add(new SpnItems(getString(R.string.countMode_autoCount), CashChanger.COUNT_MODE_AUTO_COUNT));
        mSpinner.setAdapter(endModeAdapter);
        mSpinner.setSelection(0);

        btn_setConfig.setOnClickListener(this);
        btn_setLeftCash.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mCashChanger != null) {
                mCashChanger.setConfigChangeEventListener(this);
            }
            super.setTextView(mTextCashChanger);
        } else {
            if (mCashChanger != null) {
                mCashChanger.setConfigChangeEventListener(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_setCountMode:
                onSetCountMode();
                break;
            case R.id.button_setLeftCash:
                onSetLeftCash();
                break;
            case R.id.button_clear:
                onClearCashChanger();
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onSetCountMode() {
        if (mCashChanger == null) {
            return;
        }
        int countMode = ((SpnItems) mSpinner.getSelectedItem()).getModelConstant();
        try {
            mCashChanger.setConfigCountMode(countMode);
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "setConfigCountMode", mContext);
        }
    }

    private void onSetLeftCash() {
        if (mCashChanger == null) {
            return;
        }
        try {
            mCashChanger.setConfigLeftCash(Integer.parseInt(mTextCoins.getText().toString()), Integer.parseInt(mTextBills.getText().toString()));
        } catch (Epos2Exception | NumberFormatException e) {
            ShowMsg.showException(e, "setConfigLeftCash", mContext);
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

    @Override
    public void onCChangerConfigChange(final CashChanger cChangerObj, final int code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult("onCChangerConfigChange", code, "", getContext());
            }
        });
    }
}
