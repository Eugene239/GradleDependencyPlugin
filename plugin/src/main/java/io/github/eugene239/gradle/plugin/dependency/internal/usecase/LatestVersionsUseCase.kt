package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.FAIL
import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.OK
import io.github.eugene239.gradle.plugin.dependency.internal.WARN
import io.github.eugene239.gradle.plugin.dependency.internal.cache.version.LatestVersionCache
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.filter.RegexFilter
import io.github.eugene239.gradle.plugin.dependency.internal.getLibDetails
import io.github.eugene239.gradle.plugin.dependency.internal.provider.IsSubmoduleProvider
import io.github.eugene239.gradle.plugin.dependency.internal.toIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logger
import java.lang.module.ModuleDescriptor

internal class LatestVersionsUseCase : UseCase<LatestVersionsUseCaseParams, LatestVersionsUseCaseResult> {

    private val logger: Logger by di()
    private val versionCache: LatestVersionCache by di()
    private val dependencyFilter: RegexFilter by di()
    private val isSubmodule: IsSubmoduleProvider by di()

    override suspend fun execute(params: LatestVersionsUseCaseParams): LatestVersionsUseCaseResult {
        with(params) {
            logger.info("configurations: ${params.configurations}")
            val latestMap = hashMapOf<LibIdentifier, ModuleDescriptor.Version>()
            val detailsSet = configurations
                .map { it.getLibDetails(dependencyFilter, isSubmodule) }
                .flatten()
                .filter { it.isSubmodule.not() }
                .toSet()

            logger.info("keys size: ${detailsSet.size}")

            coroutineScope {
                detailsSet.map { details ->
                    async {
                        versionCache.get(details.key, details.repositoryId!!)?.let { version ->
                            latestMap[details.key.toIdentifier()] = version
                        }
                    }
                }.awaitAll()
            }


            val outdatedLibraries = mutableSetOf<OutdatedDependency>()
            detailsSet.forEach { details ->
                val key = details.key
                val latest = latestMap[key.toIdentifier()]
                logger.info("${key.toIdentifier()} latest: ${latest?.toString()}")
                if (latest != null && latest.toString() != key.version) {
                    outdatedLibraries.add(
                        OutdatedDependency(
                            name = "${key.group}:${key.module}",
                            currentVersion = key.version,
                            latestVersion = latest.toString(),
                            status = determine(key.version, latest.toString())
                        )
                    )
                }
            }

            return LatestVersionsUseCaseResult(outdatedLibraries)
        }
    }

    private fun determine(version: String, latestVersion: String): String {
        if (version == latestVersion) return OK
        val currentMajor = version.split(".").first().toIntOrNull() ?: 0
        val latestMajor = latestVersion.split(".").first().toIntOrNull() ?: 0
        return if (currentMajor < latestMajor) {
            FAIL
        } else {
            WARN
        }
    }
}

internal class LatestVersionsUseCaseParams(
    val configurations: Set<Configuration>,
) : UseCaseParams

internal data class OutdatedDependency(
    val name: String,
    val currentVersion: String,
    val latestVersion: String,
    val status: String
)

internal data class LatestVersionsUseCaseResult(
    val dependencies: Set<OutdatedDependency>
)