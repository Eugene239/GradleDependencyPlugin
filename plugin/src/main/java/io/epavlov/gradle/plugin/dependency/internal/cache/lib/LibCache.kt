package io.epavlov.gradle.plugin.dependency.internal.cache.lib

import io.epavlov.gradle.plugin.dependency.internal.StartupFlags
import io.epavlov.gradle.plugin.dependency.internal.LibKey
import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.result.ComponentArtifactsResult
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.UnresolvedComponentResult
import org.gradle.api.component.Artifact
import org.gradle.jvm.JvmLibrary
import org.gradle.language.base.artifact.SourcesArtifact
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import java.io.File

@Deprecated("Use Dependency cache")
internal class LibCache(
    private val project: Project,
    private val versionCache: VersionCache,
    private val startupFlags: StartupFlags
) {
    private val cache = HashMap<LibKey, LibData>()

    private val logger = project.logger

    suspend fun getLibData(
        key: LibKey
    ): LibData? {
        val value = cache[key]
        if (value == null) {
            cache[key] = getData(key)
            if (startupFlags.fetchVersions) {
                versionCache.getVersionData(group = key.group, module = key.module)
            }
        }
        return cache[key]
    }

    fun getCachedData(libKey: LibKey): LibData {
        return cache[libKey] ?: throw Exception("Can't find cached data for $libKey")
    }

    private fun getData(libKey: LibKey): LibData {
        val pom = findPom(libKey) ?: return LibData(pomFile = null, libFile = null)
        return LibData(
            pomFile = pom,
            libFile = null
        )
//        val hashFile = pom.parentFile
//        val versionFile = hashFile.parentFile
//        val data = getData(versionFile)
//        return LibData(
//            pomFile = data.pomFile,
//            libFile = data.libFile //?: return null
//        )
    }

    private fun findPom(libKey: LibKey): File? {
        val result = project.dependencies.createArtifactResolutionQuery()
            .forComponents(libKey.toIdentifier())
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
            .execute()

        result.components.forEach { component ->
            when (component) {
                is ComponentArtifactsResult -> {
                    val artifact = component.getArtifacts(MavenPomArtifact::class.java)
                        .firstOrNull() as ResolvedArtifactResult?
                    return artifact?.file
                        ?: throw Exception("Can't cast to ResolvedArtifactResult for $libKey")
                }

                is UnresolvedComponentResult -> {
                    logger.warn("Can't find POM for $libKey" ,component.failure)
                    return null
                }

                else -> {
                    throw Exception("Unsupported type $component for findPom($libKey)")
                }
            }
        }
        throw Exception("POM not found: $libKey")
    }

    private fun findJar(libKey: LibKey) : File? {
        val query = project.dependencies.createArtifactResolutionQuery()
            .forComponents(libKey.toIdentifier())
            .withArtifacts(JvmLibrary::class.java, SourcesArtifact::class.java)

        query.execute().resolvedComponents.forEach {
            val arts = it.getArtifacts(Artifact::class.java)
            arts.forEach { pom ->
                val artifact = pom as ResolvedArtifactResult
                return artifact.file
            }
        }
        return  null
    }

    @Deprecated("Don't use it")
    private fun getData(resolvedDependency: ResolvedDependency): LibData? {
        val artifact = resolvedDependency.moduleArtifacts.firstOrNull() ?: return null
        val hashFile = artifact.file.parentFile
        val versionFile = hashFile.parentFile

        val data = getData(versionFile)
        return LibData(
            pomFile = data.pomFile ?: return null,
            libFile = data.libFile ?: return null
        )
    }

    // pom and aar files in different directories
    private fun getData(
        topFile: File,
        searching: Searching = Searching()
    ): Searching {
        if (searching.pomFile != null && searching.libFile != null) {
            return searching
        }
        var compute = searching

        topFile.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                val data = getData(child, searching)
                data.libFile?.let {
                    compute = compute.copy(libFile = it)
                }
                data.pomFile?.let {
                    compute = compute.copy(pomFile = it)
                }
                if (compute.pomFile != null && compute.libFile != null) {
                    return compute
                }
            } else {
                when (child.extension) {
                    "pom" -> {
                        return Searching(pomFile = child)
                    }

                    "aar" -> {
                        return Searching(libFile = child)
                    }

                    "jar" -> {
                        if (child.name.endsWith("-sources.jar").not()) {
                            return Searching(libFile = child)
                        }
                    }

                    else -> Unit
                }
            }
        }
        return compute
    }

    private data class Searching(
        val pomFile: File? = null,
        val libFile: File? = null,
    )
}