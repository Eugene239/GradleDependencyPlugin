package io.github.eugene239.gradle.plugin.dependency.internal.exception

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier

internal sealed class RepositoryException(override val message: String) : Exception(message) {
    internal data class RepositoryNotFoundException(private val lib: LibIdentifier) : RepositoryException("Repository not found for lib: $lib")
}