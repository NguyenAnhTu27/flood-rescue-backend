package com.floodrescue.shared.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class CodeGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String PREFIX = "RR";

    public static String generateRescueRequestCode() {
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("%s%s%04d", PREFIX, datePart, randomPart);
    }
}
