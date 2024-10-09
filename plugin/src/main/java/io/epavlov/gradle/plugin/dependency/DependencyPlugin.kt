package io.epavlov.gradle.plugin.dependency

import io.epavlov.gradle.plugin.dependency.internal.di.diModule
import io.epavlov.gradle.plugin.dependency.internal.di.koinInstance
import io.epavlov.gradle.plugin.dependency.task.DependencyGraphTask
import io.epavlov.gradle.plugin.dependency.task.DependencyReportTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class DependencyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate {
            setupDI(project)
            registerTask(project = project)
        }
    }

    // todo delete?
    private fun setupDI(project: Project){
        koinInstance.koin.declare(project)
        koinInstance.modules(diModule)
    }

    private fun registerTask(project: Project, ) {
        project.tasks.register("dependencyGraph", DependencyGraphTask::class.java) {
            it.group = "dependency-ui"
        }
        project.tasks.register("dependencyReport", DependencyReportTask::class.java) {
            it.group = "dependency-ui"
        }
    }
}