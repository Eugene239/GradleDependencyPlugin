package io.github.eugene239.gradle.plugin.dependency.internal.usecase

import io.github.eugene239.gradle.plugin.dependency.internal.OUTPUT_PATH
import io.github.eugene239.gradle.plugin.dependency.internal.UNSPECIFIED_VERSION
import io.github.eugene239.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.version.VersionKey
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.formatter.report.MarkdownReportFormatter
import io.github.eugene239.gradle.plugin.dependency.internal.formatter.report.ReportFormatter
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.DependencyStatus
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.OutdatedDependency
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.Project
import java.io.File

internal class ReportUseCase(
    private val versionCache: VersionCache,
) : UseCase<ReportUseCaseParams, File> {


    override suspend fun execute(params: ReportUseCaseParams): File = with(params) {
        val versionKeys = mutableSetOf<VersionKey>()
        val dependencies = mutableMapOf<VersionKey, String?>()
        val formatter: ReportFormatter = MarkdownReportFormatter(
            outputDirectory = File(project.layout.buildDirectory.asFile.get(), OUTPUT_PATH)
        )

        val depFilter: DependencyFilter = DependencyFilter(project).also {
            if (filter.isBlank().not()) {
                it.setRegex(Regex(filter))
            } else {
                it.setRegex(null)
            }
        }

        project.configurations.forEach { conf ->
            conf.dependencies
                .asSequence()
                .filter { depFilter.matches(it) }
                .filter {
                    it.group.isNullOrBlank().not()
                            && it.version.isNullOrBlank().not()
                            && UNSPECIFIED_VERSION != it.version
                }
                .forEach {
                    val key = VersionKey(
                        group = it.group.orEmpty(),
                        module = it.name
                    )
                    versionKeys.add(key)
                    dependencies[key] = it.version
                }
        }

        coroutineScope {
            versionKeys
                .map { key ->
                    async {
                        versionCache.getVersionData(key).getOrNull()
                    }
                }.awaitAll()
        }

        val outdatedLibraries = mutableSetOf<OutdatedDependency>()
        dependencies.forEach { (key, version) ->
            val latest = versionCache.getCachedData(key).getOrNull()
            if (latest != null && latest.latestVersion != version && version != null) {
                outdatedLibraries.add(
                    OutdatedDependency(
                        name = "${key.group}:${key.module}",
                        currentVersion = version,
                        latestVersion = latest.latestVersion
                    )
                )
            }
        }

        return formatter.format(
            outdatedLibraries.filter { DependencyStatus.OK != it.status }.also {
                project.logger.warn("outdated: ${it.size}")
            }
        )
    }
}

internal class ReportUseCaseParams(
    val project: Project,
    val filter: String
) : UseCaseParams