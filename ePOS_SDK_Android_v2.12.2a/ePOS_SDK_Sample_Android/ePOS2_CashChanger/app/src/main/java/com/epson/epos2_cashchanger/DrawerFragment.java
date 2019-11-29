package com.epson.epos2_cashchanger;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.epson.epos2.Epos2Exception;

import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class DrawerFragment extends CashChangerFragment implements View.OnClickListener {

    private TextView mTextCashChanger = null;

    public static DrawerFragment newInstance() {
        return new DrawerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_drawer, container, false);
        Button btn_openDrawer = (Button) rootView.findViewById(R.id.button_openDrawer);
        Button btn_clear = rootView.findViewById(R.id.button_clear);
        mTextCashChanger = rootView.findViewById(R.id.textView_drawerText);
        btn_openDrawer.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
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
            case R.id.button_openDrawer:
                onOpenDrawer();
                break;
            case R.id.button_clear:
                onClearCashChanger();
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onOpenDrawer() {
        if (mCashChanger == null) {
            return;
        }

        try {
            mCashChanger.openDrawer();
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "openDrawer", mContext);
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
