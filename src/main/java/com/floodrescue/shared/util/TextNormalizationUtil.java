package com.floodrescue.shared.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

public final class TextNormalizationUtil {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
            Pattern.compile("\u00C3."),
            Pattern.compile("\u00C4."),
            Pattern.compile("\u00C5."),
            Pattern.compile("\u00C6."),
            Pattern.compile("\u00E1\u00BA."),
            Pattern.compile("\u00E1\u00BB."),
            Pattern.compile("\u00E1\u00BC."),
            Pattern.compile("\u00E2\u20AC."),
            Pattern.compile("[\\u0080-\\u009F]"),
            Pattern.compile("\uFFFD")
    );

    private TextNormalizationUtil() {
    }

    public static String cleanDisplayText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        if (!isLikelyMojibake(value)) {
            return value;
        }

        return pickBestRepair(value, 3);
    }

    public static boolean isLikelyMojibake(String value) {
        return value != null && suspiciousScore(value) > 0;
    }

    private static boolean looksLikeMojibake(String value) {
        return isLikelyMojibake(value);
    }

    private static String pickBestRepair(String value, int remainingSteps) {
        if (remainingSteps <= 0 || !looksLikeMojibake(value)) {
            return value;
        }

        int currentScore = suspiciousScore(value);
        String bestValue = value;
        int bestScore = currentScore;

        for (String decoded : List.of(
                tryDecode(value, WINDOWS_1252),
                tryDecode(value, StandardCharsets.ISO_8859_1),
                tryDecodeLowByte(value)
        )) {
            if (decoded.equals(value) || decoded.contains("\uFFFD")) {
                continue;
            }

            String candidate = pickBestRepair(decoded, remainingSteps - 1);
            int candidateScore = suspiciousScore(candidate);
            if (candidateScore < bestScore) {
                bestValue = candidate;
                bestScore = candidateScore;
            }
        }

        return bestValue;
    }

    private static int suspiciousScore(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }

        int score = 0;
        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            score += pattern.matcher(value).results().count();
        }
        return score;
    }

    private static String tryDecode(String value, Charset sourceCharset) {
        try {
            return new String(value.getBytes(sourceCharset), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return value;
        }
    }

    private static String tryDecodeLowByte(String value) {
        try {
            byte[] bytes = new byte[value.length()];
            for (int i = 0; i < value.length(); i++) {
                char current = value.charAt(i);
                if (current > 0xFF) {
                    return value;
                }
                bytes[i] = (byte) current;
            }
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return value;
        }
    }
}
