package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.CONSOLE
import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.output.Output
import io.github.eugene239.gradle.plugin.dependency.internal.output.conflict.ConsoleConflictOutput
import io.github.eugene239.gradle.plugin.dependency.internal.output.conflict.MDConflictOutput
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ConflictLevel
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ConflictUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ConflictUseCaseParams
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ConflictUseCaseResult
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class DependencyConflictTask : BaseTask() {

    @Input
    @Option(option = "conflict-level", description = "Level to check dependencies versions (major, minor, patch), by default `major`")
    var conflictLevel: String = ""

    @Input
    @Option(option = "output-format", description = "Output format for result (console, md), by default `md`")
    var outputFormat: String = ""

    @Input
    @Option(option = "fail-on-conflict", description = "Fail task if there are any conflict, by default: false")
    var failOnConflict: String = ""


    override suspend fun exec() {
        val level = ConflictLevel.entries
            .firstOrNull { conflictLevel.equals(it.name, true) }
            ?: ConflictLevel.MAJOR

        val useCase = ConflictUseCase()

        val data = useCase.execute(
            ConflictUseCaseParams(
                configurations = project.filteredConfigurations(),
                level = level
            )
        )

        val output: Output<ConflictUseCaseResult, *> = getOutput()
        val result = output.format(data)

        if (result is File) {
            logger.lifecycle("Report in file://${result.path}")
        }

        if (failOnConflict.toBoolean() && data.conflictSet.isNotEmpty()) {
            throw Exception("Project contains ${data.conflictSet.size} conflicts")
        }
    }

    private fun getOutput(): Output<ConflictUseCaseResult, *> {
        return when (outputFormat) {
            CONSOLE -> ConsoleConflictOutput()
            else -> MDConflictOutput()
        }
    }

}