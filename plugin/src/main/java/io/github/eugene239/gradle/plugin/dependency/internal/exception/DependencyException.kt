package io.github.eugene239.gradle.plugin.dependency.internal.exception

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey

internal sealed class DependencyException(override val message: String) : Exception(message) {

    data class VersionNotFoundException(
        val groupId: String,
        val module: String,
        val parent: LibKey?
    ) : DependencyException(
        message = "Can't find version of dependency $groupId:$module ${
            if (parent != null) {
                "(parent: $parent)"
            } else {
                ""
            }
        }"
    )
}