package io.epavlov.gradle.plugin.dependency.internal.dependency

import io.epavlov.gradle.plugin.dependency.DependencyNode
import org.gradle.api.artifacts.Configuration

internal interface DependencyFetcher {

    fun fetch(configuration: Configuration): DependencyNode
}