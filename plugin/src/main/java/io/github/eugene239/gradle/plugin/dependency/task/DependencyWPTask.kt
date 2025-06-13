package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.server.PluginHttpServer
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GraphUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GraphUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

abstract class DependencyWPTask : BaseTask() {


    @Input
    @Option(option = "configuration", description = "Configurations to launch")
    var configuration: String = ""

    @Input
    @Option(option = "http-port", description = "Http server port")
    var httpPort: String = ""

    @Input
    @Option(option = "fetch-dependencies-size", description = "Flag to fetch top dependencies size")
    var fetchSize: String = ""

    @Input
    @Option(option = "fetch-latest-versions", description = "Flag to fetch top dependencies latest versions")
    var fetchLatestVersions: String = ""

    override suspend fun exec() {
        val configurations = if (configuration.isBlank()) {
            project.filteredConfigurations()
        } else {
            project.configurations.findByName(configuration)?.let { setOf(it) } ?: emptySet()
        }
        val useCase = GraphUseCase()
        useCase.execute(
            GraphUseCaseParams(
                configurations = configurations
            )
        )
        val server = PluginHttpServer()
        server.start()
    }

    override fun configuration(): WPTaskConfiguration {
        return object : WPTaskConfiguration {
            override val repositoryConnectionLimit: Int = limit.toIntOrNull() ?: DEFAULT_LIMIT
            override val regexFilter: String? = filter.ifBlank { null }
            override val connectionTimeOut: Long = connectionTimeout.toLongOrNull() ?: DEFAULT_CONNECTION_TIMEOUT
            override val fetchLatestVersions: Boolean = this@DependencyWPTask.fetchLatestVersions.toBoolean()
            override val fetchLibrarySize: Boolean = fetchSize.toBoolean()
            override val httpPort: Int? = this@DependencyWPTask.httpPort.toIntOrNull()
        }
    }

}