package dev.gaphunter.k6companion.summary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fixture JSON matches the real k6 `--summary-export` shape: a top-level
 * `metrics` object, each entry optionally holding a `thresholds` object
 * mapping the threshold expression to `{"ok": bool}`. Confirms parsing a
 * genuinely invalid/empty service account-style edge case (missing
 * `metrics` entirely) never throws -- same "never crash on malformed
 * input" bar as the other hand-rolled parsers in this workspace.
 */
class K6SummaryReaderTest {
    private val realisticSummary = """
        {
          "metrics": {
            "http_req_duration": {
              "thresholds": {
                "p(95)<500": { "ok": true },
                "avg<200": { "ok": false }
              },
              "values": { "avg": 250.5, "p(95)": 480.2 }
            },
            "http_reqs": {
              "values": { "count": 100, "rate": 10.5 }
            }
          }
        }
    """.trimIndent()

    @Test
    fun `extracts pass and fail thresholds from a realistic summary export`() {
        val root = MinimalJsonParser.parse(realisticSummary)
        val outcomes = K6SummaryReader.extractThresholds(root)
        assertEquals(2, outcomes.size)
        val passing = outcomes.first { it.expression == "p(95)<500" }
        assertTrue(passing.passed)
        assertEquals("http_req_duration", passing.metric)
        val failing = outcomes.first { it.expression == "avg<200" }
        assertFalse(failing.passed)
    }

    @Test
    fun `metrics without thresholds contribute nothing`() {
        val root = MinimalJsonParser.parse(realisticSummary)
        val outcomes = K6SummaryReader.extractThresholds(root)
        assertTrue(outcomes.none { it.metric == "http_reqs" })
    }

    @Test
    fun `a document with no metrics key returns an empty list, not a crash`() {
        val root = MinimalJsonParser.parse("""{"unrelated": true}""")
        assertTrue(K6SummaryReader.extractThresholds(root).isEmpty())
    }

    @Test(expected = K6SummaryJsonParseException::class)
    fun `malformed json throws a clear parse exception`() {
        MinimalJsonParser.parse("{not valid json")
    }

    @Test
    fun `handles non-ascii metric names correctly`() {
        val json = """{"metrics": {"café_latency": {"thresholds": {"p(95)<500": {"ok": true}}}}}"""
        val outcomes = K6SummaryReader.extractThresholds(MinimalJsonParser.parse(json))
        assertEquals("café_latency", outcomes.single().metric)
    }
}
