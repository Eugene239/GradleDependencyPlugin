package io.github.eugene239.gradle.plugin.dependency.internal.filter

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.result.ResolvedDependencyResult

internal class DependencyFilter(
    private val rootProjectName: String
) : RegexFilter {

    private var regex: Regex? = null

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