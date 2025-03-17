package io.github.eugene239.gradle.plugin.dependency.internal

import org.gradle.api.artifacts.result.ResolvedComponentResult


internal data class LibIdentifier(
    val group: String,
    val module: String
) {
    override fun toString(): String {
        return "$group:$module"
    }
}

internal data class LibKey(
    val group: String,
    val module: String,
    val version: String,
) {
    override fun toString(): String {
        return "$group:$module:$version"
    }
}

internal data class LibDetails(
    val key: LibKey,
    val isStrict: Boolean,
    val isSubmodule: Boolean,
    @Transient
    // not null only if submodule
    val result: ResolvedComponentResult? = null
)

internal fun LibKey.toIdentifier(): LibIdentifier {
    return LibIdentifier(
        group = group,
        module = module
    )
}

