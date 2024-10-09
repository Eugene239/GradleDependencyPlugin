package io.epavlov.gradle.plugin.dependency.internal.usecase

import org.gradle.api.Project
import java.io.File

internal class GraphUseCase : UseCase<GraphUseCaseParams, File> {

    override suspend fun execute(params: GraphUseCaseParams): File {
        TODO("Not yet implemented")
    }
}

data class GraphUseCaseParams(
    val project: Project
): UseCaseParams