package io.epavlov.gradle.plugin.dependency.internal.dependency

import io.epavlov.gradle.plugin.dependency.DependencyNode
import io.epavlov.gradle.plugin.dependency.internal.cache.lib.LibCache
import io.epavlov.gradle.plugin.dependency.internal.cache.lib.LibKey
import io.epavlov.gradle.plugin.dependency.internal.filter.RegexFilter
import io.epavlov.gradle.plugin.dependency.internal.pom.PomDependency
import io.epavlov.gradle.plugin.dependency.internal.pom.PomXMLParser
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult

internal class IncomingDependencyFetcher(
    private val project: Project,
    private val regexFilter: RegexFilter,
    private val libCache: LibCache,
    private val pomXMLParser: PomXMLParser
) : DependencyFetcher {
    companion object {
        private const val UNSPECIFIED = "unspecified"
    }

    override suspend fun fetch(configuration: Configuration): DependencyNode {
        println("####### FETCH ${configuration.name}")
        val incoming = configuration.incoming.resolutionResult.root.dependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .filter { regexFilter.matches(it) }
            .toSet()

        val parent = DependencyNode(name = configuration.name, isProject = true)
        println("incoming size:  ${incoming.size}")
        fillDependencyTree(
            parent = parent,
            dependencies = incoming,
        )
        println("####### FETCH ${configuration.name} END")
        return parent
    }

    private suspend fun fillDependencyTree(
        parent: DependencyNode,
        dependencies: Set<ResolvedDependencyResult>,
        pomDependencies: Set<PomDependency> = emptySet(),
    ) {
        coroutineScope {
            dependencies
                .filter { regexFilter.matches(it) }
                .map { dependency ->
                    async {
                        val selected = dependency.selected
                        val moduleVersion = selected.moduleVersion
                            ?: throw Exception("Module version not found for ${selected.id.displayName}")
                        val name = moduleVersion.module.toString()
                        val child = DependencyNode(
                            name = name,
                            versions = io.epavlov.gradle.plugin.dependency.Versions(
                                resolved = moduleVersion.version,
                                actual = moduleVersion.getPomVersion(pomDependencies)
                            ),
                            isProject = selected.id.isProjectComponent()
                        )
                        parent.children.add(child)

                        val childrenDependencies = if (child.isProject == true) {
                            emptySet()
                        } else {
                            selected.dependencies
                                .filterIsInstance<ResolvedDependencyResult>()
                                .filter { regexFilter.matches(it) }
                                .toSet()
                        }

                        val childrenPomDependencies = if (child.isProject == true) {
                            emptySet()
                        } else {
                            getPomDependencies(child)
                        }

                        fillDependencyTree(
                            parent = child,
                            dependencies = childrenDependencies,
                            pomDependencies = childrenPomDependencies
                        )
                    }
                }
                .awaitAll()
        }
    }

    private fun ModuleVersionIdentifier.getPomVersion(pomDependencies: Set<PomDependency>): String? {
        val pom = pomDependencies.find { it.name == module.name && it.groupId == group }
        if (UNSPECIFIED.equals(pom?.version, true)) {
            println("UNSPECIFIED VERSION: ${this.module}")
            return null
        }
        return pom?.version
    }

    private suspend fun getPomDependencies(node: DependencyNode): Set<PomDependency> {
        val split = node.name.split(":")
        val key = LibKey(
            group = split.first(),
            module = split.last(),
            version = node.versions.actual ?: node.versions.resolved!!
        )
        val pom = libCache.getLibData(key).pomFile
        return pomXMLParser.parse(pom)
    }

    /**
     * Pass null if not, to decrease json size
     */
    private fun ComponentIdentifier.isProjectComponent(): Boolean? {
        return when (this) {
            is ProjectComponentIdentifier -> true
            is ModuleComponentIdentifier -> null
            else -> {
                println("unknown identifier type: ${this::class.java}")
                null
            }
        }
    }
}