package io.github.eugene239.gradle.plugin.dependency.internal.pom

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import java.io.File

internal interface PomXMLParser {

    fun parse(file: File): Set<LibKey>
}