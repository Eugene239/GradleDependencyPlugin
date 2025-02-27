package io.github.eugene239.gradle.plugin.dependency.internal.formatter.report

import io.github.eugene239.gradle.plugin.dependency.internal.output.report.OutdatedDependency
import java.io.File

internal interface ReportFormatter {

    fun format(outdated: Collection<OutdatedDependency>): File
}