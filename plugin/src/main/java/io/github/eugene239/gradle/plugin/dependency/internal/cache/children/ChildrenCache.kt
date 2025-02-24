package io.github.eugene239.gradle.plugin.dependency.internal.cache.children

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.Cache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.service.Pom
import org.gradle.internal.cc.base.logger
import java.util.concurrent.ConcurrentHashMap

internal class ChildrenCache(
    private val pomCache: PomCache
) : Cache<LibKey, Result<List<LibKey>>> {
    companion object {
        private val ignoreScope = listOf("test", "provided", "runtime", "system")
    }

    private val cache = ConcurrentHashMap<LibKey, Result<List<LibKey>>>()

    override suspend fun get(key: LibKey): Result<List<LibKey>> {
        cache[key]?.let {
            logger.info("ChildrenCache found $key")
            return it
        }

        logger.info("ChildrenCache missed $key")
        cache[key] = kotlin.runCatching {
            getChildren(key)
        }
        return cache[key]!!
    }

    private suspend fun getChildren(libKey: LibKey): List<LibKey> {
        val pomResult = pomCache.get(libKey)
        pomResult.onSuccess { pom ->
            logger.info("[POM] $libKey: $pom")
            return pom.dependencies?.dependency.orEmpty()
                .filter { it.scope == null || ignoreScope.contains(it.scope).not() }
                .map { dependency ->
                    LibKey(
                        group = dependency.groupId,
                        module = dependency.artifactId,
                        version = dependency.version
                            ?: getVersionFromParent(pom, LibIdentifier(group = dependency.groupId, module = dependency.artifactId))
                            ?: throw Exception("Version not found for $dependency (parent: $libKey)") // TODO
                    )
                }
        }.onFailure {
            throw it
        }
        return emptyList()
    }

    private suspend fun getVersionFromParent(pom: Pom, identifier: LibIdentifier): String? {
        logger.info("ChildrenCache getVersion from parent $identifier")
        val parent = pom.parent ?: return null
        val parentKey = LibKey(group = parent.groupId, module = parent.artifactId, version = parent.version)
        val parentPomResult = pomCache.get(parentKey)

        return parentPomResult
            .map { parentPom ->
                logger.info("parentPom: $parentPom")
                val dependency = parentPom.dependencyManagement?.dependencies?.dependency?.find { it.groupId == identifier.group && it.artifactId == identifier.module }
                logger.info("Parent dependency $dependency")
                return@map if (dependency?.version?.startsWith("\${") == true) {
                    val key = dependency.version.removePrefix("\${").removeSuffix("}")
                    parentPom.properties?.entries?.get(key).also {
                        logger.info("Got version from properties: key:$key = $it")
                    }
                } else {
                    dependency?.version
                }
            }.getOrThrow()
    }

    fun getErrors(): List<MutableMap.MutableEntry<LibKey, Result<List<LibKey>>>> {
        return cache.entries.filter { entry ->
            entry.value.isFailure
        }
    }
}