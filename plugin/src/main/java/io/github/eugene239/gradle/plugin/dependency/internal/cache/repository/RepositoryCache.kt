package io.github.eugene239.gradle.plugin.dependency.internal.cache.repository

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.cache.Cache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.rethrowCancellationException
import io.github.eugene239.gradle.plugin.dependency.internal.exception.RepositoryException
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.gradle.internal.cc.base.logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

// TODO Add metadata checking with different versions
internal class RepositoryCache(
    private val repositoryProvider: RepositoryProvider,
    private val mavenService: MavenService,
    private val ioDispatcher: CoroutineDispatcher
) : Cache<LibIdentifier, Result<Repository>> {

    private val cache = ConcurrentMap<LibIdentifier, Result<Repository>>()
    private val counter = ConcurrentHashMap<Repository, AtomicInteger>()

    override suspend fun get(key: LibIdentifier): Result<Repository> {
        cache[key]?.let {
            logger.debug("RepositoryCache found: $key")
            return it
        }
        logger.debug("RepositoryCache missed: $key")
        cache[key] = kotlin.runCatching { findRepository(key) }.rethrowCancellationException()
        return cache[key]!!

    }


    private suspend fun findRepository(key: LibIdentifier): Repository {
        val repositories = repositoryProvider.getRepositories()
        return withContext(ioDispatcher) {
            repositories
                .sortedByDescending { counter[it]?.get() ?: 0 }
                .forEach { repository ->
                    val result = mavenService.isMetadataExists(key, repository)
                    result.onSuccess {
                        if (it) {
                            counter.putIfAbsent(repository, AtomicInteger(0))
                            counter[repository]?.incrementAndGet()
                            logger.debug("found Repository: $key -> $repository counter: ${counter[repository]?.get()}")
                            return@withContext repository
                        }
                    }.onFailure {
                        if (it !is CancellationException) {
                            logger.warn("failed to get metadata for $key in $repository", it)
                        }
                    }
                }
            throw RepositoryException.RepositoryNotFoundException(key)
        }
    }
}