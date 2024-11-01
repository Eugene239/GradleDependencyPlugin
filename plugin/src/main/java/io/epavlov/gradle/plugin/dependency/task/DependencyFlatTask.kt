package io.epavlov.gradle.plugin.dependency.task

import io.epavlov.gradle.plugin.dependency.internal.usecase.FlatGraphUseCase
import io.epavlov.gradle.plugin.dependency.internal.usecase.FlatGraphUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

abstract class DependencyFlatTask : BaseTask() {

    @Input
    @Option(option = "filter", description = "Regex filter for dependencies")
    var filter: String = ""

    override suspend fun exec() {
        val useCase = FlatGraphUseCase(
            project = project,
            filter = filter
        )

        val result = useCase.execute(
            FlatGraphUseCaseParams(
                configuration = project.configurations.findByName("worldDebugRuntimeClasspath")
                    ?: throw Exception("Can't find configuration")
            )
        )
        println("Site in ${result.path}")
    }
}