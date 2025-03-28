package io.github.eugene239.gradle.plugin.dependency.internal.output.graph

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.LibVersions
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.ConfigurationResult
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
        results: Collection<ConfigurationResult>
    ): File {
        savePluginConfiguration(pluginConfiguration)
        results.forEach {
            saveFlatDependencies(it.configuration, it.flatDependencies)
            saveTopDependencies(it.configuration, it.topDependencies)
            saveLibVersions(it.configuration, it.versions)
        }
        return uiSaver.save(rootDir)
    }

    private fun savePluginConfiguration(pluginConfiguration: PluginConfiguration?) {
        val json = prettyEncoder.encodeToString(pluginConfiguration)
        val file = File(rootDir, "configurations.json")
        file.createNewFile()
        file.writeText(json)
    }

    private fun saveFlatDependencies(configuration: String, flatDependencies: Map<LibKey, Set<LibKey>>) {
        val dir = File(rootDir, configuration)
        dir.mkdirs()
        val jsonElement = prettyEncoder.encodeToJsonElement(
            flatDependencies
                .mapKeys { it.key.toString() }
                .mapValues { it.value.map { item -> item.toString() } }
        )
        val json = prettyEncoder.encodeToString(jsonElement)
        val file = File(dir, "flat-dependencies.json")
        file.createNewFile()
        file.writeText(json)
    }

    private fun saveTopDependencies(configuration: String, topDependencies: Set<LibKey>) {
        val dir = File(rootDir, configuration)
        dir.mkdirs()
        val file = File(dir, "top-dependencies.json")
        file.createNewFile()
        val json = prettyEncoder.encodeToString(topDependencies.map { it.toString() }.sorted())
        file.writeText(json)
    }

    private fun saveLibVersions(configuration: String, libVersions: LibVersions) {
        val dir = File(rootDir, configuration)
        dir.mkdirs()
        val data = libVersions.getConflictData().mapKeys { entry -> entry.key.toString() }
        val json = prettyEncoder.encodeToString(data)
        val file = File(dir, "conflicts.json")
        file.createNewFile()
        file.writeText(json)
    }
}