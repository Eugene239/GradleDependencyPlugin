package io.github.eugene239.gradle.plugin.dependency.publication

import io.github.eugene239.gradle.plugin.dependency.internal.ARTIFACT_ID
import io.github.eugene239.gradle.plugin.dependency.internal.GROUP_ID
import io.github.eugene239.gradle.plugin.dependency.internal.VERSION
import io.github.eugene239.gradle.plugin.dependency.internal.VERSION_DATE_FORMAT
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar

object AarPublication {

    const val NAME = "AAR"
    private const val SOURCE_JAR_TASK = "generateSourceJar"
    private const val AAR_TASK = "bundleReleaseAar"

    fun create(publication: MavenPublication, config: AarConfig) {
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

        val aarArtifactTask = project.tasks.named(AAR_TASK)
        if (aarArtifactTask.isPresent.not()) {
            throw Exception("Can't create aarPublication, task bundleReleaseAar does not exists")
        }
        publication.artifact(aarArtifactTask)

        publication.pom.withXml { provider ->
            val dependenciesNode = provider.asNode().appendNode("dependencies")
            val configurations = project.filteredConfigurations()

            val dependencySet = configurations
                .asSequence()
                .map { it.allDependencies }
                .flatten()
                .filter { it.group != null && it.version != "unspecified" }
                .toSet()

            dependencySet.forEach { dependency ->
                val dependencyNode = dependenciesNode.appendNode("dependency")
                dependencyNode.appendNode(GROUP_ID, dependency.group)
                dependencyNode.appendNode(ARTIFACT_ID, dependency.name)
                dependencyNode.appendNode(VERSION, dependency.version)
            }

            logger.lifecycle("Created POM: $groupId:$artifactId:$version")
        }
    }

}