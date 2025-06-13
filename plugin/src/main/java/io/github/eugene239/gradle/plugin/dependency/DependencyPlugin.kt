package io.github.eugene239.gradle.plugin.dependency

import io.github.eugene239.gradle.plugin.dependency.internal.di.CommonModule
import io.github.eugene239.gradle.plugin.dependency.task.DependencyConflictTask
import io.github.eugene239.gradle.plugin.dependency.task.DependencyReportTask
import io.github.eugene239.gradle.plugin.dependency.task.DependencyWPTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class DependencyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate {
            registerTask(project = project)
            CommonModule.register(project)
        }
    }

    private fun registerTask(project: Project) {
        project.tasks.register("dependencyReport", DependencyReportTask::class.java) {
            it.group = "dependency-ui"
        }
        project.tasks.register("dependencyWP", DependencyWPTask::class.java) {
            it.group = "dependency-ui"
        }
        project.tasks.register("dependencyConflict", DependencyConflictTask::class.java) {
            it.group = "dependency-ui"
        }
    }

}