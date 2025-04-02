package io.github.eugene239.gradle.plugin.dependency.internal.cache.repository

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.containsVersion
import io.github.eugene239.gradle.plugin.dependency.internal.exception.RepositoryException
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.rethrowCancellationException
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenMetadata
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import io.github.eugene239.gradle.plugin.dependency.internal.toIdentifier
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.gradle.internal.cc.base.logger
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.Throws

internal class RepositoryByNameCache(
    private val repositoryProvider: RepositoryProvider,
    private val mavenService: MavenService,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val cache = ConcurrentHashMap<LibIdentifier, Repository>()
    private val metadataCache = ConcurrentHashMap<LibIdentifier, Map<Repository, MavenMetadata>>()

    @Throws(RepositoryException.MissingRepositoryName::class, RepositoryException.RepositoryWithVersionInMetadataNotFound::class, RepositoryException.RepositoryNotFoundException::class)
    suspend fun get(key: LibKey, repositoryName: String?): Repository {
        if (repositoryName == null) throw RepositoryException.MissingRepositoryName(key)
        cache[key.toIdentifier()]?.let { return it }
        return findRepository(key, repositoryName)
    }

    suspend fun getMetadataSet(key: LibKey, repositoryName: String): Set<MavenMetadata> {
        metadataCache[key.toIdentifier()]?.let { hashmap ->
            return hashmap.map { entry -> entry.value }.toSet()
        }
        get(key, repositoryName)
        return metadataCache[key.toIdentifier()]?.map { entry -> entry.value }?.toSet() ?: emptySet()
    }

    private suspend fun findRepository(libKey: LibKey, repositoryName: String): Repository = withContext(ioDispatcher) {
        val repositories = repositoryProvider.getRepositories()
            .filter { it.name == repositoryName }
        val repositoryMetadataMap = HashMap<Repository, MavenMetadata>()
        val identifier = libKey.toIdentifier()
        repositories.forEach { repository ->
            val result = mavenService.isMetadataExists(identifier, repository)
            result.onSuccess {
                if (it) {
                    val metadata = mavenService.getMetadata(identifier, repository)
                    repositoryMetadataMap[repository] = metadata
                }
            }.rethrowCancellationException()
                .onFailure {
                    when (it) {
                        is HttpRequestTimeoutException -> {
                            logger.warn("TimeoutException for $libKey in ${repository.url}, try to change `connection-timeout` parameter and restart task")
                        }

                        else -> {
                            logger.warn("failed to get metadata for $libKey in ${repository.url}", it)
                        }
                    }
                }
        }
        metadataCache[libKey.toIdentifier()] = repositoryMetadataMap

        val repository = repositoryMetadataMap
            .filter { it.value.containsVersion(libKey) }
            .keys
            .firstOrNull()

        repository?.let {
            cache[libKey.toIdentifier()] = it
            return@withContext it
        }

        val metadataSet = repositoryMetadataMap.values.toSet()
        if (metadataSet.isEmpty()) {
            throw RepositoryException.RepositoryNotFoundException(libKey)
        } else {
            throw RepositoryException.RepositoryWithVersionInMetadataNotFound(libKey, metadataSet)
        }
    }

}