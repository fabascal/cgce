package com.epson.epos2_cat;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.epson.epos2.ConnectionListener;
import com.epson.epos2.cat.Cat;
import com.epson.epos2.cat.StatusUpdateListener;

import static com.epson.epos2_cat.MainActivity.mCat;
import static com.epson.epos2_cat.MainActivity.mContext;

public class CatFragment extends Fragment implements StatusUpdateListener, ConnectionListener{

    private TextView mTextCat = null;

    public CatFragment() {
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            if (mCat != null) {
                mCat.setStatusUpdateEventListener(this);
                mCat.setConnectionEventListener(this);
            }
        } else {
            if (mCat != null) {
                mCat.setStatusUpdateEventListener(null);
                mCat.setConnectionEventListener(null);
            }
        }
    }

    protected void setTextView(TextView view) {
        if (view != null) {
            mTextCat = view;
        }
    }

    @Override
    public void onCatStatusUpdate(Cat catObj, final int status) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                String text = "";

                text += "OnStatusUpdate:\n";
                text += "  status:" + getCatStatusText(status) + "\n";

                mTextCat.append(text);
            }
        });
    }

    @Override
    public void onConnection(Object deviceObj, final int eventType) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult("onConnection", eventType, mContext);
            }
        });
    }

    public String getPaymentConditionText(int paymentCondition) {
        String text = "";
        switch (paymentCondition) {
            case Cat.PAYMENT_CONDITION_LUMP_SUM:
                text += "lump_sum";
                break;
            case Cat.PAYMENT_CONDITION_BONUS_1:
                text += "bonus_1";
                break;
            case Cat.PAYMENT_CONDITION_BONUS_2:
                text += "bonus_2";
                break;
            case Cat.PAYMENT_CONDITION_BONUS_3:
                text += "bonus_3";
                break;
            case Cat.PAYMENT_CONDITION_INSTALLMENT_1:
                text += "installment_1";
                break;
            case Cat.PAYMENT_CONDITION_INSTALLMENT_2:
                text += "installment_2";
                break;
            case Cat.PAYMENT_CONDITION_REVOLVING:
                text += "revolving";
                break;
            case Cat.PAYMENT_CONDITION_COMBINATION_1:
                text += "combination_1";
                break;
            case Cat.PAYMENT_CONDITION_COMBINATION_2:
                text += "combination_2";
                break;
            case Cat.PAYMENT_CONDITION_DEBIT:
                text += "debit";
                break;
            case Cat.PAYMENT_CONDITION_ELECTRONIC_MONEY:
                text += "electronic_money";
                break;
            case Cat.PAYMENT_CONDITION_OTHER:
                text += "other";
                break;
            default:
                break;
        }

        return text;
    }

    private String getCatStatusText(int status) {
        String text = "";
        switch (status) {
            case Cat.SUE_POWER_ONLINE:
                text += "POWER_ONLINE";
                break;
            case Cat.SUE_POWER_OFF_OFFLINE:
                text += "OFF_OFFLINE";
                break;
            case Cat.SUE_LOGSTATUS_OK:
                text += "LOGSTATUS_OK";
                break;
            case Cat.SUE_LOGSTATUS_NEARFULL:
                text += "LOGSTATUS_NEARFULL";
                break;
            case Cat.SUE_LOGSTATUS_FULL:
                text += "LOGSTATUS_FULL";
                break;
            default:
                text += status;
                break;
        }

        return text;
    }

}
