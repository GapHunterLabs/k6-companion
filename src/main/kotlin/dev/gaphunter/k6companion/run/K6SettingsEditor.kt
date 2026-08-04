package dev.gaphunter.k6companion.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

class K6SettingsEditor : SettingsEditor<K6RunConfiguration>() {
    private val scriptPathField = JBTextField()
    private val extraArgsField = JBTextField()
    private val executableField = JBTextField()

    override fun resetEditorFrom(configuration: K6RunConfiguration) {
        scriptPathField.text = configuration.scriptPath
        extraArgsField.text = configuration.extraArgs
        executableField.text = configuration.k6ExecutablePath
    }

    override fun applyEditorTo(configuration: K6RunConfiguration) {
        configuration.scriptPath = scriptPathField.text
        configuration.extraArgs = extraArgsField.text
        configuration.k6ExecutablePath = executableField.text
    }

    override fun createEditor(): JComponent =
        FormBuilder.createFormBuilder()
            .addLabeledComponent("k6 script path:", scriptPathField)
            .addLabeledComponent("Extra CLI arguments:", extraArgsField)
            .addLabeledComponent("k6 executable:", executableField)
            .panel
}
