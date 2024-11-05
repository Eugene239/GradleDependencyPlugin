package io.epavlov.gradle.plugin.dependency.internal.cache.version

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.internal.artifacts.repositories.DefaultMavenLocalArtifactRepository
import org.gradle.invocation.DefaultGradle
import org.w3c.dom.Document
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory


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
                    val credentials = entry.key.credentials
                    if (credentials.username != null) {
                        val token = Base64.getEncoder()
                            .encodeToString("${credentials.username}:${credentials.password}".toByteArray())
                        connection.setRequestProperty("Authorization", "Basic $token")
                    }
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

        getOneByTag(doc, "release")?.run {
            return this
        }
        getOneByTag(doc, "latest")?.run {
            return this
        }

        val versionNodes = doc.getElementsByTagName("version")
        return versionNodes.item(versionNodes.length - 1).textContent
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

    @Deprecated("Take a long time to resolve")
    private suspend fun getDataFromConfiguration(key: VersionKey): VersionData? {
        val configuration = project.configurations.detachedConfiguration(
            project.dependencies.create("${key.group}:${key.module}:+")
        )
        val result = configuration.incoming.resolutionResult
        return result.allDependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .firstNotNullOfOrNull {
                println("${it.selected}, ${it.selected.javaClass}")
                val version = (it.selected.id as? ModuleComponentIdentifier)?.version
                println("# $key -> $version")
                //if (version?.contains("SNAPSHOT") == true) return@firstNotNullOfOrNull null
                (it.selected.id as? ModuleComponentIdentifier)?.version?.run {
                    VersionData(this)
                }
            }
    }

//    private suspend fun resolveLatest(keys: List<VersionKey>): List<VersionData> {
//        val configuration = project.configurations.detachedConfiguration()
//        configuration.dependencies.addAll(keys.map { project.dependencies.create("${it.group}:${it.module}:+") })
//        val result = configuration.incoming.resolutionResult
//        return result.allDependencies
//            .filterIsInstance<ResolvedDependencyResult>()
//            .map {
//                println("${it.selected}, ${it.selected.javaClass}")
//                val version = (it.selected.id as? ModuleComponentIdentifier)?.version
//                println("# $key -> $version")
//                //if (version?.contains("SNAPSHOT") == true) return@firstNotNullOfOrNull null
//                (it.selected.id as? ModuleComponentIdentifier)?.version?.run {
//                    VersionData(this)
//                }
//            }
//    }


    private fun incrementValue(repository: MavenArtifactRepository) {
        val value = repositories[repository] ?: 0
        repositories[repository] = value.inc()
    }

    fun clear() {
        cache.clear()
    }
}

