package io.github.eugene239.gradle.plugin.dependency.internal.filter

import io.github.eugene239.gradle.plugin.dependency.internal.formatter.graph.DependencyNode
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.result.ResolvedDependencyResult

internal class DependencyFilter(
    private val project: Project,
) : RegexFilter {

    private var regex: Regex? = null

    private val rootProjectName = project.rootProject.name

    override fun matches(dependency: DependencyNode): Boolean {
        return matches(dependency.name)
    }

    override fun matches(dependency: String): Boolean {
        val group = dependency.split(":").first()
        val regexInstance = regex
        return rootProjectName.equals(group, true) || regexInstance == null || dependency.matches(regexInstance)
    }

    override fun matches(result: ResolvedDependencyResult): Boolean {
        val group = result.selected.moduleVersion?.group
        return (rootProjectName.equals(group, true) || matches(result.selected.id.displayName))
    }

    override fun matches(dependency: Dependency): Boolean {
        return matches("${dependency.group}:${dependency.name}${dependency.version}")
    }

    override fun isSubmodule(result: ResolvedDependencyResult): Boolean {
        val group = result.selected.moduleVersion?.group ?: return false
        return rootProjectName.equals(group, true)
    }

    fun setRegex(regex: Regex?) {
        this.regex = regex
    }
}