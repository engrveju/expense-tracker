package com.ugwueze.expenses_tracker.utils;

import com.ugwueze.expenses_tracker.util.CsvUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvUtilsTest {

    @Test
    void escapeCsvField_nullReturnsEmpty() {
        assertEquals("", CsvUtils.escapeCsvField(null));
    }

    @Test
    void escapeCsvField_noSpecialCharsReturnsOriginal() {
        assertEquals("hello", CsvUtils.escapeCsvField("hello"));
    }

    @Test
    void escapeCsvField_containsComma_isQuoted() {
        assertEquals("\"a,b\"", CsvUtils.escapeCsvField("a,b"));
    }

    @Test
    void escapeCsvField_containsQuote_quotesAreDoubledAndQuoted() {
        assertEquals("\"a\"\"b\"", CsvUtils.escapeCsvField("a\"b"));
    }

    @Test
    void escapeCsvField_containsNewline_isQuoted() {
        assertEquals("\"line1\nline2\"", CsvUtils.escapeCsvField("line1\nline2"));
    }

    @Test
    void joinCsvRow_joinsAndEscapesEachColumn() {
        String row = CsvUtils.joinCsvRow("one", "two,three", "four\"five", null);
        assertEquals("one,\"two,three\",\"four\"\"five\",", row);
    }

    @Test
    void joinCsvRow_emptyOrNullColumns() {
        assertEquals("", CsvUtils.joinCsvRow());
        assertEquals("", CsvUtils.joinCsvRow((String[]) null));
    }
}
