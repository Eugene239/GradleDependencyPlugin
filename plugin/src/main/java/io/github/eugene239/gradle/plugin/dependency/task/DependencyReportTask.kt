package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ReportUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ReportUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

abstract class DependencyReportTask : BaseTask() {

    @Input
    @Option(option = "filter", description = "Regex filter for dependencies",)
    var filter : String = ""

    override suspend fun exec() {
        val useCase = ReportUseCase()

        val result = useCase.execute(
            ReportUseCaseParams(
                project = project,
                filter = filter
            )
        )
        println("Report in ${result.path}")
    }
}