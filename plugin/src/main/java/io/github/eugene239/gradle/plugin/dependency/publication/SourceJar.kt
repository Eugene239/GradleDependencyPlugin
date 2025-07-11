package io.github.eugene239.gradle.plugin.dependency.publication

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.jvm.tasks.Jar

object SourceJar {
    private const val SOURCE_JAR_TASK = "generateSourceJar"

    fun addSources(project: Project, srcDirs: List<String>): Task? {
        if (project.tasks.findByName(SOURCE_JAR_TASK) == null) {
            project.tasks.register(SOURCE_JAR_TASK, Jar::class.java) {
                it.archiveClassifier.set("source")
                srcDirs.forEach { dir ->
                    val file = project.file(dir)
                    if (file.exists()) {
                        it.from(file)
                    } else {
                        project.logger.info("Source directory not found: $dir")
                    }
                }
            }
        }

        return project.tasks.findByName(SOURCE_JAR_TASK)
    }
}