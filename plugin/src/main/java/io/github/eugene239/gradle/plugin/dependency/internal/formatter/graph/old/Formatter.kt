package io.github.eugene239.gradle.plugin.dependency.internal.formatter.graph.old

import org.gradle.api.artifacts.Configuration
import java.io.File

@Deprecated("Use GraphOutput")
internal interface Formatter {

    fun format(node: DependencyNode, outputDir: File): File

    fun saveConfigurations(outputDir: File, configurations: List<Configuration>)

    fun saveVersions(outputDir: File, versions: Map<String, String>)
}