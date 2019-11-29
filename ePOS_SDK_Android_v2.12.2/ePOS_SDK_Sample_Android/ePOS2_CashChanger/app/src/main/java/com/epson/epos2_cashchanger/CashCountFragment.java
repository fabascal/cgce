package com.epson.epos2_cashchanger;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.cashchanger.CashChanger;
import com.epson.epos2.cashchanger.CashCountListener;

import java.util.Map;

import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class CashCountFragment extends CashChangerFragment implements View.OnClickListener, CashCountListener {

    private TextView mTextCashChanger = null;

    public static CashCountFragment newInstance() {
        return new CashCountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_cashcount, container, false);
        Button btn_readCashCount = rootView.findViewById(R.id.button_readCashCount);
        Button btn_clear = rootView.findViewById(R.id.button_clear);
        mTextCashChanger = rootView.findViewById(R.id.textView_cashCountText);
        btn_readCashCount.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mCashChanger != null) {
                mCashChanger.setCashCountEventListener(this);
            }
            super.setTextView(mTextCashChanger);
        } else {
            if (mCashChanger != null) {
                mCashChanger.setCashCountEventListener(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_readCashCount:
                onReadCashCount();
                break;
            case R.id.button_clear:
                onClearCashChanger();
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onReadCashCount() {
        if (mCashChanger == null) {
            return;
        }

        try {
            mCashChanger.readCashCount();
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "readCashCount", mContext);
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
    public void onCChangerCashCount(CashChanger cchangerObj, final int code, final Map<String, Integer> data) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowMsg.showResult("onCChangerCashCount", code, "", mContext);

                String cashCountText = "OnCashCount:\n";

                if(data != null) {
                    for (Map.Entry<String, Integer> entry : data.entrySet()) {
                        String key = entry.getKey();
                        Integer value = entry.getValue();

                        cashCountText += key + ":" + value + "\n";
                    }
                }

                mTextCashChanger.append(cashCountText);
            }
        });
    }
}
