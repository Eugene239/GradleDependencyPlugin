package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.OUTPUT_PATH
import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultRepositoryProvider
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

    private val defaultLimit = 100

    @Input
    @Option(option = "dependency-limit", description = "Max dependency processing at one time")
    var limit: String = "$defaultLimit"


    private val rootDir = File("${project.layout.buildDirectory.asFile.get()}${File.separator}$OUTPUT_PATH")
    private val logger = project.logger
    private val dependencyFilter = DependencyFilter(project = project)
    private val useCase = GraphUseCase(
        rootDir = rootDir,
        logger = logger,
        dependencyFilter = dependencyFilter,
        repositoryProvider = DefaultRepositoryProvider(
            project = project,
            logger = logger
        ),
        ioDispatcher = Dispatchers.IO
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
        if (filter.isBlank()) {
            dependencyFilter.setRegex(null)
        } else {
            dependencyFilter.setRegex(Regex(filter))
        }

        val result = useCase.execute(
            GraphUseCaseParams(
                configurations = configurations,
                startupFlags = StartupFlags(
                    fetchVersions = false
                ),
                limit = limit.toIntOrNull() ?: defaultLimit
            )
        )
        logger.lifecycle("Site in file://${result.path}")
    }
}