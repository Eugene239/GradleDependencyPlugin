package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.server.PluginHttpServer
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GraphUseCase
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GraphUseCaseParams
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

abstract class DependencyWPTask : BaseTask() {

    @Input
    @Option(option = "filter", description = "Regex filter for dependencies")
    var filter: String = ""

    @Input
    @Option(option = "configuration", description = "Configurations to launch")
    var configuration: String = ""

    @Input
    @Option(option = "repository-connection-limit", description = "Max api calls to repository at one time")
    var limit: String = "$DEFAULT_LIMIT"

    @Input
    @Option(option = "connection-timeout", description = "Ktor client connection timeout")
    var connectionTimeout: String = "$DEFAULT_CONNECTION_TIMEOUT"

    @Input
    @Option(option = "http-port", description = "Http server port")
    var httpPort: String = ""

    @Input
    @Option(option = "fetch-dependencies-size", description = "Flag to fetch top dependencies size")
    var fetchSize: String = ""

    @Input
    @Option(option = "fetch-latest-versions", description = "Flag to fetch top dependencies latest versions")
    var fetchLatestVersions: String = ""


    private val server: PluginHttpServer by di()


    override suspend fun exec() {
        val configurations = if (configuration.isBlank()) {
            project.filteredConfigurations()
        } else {
            project.configurations.findByName(configuration)?.let { setOf(it) } ?: emptySet()
        }
        val useCase = GraphUseCase()
        useCase.execute(
            GraphUseCaseParams(
                configurations = configurations,
                startupFlags = StartupFlags(
                    fetchVersions = fetchLatestVersions.toBoolean(),
                    fetchLibSize = fetchSize.toBoolean()
                ),
            )
        )
        server.start(port = httpPort.toIntOrNull())
    }

    override fun configuration(): TaskConfiguration {
        return object : WPTaskConfiguration {
            override val repositoryConnectionLimit: Int = limit.toIntOrNull() ?: DEFAULT_LIMIT
            override val regexFilter: String? = filter.ifBlank { null }
            override val connectionTimeOut: Long = connectionTimeout.toLongOrNull() ?: DEFAULT_CONNECTION_TIMEOUT
        }
    }
}