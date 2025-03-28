package io.github.eugene239.gradle.plugin.dependency.internal.output.graph

import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.ConfigurationResult
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import java.io.File

internal interface GraphOutput {

    fun save(
        pluginConfiguration: PluginConfiguration,
        results: Collection<ConfigurationResult>
    ): File
}