package dev.gaphunter.k6companion.toolwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import dev.gaphunter.k6companion.summary.K6SummaryJsonParseException
import dev.gaphunter.k6companion.summary.K6SummaryReader
import dev.gaphunter.k6companion.summary.MinimalJsonParser
import dev.gaphunter.k6companion.threshold.K6ThresholdParser
import java.awt.BorderLayout
import java.io.IOException
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

/**
 * Reads a k6 `--summary-export` JSON file the user already generated
 * locally (`k6 run --summary-export summary.json script.js`) and lists
 * each threshold's pass/fail result. Parsing runs off the EDT
 * (executeOnPooledThread) so a large summary file never freezes the UI.
 */
class K6SummaryToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = K6SummaryPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

private class K6SummaryPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val outputArea = JTextArea().apply { isEditable = false }
    private val openButton = JButton("Open k6 --summary-export JSON...")

    init {
        border = JBUI.Borders.empty(8)
        openButton.addActionListener { openAndParse() }
        add(openButton, BorderLayout.NORTH)
        add(JScrollPane(outputArea), BorderLayout.CENTER)
    }

    private fun openAndParse() {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
        val file = FileChooser.chooseFile(descriptor, project, null) ?: return
        outputArea.text = "Loading..."
        ApplicationManager.getApplication().executeOnPooledThread {
            val text = try {
                String(file.contentsToByteArray(), Charsets.UTF_8)
            } catch (e: IOException) {
                showResult("Failed to read file: ${e.message}")
                return@executeOnPooledThread
            }
            val rendered = try {
                val root = MinimalJsonParser.parse(text)
                val outcomes = K6SummaryReader.extractThresholds(root)
                renderOutcomes(outcomes)
            } catch (e: K6SummaryJsonParseException) {
                "Failed to parse summary export: ${e.message}"
            }
            showResult(rendered)
        }
    }

    private fun renderOutcomes(outcomes: List<dev.gaphunter.k6companion.summary.ThresholdOutcome>): String {
        if (outcomes.isEmpty()) return "No thresholds found in this summary export."
        return outcomes.joinToString("\n") { outcome ->
            val label = if (outcome.passed) "PASS" else "FAIL"
            val parsed = K6ThresholdParser.parse(outcome.expression)
            val description = parsed?.let { " (${it.metric}${it.arg?.let { a -> "($a)" } ?: ""} ${it.operator} ${it.value})" }.orEmpty()
            "[$label] ${outcome.metric}: ${outcome.expression}$description"
        }
    }

    private fun showResult(text: String) {
        ApplicationManager.getApplication().invokeLater {
            outputArea.text = text
        }
    }
}
