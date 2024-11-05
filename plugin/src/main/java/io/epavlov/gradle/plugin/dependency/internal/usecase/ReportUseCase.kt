package io.epavlov.gradle.plugin.dependency.internal.usecase

import io.epavlov.gradle.plugin.dependency.internal.UNSPECIFIED_VERSION
import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionKey
import io.epavlov.gradle.plugin.dependency.internal.di.PluginComponent
import io.epavlov.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.epavlov.gradle.plugin.dependency.internal.formatter.report.MarkdownReportFormatter
import io.epavlov.gradle.plugin.dependency.internal.formatter.report.OutdatedDependency
import io.epavlov.gradle.plugin.dependency.internal.formatter.report.OutdatedVersion
import io.epavlov.gradle.plugin.dependency.internal.formatter.report.ReportFormatter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.Project
import org.koin.core.component.inject
import java.io.File

internal class ReportUseCase : UseCase<ReportUseCaseParams, File>, PluginComponent {

    private val versionCache: VersionCache by inject()

    override suspend fun execute(params: ReportUseCaseParams): File = with(params) {
        val versionKeys = mutableSetOf<VersionKey>()
        val dependencies = mutableMapOf<VersionKey, String?>()
        val formatter: ReportFormatter  = MarkdownReportFormatter(project = project)

        val depFilter: DependencyFilter? = if (filter.isBlank().not()) {
            DependencyFilter(project, Regex(filter))
        } else {
            null
        }

        project.configurations.forEach { conf ->
            conf.dependencies
                .asSequence()
                .filter { depFilter?.matches(it) ?: true }
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
            if (latest != null && latest.latestVersion != version) {
                outdatedLibraries.add(
                    OutdatedDependency(
                        group = key.group,
                        module = key.module,
                        versions = OutdatedVersion(
                            current = version.orEmpty(),
                            latest = latest.latestVersion
                        )
                    )
                )
            }
        }
        project.logger.warn("outdated: ${outdatedLibraries.size}")
        return formatter.format(outdatedLibraries)
    }
}

internal class ReportUseCaseParams(
    val project: Project,
    val filter: String
) : UseCaseParams