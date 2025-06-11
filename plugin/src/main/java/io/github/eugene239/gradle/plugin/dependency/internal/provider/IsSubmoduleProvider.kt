package io.github.eugene239.gradle.plugin.dependency.internal.provider

import org.gradle.api.artifacts.result.ResolvedDependencyResult

interface IsSubmoduleProvider {

    fun isSubmodule(dependency: ResolvedDependencyResult): Boolean
}