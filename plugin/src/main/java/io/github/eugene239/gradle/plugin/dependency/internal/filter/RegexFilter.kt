package io.github.eugene239.gradle.plugin.dependency.internal.filter

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.result.ResolvedDependencyResult

internal interface RegexFilter {

    fun matches(dependency: String): Boolean
    fun matches(result: ResolvedDependencyResult): Boolean
    fun matches(dependency: Dependency): Boolean
    fun matched(dependency: DependencyResolveDetails): Boolean
    fun isSubmodule(result: ResolvedDependencyResult): Boolean
}