package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ReportUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ReportUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

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

    override suspend fun exec() {
        val useCase = ReportUseCase()

        val result = useCase.execute(
            ReportUseCaseParams(
                configurations = project.filteredConfigurations()
            )
        )
        logger.lifecycle("Report in file://${result.path}")
    }

    override fun configuration(): TaskConfiguration {
        return object : TaskConfiguration {
            override val repositoryConnectionLimit: Int = limit.toIntOrNull() ?: DEFAULT_LIMIT
            override val regexFilter: String? = filter.ifBlank { null }
            override val connectionTimeOut: Long = connectionTimeout.toLongOrNull() ?: DEFAULT_CONNECTION_TIMEOUT
        }
    }
}