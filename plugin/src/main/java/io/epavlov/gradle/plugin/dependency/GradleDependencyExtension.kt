package io.epavlov.gradle.plugin.dependency

open class GradleDependencyExtension {
    var appConfigurationNames: List<String> = emptyList()
    var dependencyNameRegex: String = ".*"
    var printConfigurations = false
    var checkVersions = true
}