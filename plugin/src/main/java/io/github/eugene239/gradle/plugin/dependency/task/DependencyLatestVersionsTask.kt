package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.CONSOLE
import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.output.Output
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.ConsoleLatestVersionsOutput
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.MDLatestVersionsOutput
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.LatestVersionsUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.LatestVersionsUseCaseParams
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.LatestVersionsUseCaseResult
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class DependencyLatestVersionsTask : BaseTask() {

    @Input
    @Option(option = "output-format", description = "Output format for result (console, md), by default `md`")
    var outputFormat: String = ""

    override suspend fun exec() {
        val useCase = LatestVersionsUseCase()

        val data = useCase.execute(
            LatestVersionsUseCaseParams(
                configurations = project.filteredConfigurations(),
            )
        )

        val output: Output<LatestVersionsUseCaseResult, *> = getOutput()
        val result = output.format(data)

        if (result is File) {
            logger.lifecycle("Report in file://${result.path}")
        }
    }

    private fun getOutput(): Output<LatestVersionsUseCaseResult, *> {
        return when (outputFormat) {
            CONSOLE -> ConsoleLatestVersionsOutput()
            else -> MDLatestVersionsOutput()
        }
    }
}