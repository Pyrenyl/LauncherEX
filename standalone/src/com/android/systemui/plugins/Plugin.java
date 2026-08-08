/*
 * Copyright (C) 2025 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.systemui.plugins;

import android.content.Context;

/** LauncherEX: compile-time contract only; third-party builds do not load SystemUI plugins. */
public interface Plugin {
    default int getVersion() {
        return -1;
    }

    default void onCreate(Context hostContext, Context pluginContext) {}

    default void onDestroy() {}
}
