package io.github.eugene239.gradle.plugin.dependency.internal.output.graph

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.LibVersions
import io.github.eugene239.gradle.plugin.dependency.internal.di.RootDir
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.ConfigurationResult
import io.github.eugene239.gradle.plugin.dependency.internal.ui.UiSaver
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

internal class DefaultGraphOutput() : GraphOutput {

    private val rootDir: RootDir by di()
    private val uiSaver: UiSaver by di()

    private val prettyEncoder = Json {
        prettyPrint = true
        explicitNulls = false
    }

    override fun save(
        pluginConfiguration: PluginConfiguration,
        results: Collection<ConfigurationResult>,
        latestVersions: Map<LibIdentifier, String>?,
        libSizes: Map<LibKey, Long>?
    ): File {
        savePluginConfiguration(pluginConfiguration)
        results.forEach {
            saveFlatDependencies(it.configuration, it.flatDependencies)
            saveTopDependencies(it.configuration, it.topDependencies)
            saveLibVersions(it.configuration, it.versions)
        }
        latestVersions?.let {
            saveLatestVersions(it)
        }
        libSizes?.let {
            saveLibSizes(it)
        }
        return uiSaver.save(rootDir.file)
    }

    private fun savePluginConfiguration(pluginConfiguration: PluginConfiguration?) {
        val json = prettyEncoder.encodeToString(pluginConfiguration)
        val file = File(rootDir.file, "configurations.json")
        file.createNewFile()
        file.writeText(json)
    }

    private fun saveFlatDependencies(configuration: String, flatDependencies: Map<LibKey, Set<LibKey>>) {
        val dir = File(rootDir.file, configuration)
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
        val dir = File(rootDir.file, configuration)
        dir.mkdirs()
        val file = File(dir, "top-dependencies.json")
        file.createNewFile()
        val json = prettyEncoder.encodeToString(topDependencies.map { it.toString() }.sorted())
        file.writeText(json)
    }

    private fun saveLibVersions(configuration: String, libVersions: LibVersions) {
        val dir = File(rootDir.file, configuration)
        dir.mkdirs()
        val data = libVersions.getConflictData().mapKeys { entry -> entry.key.toString() }
        val json = prettyEncoder.encodeToString(data)
        val file = File(dir, "conflicts.json")
        file.createNewFile()
        file.writeText(json)
    }

    private fun saveLatestVersions(latest: Map<LibIdentifier, String>) {
        val data = latest.mapKeys { entry -> entry.key.toString() }
        val json = prettyEncoder.encodeToString(data)
        val file = File(rootDir.file, "latest-versions.json")
        file.createNewFile()
        file.writeText(json)
    }

    private fun saveLibSizes(libSizes: Map<LibKey, Long>) {
        val data = libSizes.mapKeys { entry -> entry.key.toString() }
        val json = prettyEncoder.encodeToString(data)
        val file = File(rootDir.file, "lib-sizes.json")
        file.createNewFile()
        file.writeText(json)
    }

}