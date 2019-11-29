package com.epson.epos2_cat;

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
import com.epson.epos2.cat.Cat;
import com.epson.epos2.cat.DirectIOCommandReplyListener;
import com.epson.epos2.cat.DirectIOResult;
import com.epson.epos2.cat.Epos2CallbackCode;

import static com.epson.epos2_cat.MainActivity.mCat;
import static com.epson.epos2_cat.MainActivity.mContext;

public class CommandFragment extends CatFragment implements View.OnClickListener, DirectIOCommandReplyListener {

    private EditText mTextDirectIOCommand = null;
    private EditText mTextDirectIOData = null;
    private EditText mTextDirectIOString = null;
    Spinner mSpinner = null;
    private TextView mTextCat = null;

    public static CommandFragment newInstance() {
        return new CommandFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_command, container, false);

        mTextDirectIOCommand = rootView.findViewById(R.id.editText_directIOCommand);

        mTextDirectIOData = rootView.findViewById(R.id.editText_directIOData);

        mTextDirectIOString = rootView.findViewById(R.id.editText_directIOString);

        mSpinner = rootView.findViewById(R.id.spinner_service);
        ArrayAdapter<SpnItems> endModeAdapter = new ArrayAdapter<SpnItems>(getActivity(), android.R.layout.simple_spinner_item);
        endModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endModeAdapter.add(new SpnItems(getString(R.string.service_credit), Cat.SERVICE_CREDIT));
        endModeAdapter.add(new SpnItems(getString(R.string.service_debit), Cat.SERVICE_DEBIT));
        endModeAdapter.add(new SpnItems(getString(R.string.service_edy), Cat.SERVICE_EDY));
        endModeAdapter.add(new SpnItems(getString(R.string.service_id), Cat.SERVICE_ID));
        endModeAdapter.add(new SpnItems(getString(R.string.service_nanaco), Cat.SERVICE_NANACO));
        endModeAdapter.add(new SpnItems(getString(R.string.service_quicpay), Cat.SERVICE_QUICPAY));
        endModeAdapter.add(new SpnItems(getString(R.string.service_suica), Cat.SERVICE_SUICA));
        endModeAdapter.add(new SpnItems(getString(R.string.service_unionpay), Cat.SERVICE_UNIONPAY));
        endModeAdapter.add(new SpnItems(getString(R.string.service_waon), Cat.SERVICE_WAON));
        endModeAdapter.add(new SpnItems(getString(R.string.service_point), Cat.SERVICE_POINT));
        endModeAdapter.add(new SpnItems(getString(R.string.service_common), Cat.SERVICE_COMMON));
        mSpinner.setAdapter(endModeAdapter);
        mSpinner.setSelection(0);

        Button btn_sendDirectIOCommand = rootView.findViewById(R.id.button_sendDirectIOCommand);
        btn_sendDirectIOCommand.setOnClickListener(this);

        Button btn_clear = rootView.findViewById(R.id.button_clear);
        btn_clear.setOnClickListener(this);

        mTextCat = rootView.findViewById(R.id.textView_commandText);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mCat != null) {
                mCat.setDirectIOCommandReplyEventListener(this);
            }
            super.setTextView(mTextCat);
        } else {
            if (mCat != null) {
                mCat.setDirectIOCommandReplyEventListener(null);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sendDirectIOCommand:
                onSendDirectIOCommand();
                break;
            case R.id.button_clear:
                onClearCat();
                ;
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onSendDirectIOCommand() {
        int command = 0;
        int data = 0;
        int service = 0;
        try {
            command = Integer.parseInt(mTextDirectIOCommand.getText().toString());
        } catch (NumberFormatException e) {
            // Do nothing
        }
        try {
            data = Integer.parseInt(mTextDirectIOData.getText().toString());
        } catch (NumberFormatException e) {
            // Do nothing
        }
        try {
            service = ((SpnItems) mSpinner.getSelectedItem()).getItemConstant();
        } catch (NumberFormatException e) {
            // Do nothing
        }

        if (mCat == null) {
            return;
        }

        try {
            mCat.sendDirectIOCommand(command, data, mTextDirectIOString.getText().toString(), service);
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "sendDirectIOCommand", mContext);
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

    private String makeDirectIOResultMessage(DirectIOResult result) {
        String resultMsg = "";
        if (result != null) {
            resultMsg += "  AccountNumber:" + result.getAccountNumber() + "\n";
            resultMsg += "  SettledAmount:" + result.getSettledAmount() + "\n";
            resultMsg += "  SlipNumber:" + result.getSlipNumber() + "\n";
            resultMsg += "  TransactionNumber:" + result.getTransactionNumber() + "\n";
            resultMsg += "  PaymentCondition:" + super.getPaymentConditionText(result.getPaymentCondition()) + "\n";
            resultMsg += "  Balance:" + result.getBalance() + "\n";
            resultMsg += "  AdditionalSecurityInformation:" + result.getAdditionalSecurityInformation() + "\n";
        }
        return resultMsg;
    }

    @Override
    public void onCatDirectIOCommandReply(final Cat catObj, final int code, final int command, final int data, final String string,final int sequence,final int service, final DirectIOResult result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int oposCode = 0;
                if (code == Epos2CallbackCode.CODE_ERR_OPOSCODE) {
                    oposCode = catObj.getOposErrorCode();
                    ShowMsg.showResult(code, oposCode, mContext);
                } else {
                    ShowMsg.showResult(code, "", mContext);
                }

                String directIOCommandText = "OnDirectIOCommandReply:\n";

                directIOCommandText += "  command:" + command + "\n";
                directIOCommandText += "  data:" + data + "\n";
                directIOCommandText += "  string:" + string + "\n";
                directIOCommandText += makeDirectIOResultMessage(result);

                mTextCat.append(directIOCommandText);
            }
        });
    }

}
