package io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import org.gradle.api.artifacts.Configuration

/**
 * Top dependencies of configuration to build graph starting from them
 */
internal data class TopDependencies(
    val map: Map<Configuration, Set<LibKey>>
) {

    fun getData(): Map<String, Set<String>> {
        return map.mapKeys { it.key.name }
            .mapValues { it.value.map { key -> key.toString() }.toSet() }
    }
}

