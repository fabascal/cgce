package com.epson.epos2easyselect_parse.common;

import android.content.Context;
import android.content.Intent;

// ------------------------------------------------------------------------------------------------
public interface BroadcastCallback {
    // --------------------------------------------------------------------------------------------

    /**
     * Broadcast Callback
     *
     * @param context
     * @param intent
     */
    public void broadcastCallback(
            Context context,
            Intent intent);
}
