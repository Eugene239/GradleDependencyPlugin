package io.github.eugene239.gradle.plugin.dependency.internal.formatter.graph

import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.plugin.BuildConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.artifacts.Configuration
import java.io.File


@OptIn(ExperimentalSerializationApi::class)
internal class DependencyFormatter(
    private val startupFlags: StartupFlags
) : Formatter {
    private val prettyEncoder = Json {
        prettyPrint = true
        explicitNulls = false
    }
    private val encoder = Json

    override fun format(node: DependencyNode, outputDir: File): File {
        val json = encoder.encodeToString(node)
        val file = File(outputDir, "dependencies.json")
        file.createNewFile()
        file.writeText(json)
        return file
    }

    override fun saveConfigurations(outputDir: File, configurations: List<Configuration>) {
        val pluginConfiguration = PluginConfiguration(
            version = BuildConfig.PLUGIN_VERSION,
            configurations = configurations.map {
                ProjectConfiguration(
                    name = it.name,
                    description = it.description
                )
            },
            startupFlags = startupFlags
        )
        val json = prettyEncoder.encodeToString(pluginConfiguration)
        val file = File(outputDir, "configurations.json")
        file.createNewFile()
        file.writeText(json)
    }

    override fun saveVersions(outputDir: File, versions: Map<String, String>) {
        val json = prettyEncoder.encodeToString(versions.toSortedMap().toMap())
        val file = File(outputDir, "latest-versions.json")
        file.createNewFile()
        file.writeText(json)
    }

}