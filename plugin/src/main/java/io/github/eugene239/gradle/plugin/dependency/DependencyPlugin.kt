package io.github.eugene239.gradle.plugin.dependency

import io.github.eugene239.gradle.plugin.dependency.task.DependencyGraphTask
import io.github.eugene239.gradle.plugin.dependency.task.DependencyReportTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class DependencyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate {
            registerTask(project = project)
        }
    }

    private fun registerTask(project: Project, ) {
        project.tasks.register("dependencyReport", DependencyReportTask::class.java) {
            it.group = "dependency-ui"
        }
        project.tasks.register("dependencyGraph", DependencyGraphTask::class.java) {
            it.group = "dependency-ui"
        }
    }
}