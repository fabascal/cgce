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
import com.epson.epos2.cashchanger.DispenseListener;
import com.epson.epos2.cashchanger.Epos2CallbackCode;

import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class DispenseChangeFragment extends CashChangerFragment implements View.OnClickListener, DispenseListener {

    private EditText mTextData = null;
    private TextView mTextCashChanger = null;

    public static DispenseChangeFragment newInstance() {
        return new DispenseChangeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_dispensechange, container, false);
        Button btn_dispenseChange = rootView.findViewById(R.id.button_dispenseChange);
        Button btn_clear = rootView.findViewById(R.id.button_clear);
        mTextData = rootView.findViewById(R.id.editText_data);
        mTextCashChanger = rootView.findViewById(R.id.textView_dispenseChangeText);
        btn_dispenseChange.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mCashChanger != null) {
                mCashChanger.setDispenseEventListener(this);
            }
            super.setTextView(mTextCashChanger);
        } else {
            if (mCashChanger != null) {
                mCashChanger.setDispenseEventListener(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_dispenseChange:
                onDispenseChange();
                break;
            case R.id.button_clear:
                onClearCashChanger();
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onDispenseChange() {
        if (mCashChanger == null) {
            return;
        }

        try {
            mCashChanger.dispenseChange(Integer.parseInt(mTextData.getText().toString()));
        } catch (Epos2Exception | NumberFormatException e) {
            ShowMsg.showException(e, "dispenseChange", mContext);
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
    public void onCChangerDispense(final CashChanger cchangerObj, final int code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                int oposCode = 0;
                if (code == Epos2CallbackCode.CODE_ERR_OPOSCODE) {
                    oposCode = cchangerObj.getOposErrorCode();
                    ShowMsg.showResult(code, oposCode, mContext);
                } else {
                    ShowMsg.showResult("onCChangerDispense", code, "", mContext);
                }
            }
        });
    }
}
