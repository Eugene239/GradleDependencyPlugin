package io.epavlov.gradle.plugin.dependency

import io.epavlov.gradle.plugin.dependency.internal.StartupFlags
import io.epavlov.gradle.plugin.dependency.internal.di.diModule
import io.epavlov.gradle.plugin.dependency.internal.di.koinInstance
import io.epavlov.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.epavlov.gradle.plugin.dependency.internal.filter.RegexFilter
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.koin.dsl.module

abstract class DependencyStartTask : DefaultTask() {

    @Input
    abstract fun getConfigurations(): ListProperty<Configuration>

    @Input
    abstract fun getExtension(): Property<GradleDependencyExtension>

    @TaskAction
    fun exec() {
        //stopKoin()
    //    startKoin(koinInstance)
        runCatching {
            val extension = getExtension().get()
            koinInstance.koin.declare(StartupFlags(fetchVersions = extension.checkVersions))
            koinInstance.modules(
                diModule +
                    module {
                        single<Project> { project }
                        single<RegexFilter> {
                            DependencyFilter(
                                project = get(),
                                regex = Regex(extension.dependencyNameRegex)
                            )
                        }
//                        single<StartupFlags> {
//                            StartupFlags(
//                                fetchVersions = extension.checkVersions
//                            )
//                        }
                    }
            )

            val core = koinInstance.koin.get<Core>()
            core.execute(
                configurations = getConfigurations().get(),
            )
        }.onFailure {
            logger.log(LogLevel.ERROR, "Task execution failed", it)
        }.onSuccess {
            logger.log(LogLevel.INFO, "Task execution succeed")
        }
    }
}