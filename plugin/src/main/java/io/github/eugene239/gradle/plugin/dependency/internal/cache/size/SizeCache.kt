package io.github.eugene239.gradle.plugin.dependency.internal.cache.size

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryByNameCache
import io.github.eugene239.gradle.plugin.dependency.internal.coRunCatching
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import java.util.concurrent.ConcurrentHashMap

internal class SizeCache {
    private val repositoryCache: RepositoryByNameCache by di()
    private val mavenService: MavenService by di()
    private val pomCache: PomCache by di()

    private val cache = ConcurrentHashMap<LibKey, Result<Long>>()


    suspend fun getSize(libKey: LibKey, repositoryName: String): Result<Long> {
        cache[libKey]?.let {
            return it
        }
        coRunCatching {
            repositoryCache.get(libKey, repositoryName)
        }.onFailure {
            cache[libKey] = Result.failure(it)
        }.onSuccess { repository ->
            val pomResult = pomCache.get(libKey, repositoryName)

            pomResult.onFailure {
                cache[libKey] = Result.failure(it)
            }.onSuccess { pom ->
                val packaging = pom.packaging ?: "jar"
                val result: Result<Long> = coRunCatching { mavenService.getSize(libKey, repository, packaging) }
                    .recoverCatching {
                        if (pom.packaging == null) {
                            mavenService.getSize(libKey, repository, "aar")
                        } else {
                            throw it
                        }
                    }
                cache[libKey] = result
            }
        }
        return cache[libKey]!!
    }
}