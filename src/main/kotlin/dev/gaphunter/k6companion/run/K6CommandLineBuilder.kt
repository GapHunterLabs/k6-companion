package dev.gaphunter.k6companion.run

import com.intellij.execution.configurations.GeneralCommandLine

/**
 * Pure command-line construction, separated from [K6CommandLineState] so
 * it's testable with a fake macro expander -- no real k6 binary, no
 * `PathMacroManager`/`Project`, no platform test fixture required (see
 * K6CommandLineBuilderTest, which mocks a binary path and confirms macro
 * expansion happens before the command line is built).
 *
 * [expandPath] is always applied to both the script path and the
 * executable path before they reach [GeneralCommandLine] -- this is the
 * direct fix for the cited competitor complaint: "it doesn't respect
 * Macros (despite allowing macros to be used)... configuration errors
 * saying the file does not exist." The platform does not expand path
 * macros automatically for a plain string field on a custom
 * RunConfiguration; skipping this step is exactly how that bug happens.
 */
object K6CommandLineBuilder {
    fun build(
        scriptPath: String,
        extraArgs: String,
        executablePath: String,
        workDirectory: String?,
        expandPath: (String) -> String,
    ): GeneralCommandLine {
        val expandedExecutable = expandPath(executablePath.ifBlank { "k6" })
        val expandedScriptPath = expandPath(scriptPath)

        val commandLine = GeneralCommandLine()
        commandLine.setExePath(expandedExecutable)

        val params = mutableListOf("run")
        params.addAll(splitArgs(extraArgs))
        params.add(expandedScriptPath)
        commandLine.addParameters(*params.toTypedArray())

        if (!workDirectory.isNullOrBlank()) {
            commandLine.setWorkDirectory(workDirectory)
        }
        return commandLine
    }

    fun splitArgs(raw: String): List<String> =
        raw.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
}
