package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.children.ChildrenCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryCache
import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultRepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.gradle.api.logging.Logger

internal class SingleDependencyUseCase(
    private val logger: Logger,
    private val repositoryProvider: DefaultRepositoryProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mavenService: MavenService,
    private val repositoryCache: RepositoryCache = RepositoryCache(
        repositoryProvider = repositoryProvider,
        mavenService = mavenService,
        ioDispatcher = ioDispatcher
    ),
    private val pomCache: PomCache = PomCache(
        mavenService = mavenService,
        repositoryCache = repositoryCache
    ),
    private val childrenCache: ChildrenCache = ChildrenCache(
        pomCache = pomCache
    )
) : UseCase<SingleDependencyUseCaseParams, Unit> {

    override suspend fun execute(params: SingleDependencyUseCaseParams) {
        processDependency(params.libKey)
    }

    private suspend fun processDependency(libKey: LibKey) {
        logger.info("processDependency: $libKey")
        val children = childrenCache.get(libKey)
        children.onSuccess {
            logger.info("$libKey")
            it.forEach { child ->
                logger.info("[$libKey] -- $child")
                processDependency(child)
            }
        }.onFailure {
            throw Exception("Dependency processing error", it)
        }
    }
}

internal data class SingleDependencyUseCaseParams(
    val libKey: LibKey
) : UseCaseParams