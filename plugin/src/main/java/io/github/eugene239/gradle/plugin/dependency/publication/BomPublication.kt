package io.github.eugene239.gradle.plugin.dependency.publication

import io.github.eugene239.gradle.plugin.dependency.internal.ARTIFACT_ID
import io.github.eugene239.gradle.plugin.dependency.internal.BOM_DEFAULT_ARTIFACT_ID
import io.github.eugene239.gradle.plugin.dependency.internal.GROUP_ID
import io.github.eugene239.gradle.plugin.dependency.internal.VERSION
import io.github.eugene239.gradle.plugin.dependency.internal.VERSION_DATE_FORMAT
import io.github.eugene239.gradle.plugin.dependency.internal.di.DI
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.filter.RegexFilter
import io.github.eugene239.gradle.plugin.dependency.internal.filteredConfigurations
import io.github.eugene239.gradle.plugin.dependency.internal.getLibDetails
import io.github.eugene239.gradle.plugin.dependency.internal.provider.IsSubmoduleProvider
import io.github.eugene239.gradle.plugin.dependency.task.BaseTask.Companion.DEFAULT_CONNECTION_TIMEOUT
import io.github.eugene239.gradle.plugin.dependency.task.BaseTask.Companion.DEFAULT_LIMIT
import io.github.eugene239.gradle.plugin.dependency.task.TaskConfiguration
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.publish.maven.MavenPublication
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar

internal object BomPublication {
    const val NAME = "BOM"

    fun create(publication: MavenPublication, config: BomConfig) {
        val project: Project by di()
        val logger: Logger by di()

        val groupId = config.groupId ?: project.group.toString()
        val artifactId = config.artifactId ?: BOM_DEFAULT_ARTIFACT_ID
        val version = config.version ?: GregorianCalendar().toZonedDateTime().format(DateTimeFormatter.ofPattern(VERSION_DATE_FORMAT))

        publication.groupId = groupId
        publication.artifactId = artifactId
        publication.version = version

        publication.pom.withXml { provider ->
            val dependencyManagement = provider.asNode().appendNode("dependencyManagement")
            val dependenciesNode = dependencyManagement.appendNode("dependencies")
            val configurations = project.filteredConfigurations()

            val task = object : TaskConfiguration {
                override val repositoryConnectionLimit = DEFAULT_LIMIT
                override val regexFilter: String? = config.filter
                override val connectionTimeOut: Long = DEFAULT_CONNECTION_TIMEOUT
            }
            DI.register(TaskConfiguration::class.java, task)

            val regexFilter: RegexFilter = DependencyFilter()
            val isSubmodule = DI.resolve(IsSubmoduleProvider::class.java)

            val dependencySet = configurations
                .asSequence()
                .map { configuration -> configuration.getLibDetails(regexFilter, isSubmodule) }
                .flatten()
                .filter { dependency -> dependency.isSubmodule.not() }
                .map { it.key }
                .toSet()

            dependencySet.forEach { dependency ->
                val dependencyNode = dependenciesNode.appendNode("dependency")
                dependencyNode.appendNode(GROUP_ID, dependency.group)
                dependencyNode.appendNode(ARTIFACT_ID, dependency.module)
                dependencyNode.appendNode(VERSION, dependency.version)
            }

            logger.lifecycle("Created BOM: $groupId:$artifactId:$version")
        }

    }
}