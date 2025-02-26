package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.gradle.plugin.dependency.internal.cache.children.ChildrenCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.rethrowCancellationException
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultRepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.DefaultMavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.toLibKey
import io.github.eugene239.gradle.plugin.dependency.internal.ui.DefaultUiSaver
import io.github.eugene239.gradle.plugin.dependency.internal.ui.UiSaver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
        logger = logger
    ),
    private val uiSaver: UiSaver = DefaultUiSaver(logger = logger),
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


    override suspend fun execute(params: GraphUseCaseParams): File {
        logger.info("Start execution with params: $params")
        rootDir.mkdirs()

        val semaphore = Semaphore(params.limit)

        val dependencies = params.configurations
            .map { configuration ->
                configuration.incoming.resolutionResult.root.dependencies
                    .asSequence()
                    .filterIsInstance<ResolvedDependencyResult>()
                    .filter { dependencyFilter.isSubmodule(it).not() && dependencyFilter.matches(it) }
                    .toSet()
                    .map { it.toLibKey() }
            }
            .flatten()
            .toSet()

        logger.lifecycle("Getting info about ${dependencies.size} dependencies")
        if (logger.isInfoEnabled) {
            dependencies.forEach {
                logger.info("- $it")
            }
        }

        withContext(ioDispatcher) {
            dependencies.map {
                async { semaphore.withPermit { processDependency(libKey = it) } }
            }.awaitAll()
        }

        println("%%%%%%%%%%%%%% ERRORS %%%%%%%%%%%%%%%%")
        val errors = childrenCache.getErrors()
        errors.forEach { entry ->
            println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
            println(entry.key)
            entry.value.exceptionOrNull()?.printStackTrace()
        }

        val result = uiSaver.save(rootDir)
        return result
    }

    private suspend fun processDependency(libKey: LibKey): Unit = coroutineScope {
        logger.info("processDependency: $libKey")
        if (map[libKey] != null) return@coroutineScope

        val childrenResult = childrenCache.get(libKey)

        childrenResult.onSuccess { children ->
            map[libKey] = children
            children
                .filter { dependencyFilter.matches(it.toString()) }
                .forEach { processDependency(it) }

        }.rethrowCancellationException()
            .onFailure {
                logger.error("[ERROR] $libKey : ${it.message}", it)
                throw it
            }
    }
}

internal data class GraphUseCaseParams(
    val configurations: Set<Configuration>,
    val startupFlags: StartupFlags,
    val limit: Int
) : UseCaseParams