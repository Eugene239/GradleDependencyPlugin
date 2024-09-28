package io.epavlov.gradle.plugin.dependency.internal.filter

import io.epavlov.gradle.plugin.dependency.DependencyNode
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.result.ResolvedDependencyResult

internal interface RegexFilter {

    fun matches(dependency: DependencyNode): Boolean
    fun matches(dependency: ResolvedDependency): Boolean
    fun matches(dependency: String): Boolean
    fun matches(result: ResolvedDependencyResult) : Boolean
    fun matches(dependency: Dependency): Boolean
}