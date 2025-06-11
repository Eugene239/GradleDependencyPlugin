package io.github.eugene239.gradle.plugin.dependency.internal.provider

import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedDependencyResult

class DefaultIsSubmoduleProvider : IsSubmoduleProvider {

    private val project: Project by di()

    override fun isSubmodule(dependency: ResolvedDependencyResult): Boolean {
        val group = dependency.selected.moduleVersion?.group
        return project.rootProject.name.equals(group, true)
    }
}