package io.epavlov.gradle.plugin.dependency.internal.usecase

import io.epavlov.gradle.plugin.dependency.internal.OUTPUT_PATH
import io.epavlov.gradle.plugin.dependency.internal.StartupFlags
import io.epavlov.gradle.plugin.dependency.internal.cache.lib.LibCache
import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.epavlov.gradle.plugin.dependency.internal.dependency.DependencyFetcher
import io.epavlov.gradle.plugin.dependency.internal.dependency.IncomingDependencyFetcher
import io.epavlov.gradle.plugin.dependency.internal.di.PluginComponent
import io.epavlov.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.epavlov.gradle.plugin.dependency.internal.formatter.graph.DependencyFormatter
import io.epavlov.gradle.plugin.dependency.internal.formatter.graph.Formatter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.koin.core.component.inject
import java.io.File

internal class GraphUseCase(
    private val project: Project,
    private val filter: String
) : UseCase<GraphUseCaseParams, File>, PluginComponent {
    // todo move?
    private val startupFlags = StartupFlags(
        fetchVersions = true
    )
    private val versionCache: VersionCache by inject()
    private val rootDir = File(project.buildDir, OUTPUT_PATH)
    private val libCache = LibCache(
        project = project,
        versionCache = versionCache,
        startupFlags = startupFlags
    )
    private val formatter: Formatter = DependencyFormatter(startupFlags = startupFlags)
    private val depFilter: DependencyFilter? = if (filter.isBlank().not()) {
        DependencyFilter(project, Regex(filter))
    } else {
        null
    }
    private val dependencyFetcher: DependencyFetcher = IncomingDependencyFetcher(
        project = project,
        regexFilter = depFilter,
        libCache = libCache,
    )


    override suspend fun execute(params: GraphUseCaseParams): File {
        val configurations = project.configurations
            .asSequence()
            .filter { config -> config.name.contains("runtimeClasspath", true) }
            .filter { it.name.contains("test", true).not() && it.isCanBeResolved }
            .toList()

        project.logger.info("# Configurations:")
        configurations.forEach {
            project.logger.info(it.name)
        }

        coroutineScope {
            configurations
                .map { configuration ->
                    async {
                        processConfiguration(
                            rootDir = rootDir,
                            configuration = configuration
                        )
                    }
                }.awaitAll()
        }

        val result = formatter.copySite(rootDir)
        formatter.saveConfigurations(rootDir, configurations)
        if (startupFlags.fetchVersions) {
            formatter.saveVersions(rootDir, versionCache.getLatestVersions())
        }

        return result
    }

    private suspend fun processConfiguration(
        rootDir: File,
        configuration: Configuration
    ) {
        val outputDir = File(rootDir, configuration.name)
        outputDir.mkdirs()

        val result = dependencyFetcher.fetch(configuration)
        formatter.format(result, outputDir)
    }
}

data object GraphUseCaseParams : UseCaseParams