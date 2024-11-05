package io.epavlov.gradle.plugin.dependency.internal.cache.dependency

import io.epavlov.gradle.plugin.dependency.internal.LibKey
import io.epavlov.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.epavlov.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.epavlov.gradle.plugin.dependency.internal.pom.PomXMLParser
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.logging.Logger
import java.util.Collections

// todo make parallel
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

    var limit = Int.MAX_VALUE

    @Deprecated("Use fillRealChildren")
    private fun fillChildren(libKey: LibKey, children: Collection<DependencyResult>) {
        cache.computeIfAbsent(libKey) { children.filter().map { it.toLibKey() }.toSet() }
        children.filter()
            .filter { cache.containsKey(it.toLibKey()).not() }
            .forEach { dependency ->
                fillChildren(dependency.toLibKey(), dependency.selected.dependencies)
            }
    }

    suspend fun fillRealChildren(libKey: LibKey, parent: LibKey?, iteration: Int = 0) {
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

    private fun DependencyResult.toLibKey(): LibKey {
        val selected = (this as ResolvedDependencyResult).selected
        val moduleVersion = selected.moduleVersion
            ?: throw Exception("Module version not found for ${selected.id.displayName}")
        return LibKey(
            group = moduleVersion.group,
            module = moduleVersion.module.name,
            version = moduleVersion.version
        )
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
