package dev.gaphunter.k6companion.threshold

data class K6Threshold(val metric: String, val arg: String?, val operator: String, val value: Double, val raw: String)

/**
 * Hand-rolled parser for k6's threshold expression mini-syntax --
 * `metric_name[(arg)]<operator><number>`, e.g. `http_req_duration`'s
 * `p(95)<500`, or a bare `rate<0.01`. Used by the summary tool window to
 * render a human-readable description of each threshold's pass/fail
 * result, e.g. "http_req_duration p(95) < 500". Small and stable enough
 * to hand-roll rather than add a dependency, same call as elsewhere in
 * this workspace.
 */
object K6ThresholdParser {
    private val PATTERN =
        Regex("""^([A-Za-z_][A-Za-z0-9_.]*)(\(([^)]*)\))?\s*(<=|>=|===|==|!=|<|>)\s*(-?\d+(?:\.\d+)?)$""")

    fun parse(raw: String): K6Threshold? {
        val trimmed = raw.trim()
        val match = PATTERN.matchEntire(trimmed) ?: return null
        val groups = match.groupValues
        val metric = groups[1]
        val arg = groups[3].ifEmpty { null }
        val operator = groups[4]
        val value = groups[5].toDoubleOrNull() ?: return null
        return K6Threshold(metric, arg, operator, value, trimmed)
    }
}
