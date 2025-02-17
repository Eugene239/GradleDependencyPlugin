package io.github.eugene239.gradle.plugin.dependency.internal.cache.dependency

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.pom.PomXMLParser
import io.github.eugene239.gradle.plugin.dependency.internal.toLibKey
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.logging.Logger
import java.util.Collections

internal class DependencyCache(
    private val xmlParser: PomXMLParser,
    private val dependencyFilter: DependencyFilter?,
    private val pomCache: PomCache,
    private val logger: Logger
) {

    private val cache: MutableMap<LibKey, Set<LibKey>> = Collections.synchronizedMap(HashMap())
    private val failed = HashSet<FailedDependency>()

    suspend fun fill(dependencies: Collection<DependencyResult>) {
        coroutineScope {
            dependencies.filter().map {
                async {
                    fillRealChildren(libKey = it.toLibKey(), parent = null)
                }
            }.awaitAll()
        }
    }

    private var limit = Int.MAX_VALUE

    private suspend fun fillRealChildren(libKey: LibKey, parent: LibKey?, iteration: Int = 0) {
        if (cache[libKey] != null) return
        logger.info("fillRealChildren $libKey,  cached: ${cache.containsKey(libKey)}, cacheSize: ${cache.size}")
        val pom = pomCache.getPom(libKey)
        pom.onSuccess {
            val pomChildren = xmlParser.parse(pom.getOrThrow())
            logger.info("POM: $pom, children: ${pomChildren.size}")
            cache[libKey] = pomChildren
            if (iteration < limit) {
                coroutineScope {
                    pomChildren
                        .filter { key -> dependencyFilter?.matches(key.toString()) != false }
                        .toSet()
                        .filter { key -> cache.containsKey(key).not() }
                        .map { key ->
                            async { fillRealChildren(key, libKey, iteration + 1) }
                        }.awaitAll()
                }
            }
        }.onFailure {
            logger.warn("Can't find pom for $libKey", it)
            failed.add(
                FailedDependency(
                    parent = parent,
                    lib = libKey,
                    error = it
                )
            )
        }
    }

    private fun Collection<DependencyResult>.filter(): Collection<ResolvedDependencyResult> {
        return this.filterIsInstance<ResolvedDependencyResult>()
            .filter { dependencyFilter?.matches(it) != false }
            .toSet()
    }

    fun cachedValues(): HashMap<LibKey, Set<LibKey>> {
        return HashMap(cache)
    }

    fun failedValues(): Set<FailedDependency> {
        return HashSet(failed)
    }
}
