/*
 * Copyright (C) 2025 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.systemui.plugins;

import android.content.Context;

/** LauncherEX: listener surface retained for source compatibility; registration is a no-op. */
public interface PluginListener<T extends Plugin> {
    default void onPluginConnected(T plugin, Context pluginContext) {}

    default void onPluginDisconnected(T plugin) {}

    default void onPluginLoaded(
            T plugin, Context pluginContext, PluginLifecycleManager<T> manager) {
        onPluginConnected(plugin, pluginContext);
    }

    default void onPluginUnloaded(T plugin, PluginLifecycleManager<T> manager) {
        onPluginDisconnected(plugin);
    }
}
