package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ConflictReportUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ConflictReportUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

internal class ConflictReportTask : BaseTask() {

    @Input
    @Option(option = "repository-connection-limit", description = "Max api calls to repository at one time")
    var limit: String = "$DEFAULT_LIMIT"

    @Input
    @Option(option = "filter", description = "Regex filter for dependencies")
    var filter: String = ""

    @Input
    @Option(option = "connection-timeout", description = "Ktor client connection timeout")
    var connectionTimeout: String = "$DEFAULT_CONNECTION_TIMEOUT"

    private val dependencyFilter = DependencyFilter(rootProjectName = project.rootProject.name)
    private val useCase = ConflictReportUseCase(
        dependencyFilter = dependencyFilter,
        isSubmodule = { dependency ->
            val group = dependency.selected.moduleVersion?.group
            project.rootProject.name.equals(group, true)
        },
    )

    override suspend fun exec() {
        if (filter.isBlank()) {
            dependencyFilter.setRegex(null)
        } else {
            dependencyFilter.setRegex(Regex(filter))
        }

        val result = useCase.execute(
            ConflictReportUseCaseParams(
                configurations = project.filteredConfigurations()
            )
        )
        logger.lifecycle("Report in file://${result.path}")
    }
}