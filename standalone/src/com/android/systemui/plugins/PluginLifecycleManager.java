/*
 * Copyright (C) 2025 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.systemui.plugins;

import android.content.ComponentName;

/** LauncherEX: only the component identity used by Launcher3's no-op plugin hooks is retained. */
public interface PluginLifecycleManager<T extends Plugin> {
    ComponentName getComponentName();
}
