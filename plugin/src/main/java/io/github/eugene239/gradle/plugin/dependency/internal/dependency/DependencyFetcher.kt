package io.github.eugene239.gradle.plugin.dependency.internal.dependency

import io.github.eugene239.gradle.plugin.dependency.internal.formatter.graph.DependencyNode
import org.gradle.api.artifacts.Configuration

internal interface DependencyFetcher {

    suspend fun fetch(configuration: Configuration): DependencyNode
}