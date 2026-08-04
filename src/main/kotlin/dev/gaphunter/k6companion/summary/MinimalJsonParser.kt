package dev.gaphunter.k6companion.summary

sealed class JsonNode {
    object Null : JsonNode()
    data class Bool(val value: Boolean) : JsonNode()
    data class Num(val value: Double) : JsonNode()
    data class Str(val value: String) : JsonNode()
    data class Arr(val items: List<JsonNode>) : JsonNode()
    data class Obj(val entries: LinkedHashMap<String, JsonNode>) : JsonNode()
}

class K6SummaryJsonParseException(message: String) : Exception(message)

/**
 * Minimal, hand-rolled JSON reader scoped to what this plugin needs
 * (reading a k6 `--summary-export` file locally, never over the
 * network) -- not a general-purpose converter. Small, stable grammar,
 * same "hand-roll over new dependency" call as elsewhere in this
 * workspace.
 */
object MinimalJsonParser {
    fun parse(text: String): JsonNode {
        val parser = Parser(text)
        val value = parser.parseValue()
        parser.skipWhitespace()
        if (!parser.atEnd()) throw K6SummaryJsonParseException("Unexpected trailing content")
        return value
    }

    private class Parser(private val text: String) {
        var pos = 0

        fun atEnd() = pos >= text.length
        fun peek(): Char = text[pos]
        fun skipWhitespace() {
            while (pos < text.length && text[pos].isWhitespace()) pos++
        }

        fun parseValue(): JsonNode {
            skipWhitespace()
            if (atEnd()) throw K6SummaryJsonParseException("Unexpected end of input")
            return when (peek()) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> JsonNode.Str(parseString())
                't' -> parseLiteral("true", JsonNode.Bool(true))
                'f' -> parseLiteral("false", JsonNode.Bool(false))
                'n' -> parseLiteral("null", JsonNode.Null)
                else -> parseNumber()
            }
        }

        fun parseLiteral(literal: String, value: JsonNode): JsonNode {
            if (pos + literal.length > text.length || text.substring(pos, pos + literal.length) != literal) {
                throw K6SummaryJsonParseException("Invalid literal at $pos")
            }
            pos += literal.length
            return value
        }

        fun parseObject(): JsonNode.Obj {
            pos++
            val entries = LinkedHashMap<String, JsonNode>()
            skipWhitespace()
            if (!atEnd() && peek() == '}') {
                pos++
                return JsonNode.Obj(entries)
            }
            while (true) {
                skipWhitespace()
                if (atEnd() || peek() != '"') throw K6SummaryJsonParseException("Expected string key at $pos")
                val key = parseString()
                skipWhitespace()
                if (atEnd() || peek() != ':') throw K6SummaryJsonParseException("Expected ':' at $pos")
                pos++
                entries[key] = parseValue()
                skipWhitespace()
                if (atEnd()) throw K6SummaryJsonParseException("Unterminated object")
                when (peek()) {
                    ',' -> pos++
                    '}' -> {
                        pos++
                        return JsonNode.Obj(entries)
                    }
                    else -> throw K6SummaryJsonParseException("Expected ',' or '}' at $pos")
                }
            }
        }

        fun parseArray(): JsonNode.Arr {
            pos++
            val items = mutableListOf<JsonNode>()
            skipWhitespace()
            if (!atEnd() && peek() == ']') {
                pos++
                return JsonNode.Arr(items)
            }
            while (true) {
                items.add(parseValue())
                skipWhitespace()
                if (atEnd()) throw K6SummaryJsonParseException("Unterminated array")
                when (peek()) {
                    ',' -> pos++
                    ']' -> {
                        pos++
                        return JsonNode.Arr(items)
                    }
                    else -> throw K6SummaryJsonParseException("Expected ',' or ']' at $pos")
                }
            }
        }

        fun parseString(): String {
            pos++
            val sb = StringBuilder()
            while (true) {
                if (atEnd()) throw K6SummaryJsonParseException("Unterminated string")
                val c = text[pos]
                pos++
                when {
                    c == '"' -> return sb.toString()
                    c == '\\' -> {
                        if (atEnd()) throw K6SummaryJsonParseException("Unterminated escape")
                        val escaped = text[pos]
                        pos++
                        when (escaped) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'n' -> sb.append('\n')
                            't' -> sb.append('\t')
                            'r' -> sb.append('\r')
                            'b' -> sb.append('\b')
                            'u' -> {
                                if (pos + 4 > text.length) throw K6SummaryJsonParseException("Invalid unicode escape")
                                val code = text.substring(pos, pos + 4).toIntOrNull(16)
                                    ?: throw K6SummaryJsonParseException("Invalid unicode escape")
                                pos += 4
                                sb.append(code.toChar())
                            }
                            else -> throw K6SummaryJsonParseException("Invalid escape '\\$escaped'")
                        }
                    }
                    else -> sb.append(c)
                }
            }
        }

        fun parseNumber(): JsonNode.Num {
            val start = pos
            if (!atEnd() && peek() == '-') pos++
            while (!atEnd() && peek().isDigit()) pos++
            if (!atEnd() && peek() == '.') {
                pos++
                while (!atEnd() && peek().isDigit()) pos++
            }
            if (!atEnd() && (peek() == 'e' || peek() == 'E')) {
                pos++
                if (!atEnd() && (peek() == '+' || peek() == '-')) pos++
                while (!atEnd() && peek().isDigit()) pos++
            }
            if (pos == start) throw K6SummaryJsonParseException("Invalid number at $pos")
            val numberText = text.substring(start, pos)
            return JsonNode.Num(
                numberText.toDoubleOrNull() ?: throw K6SummaryJsonParseException("Invalid number '$numberText'")
            )
        }
    }
}
