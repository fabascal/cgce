package com.epson.epos2_cashchanger;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.cashchanger.CashChanger;
import com.epson.epos2.cashchanger.CollectListener;

import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class CollectFragment extends CashChangerFragment implements View.OnClickListener, CollectListener {

    private Spinner mSpinner = null;
    private TextView mTextCashChanger = null;

    public static CollectFragment newInstance() {
        return new CollectFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_collect, container, false);
        Button btn_collectCash = (Button) rootView.findViewById(R.id.button_collectCash);
        Button btn_clear = rootView.findViewById(R.id.button_clear);
        mSpinner = rootView.findViewById(R.id.spinner_collectType);
        ArrayAdapter<SpnItems> collectTypeAdapter = new ArrayAdapter<SpnItems>(getActivity(), android.R.layout.simple_spinner_item);
        collectTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collectTypeAdapter.add(new SpnItems(getString(R.string.collect_all_cash), CashChanger.COLLECT_ALL_CASH));
        collectTypeAdapter.add(new SpnItems(getString(R.string.collect_part_of_cash), CashChanger.COLLECT_PART_OF_CASH));
        mSpinner.setAdapter(collectTypeAdapter);
        mSpinner.setSelection(0);

        mTextCashChanger = rootView.findViewById(R.id.textView_collectText);
        btn_collectCash.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mCashChanger != null) {
                mCashChanger.setCollectEventListener(this);
            }
            super.setTextView(mTextCashChanger);
        } else {
            if (mCashChanger != null) {
                mCashChanger.setCollectEventListener(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_collectCash:
                onCollectCash();
                break;
            case R.id.button_clear:
                onClearCashChanger();
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onCollectCash() {
        if (mCashChanger == null) {
            return;
        }

        int type = ((SpnItems) mSpinner.getSelectedItem()).getModelConstant();
        try {
            mCashChanger.collectCash(type);
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "collectCash", mContext);
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
    public void onCChangerCollect(CashChanger cchangerObj, final int code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult("onCChangerCollect", code, "", mContext);
            }
        });
    }
}
