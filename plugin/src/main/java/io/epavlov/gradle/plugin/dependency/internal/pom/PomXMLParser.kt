package io.epavlov.gradle.plugin.dependency.internal.pom

import io.epavlov.gradle.plugin.dependency.internal.LibKey
import java.io.File

internal interface PomXMLParser {

    fun parse(file: File): Set<LibKey>
}