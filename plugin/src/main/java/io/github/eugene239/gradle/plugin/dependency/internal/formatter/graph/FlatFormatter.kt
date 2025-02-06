package io.github.eugene239.gradle.plugin.dependency.internal.formatter.graph

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.plugin.BuildConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.gradle.api.artifacts.Configuration
import org.slf4j.Logger
import java.io.File

internal class FlatFormatter(
    private val logger: Logger
) {
    private val prettyEncoder = Json {
        prettyPrint = true
        explicitNulls = false
    }

    fun format(outputDir: File, map: Map<LibKey, Collection<LibKey>>): File {
        val data = map
            .mapKeys { it.key.toString() }
            .mapValues { it.value.map { lib -> lib.toString() } }
            .toSortedMap()
            .toMap()
        val jsonElement = prettyEncoder.encodeToJsonElement(data)
        val json = prettyEncoder.encodeToString(jsonElement)
        val file = File(outputDir, "flat-dependencies.json")
        file.createNewFile()
        file.writeText(json)
        return file
    }

    fun saveTopDependencies(outputDir: File, libs: Collection<LibKey>): File {
        val data = libs
            .map { it.toString() }
            .sorted()
            .toSet()

        val json = prettyEncoder.encodeToString(data)
        val file = File(outputDir, "top-dependencies.json")
        file.createNewFile()
        file.writeText(json)
        return file
    }


    fun saveConfigurations(
        outputDir: File,
        configurations: Collection<Configuration>,
        startupFlags : StartupFlags = StartupFlags(fetchVersions = false)
    ) {
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
}