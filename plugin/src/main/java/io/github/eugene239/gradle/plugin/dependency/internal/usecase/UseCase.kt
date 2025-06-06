package io.github.eugene239.gradle.plugin.dependency.internal.usecase

internal interface UseCase<Params : UseCaseParams, out Result> {
    suspend fun execute(params: Params): Result
}

internal interface UseCaseParams