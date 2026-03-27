package com.formgenerator.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateFormatUtil {

    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateFormatUtil() {}

    public static String formatDdMmYyyy(LocalDate date) {
        return date == null ? "" : date.format(DD_MM_YYYY);
    }

    public static String twoDigitPad(int value) {
        return String.format("%02d", value);
    }

    public static String fourDigit(int value) {
        return String.format("%04d", value);
    }
}
