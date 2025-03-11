package io.github.eugene239.gradle.plugin.dependency.internal

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult


internal fun DependencyResult.toLibKey(): LibKey {
    val selected = (this as ResolvedDependencyResult).selected
    val moduleVersion = selected.moduleVersion ?: throw Exception("Module version not found for ${selected.id.displayName}")
    return LibKey(
        group = moduleVersion.group,
        module = moduleVersion.module.name,
        version = moduleVersion.version
    )
}

internal fun Project.filteredConfigurations(): Set<Configuration> {
    return configurations
        .asSequence()
        .filter { config -> config.name.contains("runtimeClasspath", true) }
        .filter { it.name.contains("test", true).not() && it.isCanBeResolved }
        .toSet()
}