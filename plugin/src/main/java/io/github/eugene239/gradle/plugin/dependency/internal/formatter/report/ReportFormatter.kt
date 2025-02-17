package io.github.eugene239.gradle.plugin.dependency.internal.formatter.report

import java.io.File

internal interface ReportFormatter {

    fun format(outdated: Collection<OutdatedDependency>): File
}