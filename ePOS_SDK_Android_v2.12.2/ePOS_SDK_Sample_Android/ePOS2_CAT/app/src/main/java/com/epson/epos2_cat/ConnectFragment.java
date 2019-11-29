package com.epson.epos2_cat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.cat.Cat;

import static com.epson.epos2_cat.MainActivity.finalizeObject;
import static com.epson.epos2_cat.MainActivity.initializeObject;
import static com.epson.epos2_cat.MainActivity.mCat;
import static com.epson.epos2_cat.MainActivity.mContext;

public class ConnectFragment extends CatFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener  {
    private Button mBtnConnect = null;
    private Switch mSwTraining = null;
    private EditText mTextTarget = null;
    private TextView mTextCat = null;

    public static ConnectFragment newInstance() {
        return new ConnectFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);
        mBtnConnect = rootView.findViewById(R.id.button_connect);
        Button btn_disconnect = rootView.findViewById(R.id.button_disconnect);
        mSwTraining = rootView.findViewById(R.id.swTraining);
        mSwTraining.setOnCheckedChangeListener(this);
        Button btn_clear = rootView.findViewById(R.id.button_clear);
        mTextTarget = rootView.findViewById(R.id.editText_target);
        mTextCat = rootView.findViewById(R.id.textView_connectText);
        mBtnConnect.setOnClickListener(this);
        btn_disconnect.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        super.setTextView(mTextCat);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            super.setTextView(mTextCat);
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
                onClearCat();
                break;
            case R.id.editText_target:

                break;
            default:
                // Do nothing
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(mCat == null) {
            return;
        }

        int mode = Cat.FALSE;
        if(isChecked) {
            mode = Cat.TRUE;
        } else {
            mode = Cat.FALSE;
        }

        try {
            mCat.setTrainingMode(mode);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "setTrainingMode", mContext);
        }

    }


    private void connectProcess() {
        mBtnConnect.setEnabled(false);

        if (!initializeObject()) {
            mBtnConnect.setEnabled(true);
            return;
        }
        super.setUserVisibleHint(true);
        if (!connectCat()) {
            mBtnConnect.setEnabled(true);
            return;
        }
    }

    private void disconnectProcess() {
        disconnectCat();

        finalizeObject();

        mBtnConnect.setEnabled(true);
    }

    private boolean connectCat() {

        if (mCat == null) {
            return false;
        }

        try {
            mCat.connect(mTextTarget.getText().toString(), Cat.PARAM_DEFAULT);
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        return true;
    }

    private void disconnectCat() {
        if (mCat == null) {
            return;
        }

        try {
            mCat.disconnect();
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "disconnect", mContext);
        }
    }

    private void onClearCat() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                mTextCat.setText("");
            }
        });
    }
}
