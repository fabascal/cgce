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
import com.epson.epos2.cashchanger.DepositListener;
import com.epson.epos2.cashchanger.Epos2CallbackCode;

import java.util.Map;

import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class DepositFragment extends CashChangerFragment implements View.OnClickListener, DepositListener {

    private Spinner mSpinner = null;
    private TextView mTextCashChanger = null;

    public static DepositFragment newInstance() {
        return new DepositFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_deposit, container, false);
        Button btn_begin = rootView.findViewById(R.id.button_begin);
        Button btn_pause = rootView.findViewById(R.id.button_pause);
        Button btn_restart = rootView.findViewById(R.id.button_restart);
        Button btn_end = rootView.findViewById(R.id.button_end);
        Button btn_clear = rootView.findViewById(R.id.button_clear);

        mSpinner = rootView.findViewById(R.id.spinner_config);
        ArrayAdapter<SpnItems> endDepositAdapter = new ArrayAdapter<SpnItems>(getActivity(), android.R.layout.simple_spinner_item);
        endDepositAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endDepositAdapter.add(new SpnItems(getString(R.string.deposit_change), CashChanger.DEPOSIT_CHANGE));
        endDepositAdapter.add(new SpnItems(getString(R.string.deposit_no_change), CashChanger.DEPOSIT_NOCHANGE));
        endDepositAdapter.add(new SpnItems(getString(R.string.deposit_repay), CashChanger.DEPOSIT_REPAY));
        mSpinner.setAdapter(endDepositAdapter);
        mSpinner.setSelection(0);

        mTextCashChanger = rootView.findViewById(R.id.textView_depositText);

        btn_begin.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_restart.setOnClickListener(this);
        btn_end.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mCashChanger != null) {
                mCashChanger.setDepositEventListener(this);
            }
            super.setTextView(mTextCashChanger);
        } else {
            if (mCashChanger != null) {
                mCashChanger.setDepositEventListener(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_begin:
                onBeginDeposit();
                break;
            case R.id.button_pause:
                onPauseDeposit();
                break;
            case R.id.button_restart:
                onRestartDeposit();
                break;
            case R.id.button_end:
                onEndDeposit();
                break;
            case R.id.button_clear:
                onClearCashChanger();
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onBeginDeposit() {
        if (mCashChanger == null) {
            return;
        }

        try {
            mCashChanger.beginDeposit();
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "beginDeposit", mContext);
        }
    }

    private void onPauseDeposit() {
        if (mCashChanger == null) {
            return;
        }

        try {
            mCashChanger.pauseDeposit();
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "pauseDeposit", mContext);
        }
    }

    private void onRestartDeposit() {
        if (mCashChanger == null) {
            return;
        }

        try {
            mCashChanger.restartDeposit();
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "restartDeposit", mContext);
        }
    }

    private void onEndDeposit() {
        if (mCashChanger == null) {
            return;
        }
        int config = ((SpnItems) mSpinner.getSelectedItem()).getModelConstant();

        try {
            mCashChanger.endDeposit(config);
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "endDeposit", mContext);
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
    public void onCChangerDeposit(final CashChanger cchangerObj, final int code, final int status, final int amount, final Map<String, Integer> data) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                int oposCode = 0;

                if (code == Epos2CallbackCode.CODE_ERR_OPOSCODE) {
                    oposCode = cchangerObj.getOposErrorCode();
                    ShowMsg.showResult(code, oposCode, mContext);
                } else {
                    ShowMsg.showResult("onCChangerDeposit", code, "", mContext);
                }

                String depositText = "";

                depositText += "OnDeposit:\n";
                depositText += "  status:" + getStatusText(status);
                depositText += "  amount:" + amount + "\n";

                if(data != null) {
                    for (Map.Entry<String, Integer> entry : data.entrySet()) {
                        String str = entry.getKey();
                        Integer value = entry.getValue();

                        depositText += str + ":" + value + "\n";
                    }
                }

                mTextCashChanger.append(depositText);
            }
        });
    }


    private String getStatusText(int status) {
        String text = "";
        switch (status) {
            case CashChanger.STATUS_BUSY:
                text += "Busy\n";
                break;
            case CashChanger.STATUS_PAUSE:
                text += "Pause\n";
                break;
            case CashChanger.STATUS_END:
                text += "End\n";
                break;
            case CashChanger.STATUS_ERR:
                text += "Error\n";
                break;
            default:
                text += status + "\n";
                break;
        }

        return text;
    }
}
