package io.epavlov.gradle.plugin.dependency.internal.filter

import io.epavlov.gradle.plugin.dependency.internal.formatter.graph.DependencyNode
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.result.ResolvedDependencyResult

internal class DependencyFilter(
    private val project: Project,
    private val regex: Regex
) : RegexFilter {

    private val rootProjectName = project.rootProject.name

    override fun matches(dependency: DependencyNode): Boolean {
        return matches(dependency.name)
    }

    override fun matches(dependency: ResolvedDependency): Boolean {
        return matches("${dependency.moduleGroup}:${dependency.moduleName}:${dependency.moduleVersion}")
    }

    override fun matches(dependency: String): Boolean {
        val moduleGroup = dependency.split(":").first()
        return rootProjectName.equals(moduleGroup, true) || dependency.matches(regex)
    }

    override fun matches(result: ResolvedDependencyResult): Boolean {
        val group = result.selected.moduleVersion?.group
        return (rootProjectName.equals(group, true) || matches(result.selected.id.displayName))
    }

    override fun matches(dependency: Dependency): Boolean {
        return matches("${dependency.group}:${dependency.name}${dependency.version}")
    }
}