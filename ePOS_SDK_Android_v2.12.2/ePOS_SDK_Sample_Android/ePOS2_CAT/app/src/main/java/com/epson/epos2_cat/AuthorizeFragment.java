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

import com.epson.epos2.cat.AccessDailyLogListener;
import com.epson.epos2.cat.AuthorizeCompletionListener;
import com.epson.epos2.cat.AuthorizeRefundListener;
import com.epson.epos2.cat.AuthorizeResult;
import com.epson.epos2.cat.AuthorizeSalesListener;
import com.epson.epos2.cat.AuthorizeVoidListener;
import com.epson.epos2.cat.Cat;
import com.epson.epos2.cat.DailyLog;
import com.epson.epos2.cat.Epos2CallbackCode;

import java.util.ArrayList;

import static com.epson.epos2_cat.MainActivity.mCat;
import static com.epson.epos2_cat.MainActivity.mContext;

public class AuthorizeFragment extends CatFragment implements View.OnClickListener, AccessDailyLogListener, AuthorizeCompletionListener, AuthorizeRefundListener, AuthorizeSalesListener, AuthorizeVoidListener {
    private Spinner mSpnService = null;
    private Spinner mSpnAuthorize = null;
    private EditText mTextAmount = null;
    private TextView mTextCat = null;


    private static final int SEQUENCE_NUM = 0001;
    private static final int AuthorizeSales = 0;
    private static final int AuthorizeVoid = 1;
    private static final int AuthorizeRefund = 2;
    private static final int AuthorizeCompletion = 3;

    public static AuthorizeFragment newInstance() {
        return new AuthorizeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_authorize, container, false);

        mSpnService = rootView.findViewById(R.id.spnService);
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
        mSpnService.setAdapter(endModeAdapter);
        mSpnService.setSelection(0);

        mTextAmount =rootView.findViewById(R.id.editText_Amount);

        mSpnAuthorize = (Spinner) rootView.findViewById(R.id.spnAuthorize);
        ArrayAdapter<SpnItems> authorizeAdapter = new ArrayAdapter<SpnItems>(getActivity(), android.R.layout.simple_spinner_item);
        authorizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        authorizeAdapter.add(new SpnItems(getString(R.string.authorize_sales), AuthorizeSales));
        authorizeAdapter.add(new SpnItems(getString(R.string.authorize_void), AuthorizeVoid));
        authorizeAdapter.add(new SpnItems(getString(R.string.authorize_refund), AuthorizeRefund));
        authorizeAdapter.add(new SpnItems(getString(R.string.authorize_completion), AuthorizeCompletion));
        mSpnAuthorize.setAdapter(authorizeAdapter);
        mSpnAuthorize.setSelection(0);

        Button btn_clealance = rootView.findViewById(R.id.button_clealance);
        btn_clealance.setOnClickListener(this);

        Button btn_accessDailyLog = rootView.findViewById(R.id.button_accessDailyLog);
        btn_accessDailyLog.setOnClickListener(this);

        Button btn_clear = rootView.findViewById(R.id.button_clear);
        btn_clear.setOnClickListener(this);

        mTextCat = rootView.findViewById(R.id.textView_commandText);

        super.setTextView(mTextCat);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mCat != null) {
                mCat.setAuthorizeSalesEventListener(this);
                mCat.setAuthorizeVoidEventListener(this);
                mCat.setAuthorizeRefundEventListener(this);
                mCat.setAuthorizeCompletionEventListener(this);
                mCat.setAccessDailyLogEventListener(this);
            }
            super.setTextView(mTextCat);
        } else {
            if (mCat != null) {
                mCat.setAuthorizeSalesEventListener(null);
                mCat.setAuthorizeVoidEventListener(null);
                mCat.setAuthorizeRefundEventListener(null);
                mCat.setAuthorizeCompletionEventListener(null);
                mCat.setAccessDailyLogEventListener(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_clealance:
                onCrealance();
                break;
            case R.id.button_accessDailyLog:
                onAccessDailyLog();
                break;
            case R.id.button_clear:
                onClearCat();
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onCrealance() {
        if (mCat == null) {
            return;
        }

        String methodName = "";
        try {
            int authorize = ((SpnItems) mSpnAuthorize.getSelectedItem()).getItemConstant();
            int service = ((SpnItems) mSpnService.getSelectedItem()).getItemConstant();
            int amount = Integer.parseInt(mTextAmount.getText().toString());
            switch (authorize) {
                case AuthorizeSales:
                    methodName += "authorizeSales";
                    mCat.authorizeSales(service, amount, SEQUENCE_NUM);
                    break;
                case AuthorizeVoid:
                    methodName += "authorizeVoid";
                    mCat.authorizeVoid(service, amount, SEQUENCE_NUM);
                    break;
                case AuthorizeRefund:
                    methodName += "authorizeRefund";
                    mCat.authorizeRefund(service, amount, SEQUENCE_NUM);
                    break;
                case AuthorizeCompletion:
                    methodName += "authorizeCompletion";
                    mCat.authorizeCompletion(service, amount, SEQUENCE_NUM);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            ShowMsg.showException(e, methodName, mContext);
        }
    }

    private void onAccessDailyLog() {
        if (mCat == null) {
            return;
        }

        try {
            int service = ((SpnItems) mSpnService.getSelectedItem()).getItemConstant();

            mCat.accessDailyLog(service, SEQUENCE_NUM);
        } catch (Exception e) {
            ShowMsg.showException(e, "accessDailyLog", mContext);
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

    private String makeAuthorizeResultMessage(AuthorizeResult result) {
        String resultMsg = "";
        if (result != null) {
            resultMsg += "  AccountNumber:" + result.getAccountNumber() + "\n";
            resultMsg += "  SettledAmount:" + result.getSettledAmount() + "\n";
            resultMsg += "  SlipNumber:" + result.getSlipNumber() + "\n";
            resultMsg += "  Kid:" + result.getKid() + "\n";
            resultMsg += "  ApprovalCode:" + result.getApprovalCode() + "\n";
            resultMsg += "  TransactionNumber:" + result.getTransactionNumber() + "\n";
            resultMsg += "  PaymentCondition:" + super.getPaymentConditionText(result.getPaymentCondition()) + "\n";
            resultMsg += "  VoidSlipNumber:" + result.getVoidSlipNumber() + "\n";
            resultMsg += "  Balance:" + result.getBalance() + "\n";
        }
        return resultMsg;
    }


    @Override
    public void onCatAuthorizeSales(final Cat catObj, final int code, int sequence, int service, final AuthorizeResult result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                int oposCode = 0;
                if (code == Epos2CallbackCode.CODE_ERR_OPOSCODE) {
                    oposCode = catObj.getOposErrorCode();
                    ShowMsg.showResult(code, oposCode, mContext);
                } else {
                    ShowMsg.showResult(code, "", mContext);
                }

                String txtCat = "";
                txtCat += "OnAuthorizeSales:\n";
                txtCat += makeAuthorizeResultMessage(result);
                mTextCat.setText(txtCat);
            }
        });
    }

    @Override
    public void onCatAuthorizeVoid(final Cat catObj, final int code, int sequence, int service, final AuthorizeResult result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                int oposCode = 0;
                if (code == Epos2CallbackCode.CODE_ERR_OPOSCODE) {
                    oposCode = catObj.getOposErrorCode();
                    ShowMsg.showResult(code, oposCode, mContext);
                } else {
                    ShowMsg.showResult(code, "", mContext);
                }

                String txtCat = "";
                txtCat += "OnAuthorizeVoid:\n";
                txtCat += makeAuthorizeResultMessage(result);
                mTextCat.setText(txtCat);
            }
        });
    }

    @Override
    public void onCatAuthorizeRefund(final Cat catObj, final int code, int sequence, int service, final AuthorizeResult result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                int oposCode = 0;
                if (code == Epos2CallbackCode.CODE_ERR_OPOSCODE) {
                    oposCode = catObj.getOposErrorCode();
                    ShowMsg.showResult(code, oposCode, mContext);
                } else {
                    ShowMsg.showResult(code, "", mContext);
                }

                String txtCat = "";
                txtCat += "OnAuthorizeRefund:\n";
                txtCat += makeAuthorizeResultMessage(result);
                mTextCat.setText(txtCat);
            }
        });
    }

    @Override
    public void onCatAuthorizeCompletion(final Cat catObj, final int code, int sequence, int service, final AuthorizeResult result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                int oposCode = 0;
                if (code == Epos2CallbackCode.CODE_ERR_OPOSCODE) {
                    oposCode = catObj.getOposErrorCode();
                    ShowMsg.showResult(code, oposCode, mContext);
                } else {
                    ShowMsg.showResult(code, "", mContext);
                }

                String txtCat = "";
                txtCat += "OnAuthorizeCompletion:\n";
                txtCat += makeAuthorizeResultMessage(result);
                mTextCat.setText(txtCat);
            }
        });
    }

    @Override
    public void onCatAccessDailyLog(final Cat catObj, final int code, int sequence, int service, final ArrayList<DailyLog> dailyLog) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                int oposCode = 0;
                if (code == Epos2CallbackCode.CODE_ERR_OPOSCODE) {
                    oposCode = catObj.getOposErrorCode();
                    ShowMsg.showResult(code, oposCode, mContext);
                } else {
                    ShowMsg.showResult(code, "", mContext);
                }

                String txtCat = "";
                txtCat += "OnAccessDailyLog:\n";
                if (dailyLog != null) {
                    for (int i = 0; i < dailyLog.size(); i++) {
                        txtCat += "  Kid:" + dailyLog.get(i).getKid() + "\n";
                        txtCat += "  SalesCount:" + dailyLog.get(i).getSalesCount() + "\n";
                        txtCat += "  SalesAmount:" + dailyLog.get(i).getSalesAmount() + "\n";
                        txtCat += "  VoidCount:" + dailyLog.get(i).getVoidCount() + "\n";
                        txtCat += "  VoidAmount:" + dailyLog.get(i).getVoidAmount() + "\n";
                    }
                }

                mTextCat.setText(txtCat);
            }
        });
    }
}
