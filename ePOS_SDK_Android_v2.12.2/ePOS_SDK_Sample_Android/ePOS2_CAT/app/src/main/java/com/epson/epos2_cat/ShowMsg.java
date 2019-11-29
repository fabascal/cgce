package com.epson.epos2_cat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.cat.Epos2CallbackCode;

public class ShowMsg {
    public static void showException(Exception e, String method, Context context) {
        String msg = "";
        if (e instanceof Epos2Exception) {
            msg = String.format(
                    "%s\n\t%s\n%s\n\t%s",
                    context.getString(R.string.title_err_code),
                    getEposExceptionText(((Epos2Exception) e).getErrorStatus()),
                    context.getString(R.string.title_err_method),
                    method);
        } else {
            msg = e.toString();
        }
        show(msg, context);
    }

    public static void showResult(int code, String errMsg, Context context) {
        String msg = "";
        if (errMsg.isEmpty()) {
            msg = String.format(
                    "\t%s\n\t%s\n",
                    context.getString(R.string.title_msg_result),
                    getCodeText(code));
        } else {
            msg = String.format(
                    "\t%s\n\t%s\n\n\t%s\n\t%s\n",
                    context.getString(R.string.title_msg_result),
                    getCodeText(code),
                    context.getString(R.string.title_msg_description),
                    errMsg);
        }
        show(msg, context);
    }
    public static void showResult(String method, int eventType, Context context) {
        String msg = "";
        msg = String.format(
                "%s\n%s\t%s\t%s\n",
                method,
                context.getString(R.string.title_msg_result),
                ":",
                getConnectionEventText(eventType));
        show(msg, context);
    }
    public static void showResult(int code, int oposCode, Context context) {
        String msg = String.format(
                "\t%s\n\t%s\n\n\t%s\n\t%s\n",
                context.getString(R.string.title_msg_result),
                getCodeText(code),
                context.getString(R.string.title_msg_oposCode),
                oposCode);
        show(msg, context);
    }

    public static void showMsg(String msg, Context context) {
        show(msg, context);
    }

    private static void show(String msg, Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                return;
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    private static String getEposExceptionText(int state) {
        String return_text = "";
        switch (state) {
            case Epos2Exception.ERR_PARAM:
                return_text = "ERR_PARAM";
                break;
            case Epos2Exception.ERR_CONNECT:
                return_text = "ERR_CONNECT";
                break;
            case Epos2Exception.ERR_TIMEOUT:
                return_text = "ERR_TIMEOUT";
                break;
            case Epos2Exception.ERR_MEMORY:
                return_text = "ERR_MEMORY";
                break;
            case Epos2Exception.ERR_ILLEGAL:
                return_text = "ERR_ILLEGAL";
                break;
            case Epos2Exception.ERR_PROCESSING:
                return_text = "ERR_PROCESSING";
                break;
            case Epos2Exception.ERR_NOT_FOUND:
                return_text = "ERR_NOT_FOUND";
                break;
            case Epos2Exception.ERR_IN_USE:
                return_text = "ERR_IN_USE";
                break;
            case Epos2Exception.ERR_TYPE_INVALID:
                return_text = "ERR_TYPE_INVALID";
                break;
            case Epos2Exception.ERR_DISCONNECT:
                return_text = "ERR_DISCONNECT";
                break;
            case Epos2Exception.ERR_ALREADY_OPENED:
                return_text = "ERR_ALREADY_OPENED";
                break;
            case Epos2Exception.ERR_ALREADY_USED:
                return_text = "ERR_ALREADY_USED";
                break;
            case Epos2Exception.ERR_BOX_COUNT_OVER:
                return_text = "ERR_BOX_COUNT_OVER";
                break;
            case Epos2Exception.ERR_BOX_CLIENT_OVER:
                return_text = "ERR_BOX_CLIENT_OVER";
                break;
            case Epos2Exception.ERR_FAILURE:
                return_text = "ERR_FAILURE";
                break;
            default:
                return_text = String.format("%d", state);
                break;
        }
        return return_text;
    }

    private static String getCodeText(int state) {
        String return_text = "";
        switch (state) {
            case Epos2CallbackCode.CODE_SUCCESS:
                return_text = "SUCCESS";
                break;
            case Epos2CallbackCode.CODE_BUSY:
                return_text = "BUSY";
                break;
            case Epos2CallbackCode.CODE_EXCEEDING_LIMIT:
                return_text = "EXCEEDING_LIMIT";
                break;
            case Epos2CallbackCode.CODE_DISAGREEMENT:
                return_text = "DISAGREEMENT";
                break;
            case Epos2CallbackCode.CODE_INVALID_CARD:
                return_text = "INVALID_CARD";
                break;
            case Epos2CallbackCode.CODE_RESET:
                return_text = "RESET";
                break;
            case Epos2CallbackCode.CODE_ERR_CENTER:
                return_text = "ERR_CENTER";
                break;
            case Epos2CallbackCode.CODE_ERR_OPOSCODE:
                return_text = "ERR_OPOSCODE";
                break;
            case Epos2CallbackCode.CODE_ERR_PARAM:
                return_text = "ERR_PARAM";
                break;
            case Epos2CallbackCode.CODE_ERR_DEVICE:
                return_text = "ERR_DEVICE";
                break;
            case Epos2CallbackCode.CODE_ERR_SYSTEM:
                return_text = "ERR_SYSTEM";
                break;
            case Epos2CallbackCode.CODE_ERR_TIMEOUT:
                return_text = "ERR_TIMEOUT";
                break;
            case Epos2CallbackCode.CODE_ERR_FAILURE:
                return_text = "ERR_FAILURE";
                break;
            default:
                return_text = String.format("%d", state);
                break;
        }
        return return_text;
    }

    private static String getConnectionEventText(int eventType) {
        String return_text = "";
        switch (eventType) {
            case CatFragment.EVENT_RECONNECTING:
                return_text = "EVENT_RECONNECTING";
                break;
            case CatFragment.EVENT_RECONNECT:
                return_text = "EVENT_RECONNECT";
                break;
            case CatFragment.EVENT_DISCONNECT:
                return_text = "EVENT_DISCONNECT";
                break;

            default:
                return_text = String.format("%d", eventType);
                break;
        }
        return return_text;
    }
}
