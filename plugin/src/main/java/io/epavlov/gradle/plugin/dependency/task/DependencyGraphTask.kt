package io.epavlov.gradle.plugin.dependency.task

import io.epavlov.gradle.plugin.dependency.internal.usecase.GraphUseCase
import io.epavlov.gradle.plugin.dependency.internal.usecase.GraphUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

abstract class DependencyGraphTask : BaseTask() {

    @Input
    @Option(option = "filter", description = "Regex filter for dependencies")
    var filter: String = ""

    override suspend fun exec() {
        val useCase = GraphUseCase(
            project = project,
            filter = filter
        )

        val result = useCase.execute(GraphUseCaseParams)
        println("Site in ${result.path}")
    }
}