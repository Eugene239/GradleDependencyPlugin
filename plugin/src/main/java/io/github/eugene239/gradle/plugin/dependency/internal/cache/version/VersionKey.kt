package io.github.eugene239.gradle.plugin.dependency.internal.cache.version

@Deprecated("TODO DELETE")
internal data class VersionKey(
    val group: String,
    val module: String
) {
    override fun toString(): String {
        return "$group:$module"
    }
}