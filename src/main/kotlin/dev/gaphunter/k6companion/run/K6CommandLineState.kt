package dev.gaphunter.k6companion.run

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.PathMacroManager

/**
 * Extends the platform's own [CommandLineState] -- its default `execute()`
 * builds the console via the platform's native `ConsoleView`
 * (`TextConsoleBuilder`), not a hand-rolled terminal emulator. This is the
 * direct fix for the cited competitor complaint about the "animated
 * progress bar" in k6's terminal output being broken for years: rendering
 * process output is the platform's job here, not this plugin's.
 */
class K6CommandLineState(environment: ExecutionEnvironment, private val configuration: K6RunConfiguration) :
    CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val project = configuration.project
        val macroManager = PathMacroManager.getInstance(project)

        val commandLine = K6CommandLineBuilder.build(
            scriptPath = configuration.scriptPath,
            extraArgs = configuration.extraArgs,
            executablePath = configuration.k6ExecutablePath,
            workDirectory = project.basePath,
            expandPath = macroManager::expandPath,
        )

        val processHandler = OSProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }
}
