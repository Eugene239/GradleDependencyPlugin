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
import io.github.eugene239.gradle.plugin.dependency.internal.LibVersions
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryByNameCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.size.SizeCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.version.LatestVersionCache
import io.github.eugene239.gradle.plugin.dependency.internal.getLibDetails
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.ConfigurationResult
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.PluginConfiguration
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model.ProjectConfiguration
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
    private val latestVersionCache: LatestVersionCache = LatestVersionCache(
        logger = logger,
        repositoryCache = repositoryCache
    ),
    private val sizeCache: SizeCache = SizeCache(
        repositoryCache = repositoryCache,
        mavenService = mavenService,
        pomCache = pomCache
    )
) : UseCase<GraphUseCaseParams, File> {

    private val libIdToRepositoryName = HashMap<LibIdentifier, String?>()

    override suspend fun execute(params: GraphUseCaseParams): File = coroutineScope {
        logger.info("Start execution with params: $params")
        rootDir.mkdirs()

        val allTopLibDetails = params.configurations
            .asSequence()
            .map { it.getLibDetails(dependencyFilter, isSubmodule) }
            .flatten()
            .toSet()

        preProcessDependencies(allTopLibDetails)

        val results = params.configurations.map {
            async { processConfiguration(it) }
        }.awaitAll()

        return@coroutineScope output.save(
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
            results = results,
            latestVersions = getLatestVersions(params.startupFlags.fetchVersions, allTopLibDetails),
            libSizes = getLibSizes(params.startupFlags.fetchLibSize, allTopLibDetails)
        )
    }

    // Fill cache for all dependencies in all configurations
    private suspend fun preProcessDependencies(allTopLibDetails: Set<LibDetails>) = coroutineScope {
        val dependencies = allTopLibDetails
            .filter { it.isSubmodule.not() }
            .toSet()

        libIdToRepositoryName.putAll(dependencies.associate { it.key.toIdentifier() to it.repositoryId })
        val identifiers = dependencies.map { it.key.toIdentifier() }.toSet()

        processDependencies(
            dependencies = dependencies.map { it.key }.toSet(),
            identifiers = identifiers,
            flatDependencies = hashMapOf(),
            libVersions = LibVersions()
        )
    }

    private suspend fun processConfiguration(configuration: Configuration): ConfigurationResult {
        val libVersions = LibVersions()
        val topDependencies = configuration.getLibDetails(dependencyFilter, isSubmodule)

        val identifiers = topDependencies.map { it.key.toIdentifier() }.toSet()

        val submodules = topDependencies
            .filter { it.isSubmodule }
            .toSet()

        val dependencies = topDependencies
            .filter { it.isSubmodule.not() }
            .map { it.key }
            .toSet()

        val flatDependencies = ConcurrentHashMap<LibKey, Set<LibKey>>()

        withContext(ioDispatcher) {
            listOf(
                async { processDependencies(dependencies, identifiers, flatDependencies, libVersions) },
                async { processSubmodules(submodules, flatDependencies, identifiers, libVersions) }
            ).awaitAll()
        }
        return ConfigurationResult(
            configuration = configuration.name,
            versions = libVersions,
            topDependencies = topDependencies.map { it.key }.toSet(),
            flatDependencies = flatDependencies
        )
    }

    private suspend fun processDependencies(
        dependencies: Set<LibKey>,
        identifiers: Set<LibIdentifier>,
        flatDependencies: MutableMap<LibKey, Set<LibKey>>,
        libVersions: LibVersions? = null,
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
                        libVersions?.setResolved(dependency.toIdentifier(), dependency.version)
                    }
                    processDependency(
                        libKey = dependency,
                        flatDependencies,
                        libVersions = libVersions,
                        libIdToRepositoryName[dependency.toIdentifier()]
                    ).rethrowCancellationException()
                        .onSuccess { children ->
                            logger.lifecycle("Processed: $dependency  time: ${System.currentTimeMillis() - startTime} ms")
                            flatDependencies[dependency] = children
                                .filter { dependencyFilter.matches(it.toString()) }
                                .filter { identifiers.contains(it.toIdentifier()) }
                                .toSet()
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

            processDependencies(subDependencies, identifiers, flatDependencies, libVersions, level + 1)
        }
    }

    private suspend fun processSubmodules(
        submodules: Set<LibDetails>,
        flatDependencies: MutableMap<LibKey, Set<LibKey>>,
        identifiers: Set<LibIdentifier>,
        libVersions: LibVersions
    ) {
        withContext(ioDispatcher) {
            submodules.map {
                async {
                    processSubmodule(it, flatDependencies, identifiers, libVersions)
                }
            }.awaitAll()
        }
    }

    private suspend fun processDependency(
        libKey: LibKey,
        flatDependencies: MutableMap<LibKey, Set<LibKey>>,
        libVersions: LibVersions?,
        repositoryName: String?,
    ): Result<Set<LibKey>> {
        if (libKey.version.isBlank()) {
            return Result.success(emptySet())
        }
        if (repositoryName.isNullOrBlank()) {
            throw Exception("Can't process dependency $libKey, repositoryName is null")
        }
        libVersions?.add(libKey.toIdentifier(), libKey.version)
        val resolved = flatDependencies[libKey]
        return if (resolved != null) {
            Result.success(resolved)
        } else {
            childrenCache.get(libKey, repositoryName)
                .map { it.toSet() }
        }
    }

    private suspend fun processSubmodule(
        libDetails: LibDetails,
        flatDependencies: MutableMap<LibKey, Set<LibKey>>,
        identifiers: Set<LibIdentifier>,
        libVersions: LibVersions
    ) {
        logger.debug("processSubmodule: ${libDetails.key}")
        if (flatDependencies[libDetails.key] != null) {
            return
        }
        val children = libDetails.result?.dependencies
            ?.filterIsInstance<ResolvedDependencyResult>()
            .orEmpty()

        val detailsSet = children
            .filter { child -> dependencyFilter.matches(child) }
            .map { child -> child.toLibDetails(isSubmodule = isSubmodule.invoke(child)) }
            .toSet()
        flatDependencies[libDetails.key] = detailsSet.map { it.key }.toSet()

        processSubmodules(detailsSet.filter { it.isSubmodule }.toSet(), flatDependencies, identifiers, libVersions)
        processDependencies(detailsSet.filter { it.isSubmodule.not() }.map { it.key }.toSet(), identifiers, flatDependencies, libVersions, 1)
    }

    private suspend fun getLatestVersions(fetchVersions: Boolean, libs: Set<LibDetails>): Map<LibIdentifier, String>? = coroutineScope {
        if (fetchVersions.not()) return@coroutineScope null
        return@coroutineScope libs
            .filter { it.isSubmodule.not() }
            .map { details ->
                async {
                    details.key.toIdentifier() to latestVersionCache.get(details.key, details.repositoryId!!)?.toString()
                }
            }
            .awaitAll()
            .toMap()
            .mapNotNull { (key, value) -> value?.let { key to value } }
            .toMap()
    }

    private suspend fun getLibSizes(fetchSize: Boolean, libs: Set<LibDetails>): Map<LibKey, Long>? = coroutineScope {
        if (fetchSize.not()) return@coroutineScope null
        return@coroutineScope libs
            .filter { it.isSubmodule.not() }
            .map { details ->
                async {
                    details.key to sizeCache.getSize(details.key, details.repositoryId!!)
                        .onFailure { logger.warn("Can't get lib size for ${details.key}", it) }
                        .getOrNull()
                }
            }
            .awaitAll()
            .toMap()
            .mapNotNull { (key, value) -> value?.let { key to value } }
            .toMap()
    }
}

internal data class GraphUseCaseParams(
    val configurations: Set<Configuration>,
    val startupFlags: StartupFlags,
) : UseCaseParams