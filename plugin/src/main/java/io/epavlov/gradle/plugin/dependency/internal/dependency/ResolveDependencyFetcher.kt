package io.epavlov.gradle.plugin.dependency.internal.dependency

import io.epavlov.gradle.plugin.dependency.DependencyNode
import io.epavlov.gradle.plugin.dependency.internal.cache.lib.LibCache
import io.epavlov.gradle.plugin.dependency.internal.filter.RegexFilter
import io.epavlov.gradle.plugin.dependency.internal.pom.PomDependency
import io.epavlov.gradle.plugin.dependency.internal.pom.PomXMLParser
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency

@Deprecated("Use incoming dependency fetcher")
internal class ResolveDependencyFetcher(
    private val filter: RegexFilter,
    private val libCache: LibCache,
    private val pomXMLParser: PomXMLParser
) : DependencyFetcher {

    override suspend fun fetch(configuration: Configuration): DependencyNode {
        runCatching {
            configuration.resolve()
        }.onFailure {
            throw Exception("can't resolve ${configuration.name}", it)
        }
        val dependencies = configuration.resolvedConfiguration.lenientConfiguration.allModuleDependencies
        val root = DependencyNode(configuration.name, isProject = true)
        fillDependencyTree(rootNode = root, dependencies = dependencies, pomDependencies = emptySet())
        return root
    }

    private fun fillDependencyTree(
        rootNode: DependencyNode,
        dependencies: Set<ResolvedDependency>,
        pomDependencies: Set<PomDependency>
    ) {
        dependencies
            .filter { filter.matches(it) }
            .forEach { dep ->
                val name = "${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}"
                val node = DependencyNode(
                    name = name,
                    children = mutableListOf(),
                    isProject = false
                )

                if (filter.matches(node)) {
                    val pomDeps = emptySet<PomDependency>()  //getPomDependencies(dep)
                    rootNode.children.add(node)
                    val subDep = dep.children
                    if (subDep.isNotEmpty()) {
                        fillDependencyTree(node, subDep, pomDeps)
                    }
                }
            }
    }

//    private fun getPomName(
//        pomDependencies: Set<PomDependency>,
//        dependency: ResolvedDependency
//    ): String? {
//        val pom = pomDependencies.find {
//            it.name == dependency.moduleName &&
//                it.groupId == dependency.moduleGroup
//        }
//        return pom?.run {
//            "${pom.groupId}:${pom.name}:${pom.version}"
//        }
//    }

//    private fun getPomDependencies(dependency: ResolvedDependency): Set<PomDependency> {
//        val pom = libCache.getLibData(dependency).pomFile
//        return pomXMLParser.parse(pom)
//    }
}