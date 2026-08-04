package dev.gaphunter.k6companion.run

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue

/**
 * The k6 Run Configuration type/factory (combined via [SimpleConfigurationType],
 * since this plugin only ever needs one factory). First use of a real
 * `RunConfigurationType` in this workspace's catalog -- see
 * AUTOMATION_PLAYBOOK.md's note on this plugin being new SDK surface.
 */
class K6ConfigurationType : SimpleConfigurationType(
    "K6RunConfiguration",
    "k6",
    "Run a k6 load test script",
    NotNullLazyValue.createConstantValue(AllIcons.Actions.Execute),
) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        K6RunConfiguration(project, this, "k6")
}
