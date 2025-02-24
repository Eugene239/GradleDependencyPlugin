package io.github.eugene239.gradle.plugin.dependency.internal.service

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey

internal interface MavenService {

    suspend fun isMetadataExists(libIdentifier: LibIdentifier, repository: Repository): Result<Boolean>

    suspend fun getMetadata(libIdentifier: LibIdentifier, repository: Repository): MavenMetadata

    suspend fun getPom(libKey: LibKey, repository: Repository): Pom

    suspend fun getSize(libKey: LibKey, repository: Repository, packaging: String): Long
}