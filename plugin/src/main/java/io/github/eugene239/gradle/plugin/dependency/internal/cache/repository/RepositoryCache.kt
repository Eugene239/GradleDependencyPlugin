package io.github.eugene239.gradle.plugin.dependency.internal.cache.repository

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.Cache
import io.github.eugene239.gradle.plugin.dependency.internal.containsVersion
import io.github.eugene239.gradle.plugin.dependency.internal.exception.RepositoryException
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.rethrowCancellationException
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenMetadata
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import io.github.eugene239.gradle.plugin.dependency.internal.toIdentifier
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.gradle.internal.cc.base.logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Deprecated("Use RepositoryByNameCache")
internal class RepositoryCache(
    private val repositoryProvider: RepositoryProvider,
    private val mavenService: MavenService,
    private val ioDispatcher: CoroutineDispatcher
) : Cache<LibKey, Result<Repository>> {

    private val cache = ConcurrentMap<LibIdentifier, HashMap<Repository, MavenMetadata>>()
    private val counter = ConcurrentHashMap<Repository, AtomicInteger>()

    override suspend fun get(key: LibKey): Result<Repository> {
        cache[key.toIdentifier()]?.let { map ->
            map.firstNotNullOfOrNull { entry ->
                if (entry.value.containsVersion(key)) {
                    logger.debug("RepositoryCache found: $key, ${entry.key}")
                    entry.key
                } else {
                    null
                }
            }?.let { entry ->
                return Result.success(entry)
            }
        }
        logger.debug("RepositoryCache missed: $key")
        val identifier = key.toIdentifier()
        cache.putIfAbsent(identifier, hashMapOf())
        return kotlin.runCatching { findRepository(key) }.rethrowCancellationException()
    }

    suspend fun getMetadataSet(key: LibKey): Set<MavenMetadata> {
        cache[key.toIdentifier()]?.let { hashmap ->
            return hashmap.map { entry -> entry.value }.toSet()
        }
        get(key)
            .onSuccess {
                return cache[key.toIdentifier()]?.map { entry -> entry.value }?.toSet() ?: emptySet()
            }
            .onFailure {
                logger.warn(it.message, it)
                return emptySet()
            }
        return emptySet()
    }


    private suspend fun findRepository(key: LibKey): Repository {
        val repositories = repositoryProvider.getRepositories()
        val metadataSet = hashSetOf<MavenMetadata>()
        val identifier = key.toIdentifier()
        return withContext(ioDispatcher) {
            repositories
                .sortedByDescending { counter[it]?.get() ?: 0 }
                .forEach { repository ->
                    val result = mavenService.isMetadataExists(identifier, repository)
                    result.onSuccess {
                        if (it) {
                            val metadata = mavenService.getMetadata(identifier, repository)
                            metadataSet.add(metadata)
                            cache.putIfAbsent(identifier, hashMapOf())
                            cache[identifier]?.putIfAbsent(repository, metadata)
                        }
                    }.rethrowCancellationException()
                        .onFailure {
                            when (it) {
                                is HttpRequestTimeoutException -> {
                                    logger.warn("TimeoutException for $key in ${repository.url}, try to change `connection-timeout` parameter and restart task")
                                }

                                else -> {
                                    logger.warn("failed to get metadata for $key in ${repository.url}", it)
                                }
                            }
                        }
                }
            cache[identifier]
                ?.entries
                ?.firstOrNull { entry -> entry.value.containsVersion(key) }
                ?.let {
                    return@withContext it.key
                }
            if (metadataSet.isEmpty()) {
                throw RepositoryException.RepositoryNotFoundException(key)
            } else {
                throw RepositoryException.RepositoryWithVersionInMetadataNotFound(key, metadataSet)
            }
        }
    }

}