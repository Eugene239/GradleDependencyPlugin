package io.github.eugene239.gradle.plugin.dependency.internal.output.graph

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.FlatDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.TopDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.Versions
import java.io.File

internal interface GraphOutput {

    fun save(
        pluginConfiguration: PluginConfiguration,
        flatDependencies: FlatDependencies,
        topDependencies: TopDependencies,
        libVersions: Map<String, Map<LibIdentifier, Versions>>
    ): File
}