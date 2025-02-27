package io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model

import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import kotlinx.serialization.Serializable
import org.gradle.api.artifacts.Configuration

@Serializable
internal data class PluginConfiguration(
    val configurations: Set<Configuration>,
    val startupFlags: StartupFlags,
    val version: String,
)

@Serializable
internal data class ProjectConfiguration(
    val name: String,
    val description: String?
)
