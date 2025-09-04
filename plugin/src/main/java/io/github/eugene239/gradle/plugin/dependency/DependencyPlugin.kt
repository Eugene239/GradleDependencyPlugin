package io.github.eugene239.gradle.plugin.dependency

import io.github.eugene239.gradle.plugin.dependency.internal.di.CommonModule
import io.github.eugene239.gradle.plugin.dependency.publication.AarPublication
import io.github.eugene239.gradle.plugin.dependency.publication.BomPublication
import io.github.eugene239.gradle.plugin.dependency.publication.JarPublication
import io.github.eugene239.gradle.plugin.dependency.publication.PublicationExtension
import io.github.eugene239.gradle.plugin.dependency.task.DependencyConflictTask
import io.github.eugene239.gradle.plugin.dependency.task.DependencyLatestVersionsTask
import io.github.eugene239.gradle.plugin.dependency.task.DependencyWPTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

@Suppress("unused")
class DependencyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        createExtensions(project)
        project.afterEvaluate {
            registerTask(project = project)
            CommonModule.register(project)
            registerPublication(project)
        }
    }

    private fun registerTask(project: Project) {
        project.tasks.register("dependencyLatestVersions", DependencyLatestVersionsTask::class.java) {
            it.group = "dependency-ui"
        }
        project.tasks.register("dependencyWP", DependencyWPTask::class.java) {
            it.group = "dependency-ui"
        }
        project.tasks.register("dependencyConflict", DependencyConflictTask::class.java) {
            it.group = "dependency-ui"
        }
    }

    private fun createExtensions(project: Project) {
        if (project.extensions.findByName(PublicationExtension.EXTENSION_NAME) == null) {
            project.extensions.create(PublicationExtension.EXTENSION_NAME, PublicationExtension::class.java)
        }
    }


    private fun registerPublication(project: Project) {
        if (project.plugins.hasPlugin("maven-publish")) {

            val publicationExtension = project.extensions.getByType(PublicationExtension::class.java)

            val extension = project.extensions.getByType(PublishingExtension::class.java)
            publicationExtension.bom?.let { bomConfig ->
                if (extension.publications.findByName(BomPublication.NAME) == null) {
                    extension.publications.create(BomPublication.NAME, MavenPublication::class.java) {
                        BomPublication.create(it, bomConfig)
                    }
                }
            }
            publicationExtension.aar?.let { aarConfig ->
                if (extension.publications.findByName(AarPublication.NAME) == null) {
                    extension.publications.create(AarPublication.NAME, MavenPublication::class.java) {
                        AarPublication.create(it, aarConfig)
                    }
                }
            }
            publicationExtension.jar?.let { jarConfig ->
                if (extension.publications.findByName(JarPublication.NAME) == null) {
                    extension.publications.create(JarPublication.NAME, MavenPublication::class.java) {
                        JarPublication.create(it, jarConfig)
                    }
                }
            }
        }
    }
}