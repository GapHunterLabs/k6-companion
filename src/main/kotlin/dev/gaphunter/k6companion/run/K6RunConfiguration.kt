package dev.gaphunter.k6companion.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element

/**
 * Holds the k6 script path, extra CLI arguments, and k6 executable path.
 * Persistence is plain JDOM attributes (not the newer BaseState/Options
 * delegate system) -- simpler to reason about for three string fields,
 * and `LocatableConfigurationBase<LocatableRunConfigurationOptions>`
 * (the base's own options type, untouched) keeps the base class's own
 * options-persistence machinery on its documented default path.
 */
class K6RunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    LocatableConfigurationBase<LocatableRunConfigurationOptions>(project, factory, name) {

    var scriptPath: String = ""
    var extraArgs: String = ""
    var k6ExecutablePath: String = "k6"

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = K6SettingsEditor()

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
        K6CommandLineState(environment, this)

    override fun checkConfiguration() {
        if (scriptPath.isBlank()) {
            throw RuntimeConfigurationException("k6 script path must be set")
        }
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        scriptPath = element.getAttributeValue(ATTR_SCRIPT_PATH) ?: ""
        extraArgs = element.getAttributeValue(ATTR_EXTRA_ARGS) ?: ""
        k6ExecutablePath = element.getAttributeValue(ATTR_EXECUTABLE) ?: "k6"
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.setAttribute(ATTR_SCRIPT_PATH, scriptPath)
        element.setAttribute(ATTR_EXTRA_ARGS, extraArgs)
        element.setAttribute(ATTR_EXECUTABLE, k6ExecutablePath)
    }

    companion object {
        private const val ATTR_SCRIPT_PATH = "k6ScriptPath"
        private const val ATTR_EXTRA_ARGS = "k6ExtraArgs"
        private const val ATTR_EXECUTABLE = "k6Executable"
    }
}
