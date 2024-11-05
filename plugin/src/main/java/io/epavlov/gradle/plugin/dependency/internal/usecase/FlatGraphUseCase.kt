package io.epavlov.gradle.plugin.dependency.internal.usecase

import io.epavlov.gradle.plugin.dependency.internal.OUTPUT_PATH
import io.epavlov.gradle.plugin.dependency.internal.cache.dependency.DependencyCache
import io.epavlov.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.epavlov.gradle.plugin.dependency.internal.di.PluginComponent
import io.epavlov.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.epavlov.gradle.plugin.dependency.internal.formatter.flat.FlatFormatter
import io.epavlov.gradle.plugin.dependency.internal.pom.PomXMLParserImpl
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.koin.core.component.inject
import java.io.File

internal class FlatGraphUseCase(
    project: Project,
    filter: String
) : UseCase<FlatGraphUseCaseParams, File>, PluginComponent {

    private val versionCache: VersionCache by inject()
    private val rootDir = File(project.buildDir, OUTPUT_PATH)
    private val depFilter: DependencyFilter? = if (filter.isBlank().not()) {
        DependencyFilter(project, Regex(filter))
    } else {
        null
    }
    private val logger = project.logger
    private val dependencyCache = DependencyCache(
        dependencyFilter = depFilter,
        logger = logger,
        pomCache = PomCache(
            project = project,
            versionCache = versionCache,
        ),
        xmlParser = PomXMLParserImpl(
            logger = project.logger,
            filter = depFilter
        )
    )
    private val formatter = FlatFormatter()

    override suspend fun execute(params: FlatGraphUseCaseParams): File {
        logger.lifecycle("####### FETCH ${params.configuration.name}")
        val incoming = params.configuration.incoming.resolutionResult.root.dependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .filter { depFilter?.matches(it) != false }
            .toSet()

        logger.lifecycle("incoming size: ${incoming.size}")
        dependencyCache.fill(incoming)
        val cached = dependencyCache.cachedValues()
        cached.forEach { (libKey, libKeys) ->
            libKeys.forEach { child ->
                println("# $libKey ---- $child")
            }
            println("#################################")
        }
        val result = formatter.format(rootDir, cached)
        println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
        val failed = dependencyCache.failedValues()
        failed.forEach {
            println("%% FAILED ${it.lib}, parent: ${it.parent}")
            it.error.printStackTrace()
            println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
        }
        println("%% FAILED: ${failed.size}")
        failed.forEach {
            println("${it.lib} <---- ${it.parent}")
        }
        logger.lifecycle("####### FETCH ${params.configuration.name} END")
        return result
    }


}

internal data class FlatGraphUseCaseParams(val configuration: Configuration) : UseCaseParams