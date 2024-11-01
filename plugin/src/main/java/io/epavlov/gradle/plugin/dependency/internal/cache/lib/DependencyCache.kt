package io.epavlov.gradle.plugin.dependency.internal.cache.lib

import io.epavlov.gradle.plugin.dependency.internal.filter.DependencyFilter
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import java.util.Collections

// todo find real libraries versions and their children
internal class DependencyCache(
    private val dependencyFilter: DependencyFilter?
) {
    private val cache: MutableMap<LibKey, List<LibKey>> = Collections.synchronizedMap(HashMap())

    fun fill(dependencies: Collection<DependencyResult>) {
        dependencies.filter().forEach {
            fillChildren(it.toLibKey(), it.selected.dependencies)
        }
    }

    private fun fillChildren(libKey: LibKey, children: Collection<DependencyResult>) {
        cache.computeIfAbsent(libKey) { children.filter().map { it.toLibKey() } }
        children.filter()
            .filter { cache.containsKey(it.toLibKey()).not() }
            .forEach { dependency ->
                fillChildren(dependency.toLibKey(), dependency.selected.dependencies)
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

    fun cachedValues(): HashMap<LibKey, List<LibKey>> {
        return HashMap(cache)
    }
}
