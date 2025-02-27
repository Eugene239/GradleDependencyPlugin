package io.github.eugene239.gradle.plugin.dependency.internal.output.graph

import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.FlatDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.TopDependencies
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

    override fun save(pluginConfiguration: PluginConfiguration?, flatDependencies: FlatDependencies, topDependencies: TopDependencies?): File {
        savePluginConfiguration(pluginConfiguration)
        saveFlatDependencies(flatDependencies)
        saveTopDependencies(topDependencies)
        return uiSaver.save(rootDir)
    }

    private fun savePluginConfiguration(pluginConfiguration: PluginConfiguration?) {

    }

    private fun saveFlatDependencies(flatDependencies: FlatDependencies) {
        val data = flatDependencies.getData()
        val jsonElement = prettyEncoder.encodeToJsonElement(data)
        val json = prettyEncoder.encodeToString(jsonElement)
        val file = File(rootDir, "flat-dependencies.json")
        file.createNewFile()
        file.writeText(json)
    }

    private fun saveTopDependencies(topDependencies: TopDependencies?) {

    }
}