package io.epavlov.gradle.plugin.dependency.internal.cache.dependency

import io.epavlov.gradle.plugin.dependency.internal.LibKey

internal data class FailedDependency(
    val parent: LibKey?,
    val lib: LibKey,
    val error: Throwable
)