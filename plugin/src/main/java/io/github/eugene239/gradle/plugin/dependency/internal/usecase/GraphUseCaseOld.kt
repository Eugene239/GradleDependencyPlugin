package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.OUTPUT_PATH
import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.gradle.plugin.dependency.internal.cache.dependency.DependencyCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.formatter.graph.FlatFormatter
import io.github.eugene239.gradle.plugin.dependency.internal.formatter.graph.DependencyFormatter
import io.github.eugene239.gradle.plugin.dependency.internal.formatter.graph.Formatter
import io.github.eugene239.gradle.plugin.dependency.internal.pom.PomXMLParserImpl
import io.github.eugene239.gradle.plugin.dependency.internal.toLibKey
import io.github.eugene239.gradle.plugin.dependency.internal.ui.DefaultUiSaver
import io.github.eugene239.gradle.plugin.dependency.internal.ui.UiSaver
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import java.io.File

@Deprecated("TODO DELETE")
internal class GraphUseCaseOld(
    project: Project,
    filter: String
) : UseCase<GraphOldUseCaseParams, File> {

    private val versionCache = VersionCache(project)
    private val rootDir =
        File("${project.layout.buildDirectory.asFile.get()}${File.separator}$OUTPUT_PATH")
    private val depFilter: DependencyFilter? = if (filter.isBlank().not()) {
        DependencyFilter(project, Regex(filter))
    } else {
        null
    }
    private val logger = project.logger
    private val dependencyCache = DependencyCache(
        dependencyFilter = depFilter,
        logger = logger,
//        pomCache = PomCache(
//            project = project,
//            versionCache = versionCache,
//        ),
        xmlParser = PomXMLParserImpl(
            logger = project.logger,
            filter = depFilter
        )
    )
    private val formatter = FlatFormatter(
        logger = project.logger
    )
    private val uiSaver: UiSaver = DefaultUiSaver(
        logger = logger
    )
    private val depFormatter: Formatter = DependencyFormatter(
        startupFlags = StartupFlags(fetchVersions = false)
    )

    override suspend fun execute(params: GraphOldUseCaseParams): File {
        rootDir.mkdirs()
        coroutineScope {
            params.configurations.map { configuration ->
                async {
                    processConfiguration(rootDir, configuration)
                }
            }.awaitAll()
        }

        val cached = dependencyCache.cachedValues()
        formatter.format(rootDir, cached)
        val failed = dependencyCache.failedValues()
        formatter.saveConfigurations(rootDir, params.configurations)
        val result = uiSaver.save(rootDir)

        logger.warn("FAILED DEPENDENCIES: ${failed.size}")
        failed.forEach {
            logger.warn("[PARENT] ${it.parent} ----> [DEPENDENCY] ${it.lib}")
            if (logger.isInfoEnabled) {
                it.error.printStackTrace()

            }
        }
        depFormatter.saveVersions(rootDir, versionCache.getLatestVersions())
        return result
    }

    private suspend fun processConfiguration(
        rootDir: File,
        configuration: Configuration
    ) {
        logger.lifecycle("####### PROCESSING ${configuration.name}")
        val incoming = configuration.incoming.resolutionResult.root.dependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .filter { depFilter?.matches(it) != false }
            .toSet()

        logger.lifecycle("incoming size: ${incoming.size}")
        dependencyCache.fill(incoming)

        val outputDir = File(rootDir, configuration.name)
        outputDir.mkdirs()
        formatter.saveTopDependencies(outputDir, incoming.map { it.toLibKey() })
        logger.lifecycle("####### PROCESSING ${configuration.name} END")
    }


}

@Deprecated("TODO DELETE")
internal data class GraphOldUseCaseParams(
    val configurations: Set<Configuration>
) : UseCaseParams