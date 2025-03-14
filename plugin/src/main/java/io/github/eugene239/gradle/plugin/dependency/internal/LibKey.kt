package io.github.eugene239.gradle.plugin.dependency.internal

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
    val isSubmodule: Boolean
)

internal fun LibKey.toIdentifier(): LibIdentifier {
    return LibIdentifier(
        group = group,
        module = module
    )
}

