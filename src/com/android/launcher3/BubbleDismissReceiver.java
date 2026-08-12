package com.android.launcher3;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutManager;

import java.util.List;

/** Removes the notification and conversation shortcut after a bubble is dismissed. */
public final class BubbleDismissReceiver extends BroadcastReceiver {

    static final String EXTRA_SHORTCUT_ID = "shortcut_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String shortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID);
        if (shortcutId == null || !shortcutId.startsWith("bubble:")) return;

        context.getSystemService(NotificationManager.class).cancel(shortcutId, 0);
        context.getSystemService(ShortcutManager.class)
                .removeLongLivedShortcuts(List.of(shortcutId));
    }
}
