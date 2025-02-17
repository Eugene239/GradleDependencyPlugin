package io.github.eugene239.gradle.plugin.dependency.internal.ui

import java.io.File

internal interface UiSaver {

    fun save(outputDir: File): File
}