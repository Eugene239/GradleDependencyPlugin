package io.epavlov.gradle.plugin.dependency.internal.formatter.report

import java.io.File

internal interface ReportFormatter {

    fun format(outdated: Set<OutdatedDependency>) : File
}