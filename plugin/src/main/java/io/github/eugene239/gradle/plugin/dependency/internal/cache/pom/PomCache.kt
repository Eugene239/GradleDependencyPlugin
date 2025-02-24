package io.github.eugene239.gradle.plugin.dependency.internal.cache.pom

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.Cache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryCache
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.Pom
import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import org.gradle.internal.cc.base.logger
import java.util.concurrent.ConcurrentHashMap

internal class PomCache(
    private val mavenService: MavenService,
    private val repositoryCache: RepositoryCache
) : Cache<LibKey, Result<Pom>> {

    private val cache = ConcurrentHashMap<LibKey, Result<Pom>>()

    override suspend fun get(key: LibKey): Result<Pom> {
        cache[key]?.let { return it }

        logger.info("PomCache missed $key")
        val repository = repositoryCache.get(key)

        repository
            .onSuccess {
                val children = getPom(key, it)
                cache[key] = Result.success(children)
            }
            .onFailure {
                cache[key] = Result.failure(it)
            }

        return cache[key]!!
    }

    private suspend fun getPom(key: LibKey, repository: Repository): Pom {
        return mavenService.getPom(libKey = key, repository = repository)
    }
}