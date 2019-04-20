package me.gommeantilegit.minecraft.utils;

import org.jetbrains.annotations.Nullable;

public class StringUtil {

    /**
     * @param string the given string parameter
     * @return true if the given string is null or empty
     */
    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }

}
