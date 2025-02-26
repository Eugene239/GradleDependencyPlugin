package io.github.eugene239.gradle.plugin.dependency.internal

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