package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.StartupFlags
import io.github.eugene239.gradle.plugin.dependency.internal.cache.children.ChildrenCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryCache
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.DefaultMavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.toLibKey
import io.github.eugene239.gradle.plugin.dependency.internal.ui.DefaultUiSaver
import io.github.eugene239.gradle.plugin.dependency.internal.ui.UiSaver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.logging.Logger
import java.io.File

internal class GraphUseCase(
    private val rootDir: File,
    private val logger: Logger,
    private val repositoryProvider: RepositoryProvider,

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

    override suspend fun execute(params: GraphUseCaseParams): File {
        rootDir.mkdirs()

        params.configurations.map { configuration ->
            processConfiguration(configuration, params.filter)
        }

        val result = uiSaver.save(rootDir)
        return result
    }

    private suspend fun processConfiguration(
        configuration: Configuration,
        filter: DependencyFilter?
    ) {
        logger.lifecycle("####### PROCESSING ${configuration.name}")
        val dependencies = configuration.incoming.resolutionResult.root.dependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .filter { filter?.matches(it) != false }
            .toSet()
            .map { it.toLibKey() }


        withContext(ioDispatcher) {
            dependencies
                // .filterIndexed { index, libKey -> index == 3 }
                .map {
                    async { processDependency(libKey = it) }
                }.awaitAll()
//                .forEach {
//                    processDependency(it)
//                }
        }

        println("%%%%%%%%%%%%%% ERRORS %%%%%%%%%%%%%%%%")
        val errors = childrenCache.getErrors()
        errors.forEach { entry ->
            println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
            println(entry.key)
            entry.value.exceptionOrNull()?.printStackTrace()
        }

        logger.lifecycle("####### PROCESSING ${configuration.name} END")
    }

    private suspend fun processDependency(libKey: LibKey) {
        val children = childrenCache.get(libKey)
        children.onSuccess {
            logger.info("----------------- CHILDREN ------------------------")
            logger.info("$libKey")
            it.forEach { child ->
                logger.info("[$libKey] -- $child")
                processDependency(child)
            }
            logger.info("----------------- CHILDREN END------------------------")
        }.onFailure {
            println("ERRRRROOOOOOR $libKey")
            it.printStackTrace()
        }
    }
}


internal data class GraphUseCaseParams(
    val configurations: Set<Configuration>,
    val startupFlags: StartupFlags,
    val filter: DependencyFilter?,
) : UseCaseParams