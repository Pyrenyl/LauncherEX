package com.android.wm.shell;

/** LauncherEX: defaults frozen from GrapheneOS 2026072900. */
public final class Flags {
    private Flags() {}

    public static boolean enable2x1Split() {
        return false;
    }

    public static boolean enableBubbleBar() {
        return true;
    }

    public static boolean enableBubbleBarOnPhones() {
        return false;
    }

    public static boolean enableGsf() {
        return true;
    }

    public static boolean enableTinyTaskbar() {
        return false;
    }
}
