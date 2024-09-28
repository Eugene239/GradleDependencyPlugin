package io.epavlov.gradle.plugin.dependency

import io.epavlov.gradle.plugin.dependency.internal.StartupFlags
import io.epavlov.gradle.plugin.dependency.internal.cache.lib.LibCache
import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.epavlov.gradle.plugin.dependency.internal.dependency.IncomingDependencyFetcher
import io.epavlov.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.epavlov.gradle.plugin.dependency.internal.pom.PomXMLParserImpl
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

@Suppress("unused")
class DependencyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: GradleDependencyExtension =
            project.extensions.create("dependencyGraphOptions", GradleDependencyExtension::class.java)

        project.afterEvaluate {
            val extensionConfigurations = extension.appConfigurationNames
            val printConfiguration = extension.printConfigurations

            if (printConfiguration) {
                println("Project configurations: ")
                println("------------------------")
            }
            val configurations = project.configurations
                .filter { it.name.contains("test", true).not() && it.isCanBeResolved }
                .filter { config ->
                    val enabled = extensionConfigurations.any { name -> config.name.contains(name, true) }
                    if (printConfiguration) {
                        println("${config.name}, enabled: $enabled")
                    }
                    enabled
                }
            if (printConfiguration) {
                println("------------------------")
            }
            registerTask(
                project = project,
                extension = extension,
                configurations = configurations
            )
        }
    }

    private fun registerTask(
        project: Project,
        extension: GradleDependencyExtension,
        configurations: List<Configuration>
    ) {
        // uncomment to launch on sync
//        test(
//            project = project,
//            configuration = configurations.first(),
//            extension = extension
//        )
        val action = Action<io.epavlov.gradle.plugin.dependency.DependencyStartTask> {
            it.getConfigurations().set(configurations)
            it.getExtension().set(extension)
        }
        project.tasks.register("dependencyUI", io.epavlov.gradle.plugin.dependency.DependencyStartTask::class.java, action)
    }

    private fun test(
        project: Project,
        configuration: Configuration,
        extension: GradleDependencyExtension
    ) {
        val regexFilter = DependencyFilter(
            project = project,
            regex = Regex(extension.dependencyNameRegex)
        )
        val fetcher = IncomingDependencyFetcher(
            project = project,
            regexFilter = regexFilter,
            libCache = LibCache(
                versionCache = VersionCache(project),
                startupFlags = StartupFlags(
                    fetchVersions = false
                ),
                project = project
            ),
            pomXMLParser = PomXMLParserImpl(
                filter = regexFilter
            )
        )
        fetcher.fetch(configuration)
    }
}