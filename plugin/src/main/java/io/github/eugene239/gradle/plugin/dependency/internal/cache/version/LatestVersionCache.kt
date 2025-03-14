package io.github.eugene239.gradle.plugin.dependency.internal.cache.version

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.PREP_RELEASE_KEYS
import io.github.eugene239.gradle.plugin.dependency.internal.cache.Cache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryCache
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenMetadata
import io.github.eugene239.gradle.plugin.dependency.internal.toIdentifier
import org.gradle.api.logging.Logger
import java.lang.module.ModuleDescriptor
import java.util.concurrent.ConcurrentHashMap

internal class LatestVersionCache(
    private val logger: Logger,
    private val repositoryCache: RepositoryCache
) : Cache<LibKey, ModuleDescriptor.Version?> {

    private val cache = ConcurrentHashMap<LibIdentifier, ModuleDescriptor.Version?>()

    override suspend fun get(key: LibKey): ModuleDescriptor.Version? {
        val identifier = key.toIdentifier()
        cache[identifier]?.let {
            return it
        }
        val metadataSet = repositoryCache.getMetadataSet(key)
        val latest = metadataSet.map(::toVersions)
            .flatten()
            .maxOrNull()
        latest?.let {
            cache[identifier] = latest
        }

        return latest
    }

    private fun toVersions(metadata: MavenMetadata): Set<ModuleDescriptor.Version> {
        val set = mutableSetOf<ModuleDescriptor.Version>()
        metadata.versioning?.latest?.tryParse()?.let {
            set.add(it)
        }
        metadata.versioning?.release?.tryParse()?.let {
            set.add(it)
        }
        set.addAll(metadata.versioning?.versions?.version?.mapNotNull { it.tryParse() } ?: emptyList())
        return set
            .filter { version -> PREP_RELEASE_KEYS.any { version.toString().lowercase().contains(it) }.not() }
            .toSet()
    }

    private fun String.tryParse(): ModuleDescriptor.Version? {
        return kotlin.runCatching {
            ModuleDescriptor.Version.parse(this)
        }.onFailure {
            logger.info("Can't parse version of: $this")
        }.getOrNull()
    }
}