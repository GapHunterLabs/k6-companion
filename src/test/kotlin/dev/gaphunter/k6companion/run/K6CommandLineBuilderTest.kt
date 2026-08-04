package dev.gaphunter.k6companion.run

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Confirms path-macro expansion happens for both the script path and the
 * k6 executable path, using a fake macro expander -- no real k6 binary
 * needed, no Project/PathMacroManager, so this runs in any CI/verification
 * environment regardless of whether k6 is actually installed. Direct
 * regression test for the cited competitor complaint: "it doesn't respect
 * Macros (despite allowing macros to be used)... configuration errors
 * saying the file does not exist."
 */
class K6CommandLineBuilderTest {
    private fun fakeExpand(macros: Map<String, String>): (String) -> String = { raw ->
        var result = raw
        for ((macro, value) in macros) result = result.replace(macro, value)
        result
    }

    @Test
    fun `expands macros in both script path and executable path`() {
        val expand = fakeExpand(mapOf("\$PROJECT_DIR\$" to "/home/dev/project"))
        val commandLine = K6CommandLineBuilder.build(
            scriptPath = "\$PROJECT_DIR\$/scripts/load-test.js",
            extraArgs = "",
            executablePath = "\$PROJECT_DIR\$/bin/k6",
            workDirectory = null,
            expandPath = expand,
        )
        assertEquals("/home/dev/project/bin/k6", commandLine.exePath)
        assertTrue(commandLine.parametersList.parameters.contains("/home/dev/project/scripts/load-test.js"))
    }

    @Test
    fun `defaults executable to k6 when blank`() {
        val commandLine = K6CommandLineBuilder.build(
            scriptPath = "script.js",
            extraArgs = "",
            executablePath = "",
            workDirectory = null,
            expandPath = { it },
        )
        assertEquals("k6", commandLine.exePath)
    }

    @Test
    fun `always passes run as the first argument`() {
        val commandLine = K6CommandLineBuilder.build(
            scriptPath = "script.js",
            extraArgs = "",
            executablePath = "k6",
            workDirectory = null,
            expandPath = { it },
        )
        assertEquals("run", commandLine.parametersList.parameters.first())
    }

    @Test
    fun `splits extra args on whitespace and appends before the script path`() {
        val commandLine = K6CommandLineBuilder.build(
            scriptPath = "script.js",
            extraArgs = "--vus 10 --duration 30s",
            executablePath = "k6",
            workDirectory = null,
            expandPath = { it },
        )
        assertEquals(
            listOf("run", "--vus", "10", "--duration", "30s", "script.js"),
            commandLine.parametersList.parameters,
        )
    }

    @Test
    fun `sets working directory when provided`() {
        val commandLine = K6CommandLineBuilder.build(
            scriptPath = "script.js",
            extraArgs = "",
            executablePath = "k6",
            workDirectory = "/home/dev/project",
            expandPath = { it },
        )
        assertEquals("/home/dev/project", commandLine.workDirectory?.path?.replace('\\', '/'))
    }
}
