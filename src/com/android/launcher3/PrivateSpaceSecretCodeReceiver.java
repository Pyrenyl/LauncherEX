/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.launcher3.pm.UserCache;

/** Toggles Private Space when the LauncherEX dialer code is entered. */
public final class PrivateSpaceSecretCodeReceiver extends BroadcastReceiver {

    private static final String TAG = "PrivateSpaceCode";
    private static final String SECRET_CODE = "3825";

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri data = intent.getData();
        if (!TelephonyManager.ACTION_SECRET_CODE.equals(intent.getAction())
                || data == null
                || !SECRET_CODE.equals(data.getHost())) {
            return;
        }

        UserManager userManager = context.getSystemService(UserManager.class);
        // LauncherEX: the telephony service broadcasts to every user; only the system user's
        // receiver may toggle the shared private profile.
        if (userManager == null || !userManager.isSystemUser()) {
            return;
        }

        UserCache userCache = UserCache.getInstance(context);
        UserHandle privateUser = userCache.getUserProfiles().stream()
                .filter(user -> userCache.getUserInfo(user).isPrivate())
                .findFirst()
                .orElse(null);
        if (privateUser == null) {
            return;
        }

        try {
            userManager.requestQuietModeEnabled(
                    !userManager.isQuietModeEnabled(privateUser), privateUser);
        } catch (SecurityException e) {
            // LauncherEX: only the active HOME app may change a private profile's quiet mode.
            Log.w(TAG, "LauncherEX is not allowed to toggle Private Space", e);
        }
    }
}
