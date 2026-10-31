package com.library.model;

import java.util.Locale;

public final class Keys {

    private Keys() {
    }

    public static String of(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
