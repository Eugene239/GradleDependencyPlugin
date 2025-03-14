package io.github.eugene239.gradle.plugin.dependency.internal.cache.pom

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryByNameCache
import io.github.eugene239.gradle.plugin.dependency.internal.rethrowCancellationException
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.Pom
import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import org.gradle.internal.cc.base.logger
import java.util.concurrent.ConcurrentHashMap

internal class PomCache(
    private val mavenService: MavenService,
    private val repositoryCache: RepositoryByNameCache
) {

    private val cache = ConcurrentHashMap<LibKey, Result<Pom>>()

    suspend fun get(key: LibKey, repositoryName: String): Result<Pom> {
        cache[key]?.let {
            logger.debug("PomCache found: $key")
            return it
        }

        logger.debug("PomCache missed $key")
        val repository = repositoryCache.get(key, repositoryName)

        cache[key] = kotlin.runCatching { getPom(key, repository) }.rethrowCancellationException()

        return cache[key]!!
    }

    private suspend fun getPom(key: LibKey, repository: Repository): Pom {
        return mavenService.getPom(libKey = key, repository = repository)
    }
}