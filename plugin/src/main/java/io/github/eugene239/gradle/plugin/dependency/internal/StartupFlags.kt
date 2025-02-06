package io.github.eugene239.gradle.plugin.dependency.internal

import kotlinx.serialization.Serializable

@Serializable
data class StartupFlags(
    val fetchVersions: Boolean
)
