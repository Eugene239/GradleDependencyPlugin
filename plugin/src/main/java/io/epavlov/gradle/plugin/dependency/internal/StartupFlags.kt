package io.epavlov.gradle.plugin.dependency.internal

import kotlinx.serialization.Serializable

@Serializable
data class StartupFlags(
    val fetchVersions: Boolean
)
