package io.github.eugene239.gradle.plugin.dependency.internal.provider

import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import io.github.eugene239.gradle.plugin.dependency.internal.service.toRepository
import kotlinx.coroutines.sync.Semaphore
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.artifacts.repositories.DefaultMavenLocalArtifactRepository
import org.gradle.api.logging.Logger
import org.gradle.invocation.DefaultGradle

@Suppress("UnstableApiUsage")
internal class DefaultRepositoryProvider(
    private val project: Project,
    private val logger: Logger,
    private val limit: Int
) : RepositoryProvider {

    private val repositoriesSet: Set<Repository> by lazy { collect() }
    private val repositoryLimit: Map<Repository, Semaphore> by lazy {
        repositoriesSet.associateWith { Semaphore(limit) }
    }

    override fun getRepositories(): Set<Repository> {
        return repositoriesSet
    }

    override fun getConnectionLimit(repository: Repository): Semaphore {
        return repositoryLimit[repository]!!
    }

    private fun collect(): Set<Repository> {
        val repos = LinkedHashSet<Repository>()
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
}