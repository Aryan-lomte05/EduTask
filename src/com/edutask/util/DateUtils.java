package com.edutask.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public static String formatForDisplay(LocalDate date) {
        return date.format(DISPLAY_FORMAT);
    }

    public static String getDueLabel(LocalDate due) {
        LocalDate today = LocalDate.now();
        if (due.equals(today)) return "Today";
        if (due.equals(today.plusDays(1))) return "Tomorrow";
        if (due.isBefore(today)) return "Overdue";
        return formatForDisplay(due);
    }
}
