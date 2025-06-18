package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.filter.RegexFilter
import io.github.eugene239.gradle.plugin.dependency.internal.getLibDetails
import io.github.eugene239.gradle.plugin.dependency.internal.provider.IsSubmoduleProvider
import org.gradle.api.artifacts.Configuration

internal class GenerateBomUseCase : UseCase<GenerateBomUseCaseParams, GenerateBomUseCaseResult> {
    private val filter: RegexFilter by di()
    private val isSubmodule: IsSubmoduleProvider by di()


    override suspend fun execute(params: GenerateBomUseCaseParams): GenerateBomUseCaseResult {
        val dependenciesSet = params.configurations
            .asSequence()
            .map { it.getLibDetails(filter, isSubmodule) }
            .flatten()
            .filter { it.isSubmodule.not() }
            .map { it.key }
            .toSet()

        return GenerateBomUseCaseResult(
            library = params.library,
            dependencies = dependenciesSet
        )
    }
}


internal data class GenerateBomUseCaseParams(
    val configurations: Set<Configuration>,
    val library: LibKey
) : UseCaseParams

internal data class GenerateBomUseCaseResult(
    val library: LibKey,
    val dependencies: Set<LibKey>
)