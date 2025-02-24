package io.github.eugene239.gradle.plugin.dependency.internal.cache.repository

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.cache.Cache
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.gradle.internal.cc.base.logger

internal class RepositoryCache(
    private val repositoryProvider: RepositoryProvider,
    private val mavenService: MavenService,
    private val ioDispatcher: CoroutineDispatcher
) : Cache<LibIdentifier, Result<Repository>> {

    private val map = ConcurrentMap<LibIdentifier, Result<Repository>>()

    override suspend fun get(key: LibIdentifier): Result<Repository> {
        map[key]?.let { return it }

        logger.info("RepositoryCache missed $key")
        val repository = findRepository(key)
        val value = if (repository != null) {
            Result.success(repository)
        } else {
            Result.failure(Exception("Repository not found for: $key"))
        }
        map[key] = value
        return value

    }


    private suspend fun findRepository(key: LibIdentifier): Repository? {
        val repositories = repositoryProvider.getRepositories()
        var result: Repository? = null
        withContext(ioDispatcher) {
            logger.info("start search")
            val results = repositories
                .map { repository -> async { if (mavenService.isMetadataExists(key, repository).getOrNull() == true) repository else null } }
                .awaitAll()
                .filterNotNull()

            logger.info("found in: $results")
            result = results.firstOrNull()
        }
        return result
    }
}