package io.github.eugene239.gradle.plugin.dependency.internal.cache.repository

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.cache.Cache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.rethrowCancellationException
import io.github.eugene239.gradle.plugin.dependency.internal.exception.RepositoryException
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.gradle.internal.cc.base.logger

internal class RepositoryCache(
    private val repositoryProvider: RepositoryProvider,
    private val mavenService: MavenService,
    private val ioDispatcher: CoroutineDispatcher
) : Cache<LibIdentifier, Result<Repository>> {

    private val groupCache = ConcurrentMap<String, Repository>()
    private val cache = ConcurrentMap<LibIdentifier, Result<Repository>>()

    override suspend fun get(key: LibIdentifier): Result<Repository> {
        cache[key]?.let {
            logger.info("RepositoryCache found: $key")
            return it
        }

        logger.info("RepositoryCache missed $key")
        cache[key] = kotlin.runCatching { findRepository(key) }.rethrowCancellationException()
        return cache[key]!!

    }


    private suspend fun findRepository(key: LibIdentifier): Repository {
        val repositories = repositoryProvider.getRepositories()
        return withContext(ioDispatcher) {
            repositories
                .apply { if (groupCache[key.group] != null) this.sortedByDescending { groupCache[key.group] != null } }
                .forEach { repository ->
                    if (mavenService.isMetadataExists(key, repository).getOrNull() == true) {
                        groupCache[key.group] = repository
                        return@withContext repository
                    }
                }
            throw RepositoryException.RepositoryNotFoundException(key)
        }
    }
}