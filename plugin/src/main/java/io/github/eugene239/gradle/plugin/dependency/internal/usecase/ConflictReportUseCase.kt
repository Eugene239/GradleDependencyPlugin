package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.LibDetails
import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibVersions
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.getLibDetails
import io.github.eugene239.gradle.plugin.dependency.internal.toIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import java.io.File

internal class ConflictReportUseCase(
    private val dependencyFilter: DependencyFilter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val isSubmodule: (ResolvedDependencyResult) -> Boolean,
) : UseCase<ConflictReportUseCaseParams, File> {

    private val libIdToRepositoryName = HashMap<LibIdentifier, String?>()

    override suspend fun execute(params: ConflictReportUseCaseParams): File = coroutineScope {

        val allTopLibDetails = params.configurations
            .asSequence()
            .map { it.getLibDetails(dependencyFilter, isSubmodule) }
            .flatten()
            .toSet()

        preProcessDependencies(allTopLibDetails)

        val results = params.configurations.map {
            async { processConfiguration(it) }
        }.awaitAll()

        return@coroutineScope output.save()
    }


    // Fill cache for all dependencies in all configurations
    private suspend fun preProcessDependencies(allTopLibDetails: Set<LibDetails>) = coroutineScope {
        val dependencies = allTopLibDetails
            .filter { it.isSubmodule.not() }
            .toSet()

        libIdToRepositoryName.putAll(dependencies.associate { it.key.toIdentifier() to it.repositoryId })
        val identifiers = dependencies.map { it.key.toIdentifier() }.toSet()

        processDependencies(
            dependencies = dependencies.map { it.key }.toSet(),
            identifiers = identifiers,
            flatDependencies = hashMapOf(),
            libVersions = LibVersions()
        )
    }
}

internal class ConflictReportUseCaseParams(
    val configurations: Set<Configuration>,
) : UseCaseParams