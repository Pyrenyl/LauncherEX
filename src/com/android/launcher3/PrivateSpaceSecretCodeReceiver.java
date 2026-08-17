/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.launcher3;

import static android.content.Context.RECEIVER_EXPORTED;
import static com.android.launcher3.LauncherPrefs.PRIVATE_SPACE_SECRET_CODE;
import static com.android.launcher3.LauncherPrefs.PRIVATE_SPACE_SECRET_CODE_MAX_LENGTH;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.launcher3.pm.UserCache;

/** Toggles Private Space when the LauncherEX dialer code is entered. */
public final class PrivateSpaceSecretCodeReceiver extends BroadcastReceiver
        implements LauncherPrefChangeListener {

    private static final String TAG = "PrivateSpaceCode";

    private final Context mContext;
    private boolean mRegistered;

    PrivateSpaceSecretCodeReceiver(Context context) {
        mContext = context.getApplicationContext();
    }

    void start() {
        LauncherPrefs prefs = LauncherPrefs.get(mContext);
        String code = prefs.get(PRIVATE_SPACE_SECRET_CODE);
        String normalizedCode = normalizeSecretCode(code);
        if (!normalizedCode.equals(code)) {
            prefs.put(PRIVATE_SPACE_SECRET_CODE, normalizedCode);
        }
        prefs.addListener(this, PRIVATE_SPACE_SECRET_CODE);
        updateRegistration();
    }

    @Override
    public void onPrefChanged(String key) {
        if (PRIVATE_SPACE_SECRET_CODE.getSharedPrefKey().equals(key)) {
            updateRegistration();
        }
    }

    private void updateRegistration() {
        if (mRegistered) {
            mContext.unregisterReceiver(this);
            mRegistered = false;
        }

        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_SECRET_CODE);
        filter.addDataScheme("android_secret_code");
        filter.addDataAuthority(getSecretCode(), null);
        mContext.registerReceiver(this, filter, RECEIVER_EXPORTED);
        mRegistered = true;
    }

    private String getSecretCode() {
        return normalizeSecretCode(LauncherPrefs.get(mContext).get(PRIVATE_SPACE_SECRET_CODE));
    }

    private static String normalizeSecretCode(String code) {
        if (TextUtils.isEmpty(code) || !TextUtils.isDigitsOnly(code)) {
            return PRIVATE_SPACE_SECRET_CODE.getDefaultValue();
        }
        return code.substring(0, Math.min(code.length(), PRIVATE_SPACE_SECRET_CODE_MAX_LENGTH));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri data = intent.getData();
        if (!TelephonyManager.ACTION_SECRET_CODE.equals(intent.getAction())
                || data == null
                || !getSecretCode().equals(data.getHost())) {
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
