package io.github.eugene239.gradle.plugin.dependency.internal.di

import java.io.File

internal data class RootDir(val file: File) {

    init {
        if (file.exists().not()) {
            file.mkdirs()
        }
    }
}
