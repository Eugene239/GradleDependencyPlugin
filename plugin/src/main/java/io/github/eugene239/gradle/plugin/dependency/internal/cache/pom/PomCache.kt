package io.github.eugene239.gradle.plugin.dependency.internal.cache.pom

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.version.VersionCache
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ComponentArtifactsResult
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.UnresolvedArtifactResult
import org.gradle.api.artifacts.result.UnresolvedComponentResult
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import java.io.File

internal class PomCache(
    private val project: Project,
    private val versionCache: VersionCache,
) {
    private val cache = HashMap<LibKey, Result<File>>()

    private val logger = project.logger

    suspend fun getPom(
        key: LibKey
    ): Result<File> {
        val value = cache[key]
        if (value == null) {
            cache[key] = findPom(key)
            versionCache.getVersionData(group = key.group, module = key.module)
        }
        return cache[key]!!
    }

    private fun findPom(libKey: LibKey): Result<File> {
        val result = project.dependencies.createArtifactResolutionQuery()
            .forComponents(libKey.toIdentifier())
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
            .execute()

        result.components.forEach { component ->
            when (component) {
                is ComponentArtifactsResult -> {
                    return getArtifact(libKey, component)
                }

                is UnresolvedComponentResult -> {
                    logger.warn("Can't find POM for $libKey", component.failure)
                    return Result.failure(component.failure)
                }

                else -> {
                    throw Exception("Unsupported type $component for findPom($libKey)")
                }
            }
        }
        throw Exception("POM not found: $libKey")
    }

    private fun getArtifact(libKey: LibKey, component: ComponentArtifactsResult): Result<File> {
        val artifact = component.getArtifacts(MavenPomArtifact::class.java)
            .first()

        return when (artifact) {
            is ResolvedArtifactResult -> {
                logger.info("foundPom : $libKey -> ${artifact.file.path}")
                Result.success(artifact.file)
            }

            is UnresolvedArtifactResult -> {
                Result.failure(Exception("Artifact for $libKey unresolved", artifact.failure))
            }

            else -> throw Exception("getArtifact handling error $artifact for $libKey")
        }
    }
}
