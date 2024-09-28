package io.epavlov.gradle.plugin.dependency.internal

import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.epavlov.gradle.plugin.dependency.internal.dependency.DependencyFetcher
import io.epavlov.gradle.plugin.dependency.internal.formatter.Formatter
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import java.io.File

internal class CoreImpl(
    private val formatter: Formatter,
    private val versionCache: VersionCache,
    private val project: Project,
    private val startupFlags: StartupFlags,
    private val dependencyFetcher: DependencyFetcher
) : io.epavlov.gradle.plugin.dependency.Core {

    override fun execute(
        configurations: List<Configuration>,
    ) {
        val rootDir = File(project.buildDir, "dependencyUI")
        if (rootDir.exists()) {
            rootDir.deleteRecursively()
            rootDir.mkdirs()
        } else {
            rootDir.mkdirs()
        }
        configurations.map { configuration ->
            processConfiguration(
                rootDir = rootDir,
                configuration = configuration
            )
        }
        formatter.copySite(rootDir)
        formatter.saveConfigurations(rootDir, configurations)
        if (startupFlags.fetchVersions) {
            formatter.saveVersions(rootDir, versionCache.getLatestVersions())
        }
    }

    private fun processConfiguration(
        rootDir: File,
        configuration: Configuration
    ) {
        val outputDir = File(rootDir, configuration.name)
        outputDir.mkdirs()

        val result = dependencyFetcher.fetch(configuration)
        formatter.format(result, outputDir)
    }
}