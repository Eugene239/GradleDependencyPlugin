package io.epavlov.gradle.plugin.dependency.internal.cache.lib

import java.io.File

@Deprecated("Use Dependency cache")
internal data class LibData(
    val pomFile: File?,
    val libFile: File? // todo check
)