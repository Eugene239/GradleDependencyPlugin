package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.filter.RegexFilter
import org.gradle.api.artifacts.Configuration
import org.gradle.internal.cc.base.logger

internal class ConflictUseCase : UseCase<ConflictUseCaseParams, ConflictUseCaseResult> {
    private val filter: RegexFilter by di()

    private val versionMap = mutableMapOf<LibIdentifier, MutableSet<String>>()

    override suspend fun execute(params: ConflictUseCaseParams): ConflictUseCaseResult {
        params.configurations.forEach { configuration ->
            configuration.resolutionStrategy
                .eachDependency { dep ->
                    if (filter.matched(dep).not()) return@eachDependency
                    val key = LibIdentifier(
                        dep.requested.group,
                        dep.requested.name
                    )

                    dep.requested.version?.let {
                        versionMap.getOrPut(key) { mutableSetOf() }.add(it)
                    }
                }

            configuration.resolve()
        }
        logger.debug("Total : ${versionMap.size}")
        val conflicts = versionMap.filter { it.value.size > 1 }

        logger.debug("Total with conflicts: ${conflicts.size}")
        val result = conflicts.mapNotNull { (id, versions) ->
            Conflict(
                library = id,
                versions = versions,
                level = classifyConflict(versions) ?: return@mapNotNull null
            )
        }

        return ConflictUseCaseResult(
            conflictSet = result.filter { conflict ->
                return@filter when (params.level) {
                    ConflictLevel.MAJOR -> conflict.level == ConflictLevel.MAJOR
                    ConflictLevel.MINOR -> conflict.level == ConflictLevel.MAJOR || conflict.level == ConflictLevel.MINOR
                    ConflictLevel.PATCH -> true
                }
            }.toSet()
        )
    }

    private fun classifyConflict(conflictVersions: Set<String>): ConflictLevel? {
        val semanticVersions = conflictVersions.mapNotNull { version ->
            val split = version.split(".")
            val major = split.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val minor = split.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            val patch = split.getOrNull(2) ?: return@mapNotNull null
            SemanticVersion(major, minor, patch)
        }.toSet()

        val majors = semanticVersions.map { it.major }.toSet()
        if (majors.size > 1) return ConflictLevel.MAJOR

        val minors = semanticVersions.map { it.minor }.toSet()
        if (minors.size > 1) return ConflictLevel.MINOR

        val patches = semanticVersions.map { it.patch }.toSet()
        if (patches.size > 1) return ConflictLevel.PATCH

        return null
    }
}

internal data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: String
)

internal enum class ConflictLevel {
    MAJOR,
    MINOR,
    PATCH
}

internal data class ConflictUseCaseParams(
    val configurations: Set<Configuration>,
    val level: ConflictLevel
) : UseCaseParams

internal data class ConflictUseCaseResult(
    val conflictSet: Set<Conflict>
)

internal data class Conflict(
    val library: LibIdentifier,
    val versions: Set<String>,
    val level: ConflictLevel
)