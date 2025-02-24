package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.OUTPUT_PATH
import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GraphUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GraphUseCaseParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option
import java.io.File
import java.util.concurrent.Executors

abstract class DependencyGraphTask : BaseTask() {

    @Input
    @Option(option = "filter", description = "Regex filter for dependencies")
    var filter: String = ""

    @Input
    @Option(option = "configuration", description = "Configurations to launch")
    var configuration: String = ""

    private val rootDir = File("${project.layout.buildDirectory.asFile.get()}${File.separator}$OUTPUT_PATH")
    private val logger = project.logger

    private val useCase = GraphUseCase(
        rootDir = rootDir,
        logger = logger,
        repositoryProvider = RepositoryProvider(
            project = project,
            logger = logger
        ),
        ioDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher() // todo remove
    )

    override suspend fun exec() {
        val configurations = if (configuration.isBlank()) {
            project.configurations
                .asSequence()
                .filter { config -> config.name.contains("runtimeClasspath", true) }
                .filter { it.name.contains("test", true).not() && it.isCanBeResolved }
                .toSet()
        } else {
            setOf(project.configurations.findByName(configuration))
        }
        val depFilter: DependencyFilter? = if (filter.isBlank().not()) {
            DependencyFilter(project, Regex(filter))
        } else {
            null
        }

        val result = useCase.execute(
            GraphUseCaseParams(
                configurations = configurations,
                startupFlags = StartupFlags(
                    fetchVersions = false
                ),
                filter = depFilter
            )
        )
        logger.lifecycle("Site in file://${result.path}")
    }
}