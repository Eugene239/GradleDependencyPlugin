package io.epavlov.gradle.plugin.dependency.internal.pom

import java.io.File

internal interface PomXMLParser {

    fun parse(file: File): Set<PomDependency>
}