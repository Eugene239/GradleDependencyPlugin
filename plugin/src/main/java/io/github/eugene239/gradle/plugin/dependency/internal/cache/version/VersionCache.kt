package io.github.eugene239.gradle.plugin.dependency.internal.cache.version

import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import io.github.eugene239.gradle.plugin.dependency.internal.service.toRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.internal.artifacts.repositories.DefaultMavenLocalArtifactRepository
import org.gradle.invocation.DefaultGradle
import org.w3c.dom.Document
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

@Deprecated("TODO DELETE")
@Suppress("UnstableApiUsage")
internal class VersionCache(
    private val project: Project
) {
    companion object {
        private const val POSTFIX = "maven-metadata.xml"
        private val PREP_RELEASE_KEYS = setOf(
            "rc", "alpha", "beta", "snapshot",
            "preview", "dev", "incubating",
            // adding keys m1, b9, p2, a3... too
            *setOf(
                IntRange(0, 10).map { setOf("m$it", "b$it", "p$it", "a$it") }
            ).flatten()
                .flatten()
                .toTypedArray()
        )
    }

    private val logger = project.logger
    private val cache = HashMap<VersionKey, VersionData>()
    private val repositories: MutableMap<Repository, Int> by lazy {
        getRepositories()
            .associateWith { 0 }
            .toMutableMap()
    }

    private fun getRepositories(): Set<Repository> {
        val repos = mutableSetOf<Repository>()
        repos.addAll(project.repositories.filterRepositories())
        repos.addAll(project.parent?.repositories.filterRepositories())

        val settingsRepos = (project.gradle as? DefaultGradle)?.settings?.pluginManagement
            ?.repositories.filterRepositories()

        val resolutionManagement = (project.gradle as? DefaultGradle)?.settings
            ?.dependencyResolutionManagement?.repositories.filterRepositories()

        repos.addAll(settingsRepos)
        repos.addAll(resolutionManagement)

        logger.info("REPOSITORIES")
        repos.forEach {
            logger.info("${it.name} ${it.url}")
        }
        return repos
    }

    private fun RepositoryHandler?.filterRepositories(): List<Repository> {
        return orEmpty()
            .filterIsInstance<MavenArtifactRepository>()
            .filterNot { it is DefaultMavenLocalArtifactRepository }
            .map { it.toRepository() }
    }

    suspend fun getVersionData(key: VersionKey): Result<VersionData> {
        val value = cache[key]
        if (value == null) {
            getData(key)?.let {
                cache[key] = it
            }
        }
        return getCachedData(key)
    }

    suspend fun getVersionData(
        group: String,
        module: String
    ): Result<VersionData> {
        val key = VersionKey(
            group = group,
            module = module,
        )
        return getVersionData(key)
    }

    fun getCachedData(key: VersionKey): Result<VersionData> {
        val value = cache[key]
        return if (value != null) {
            Result.success(value)
        } else {
            Result.failure(Exception("Latest version not found for $key"))
        }
    }

    fun getLatestVersions(): Map<String, String> {
        return cache.map { entry ->
            "${entry.key.group}:${entry.key.module}" to entry.value.latestVersion
        }.toMap()
    }

    private suspend fun getData(key: VersionKey): VersionData? {
        return getDataFromHttp(key)
    }


    private suspend fun getDataFromHttp(key: VersionKey): VersionData? {
        return withContext(Dispatchers.IO) {
            val sortedRepositories = repositories.entries.sortedByDescending { it.value }
            sortedRepositories.forEach { entry ->
                val group = key.group.replace(".", "/")
                val module = key.module
                val url = "${entry.key.url}".removeSuffix("/")
                val metaUrl = "$url/$group/$module/$POSTFIX"
                logger.info("url: $metaUrl")
                runCatching {
                    val connection = URL(metaUrl).openConnection() as HttpURLConnection
//                    val credentials = entry.key.credentials
//                    if (credentials.username != null) {
//                        val token = Base64.getEncoder()
//                            .encodeToString("${credentials.username}:${credentials.password}".toByteArray())
//                        connection.setRequestProperty("Authorization", "Basic $token")
//                    }
                    connection.connect()
                    val metaData = connection.inputStream
                    val latestVersion = fetchVersionByDocument(metaData)
                    if (latestVersion != null) {
                        incrementValue(entry.key)
                        logger.warn("metadata: $metaUrl")
                        logger.info("$group:$module = $latestVersion")
                        return@withContext VersionData(latestVersion)
                    }
                }.onFailure {
                    logger.info("Can't metaData $key, url: $metaUrl", it)
                }
            }
            return@withContext null
        }
    }

    private fun fetchVersionByDocument(metadata: InputStream): String? {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(metadata)
        doc.documentElement.normalize()

        val versionNodes = doc.getElementsByTagName("version")
        var version: String? = null
        (0..<versionNodes.length)
            .forEach { index ->
                val nodeVersion = versionNodes.item(index).textContent
                if (PREP_RELEASE_KEYS.any { nodeVersion.lowercase().contains(it) }.not()) {
                    version = nodeVersion
                }
            }

        return version
    }

    private fun getOneByTag(doc: Document, tag: String): String? {
        val nodeList = doc.getElementsByTagName(tag)
        return if (nodeList.length == 1) {
            nodeList.item(0).textContent
        } else null
    }

    @Deprecated("Use document parser `fetchVersionByDocument`")
    private fun fetchVersionByRegex(metadata: InputStream): String? {
        val text = metadata.bufferedReader().readText()
        logger.info("response: $text")
        val matcher = "<release>(.+)</release>".toRegex().find(text)
        if (matcher != null) {
            val lastVersion = matcher.groupValues[1]
            return lastVersion
        }
        return null
    }

    @Suppress("RedundantSuspendModifier", "Takes a long time to resolve")
    private suspend fun getDataFromConfiguration(key: VersionKey): VersionData? {
        val configuration = project.configurations.detachedConfiguration(
            project.dependencies.create("${key.group}:${key.module}:+")
        )
        val result = configuration.incoming.resolutionResult
        return result.allDependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .firstNotNullOfOrNull {
                val version = (it.selected.id as? ModuleComponentIdentifier)?.version
                version?.run {
                    VersionData(this)
                }
            }
    }


    private fun incrementValue(repository: Repository) {
        val value = repositories[repository] ?: 0
        repositories[repository] = value.inc()
    }

    fun clear() {
        cache.clear()
    }
}

