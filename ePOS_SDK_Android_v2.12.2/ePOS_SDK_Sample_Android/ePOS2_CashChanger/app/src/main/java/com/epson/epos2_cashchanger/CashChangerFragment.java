package com.epson.epos2_cashchanger;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.epson.epos2.ConnectionListener;
import com.epson.epos2.cashchanger.CashChanger;
import com.epson.epos2.cashchanger.DirectIOListener;
import com.epson.epos2.cashchanger.StatusChangeListener;
import com.epson.epos2.cashchanger.StatusUpdateListener;

import java.util.Map;

import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class CashChangerFragment extends Fragment implements StatusChangeListener, StatusUpdateListener, DirectIOListener, ConnectionListener {

    private TextView mTextCashChanger = null;

    public CashChangerFragment() {
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            if (mCashChanger != null) {
                mCashChanger.setStatusChangeEventListener(this);
                mCashChanger.setStatusUpdateEventListener(this);
                mCashChanger.setDirectIOEventListener(this);
                mCashChanger.setConnectionEventListener(this);
            }
        } else {
            if (mCashChanger != null) {
                mCashChanger.setStatusChangeEventListener(null);
                mCashChanger.setStatusUpdateEventListener(null);
                mCashChanger.setDirectIOEventListener(null);
                mCashChanger.setConnectionEventListener(null);
            }
        }
    }

    protected void setTextView(TextView view) {
        if (view != null) {
            mTextCashChanger = view;
        }
    }

    @Override
    public void onCChangerStatusChange(CashChanger cchangerObj, final int code, final Map<String, Integer> status) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult("onCChangerStatusChange", code, "", mContext);

                String text = "";

                text += "OnStatusChange:\n";
                if(status != null) {
                    for (Map.Entry<String, Integer> entry : status.entrySet()) {
                        String str = entry.getKey();
                        String value = getCashStatusText(entry.getValue());

                        text += "  " + str + ":" + value + "\n";
                    }
                }

                mTextCashChanger.append(text);
            }
        });

    }

    @Override
    public void onCChangerStatusUpdate(CashChanger cchangerObj, final int status) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                String text = "";

                text += "OnStatusUpdate:\n";
                text += "  status:" + getStatusUpdateText(status) + "\n";

                mTextCashChanger.append(text);
            }
        });
    }

    @Override
    public void onCChangerDirectIO(CashChanger cchangerObj, final int eventNumber, final int data, final String string) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                String text = "";

                text += "OnDirectIO:\n";
                text += "  eventNumber:" + eventNumber + "\n";
                text += "  data:" + data + "\n";
                text += "  string:" + string + "\n";

                mTextCashChanger.append(text);
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

    private String getCashStatusText(int status) {
        String text = "";
        switch (status) {
            case CashChanger.ST_EMPTY:
                text += "Empty";
                break;
            case CashChanger.ST_NEAR_EMPTY:
                text += "NearEmpty";
                break;
            case CashChanger.ST_OK:
                text += "OK";
                break;
            case CashChanger.ST_NEAR_FULL:
                text += "NearFull";
                break;
            case CashChanger.ST_FULL:
                text += "Full";
                break;
            default:
                break;
        }

        return text;
    }

    private String getStatusUpdateText(int status) {
        String text = "";
        switch (status) {
            case CashChanger.SUE_POWER_ONLINE:
                text += "POWER_ONLINE";
                break;
            case CashChanger.SUE_POWER_OFF:
                text += "POWER_OFF";
                break;
            case CashChanger.SUE_POWER_OFFLINE:
                text += "POWER_OFFLINE";
                break;
            case CashChanger.SUE_POWER_OFF_OFFLINE:
                text += "OFF_OFFLINE";
                break;
            case CashChanger.SUE_STATUS_EMPTY:
                text += "STATUS_EMPTY";
                break;
            case CashChanger.SUE_STATUS_NEAREMPTY:
                text += "STATUS_NEAREMPTY";
                break;
            case CashChanger.SUE_STATUS_EMPTYOK:
                text += "STATUS_EMPTYOK";
                break;
            case CashChanger.SUE_STATUS_FULL:
                text += "STATUS_FULL";
                break;
            case CashChanger.SUE_STATUS_NEARFULL:
                text += "STATUS_NEARFULL";
                break;
            case CashChanger.SUE_STATUS_FULLOK:
                text += "STATUS_FULLOK";
                break;
            case CashChanger.SUE_STATUS_JAM:
                text += "STATUS_JAM";
                break;
            case CashChanger.SUE_STATUS_JAMOK:
                text += "STATUS_JAMOK";
                break;
            default:
                text += String.valueOf(status);
                break;
        }

        return text;
    }
}
