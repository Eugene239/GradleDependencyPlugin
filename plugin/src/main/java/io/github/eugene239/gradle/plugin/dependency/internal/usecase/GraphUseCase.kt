package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.LibDetails
import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.gradle.plugin.dependency.internal.cache.children.ChildrenCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.exception.RepositoryException
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.DefaultGraphOutput
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.GraphOutput
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.FlatDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.LibVersions
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryByNameCache
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.ProjectConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.TopDependencies
import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultRepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.rethrowCancellationException
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
    private val isSubmodule: (ResolvedDependencyResult) -> Boolean,
    private val mavenService: MavenService,
    private val uiSaver: UiSaver = DefaultUiSaver(logger = logger),
    private val output: GraphOutput = DefaultGraphOutput(
        rootDir = rootDir,
        uiSaver = uiSaver
    ),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val repositoryCache: RepositoryByNameCache = RepositoryByNameCache(
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
    ),
) : UseCase<GraphUseCaseParams, File> {

    private val map = ConcurrentHashMap<LibKey, List<LibKey>>()
    private val libIdToRepositoryName = HashMap<LibIdentifier, String?>()

    override suspend fun execute(params: GraphUseCaseParams): File {
        logger.info("Start execution with params: $params")
        rootDir.mkdirs()

        val topDependencies = params.configurations.associateWith { configuration ->
            configuration.incoming.resolutionResult.root.dependencies
                .asSequence()
                .filterIsInstance<ResolvedDependencyResult>()
                .filter { dependencyFilter.matches(it) }
                .toSet()
                .map { dependency -> dependency.toLibDetails(isSubmodule = isSubmodule.invoke(dependency)) }
                .toSet()
        }

        libIdToRepositoryName.putAll(
            topDependencies
                .values
                .toSet()
                .flatten()
                .associate { it.key.toIdentifier() to it.repositoryId }
        )

        // all dependencies key to fetch information about
        val identifiers = topDependencies.map { entry -> entry.value }.flatten().map { it.key.toIdentifier() }.toSet()

        val confToLibVersions = withContext(ioDispatcher) {
            topDependencies
                .map { (configuration, libDetails) ->
                    processConfiguration(configuration, libDetails, identifiers)
                }
                .toMap()
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
                startupFlags = params.startupFlags
            ),
            topDependencies = TopDependencies(topDependencies.mapValues { entry -> entry.value.map { it.key }.toSet() }),
            flatDependencies = FlatDependencies(map),
            libVersions = confToLibVersions
                .mapKeys { it.key.name }
                .mapValues { it.value.getConflictData() }
        )
    }

    private suspend fun processConfiguration(
        configuration: Configuration,
        confDependencies: Set<LibDetails>,
        identifiers: Set<LibIdentifier>
    ): Pair<Configuration, LibVersions> {
        val libVersions = LibVersions()

        val submodules = confDependencies
            .filter { it.isSubmodule }
            .toSet()

        val dependencies = confDependencies
            .filter { it.isSubmodule.not() }
            .map { it.key }
            .toSet()

        withContext(ioDispatcher) {
            listOf(
                async { processDependencies(dependencies, identifiers, libVersions) },
                async { processSubmodules(submodules, identifiers, libVersions) }
            ).awaitAll()
        }
        return configuration to libVersions
    }

    private suspend fun processDependencies(
        dependencies: Set<LibKey>,
        identifiers: Set<LibIdentifier>,
        libVersions: LibVersions,
        level: Int = 0
    ) {
        val filteredDependencies = dependencies
            .filter { identifiers.contains(it.toIdentifier()) }

        if (filteredDependencies.isEmpty()) {
            return
        }

        logger.lifecycle("Process dependencies: ${filteredDependencies.size}, level: $level")
        withContext(ioDispatcher) {
            val subDependencies = filteredDependencies.map { dependency ->
                async {
                    val startTime = System.currentTimeMillis()
                    if (level == 0) {
                        libVersions.setResolved(dependency.toIdentifier(), dependency.version)
                    }
                    processDependency(
                        libKey = dependency,
                        libVersions = libVersions,
                        libIdToRepositoryName[dependency.toIdentifier()]
                    ).rethrowCancellationException()
                        .onSuccess { children ->
                            logger.lifecycle("Processed: $dependency  time: ${System.currentTimeMillis() - startTime} ms")
                            map[dependency] = children
                                .filter { dependencyFilter.matches(it.toString()) }
                                .filter { identifiers.contains(it.toIdentifier()) }
                        }
                        .onFailure {
                            when (it) {
                                is RepositoryException.RepositoryWithVersionInMetadataNotFound -> {
                                    logger.warn("[WARN] ${it.message}")
                                }

                                else -> {
                                    logger.error("[ERROR] $dependency: ${it.message}", it)
                                    throw it
                                }
                            }
                        }
                        .getOrElse { emptyList() }
                }
            }.awaitAll()
                .flatten()
                .toSet()

            processDependencies(subDependencies, identifiers, libVersions, level + 1)
        }
    }

    private suspend fun processSubmodules(
        submodules: Set<LibDetails>,
        identifiers: Set<LibIdentifier>,
        libVersions: LibVersions
    ) {
        withContext(ioDispatcher) {
            submodules.map {
                async {
                    processSubmodule(it, identifiers, libVersions)
                }
            }.awaitAll()
        }
    }

    private suspend fun processDependency(
        libKey: LibKey,
        libVersions: LibVersions,
        repositoryName: String?,
    ): Result<List<LibKey>> {
        if (libKey.version.isBlank()) {
            return Result.success(emptyList())
        }
        if (repositoryName.isNullOrBlank()) {
            throw Exception("Can't process dependency $libKey, repositoryName is null")
        }
        libVersions.add(libKey.toIdentifier(), libKey.version)
        val resolved = map[libKey]
        return if (resolved != null) {
            Result.success(resolved)
        } else {
            childrenCache.get(libKey, repositoryName)
        }
    }

    private suspend fun processSubmodule(
        libDetails: LibDetails,
        identifiers: Set<LibIdentifier>,
        libVersions: LibVersions
    ) {
        logger.debug("processSubmodule: ${libDetails.key}")
        if (map[libDetails.key] != null) {
            return
        }
        val children = libDetails.result?.dependencies
            ?.filterIsInstance<ResolvedDependencyResult>()
            .orEmpty()

        val detailsSet = children
            .filter { child-> dependencyFilter.matches(child) }
            .map { child -> child.toLibDetails(isSubmodule = isSubmodule.invoke(child)) }
            .toSet()
        map[libDetails.key] = detailsSet.map { it.key }

        processSubmodules(detailsSet.filter { it.isSubmodule }.toSet(), identifiers, libVersions)
        processDependencies(detailsSet.filter { it.isSubmodule.not() }.map { it.key }.toSet(), identifiers, libVersions, 1)
    }
}

internal data class GraphUseCaseParams(
    val configurations: Set<Configuration>,
    val startupFlags: StartupFlags,
) : UseCaseParams