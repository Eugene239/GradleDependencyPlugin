package io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey

/**
 * Huge collection of dependency with their children
 */
internal data class FlatDependencies(
    private val map: Map<LibKey, Collection<LibKey>>
) {

    fun getData(): Map<String, List<String>> {
        return map.mapKeys { it.key.toString() }
            .mapValues { it.value.map { lib -> lib.toString() } }
            .toSortedMap()
            .toMap()
    }
}