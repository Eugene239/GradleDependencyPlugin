package io.github.eugene239.gradle.plugin.dependency.internal

import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenMetadata
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.internal.artifacts.result.DefaultResolvedComponentResult
import kotlin.coroutines.cancellation.CancellationException


internal fun DependencyResult.toLibDetails(isSubmodule: Boolean = false): LibDetails {
    val selected = (this as ResolvedDependencyResult).selected
    val moduleVersion = selected.moduleVersion ?: throw Exception("Module version not found for ${selected.id.displayName}")
    val isStrictly = (this.requested as? ModuleComponentSelector)?.versionConstraint?.strictVersion.isNullOrBlank().not()
    return LibDetails(
        key = LibKey(
            group = moduleVersion.group,
            module = moduleVersion.module.name,
            version = moduleVersion.version
        ),
        isStrict = isStrictly,
        isSubmodule = isSubmodule,
        repositoryId = (selected as? DefaultResolvedComponentResult)?.repositoryId,
        result = if (isSubmodule) {
            selected
        } else {
            null
        }
    )
}

internal fun Project.filteredConfigurations(): Set<Configuration> {
    return configurations
        .asSequence()
        .filter { config -> config.name.contains("runtimeClasspath", true) }
        .filter { it.name.contains("test", true).not() && it.isCanBeResolved }
        .toSet()
}

internal fun MavenMetadata.containsVersion(libKey: LibKey): Boolean {
    return versioning?.versions?.version?.contains(libKey.version) == true
}

fun <T> Result<T>.rethrowCancellationException(): Result<T> {
    return fold(
        onFailure = { exception ->
            when (exception) {
                is CancellationException -> throw exception
                else -> this
            }
        },
        onSuccess = { this }
    )
}