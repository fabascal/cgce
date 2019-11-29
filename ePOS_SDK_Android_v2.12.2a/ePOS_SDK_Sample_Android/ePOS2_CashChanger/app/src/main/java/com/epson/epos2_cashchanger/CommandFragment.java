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
import com.epson.epos2.cashchanger.CommandReplyListener;
import com.epson.epos2.cashchanger.DirectIOCommandReplyListener;
import com.epson.epos2.cashchanger.Epos2CallbackCode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.epson.epos2_cashchanger.MainActivity.mCashChanger;
import static com.epson.epos2_cashchanger.MainActivity.mContext;

public class CommandFragment extends CashChangerFragment implements View.OnClickListener, CommandReplyListener, DirectIOCommandReplyListener {

    private EditText mTextCommandData = null;
    private EditText mTextDirectIOCommand = null;
    private EditText mTextDirectIOData = null;
    private EditText mTextDirectIOString = null;
    private TextView mTextCashChanger = null;

    public static CommandFragment newInstance() {
        return new CommandFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_command, container, false);
        Button btn_sendCommand = rootView.findViewById(R.id.button_sendCommand);
        Button btn_sendDirectIOCommand = rootView.findViewById(R.id.button_sendDirectIOCommand);
        Button btn_clear = rootView.findViewById(R.id.button_clear);
        mTextCommandData = rootView.findViewById(R.id.editText_commandData);
        mTextDirectIOCommand = rootView.findViewById(R.id.editText_directIOCommand);
        mTextDirectIOData = rootView.findViewById(R.id.editText_directIOData);
        mTextDirectIOString = rootView.findViewById(R.id.editText_directIOString);
        mTextCashChanger = rootView.findViewById(R.id.textView_commandText);
        btn_sendCommand.setOnClickListener(this);
        btn_sendDirectIOCommand.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mCashChanger != null) {
                mCashChanger.setCommandReplyEventListener(this);
                mCashChanger.setDirectIOCommandReplyEventListener(this);
            }
            super.setTextView(mTextCashChanger);
        } else {
            if (mCashChanger != null) {
                mCashChanger.setCommandReplyEventListener(null);
                mCashChanger.setDirectIOCommandReplyEventListener(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sendCommand:
                onSendCommand();
                break;
            case R.id.button_sendDirectIOCommand:
                onSendDirectIOCommand();
                break;
            case R.id.button_clear:
                onClearCashChanger();
                ;
                break;
            default:
                // Do nothing
                break;
        }
    }

    private void onSendCommand() {
        String text = mTextCommandData.getText().toString();
        ArrayList<Byte> dataBuffer = new ArrayList<>();
        Character enableNumber[] = {
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
                0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
                0x61, 0x62, 0x63, 0x64, 0x65, 0x66
        };
        int start = 0;

        if (mCashChanger == null) {
            return;
        }

        for (int index = 0; index < text.length(); index++) {
            char charcter = text.charAt(index);

            if (charcter == 0x20 || index == text.length() - 1) {
                boolean numFlag = false;
                String target;
                if (charcter == 0x20) {
                    target = text.substring(start, index);
                } else {
                    target = text.substring(start, text.length());
                }
                start = index + 1;

                if (target.length() <= 2) {
                    for (int i = 0; i < target.length(); i++) {
                        char chkChar = target.charAt(i);
                        if (Arrays.asList(enableNumber).contains(chkChar)) {
                            numFlag = true;
                        } else {
                            numFlag = false;
                            break;
                        }
                    }
                }
                if (numFlag) {
                    dataBuffer.add((byte) (Short.parseShort(target, 16) & 0xFF));
                } else {
                    byte[] bytes = null;
                    try {
                        bytes = target.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        ShowMsg.showException(e, "getBytes", mContext);
                    }

                    if (bytes != null) {
                        for (byte aByte : bytes) {
                            dataBuffer.add(aByte);
                        }
                    }
                }
            }
        }

        byte[] sendData = new byte[dataBuffer.size()];
        for (int n = 0; n < sendData.length; n++) {
            sendData[n] = dataBuffer.get(n);
        }

        if (sendData.length > 0) {
            try {
                mCashChanger.sendCommand(sendData);
            } catch (Epos2Exception e) {
                ShowMsg.showException(e, "sendCommand", mContext);
            }
        }

    }

    private void onSendDirectIOCommand() {
        int command = 0;
        int data = 0;
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

        if (mCashChanger == null) {
            return;
        }

        try {
            mCashChanger.sendDirectIOCommand(command, data, mTextDirectIOString.getText().toString());
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "sendDirectIOCommand", mContext);
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
    public void onCChangerCommandReply(CashChanger cchangerObj, final int code, final byte[] data) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult("onCChangerCommandReply",code, "", mContext);
                String commandText = "OnCommandReply:\n";
                if (data != null) {
                    commandText += getBinaryString(data) + "\n";
                }
                mTextCashChanger.append(commandText);
            }
        });
    }

    @Override
    public void onCChangerDirectIOCommandReply(final CashChanger cchangerObj, final int code, final int command, final int data, final String string) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int oposCode = 0;
                if (code == Epos2CallbackCode.CODE_ERR_OPOSCODE) {
                    oposCode = cchangerObj.getOposErrorCode();
                    ShowMsg.showResult(code, oposCode, mContext);
                } else {
                    ShowMsg.showResult("onCChangerDirectIOCommandReply", code, "", mContext);
                }

                String directIOCommandText = "OnDirectIOCommandReply:\n";

                directIOCommandText += "  command:" + command + "\n";
                directIOCommandText += "  data:" + data + "\n";
                directIOCommandText += "  string:" + string + "\n";

                mTextCashChanger.append(directIOCommandText);
            }
        });
    }

    private String getBinaryString(byte[] data) {
        int counter = 0;
        StringBuilder buffer = new StringBuilder();
        for (counter = 0; counter < data.length; counter++) {
            buffer.append(String.format("%02x ", data[counter]));
        }
        return buffer.toString();
    }
}
