package com.epson.epos2easyselect_parse.common.bluetooth;

public enum IntentListBluetooth {
    INTENT_BLUETOOTH_STATE_CHANGE("android.bluetooth.adapter.action.STATE_CHANGED");

    private String _action;

    private IntentListBluetooth(
            String action) {
        _action = action;
    }

    public String getAction() {
        return _action;
    }
}
