package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.gradle.plugin.dependency.internal.cache.children.ChildrenCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.rethrowCancellationException
import io.github.eugene239.gradle.plugin.dependency.internal.exception.RepositoryException
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.DefaultGraphOutput
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.GraphOutput
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.FlatDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.LibVersions
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.ProjectConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.TopDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultRepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.DefaultMavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.toIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.toLibDetails
import io.github.eugene239.gradle.plugin.dependency.internal.ui.DefaultUiSaver
import io.github.eugene239.gradle.plugin.dependency.internal.ui.UiSaver
import io.github.eugene239.plugin.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.logging.Logger
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal class GraphUseCase(
    private val rootDir: File,
    private val logger: Logger,
    private val dependencyFilter: DependencyFilter,
    private val repositoryProvider: DefaultRepositoryProvider,
    private val mavenService: MavenService = DefaultMavenService(
        repositoryProvider = repositoryProvider,
        logger = logger
    ),

    private val uiSaver: UiSaver = DefaultUiSaver(logger = logger),
    private val output: GraphOutput = DefaultGraphOutput(
        rootDir = rootDir,
        uiSaver = uiSaver
    ),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val repositoryCache: RepositoryCache = RepositoryCache(
        repositoryProvider = repositoryProvider,
        mavenService = mavenService,
        ioDispatcher = ioDispatcher
    ),
    private val pomCache: PomCache = PomCache(
        mavenService = mavenService,
        repositoryCache = repositoryCache,
    ),
    private val childrenCache: ChildrenCache = ChildrenCache(
        pomCache = pomCache
    )
) : UseCase<GraphUseCaseParams, File> {

    private val map = ConcurrentHashMap<LibKey, List<LibKey>>()
    private val libVersions = LibVersions()

    override suspend fun execute(params: GraphUseCaseParams): File {
        logger.info("Start execution with params: $params")
        rootDir.mkdirs()

        val topDependencies = params.configurations.associateWith { configuration ->
            configuration.incoming.resolutionResult.root.dependencies
                .asSequence()
                .filterIsInstance<ResolvedDependencyResult>()
                .filter { dependencyFilter.matches(it) }
                .toSet()
                .map { it.toLibDetails(isSubmodule = dependencyFilter.isSubmodule(it)) }
                .toSet()
        }

        val dependencies = topDependencies
            .map { entry -> entry.value }
            .flatten()
            .toSet()

        val identifiers = dependencies.map { it.key.toIdentifier() }.toSet()

        logger.lifecycle("Getting info about ${dependencies.size} dependencies")
        if (logger.isInfoEnabled) {
            dependencies.forEach {
                logger.info("- $it")
            }
        }

        withContext(ioDispatcher) {
            dependencies.map {
                async {
                    libVersions.setResolved(it.key.toIdentifier(), it.key.version)
                    processDependency(libKey = it.key, identifiers)
                }
            }.awaitAll()
        }

        return output.save(
            pluginConfiguration = PluginConfiguration(
                configurations = params.configurations.map {
                    ProjectConfiguration(
                        name = it.name,
                        description = it.description
                    )
                }.toSet(),
                version = BuildConfig.PLUGIN_VERSION,
                startupFlags = StartupFlags(
                    fetchVersions = false
                )
            ),
            topDependencies = TopDependencies(topDependencies.mapValues { entry -> entry.value.map { it.key }.toSet() }),
            flatDependencies = FlatDependencies(map),
            libVersions = libVersions.getConflictData()
        )
    }

    private suspend fun processDependency(libKey: LibKey, identifiers: Set<LibIdentifier>): Unit = coroutineScope {
        logger.debug("processDependency: $libKey")
        if (map[libKey] != null
            || identifiers.contains(libKey.toIdentifier()).not()
            || libKey.version.isBlank()
        ) {
            return@coroutineScope
        }
        libVersions.add(libKey.toIdentifier(), libKey.version)
        val childrenResult = childrenCache.get(libKey)

        childrenResult.onSuccess { children ->
            map[libKey] = children
            children
                .filter { dependencyFilter.matches(it.toString()) }
                .forEach {
                    processDependency(it, identifiers)
                }
            logger.lifecycle("Processed: $libKey")
        }.rethrowCancellationException()
            .onFailure {
                when (it) {
                    is RepositoryException.RepositoryWithVersionInMetadataNotFound -> {
                        logger.warn("[WARN] ${it.message}")
                    }

                    else -> {
                        logger.error("[ERROR] $libKey : ${it.message}", it)
                        throw it
                    }
                }
            }
    }
}

internal data class GraphUseCaseParams(
    val configurations: Set<Configuration>,
    val startupFlags: StartupFlags,
) : UseCaseParams