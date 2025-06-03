package io.github.eugene239.gradle.plugin.dependency

import io.github.eugene239.gradle.plugin.dependency.task.ConflictReportTask
import io.github.eugene239.gradle.plugin.dependency.task.DependencyReportTask
import io.github.eugene239.gradle.plugin.dependency.task.DependencyWPTask
import io.github.eugene239.gradle.plugin.dependency.task.SingleDependencyTask
import io.github.eugene239.plugin.BuildConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class DependencyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate {
            registerTask(project = project)
        }
    }

    private fun registerTask(project: Project) {
        project.tasks.register("dependencyReport", DependencyReportTask::class.java) {
            it.group = "dependency-ui"
        }
        project.tasks.register("conflictReport", ConflictReportTask::class.java) {
            it.group = "dependency-ui"
        }
        project.tasks.register("dependencyWP", DependencyWPTask::class.java) {
            it.group = "dependency-ui"
        }
        if (BuildConfig.IS_DEBUG) {
            project.tasks.register("singleDependency", SingleDependencyTask::class.java) {
                it.group = "dependency-ui"
            }
        }
    }

}