package dev.gaphunter.k6companion.threshold

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class K6ThresholdParserTest {
    @Test
    fun `parses a percentile threshold with an argument`() {
        val result = K6ThresholdParser.parse("p(95)<500")
        assertEquals(K6Threshold("p", "95", "<", 500.0, "p(95)<500"), result)
    }

    @Test
    fun `parses a bare metric threshold without an argument`() {
        val result = K6ThresholdParser.parse("rate<0.01")
        assertEquals(K6Threshold("rate", null, "<", 0.01, "rate<0.01"), result)
    }

    @Test
    fun `parses greater-than-or-equal and negative values`() {
        val result = K6ThresholdParser.parse("count>=-5")
        assertEquals(K6Threshold("count", null, ">=", -5.0, "count>=-5"), result)
    }

    @Test
    fun `trims surrounding whitespace`() {
        val result = K6ThresholdParser.parse("  avg < 200  ")
        assertEquals(K6Threshold("avg", null, "<", 200.0, "avg < 200"), result)
    }

    @Test
    fun `returns null for unparseable text`() {
        assertNull(K6ThresholdParser.parse("not a threshold"))
        assertNull(K6ThresholdParser.parse(""))
    }
}
