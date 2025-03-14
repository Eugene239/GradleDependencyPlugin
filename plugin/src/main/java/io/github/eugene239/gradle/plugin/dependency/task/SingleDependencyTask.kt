package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultRepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.DefaultMavenService
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.SingleDependencyUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.SingleDependencyUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

abstract class SingleDependencyTask : BaseTask() {

    private val logger = project.logger

    @Input
    @Option(option = "name", description = "Name of the dependency with version")
    var dependency: String = ""

    @Input
    @Option(option = "connection-timeout", description = "Ktor client connection timeout")
    var connectionTimeout: String = "$DEFAULT_CONNECTION_TIMEOUT"

    private val repositoryProvider = DefaultRepositoryProvider(
        project = project,
        logger = logger,
        limit = 1
    )

    private val useCase = SingleDependencyUseCase(
        logger = logger,
        repositoryProvider = repositoryProvider,
        mavenService = DefaultMavenService(
            logger = logger,
            repositoryProvider = repositoryProvider,
            timeoutMillis = connectionTimeout.toLongOrNull() ?: DEFAULT_CONNECTION_TIMEOUT
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