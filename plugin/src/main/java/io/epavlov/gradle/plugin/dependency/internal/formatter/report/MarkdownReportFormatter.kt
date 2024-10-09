package io.epavlov.gradle.plugin.dependency.internal.formatter.report

import io.epavlov.gradle.plugin.dependency.internal.OUTPUT_PATH
import io.epavlov.gradle.plugin.dependency.internal.STATUS_ERROR
import io.epavlov.gradle.plugin.dependency.internal.STATUS_OK
import io.epavlov.gradle.plugin.dependency.internal.STATUS_WARN
import org.gradle.api.Project
import java.io.File

internal class MarkdownReportFormatter(
    private val project: Project
) : ReportFormatter {


    override fun format(outdated: Set<OutdatedDependency>): File {
        val file = createFile()
        file.writeText("## Dependency Report\n")
        if (outdated.isEmpty()) {
            file.appendText("$STATUS_OK Everything is up-to-date")
            return file
        }

        file.appendText(
            """
                | Name | Status | Version | Latest version |
                | ---- | ------ | ------- | -------------- |
        """.trimIndent()
        )
        val sorted = outdated.sortedBy { getVersionStatus(it).ordinal }
        sorted.forEach { dep ->
            file.appendText("\n| ${dep.group}:${dep.module} | ${getVersionStatus(dep).text} | ${dep.versions.current} | ${dep.versions.latest} |")
        }

        return file
    }

    private fun getVersionStatus(outdatedDependency: OutdatedDependency): Status {
        val versions = outdatedDependency.versions
        val currentMajor = versions.current.split(".").first().toIntOrNull() ?: 0
        val latestMajor = versions.latest.split(".").first().toIntOrNull() ?: 0
        return if (currentMajor < latestMajor) {
            Status.ERROR
        } else {
            Status.WARN
        }
    }

    private enum class Status(val text: String) {
        ERROR(STATUS_ERROR),
        WARN(STATUS_WARN)
    }

    private fun createFile(): File {
        val dir = File(project.buildDir, OUTPUT_PATH)
        if (dir.exists().not()) {
            dir.mkdirs()
        }
        val file = File(dir, "Report.md")
        file.createNewFile()
        return file
    }
}