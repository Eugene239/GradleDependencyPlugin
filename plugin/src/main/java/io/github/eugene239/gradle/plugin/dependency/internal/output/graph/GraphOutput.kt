package io.github.eugene239.gradle.plugin.dependency.internal.output.graph

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.ConfigurationResult
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import java.io.File

internal interface GraphOutput {

    fun save(
        pluginConfiguration: PluginConfiguration,
        results: Collection<ConfigurationResult>,
        latestVersions: Map<LibIdentifier, String>? = null,
        libSizes: Map<LibKey, Long>? = null
    ): File
}