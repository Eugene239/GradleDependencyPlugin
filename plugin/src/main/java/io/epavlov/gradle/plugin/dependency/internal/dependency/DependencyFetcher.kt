package io.epavlov.gradle.plugin.dependency.internal.dependency

import io.epavlov.gradle.plugin.dependency.internal.formatter.DependencyNode
import org.gradle.api.artifacts.Configuration

internal interface DependencyFetcher {

    suspend fun fetch(configuration: Configuration): DependencyNode
}