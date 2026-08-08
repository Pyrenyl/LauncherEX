/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.launcher3;

import android.app.Activity;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import android.util.Log;

import com.android.launcher3.pm.UserCache;

/** Locks Private Space through Android's secure-camera lock-screen shortcut. */
public final class PrivateSpaceLockActivity extends Activity {

    private static final String TAG = "PrivateSpaceLock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // LauncherEX: reject every launch path except the lock-screen secure-camera action.
        if (!MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(getIntent().getAction())) {
            finish();
            return;
        }

        UserManager userManager = getSystemService(UserManager.class);
        // LauncherEX: only the system user's launcher owns the shared private profile.
        if (userManager == null || !userManager.isSystemUser()) {
            finish();
            return;
        }

        UserCache userCache = UserCache.getInstance(this);
        UserHandle privateUser = userCache.getUserProfiles().stream()
                .filter(user -> userCache.getUserInfo(user).isPrivate())
                .findFirst()
                .orElse(null);

        if (privateUser != null && !userManager.isQuietModeEnabled(privateUser)) {
            try {
                // LauncherEX: this entry point is deliberately lock-only and never unlocks.
                userManager.requestQuietModeEnabled(true, privateUser);
            } catch (SecurityException e) {
                // LauncherEX: Android grants this operation only to the active HOME app.
                Log.w(TAG, "LauncherEX is not allowed to lock Private Space", e);
            }
        }
        finish();
    }
}
