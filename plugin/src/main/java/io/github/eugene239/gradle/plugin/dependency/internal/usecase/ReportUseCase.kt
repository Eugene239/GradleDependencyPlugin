package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.UNSPECIFIED_VERSION
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.version.LatestVersionCache
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.MarkdownReportFormatter
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.ReportFormatter
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.DependencyStatus
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.OutdatedDependency
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.DefaultMavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.toIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logger
import java.io.File
import java.lang.module.ModuleDescriptor

internal class ReportUseCase(
    private val rootDir: File,
    private val logger: Logger,
    private val formatter: ReportFormatter = MarkdownReportFormatter(rootDir),
    private val repositoryProvider: RepositoryProvider,
    private val dependencyFilter: DependencyFilter,
    private val mavenService: MavenService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val repositoryCache: RepositoryCache = RepositoryCache(
        repositoryProvider = repositoryProvider,
        mavenService = mavenService,
        ioDispatcher = ioDispatcher
    ),
    private val versionCache: LatestVersionCache = LatestVersionCache(logger, repositoryCache),
) : UseCase<ReportUseCaseParams, File> {

    override suspend fun execute(params: ReportUseCaseParams): File = with(params) {
        logger.info("configurations: ${params.configurations}")
        val latestMap = hashMapOf<LibIdentifier, ModuleDescriptor.Version>()
        val keys = configurations
            .map { it.incoming.dependencies }
            .flatten()
            .toSet()
            .asSequence()
            .filter { dependencyFilter.matches(it) }
            .filter {
                it.group.isNullOrBlank().not()
                        && it.version.isNullOrBlank().not()
                        && UNSPECIFIED_VERSION != it.version
            }.map {
                LibKey(group = it.group!!, module = it.name, version = it.version!!)
            }
            .toSet()

        logger.info("keys size: ${keys.size}")

        coroutineScope {
            keys.map { key ->
                async {
                    versionCache.get(key)?.let { version ->
                        latestMap[key.toIdentifier()] = version
                    }
                }
            }.awaitAll()
        }


        val outdatedLibraries = mutableSetOf<OutdatedDependency>()
        keys.forEach { key ->
            val latest = latestMap[key.toIdentifier()]
            logger.info("${key.toIdentifier()} latest: ${latest?.toString()}")
            if (latest != null && latest.toString() != key.version) {
                outdatedLibraries.add(
                    OutdatedDependency(
                        name = "${key.group}:${key.module}",
                        currentVersion = key.version,
                        latestVersion = latest.toString()
                    )
                )
            }
        }

        return formatter.format(
            outdatedLibraries.filter { DependencyStatus.OK != it.status }.also {
                logger.warn("outdated: ${it.size}")
            }
        )
    }
}

internal class ReportUseCaseParams(
    val configurations: Set<Configuration>,
) : UseCaseParams