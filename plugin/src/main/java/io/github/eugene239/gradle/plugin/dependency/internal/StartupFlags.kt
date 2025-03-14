package io.github.eugene239.gradle.plugin.dependency.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class StartupFlags(
    val fetchVersions: Boolean = false,
    val fetchLibSize: Boolean = false
)
