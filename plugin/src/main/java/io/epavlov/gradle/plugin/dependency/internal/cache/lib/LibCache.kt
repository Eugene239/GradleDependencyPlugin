package io.epavlov.gradle.plugin.dependency.internal.cache.lib

import io.epavlov.gradle.plugin.dependency.internal.StartupFlags
import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import java.io.File

internal class LibCache(
    private val project: Project,
    private val versionCache: VersionCache,
    private val startupFlags: StartupFlags
) {
    private val cache = HashMap<LibKey, LibData>()

    suspend fun getLibData(
        key: LibKey
    ): LibData {
        val value = cache[key]
        if (value == null) {
            cache[key] = getData(key)?: throw Exception("Can't get lib information for $key")
            if (startupFlags.fetchVersions) {
                versionCache.getVersionData(group = key.group, module = key.module)
            }
        }
        return cache[key]!!
    }

    fun getCachedData(libKey: LibKey): LibData {
        return cache[libKey] ?: throw Exception("Can't find cached data for $libKey")
    }

    private fun getData(libKey: LibKey): LibData? {
        val pom = findPom(libKey)
      //  println("[$libKey] pom: ${pom.path}")
        val hashFile = pom.parentFile
        val versionFile = hashFile.parentFile
        val data = getData(versionFile)
        return LibData(
            pomFile = data.pomFile ?: return null,
            libFile = data.libFile //?: return null
        )
    }

    private fun findPom(libKey: LibKey): File {
        val query = project.dependencies.createArtifactResolutionQuery()
            .forComponents(libKey.toIdentifier())
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)

        query.execute().resolvedComponents.forEach {
            val poms = it.getArtifacts(MavenPomArtifact::class.java)
            poms.forEach { pom ->
                val artifact = pom as ResolvedArtifactResult
                return artifact.file
            }
        }
        throw Exception("POM not found: $libKey")
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