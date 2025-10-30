package com.ugwueze.expenses_tracker.util;

public final class CsvUtils {

    private CsvUtils() {
    }

    private static final char QUOTE = '"';
    private static final char COMMA = ',';
    private static final String NEWLINE = "\n";

    public static String escapeCsvField(String input) {
        if (input == null) {
            return "";
        }
        String value = input;
        boolean containsSpecial = value.indexOf(QUOTE) >= 0 || value.indexOf(COMMA) >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;

        if (value.indexOf(QUOTE) >= 0) {
            value = value.replace("\"", "\"\"");
            containsSpecial = true;
        }

        if (containsSpecial) {
            StringBuilder sb = new StringBuilder();
            sb.append(QUOTE).append(value).append(QUOTE);
            return sb.toString();
        } else {
            return value;
        }
    }

    public static String joinCsvRow(String... columns) {
        if (columns == null || columns.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) sb.append(COMMA);
            sb.append(escapeCsvField(columns[i]));
        }
        return sb.toString();
    }
}