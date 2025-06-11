package io.github.eugene239.gradle.plugin.dependency.internal.filter

import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.task.TaskConfiguration
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.result.ResolvedDependencyResult

internal class DependencyFilter : RegexFilter {

    private val project: Project by di()

    private val taskConfiguration: TaskConfiguration by di()
    private val rootProjectName by lazy {
        project.rootProject.name
    }
    private val regex: Regex? by lazy {
        taskConfiguration.regexFilter?.run { Regex(this) }
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

}