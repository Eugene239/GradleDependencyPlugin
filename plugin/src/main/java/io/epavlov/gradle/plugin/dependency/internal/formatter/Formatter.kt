package io.epavlov.gradle.plugin.dependency.internal.formatter

import io.epavlov.gradle.plugin.dependency.DependencyNode
import org.gradle.api.artifacts.Configuration
import java.io.File

internal interface Formatter {

    fun format(node: DependencyNode, outputDir: File): File

    fun copySite(outputDir: File)

    fun saveConfigurations(outputDir: File, configurations: List<Configuration>)

    fun saveVersions(outputDir: File, versions: Map<String, String>)
}