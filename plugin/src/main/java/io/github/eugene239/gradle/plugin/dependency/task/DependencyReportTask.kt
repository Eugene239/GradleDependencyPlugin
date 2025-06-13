package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ReportUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ReportUseCaseParams

abstract class DependencyReportTask : BaseTask() {

    override suspend fun exec() {
        val useCase = ReportUseCase()

        val result = useCase.execute(
            ReportUseCaseParams(
                configurations = project.filteredConfigurations()
            )
        )
        logger.lifecycle("Report in file://${result.path}")
    }

}