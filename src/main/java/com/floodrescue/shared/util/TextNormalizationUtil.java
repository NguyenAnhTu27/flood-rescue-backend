package com.floodrescue.shared.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class TextNormalizationUtil {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final Pattern MOJIBAKE_PATTERN = Pattern.compile("[ÃÂÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßâ€œâ€â€˜â€™â€¢â€¦]");

    private TextNormalizationUtil() {
    }

    public static String cleanDisplayText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String current = value;
        for (int i = 0; i < 3; i++) {
            if (!looksLikeMojibake(current)) {
                break;
            }

            String repaired = tryDecode(current, WINDOWS_1252);
            if (repaired.equals(current) || repaired.contains("\uFFFD")) {
                repaired = tryDecode(current, StandardCharsets.ISO_8859_1);
            }
            if (repaired.equals(current) || repaired.contains("\uFFFD")) {
                break;
            }
            current = repaired;
        }

        return current;
    }

    private static boolean looksLikeMojibake(String value) {
        return value != null && MOJIBAKE_PATTERN.matcher(value).find();
    }

    private static String tryDecode(String value, Charset sourceCharset) {
        try {
            return new String(value.getBytes(sourceCharset), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return value;
        }
    }
}
