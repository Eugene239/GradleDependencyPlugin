package io.epavlov.gradle.plugin.dependency.internal.formatter.graph

import io.epavlov.gradle.plugin.dependency.internal.StartupFlags
import kotlinx.serialization.Serializable

@Serializable
internal data class PluginConfiguration(
    val configurations: List<ProjectConfiguration>,
    val startupFlags: StartupFlags,
    val version: String,
)

@Serializable
internal data class ProjectConfiguration(
    val name: String,
    val description: String?
)
