package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.OUTPUT_PATH
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultRepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.DefaultMavenService
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ReportUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ReportUseCaseParams
import kotlinx.coroutines.Dispatchers
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class DependencyReportTask : BaseTask() {

    @Input
    @Option(option = "repository-connection-limit", description = "Max api calls to repository at one time")
    var limit: String = "$DEFAULT_LIMIT"

    @Input
    @Option(option = "filter", description = "Regex filter for dependencies")
    var filter: String = ""

    @Input
    @Option(option = "connection-timeout", description = "Ktor client connection timeout")
    var connectionTimeout: String = "$DEFAULT_CONNECTION_TIMEOUT"

    private val rootDir = File("${project.layout.buildDirectory.asFile.get()}${File.separator}$OUTPUT_PATH")
    private val logger = project.logger
    private val dependencyFilter = DependencyFilter(rootProjectName = project.rootProject.name)
    private val repositoryProvider = DefaultRepositoryProvider(
        project = project,
        logger = logger,
        limit = limit.toIntOrNull() ?: DEFAULT_LIMIT
    )
    private val useCase = ReportUseCase(
        rootDir = rootDir,
        logger = logger,
        dependencyFilter = dependencyFilter,
        repositoryProvider = repositoryProvider,
        mavenService = DefaultMavenService(
            logger = logger,
            repositoryProvider = repositoryProvider,
            timeoutMillis = connectionTimeout.toLongOrNull() ?: DEFAULT_CONNECTION_TIMEOUT
        ),
        isSubmodule = { dependency ->
            val group = dependency.selected.moduleVersion?.group
            project.rootProject.name.equals(group, true)
        },
        ioDispatcher = Dispatchers.IO
    )


    override suspend fun exec() {
        if (filter.isBlank()) {
            dependencyFilter.setRegex(null)
        } else {
            dependencyFilter.setRegex(Regex(filter))
        }

        val result = useCase.execute(
            ReportUseCaseParams(
                configurations = project.filteredConfigurations()
            )
        )
        logger.lifecycle("Report in file://${result.path}")
    }
}