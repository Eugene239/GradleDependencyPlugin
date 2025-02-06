package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GraphUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GraphUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

abstract class DependencyGraphTask : BaseTask() {

    @Input
    @Option(option = "filter", description = "Regex filter for dependencies")
    var filter: String = ""

    @Input
    @Option(option = "configuration", description = "Configurations to launch")
    var configuration: String = ""


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

        val useCase = GraphUseCase(
            project = project,
            filter = filter
        )
        val result = useCase.execute(
            GraphUseCaseParams(configurations = configurations)
        )
        logger.lifecycle("Site in /${result.path}")
    }
}