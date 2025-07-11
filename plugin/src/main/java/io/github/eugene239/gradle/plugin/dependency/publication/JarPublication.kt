package io.github.eugene239.gradle.plugin.dependency.publication

import io.github.eugene239.gradle.plugin.dependency.internal.VERSION_DATE_FORMAT
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.publish.maven.MavenPublication
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar

object JarPublication {
    const val NAME = "JAR"

    fun create(publication: MavenPublication, config: JarConfig) {
        val project: Project by di()
        val logger: Logger by di()


        val groupId = config.groupId ?: project.group.toString()
        val artifactId = config.artifactId ?: project.name
        val version = config.version ?: GregorianCalendar().toZonedDateTime().format(DateTimeFormatter.ofPattern(VERSION_DATE_FORMAT))

        publication.groupId = groupId
        publication.artifactId = artifactId
        publication.version = version

        if (config.addSource) {
            SourceJar.addSources(project, config.srcDirs)?.let { task ->
                publication.artifact(task)
            }
        }

        if (config.artifacts.isNotEmpty()) {
            config.artifacts.forEach {
                publication.artifact(it)
            }
        }

        publication.from(project.components.getByName("java"))
    }
}