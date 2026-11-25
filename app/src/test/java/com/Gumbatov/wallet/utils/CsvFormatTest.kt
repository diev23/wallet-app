package com.Gumbatov.wallet.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvFormatTest {

    @Test
    fun amount_isWrittenWithDecimalComma() {
        assertEquals("1234,50", CsvFormat.amount(123450))
    }

    @Test
    fun amount_smallValueKeepsLeadingZero() {
        assertEquals("0,05", CsvFormat.amount(5))
    }

    @Test
    fun escape_keepsPlainText() {
        assertEquals("Кофе", CsvFormat.escape("Кофе"))
    }

    @Test
    fun escape_wrapsTextWithSeparatorInQuotes() {
        assertEquals("\"a;b\"", CsvFormat.escape("a;b"))
    }

    @Test
    fun escape_doublesQuotesInsideText() {
        assertEquals("\"say \"\"hi\"\"\"", CsvFormat.escape("say \"hi\""))
    }

    @Test
    fun escape_blocksFormulas() {
        assertEquals("'=1+1", CsvFormat.escape("=1+1"))
    }

    @Test
    fun line_joinsCellsWithSemicolon() {
        assertEquals("a;b\r\n", CsvFormat.line(listOf("a", "b")))
    }
}