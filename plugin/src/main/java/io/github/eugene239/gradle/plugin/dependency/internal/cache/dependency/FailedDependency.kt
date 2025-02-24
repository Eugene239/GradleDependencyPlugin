package io.github.eugene239.gradle.plugin.dependency.internal.cache.dependency

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey

@Deprecated("TODO DELETE?")
internal data class FailedDependency(
    val parent: LibKey?,
    val lib: LibKey,
    val error: Throwable
)