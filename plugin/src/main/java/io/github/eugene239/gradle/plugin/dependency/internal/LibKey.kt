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
    val version: String
) {
    override fun toString(): String {
        return "$group:$module:$version"
    }
}

internal fun LibKey.toIdentifier(): LibIdentifier {
    return LibIdentifier(
        group = group,
        module = module
    )
}

