package io.epavlov.gradle.plugin.dependency.internal.formatter

import io.epavlov.gradle.plugin.dependency.DependencyNode
import io.epavlov.gradle.plugin.dependency.internal.StartupFlags
import io.epavlov.plugin.BuildConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.artifacts.Configuration
import java.io.File


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
        println("open to check dependencies: $outputDir")
        return file
    }

    override fun copySite(outputDir: File) {
        create(outputDir, "dep.html")
        create(outputDir, "main.js")
        create(outputDir, "style.css")
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

    private fun create(outputDir: File, name: String) {
        kotlin.runCatching {
            val url = this::class.java.classLoader.getResource(name) ?: throw Exception("resource not found: $name")
            val bytes = url.readBytes()
            val file = File(outputDir, name)
            file.createNewFile()
            file.writeBytes(bytes)
        }.onFailure {
            it.printStackTrace()
        }
    }
}