package io.github.eugene239.gradle.plugin.dependency.internal.output.graph

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.FlatDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.TopDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.Versions
import io.github.eugene239.gradle.plugin.dependency.internal.ui.UiSaver
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

internal class DefaultGraphOutput(
    private val rootDir: File,
    private val uiSaver: UiSaver
) : GraphOutput {

    private val prettyEncoder = Json {
        prettyPrint = true
        explicitNulls = false
    }

    override fun save(
        pluginConfiguration: PluginConfiguration,
        flatDependencies: FlatDependencies,
        topDependencies: TopDependencies,
        libVersions: Map<String, Map<LibIdentifier, Versions>>
    ): File {
        savePluginConfiguration(pluginConfiguration)
        saveFlatDependencies(flatDependencies)
        saveTopDependencies(topDependencies)
        saveLibVersions(libVersions)
        return uiSaver.save(rootDir)
    }

    private fun savePluginConfiguration(pluginConfiguration: PluginConfiguration?) {
        val json = prettyEncoder.encodeToString(pluginConfiguration)
        val file = File(rootDir, "configurations.json")
        file.createNewFile()
        file.writeText(json)
    }

    private fun saveFlatDependencies(flatDependencies: FlatDependencies) {
        val data = flatDependencies.getData()
        val jsonElement = prettyEncoder.encodeToJsonElement(data)
        val json = prettyEncoder.encodeToString(jsonElement)
        val file = File(rootDir, "flat-dependencies.json")
        file.createNewFile()
        file.writeText(json)
    }

    private fun saveTopDependencies(topDependencies: TopDependencies) {
        topDependencies.getData().forEach { (configuration, dependencies) ->
            val dir = File(rootDir, configuration)
            dir.mkdirs()
            val file = File(dir, "top-dependencies.json")
            file.createNewFile()
            val json = prettyEncoder.encodeToString(dependencies.sorted())
            file.writeText(json)
        }
    }

    private fun saveLibVersions(confToLibVersions: Map<String, Map<LibIdentifier, Versions>>) {
        confToLibVersions.forEach { (configuration, conflicts) ->
            val dir = File(rootDir, configuration)
            dir.mkdirs()

            val data = conflicts.mapKeys { entry -> entry.key.toString() }
            val json = prettyEncoder.encodeToString(data)
            val file = File(dir, "conflicts.json")
            file.createNewFile()
            file.writeText(json)
        }
    }
}