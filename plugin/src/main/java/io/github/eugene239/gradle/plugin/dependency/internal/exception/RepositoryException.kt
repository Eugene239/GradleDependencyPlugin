package io.github.eugene239.gradle.plugin.dependency.internal.exception

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenMetadata

internal sealed class RepositoryException(override val message: String) : Exception(message) {
    internal data class RepositoryNotFoundException(private val lib: LibKey) : RepositoryException("Repository not found for lib: $lib")
    internal data class MissingRepositoryName(private val lib: LibKey) : RepositoryException("Missing repository name lib: $lib")
    internal data class RepositoryWithVersionInMetadataNotFound(private val lib: LibKey, private val metadataSet: Set<MavenMetadata>) :
        RepositoryException("Repository found metadata for :$lib, but exact version not found: ${metadataSet.mapNotNull { it.url }}")
}