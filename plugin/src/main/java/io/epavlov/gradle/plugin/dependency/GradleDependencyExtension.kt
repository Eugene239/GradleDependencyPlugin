package io.epavlov.gradle.plugin.dependency

open class GradleDependencyExtension {
    var appConfigurationNames: List<String> = listOf("runtimeClasspath")
    var dependencyNameRegex: String = ".*"
    var printConfigurations = false
    var checkVersions = true
}