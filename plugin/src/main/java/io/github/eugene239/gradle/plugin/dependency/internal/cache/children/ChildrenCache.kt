package io.github.eugene239.gradle.plugin.dependency.internal.cache.children

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.UNSPECIFIED_VERSION
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.exception.DependencyException
import io.github.eugene239.gradle.plugin.dependency.internal.exception.PomException
import io.github.eugene239.gradle.plugin.dependency.internal.rethrowCancellationException
import io.github.eugene239.gradle.plugin.dependency.internal.service.Dependency
import io.github.eugene239.gradle.plugin.dependency.internal.service.Pom
import org.gradle.internal.cc.base.logger
import java.util.concurrent.ConcurrentHashMap

internal class ChildrenCache(
    private val pomCache: PomCache
) {
    companion object {
        private val ignoreScope = listOf("test", "provided", "runtime", "system")
    }

    private val cache = ConcurrentHashMap<LibKey, Result<List<LibKey>>>()

    suspend fun get(key: LibKey, repositoryName: String): Result<List<LibKey>> {
        cache[key]?.let {
            logger.debug("ChildrenCache found $key")
            return it
        }

        logger.debug("ChildrenCache missed $key")
        cache[key] = kotlin.runCatching { getChildren(key, repositoryName) }.rethrowCancellationException()
        return cache[key]!!
    }

    private suspend fun getChildren(libKey: LibKey, repositoryName: String): List<LibKey> {
        val pomResult = pomCache.get(libKey, repositoryName)
        pomResult.onSuccess { pom ->
            logger.debug("[POM] $libKey: $pom")
            return pom.dependencies?.dependency.orEmpty()
                .filter { it.scope == null || ignoreScope.contains(it.scope).not() }
                .map { dependency ->
                    LibKey(
                        group = dependency.groupId!!,
                        module = dependency.artifactId!!,
                        version = kotlin.runCatching {
                            getVersion(pom, dependency, libKey, repositoryName)
                        }.onFailure {
                            when (it) {
                                is DependencyException.VersionNotFoundException -> {
                                    logger.warn("Can't find version for $libKey, in repository: $repositoryName", it)
                                }

                                else -> throw it
                            }
                        }
                            .getOrNull().orEmpty()
                    )
                }
        }.onFailure {
            when (it) {
                is PomException.PomNotFoundException -> {
                    logger.warn("Pom not found for $libKey, in $repositoryName", it)
                    return emptyList()
                }

                else -> throw it
            }
        }
        return emptyList()
    }

    private suspend fun getVersion(pom: Pom, dependency: Dependency, parent: LibKey?, repositoryName: String): String {
        var versionKey: String? = null
        val version = dependency.version
        var result: String? = when {
            version == null -> null

            version.startsWith("[") -> {
                version.split(",").first().removePrefix("[").removeSuffix("]").also {
                    logger.debug("Took version from list: $it (${dependency.version})")
                }
            }

            version.lowercase() == "\${project.version}" -> {
                pom.version
            }

            version.startsWith("\${") -> {
                versionKey = version.removePrefix("\${").removeSuffix("}")
                pom.properties?.entries?.get(versionKey)
            }

            UNSPECIFIED_VERSION == dependency.version -> {
                pom.version
            }

            else -> dependency.version
        }

        if (result == null) {
            result = getVersionFromParent(pom, LibIdentifier(group = dependency.groupId!!, module = dependency.artifactId!!), versionKey, repositoryName)
        }
        if (result == null) {
            throw DependencyException.VersionNotFoundException(groupId = dependency.groupId!!, module = dependency.artifactId!!, parent = parent)
        }
        return result
    }

    private suspend fun getVersionFromParent(pom: Pom, identifier: LibIdentifier, versionKey: String?, repositoryName: String): String? {
        logger.debug("ChildrenCache getVersion from parent $identifier")
        val parent = pom.parent ?: return null
        val parentKey = LibKey(group = parent.groupId!!, module = parent.artifactId!!, version = parent.version!!)
        val parentPomResult = pomCache.get(parentKey, repositoryName)

        return parentPomResult
            .map { parentPom ->
                logger.debug("parentPom: $parentPom")
                if (versionKey != null) {
                    parentPom.properties?.entries?.get(versionKey)?.let {
                        logger.debug("Got version from properties by versionKey $it")
                        return it
                    }
                }
                val dependency = parentPom.dependencyManagement?.dependencies?.dependency?.find { it.groupId == identifier.group && it.artifactId == identifier.module }
                logger.debug("Parent dependency $dependency")
                val version = dependency?.version
                return@map when {
                    version?.startsWith("\${") == true -> {
                        val key = version.removePrefix("\${").removeSuffix("}")
                        parentPom.properties?.entries?.get(key).also {
                            logger.debug("Got version from properties: key:$key = $it")
                        }
                    }

                    dependency?.version != null -> {
                        return dependency.version
                    }
                    // Recursive call to get version from grandparent if possible
                    parentPom.parent != null -> {
                        getVersionFromParent(parentPom, LibIdentifier(group = parent.groupId!!, module = parent.artifactId!!), versionKey, repositoryName)
                    }

                    else -> null
                }
            }.getOrThrow()
    }

    fun getErrors(): List<MutableMap.MutableEntry<LibKey, Result<List<LibKey>>>> {
        return cache.entries.filter { entry ->
            entry.value.isFailure
        }
    }
}