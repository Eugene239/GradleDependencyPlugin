package io.epavlov.gradle.plugin.dependency.internal.cache.version

internal data class VersionKey(
    val group: String,
    val module: String
) {
    override fun toString(): String {
        return "$group:$module"
    }
}