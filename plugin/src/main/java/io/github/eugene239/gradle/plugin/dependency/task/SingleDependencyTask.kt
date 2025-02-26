package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultRepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.SingleDependencyUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.SingleDependencyUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

abstract class SingleDependencyTask : BaseTask() {

    private val logger = project.logger

    @Input
    @Option(option = "name", description = "Name of the dependency with version")
    var dependency: String = ""


    private val useCase = SingleDependencyUseCase(
        logger = logger,
        repositoryProvider = DefaultRepositoryProvider(
            project = project,
            logger = logger
        )
    )

    override suspend fun exec() {
        if (dependency.isBlank() || dependency.count { it == ':' } != 2) {
            throw Exception("Pass 'name' parameter to execute task for one dependency")
        }
        val split = dependency.split(":")
        useCase.execute(
            SingleDependencyUseCaseParams(
                libKey = LibKey(
                    group = split[0],
                    module = split[1],
                    version = split[2]
                )
            )
        )
        logger.lifecycle("Task executed")
    }
}