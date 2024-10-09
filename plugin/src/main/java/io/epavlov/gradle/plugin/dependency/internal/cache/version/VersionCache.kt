package io.epavlov.gradle.plugin.dependency.internal.cache.version

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.artifacts.repositories.DefaultMavenLocalArtifactRepository
import org.gradle.invocation.DefaultGradle
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64


internal class VersionCache(
    private val project: Project
) {
    companion object {
        private const val POSTFIX = "maven-metadata.xml"
    }

    private val logger = project.logger
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

        val resolutionManagement =
            (project.gradle as? DefaultGradle)?.settings?.dependencyResolutionManagement
                ?.repositories.orEmpty()
                .filterIsInstance<MavenArtifactRepository>()

        repos.addAll(settingsRepos)
        repos.addAll(resolutionManagement)

        repos.removeIf { it is DefaultMavenLocalArtifactRepository }
        logger.info("REPOSITORIES")
        repos.forEach {
            logger.info ("${it.name} ${it.url}")
        }
        return repos
    }

    suspend fun getVersionData(key: VersionKey): VersionData {
        val value = cache[key]
        if (value == null) {
            cache[key] = getData(key) ?: throw Exception("Can't get version information for $key")
        }
        return getCachedData(key)
    }

    suspend fun getVersionData(
        group: String,
        module: String
    ): VersionData {
        val key = VersionKey(
            group = group,
            module = module,
        )
        return getVersionData(key)
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
                logger.info("url: $url")
                runCatching {
                    val connection = URL(metaUrl).openConnection() as HttpURLConnection
                    val credentials = entry.key.credentials
                    if (credentials.username != null) {
                        val token = Base64.getEncoder()
                            .encodeToString("${credentials.username}:${credentials.password}".toByteArray())
                        connection.setRequestProperty("Authorization", "Basic $token")
                    }
                    connection.connect()
                    val metaData = connection.inputStream.bufferedReader().readText()
                    logger.info("response: $metaData")
                    val matcher = "<release>(.+)</release>".toRegex().find(metaData)
                    if (matcher != null) {
                        val lastVersion = matcher.groupValues[1]
                        incrementValue(entry.key)
                        logger.info("$group:$module = $lastVersion")
                        return@withContext VersionData(lastVersion)
                    }
                }.onFailure {
                    logger.debug("Can't metaData $key, url: $metaUrl", it)
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