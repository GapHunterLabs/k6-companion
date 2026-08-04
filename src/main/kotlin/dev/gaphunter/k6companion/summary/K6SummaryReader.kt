package dev.gaphunter.k6companion.summary

data class ThresholdOutcome(val metric: String, val expression: String, val passed: Boolean)

/**
 * Walks the real k6 `--summary-export` JSON shape: a top-level `metrics`
 * object, each entry optionally holding a `thresholds` object mapping the
 * threshold expression string to `{"ok": true|false}`.
 */
object K6SummaryReader {
    fun extractThresholds(root: JsonNode): List<ThresholdOutcome> {
        val metrics = (root as? JsonNode.Obj)?.entries?.get("metrics") as? JsonNode.Obj ?: return emptyList()
        val results = mutableListOf<ThresholdOutcome>()
        for ((metricName, metricValue) in metrics.entries) {
            val metricObj = metricValue as? JsonNode.Obj ?: continue
            val thresholds = metricObj.entries["thresholds"] as? JsonNode.Obj ?: continue
            for ((expression, outcome) in thresholds.entries) {
                val ok = ((outcome as? JsonNode.Obj)?.entries?.get("ok") as? JsonNode.Bool)?.value ?: false
                results.add(ThresholdOutcome(metricName, expression, ok))
            }
        }
        return results
    }
}
