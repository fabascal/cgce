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

import java.util.HashMap;
import java.util.Map;

import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class DispenseCashFragment extends CashChangerFragment implements View.OnClickListener, DispenseListener {

    private EditText mTextJpy1 = null;
    private EditText mTextJpy5 = null;
    private EditText mTextJpy10 = null;
    private EditText mTextJpy50 = null;
    private EditText mTextJpy100 = null;
    private EditText mTextJpy500 = null;
    private EditText mTextJpy1000 = null;
    private EditText mTextJpy2000 = null;
    private EditText mTextJpy5000 = null;
    private EditText mTextJpy10000 = null;
    private TextView mTextCashChanger = null;

    public static DispenseCashFragment newInstance() {
        return new DispenseCashFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_dispensecash, container, false);
        Button btn_dispenseCash = rootView.findViewById(R.id.button_dispenseCash);
        Button btn_clear = rootView.findViewById(R.id.button_clear);
        mTextJpy1 = rootView.findViewById(R.id.editText_jpy1);
        mTextJpy5 = rootView.findViewById(R.id.editText_jpy5);
        mTextJpy10 = rootView.findViewById(R.id.editText_jpy10);
        mTextJpy50 = rootView.findViewById(R.id.editText_jpy50);
        mTextJpy100 = rootView.findViewById(R.id.editText_jpy100);
        mTextJpy500 = rootView.findViewById(R.id.editText_jpy500);
        mTextJpy1000 = rootView.findViewById(R.id.editText_jpy1000);
        mTextJpy2000 = rootView.findViewById(R.id.editText_jpy2000);
        mTextJpy5000 = rootView.findViewById(R.id.editText_jpy5000);
        mTextJpy10000 = rootView.findViewById(R.id.editText_jpy10000);
        mTextCashChanger = rootView.findViewById(R.id.textView_dispenseCashText);
        btn_dispenseCash.setOnClickListener(this);
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
            case R.id.button_dispenseCash:
                onDispenseCash();
                break;
            case R.id.button_clear:
                onClearCashChanger();
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onDispenseCash() {
        if (mCashChanger == null) {
            return;
        }
        try {
            Map<String, Integer> map = new HashMap<>();
            map.put("jpy1", Integer.parseInt(mTextJpy1.getText().toString()));
            map.put("jpy5", Integer.parseInt(mTextJpy5.getText().toString()));
            map.put("jpy10", Integer.parseInt(mTextJpy10.getText().toString()));
            map.put("jpy50", Integer.parseInt(mTextJpy50.getText().toString()));
            map.put("jpy100", Integer.parseInt(mTextJpy100.getText().toString()));
            map.put("jpy500", Integer.parseInt(mTextJpy500.getText().toString()));
            map.put("jpy1000", Integer.parseInt(mTextJpy1000.getText().toString()));
            map.put("jpy2000", Integer.parseInt(mTextJpy2000.getText().toString()));
            map.put("jpy5000", Integer.parseInt(mTextJpy5000.getText().toString()));
            map.put("jpy10000", Integer.parseInt(mTextJpy10000.getText().toString()));
            mCashChanger.dispenseCash(map);
        } catch (Epos2Exception | NumberFormatException e) {
            ShowMsg.showException(e, "dispenseCash", mContext);
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
