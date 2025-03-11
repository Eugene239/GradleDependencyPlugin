package io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model

import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import kotlinx.serialization.Serializable

@Serializable
internal data class PluginConfiguration(
    val configurations: Set<ProjectConfiguration>,
    val startupFlags: StartupFlags,
    val version: String,
)

@Serializable
internal data class ProjectConfiguration(
    val name: String,
    val description: String?
)
