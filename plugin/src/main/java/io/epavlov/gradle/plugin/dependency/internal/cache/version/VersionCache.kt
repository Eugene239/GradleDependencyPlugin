package io.epavlov.gradle.plugin.dependency.internal.cache.version

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.invocation.DefaultGradle
import java.net.URL


internal class VersionCache(
    private val project: Project
) {
    companion object {
        private const val POSTFIX = "maven-metadata.xml"
    }

    private val cache = HashMap<VersionKey, VersionData>()
    private val repositories: MutableMap<MavenArtifactRepository, Int> by lazy {
        getRepositories()
            .associateWith { 0 }
            .toMutableMap()
    }

    private fun getRepositories(): Set<MavenArtifactRepository> {
        val repos = mutableSetOf<MavenArtifactRepository>()
        repos.addAll(
            project.repositories
                .filterIsInstance<MavenArtifactRepository>()
        )
        repos.addAll(
            project.parent?.repositories
                ?.filterIsInstance<MavenArtifactRepository>()
                .orEmpty()
        )
        val settingsRepos = (project.gradle as? DefaultGradle)?.settings?.pluginManagement?.repositories.orEmpty()
            .filterIsInstance<MavenArtifactRepository>()

        repos.addAll(settingsRepos)
        println("settingsRepos : ${settingsRepos.size}")
        println("repos : ${repos.size}")
        return repos
    }

//    @Deprecated("Use version with group and module")
//    fun getVersionData(
//        resolvedDependency: ResolvedDependency
//    ): VersionData {
//        val key = VersionKey(
//            group = resolvedDependency.moduleGroup,
//            module = resolvedDependency.moduleName,
//        )
//        return cache.computeIfAbsent(key) {
//            getData(key) ?: throw Exception("Can't get version information for $key")
//        }
//    }

    suspend fun getVersionData(
        group: String,
        module: String
    ): VersionData {
        val key = VersionKey(
            group = group,
            module = module,
        )
        val value = cache[key]
        if (value == null) {
            cache[key] = getData(key) ?: throw Exception("Can't get version information for $key")
        }
        return cache[key]!!
    }

    fun getCachedData(key: VersionKey): VersionData {
        return cache[key] ?: throw Exception("Can't find cached data for $key")
    }

    fun getLatestVersions(): Map<String, String> {
        return cache.map { entry ->
            "${entry.key.group}:${entry.key.module}" to entry.value.latestVersion
        }.toMap()
    }

    private suspend fun getData(key: VersionKey): VersionData? {
        return withContext(Dispatchers.IO) {
            val sortedRepositories = repositories.entries.sortedByDescending { it.value }
            sortedRepositories.forEach { entry ->
                val group = key.group.replace(".", "/")
                val module = key.module.replace(".", "/")
                val url = "${entry.key.url}".removeSuffix("/")
                val metaUrl = "$url/$group/$module/$POSTFIX"
                runCatching {
                    val metaData = URL(metaUrl).readText()
                    val matcher = "<release>(.+)</release>".toRegex().find(metaData)
                    if (matcher != null) {
                        val lastVersion = matcher.groupValues[1]
                        incrementValue(entry.key)
                        println("$group:$module = $lastVersion")
                        return@withContext VersionData(lastVersion)
                    }
                }.onFailure {
                    println("Can't metaData $key, url: $metaUrl")
                }
            }
            return@withContext null
        }
    }

    private fun incrementValue(repository: MavenArtifactRepository) {
        val value = repositories[repository] ?: 0
        repositories[repository] = value.inc()
    }

    fun clear() {
        cache.clear()
    }
}