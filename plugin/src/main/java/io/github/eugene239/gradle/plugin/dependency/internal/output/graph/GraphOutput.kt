package io.github.eugene239.gradle.plugin.dependency.internal.output.graph

import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.FlatDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.TopDependencies
import java.io.File

internal interface GraphOutput {

    fun save(
        pluginConfiguration: PluginConfiguration? = null,
        flatDependencies: FlatDependencies,
        topDependencies: TopDependencies? = null
    ): File
}