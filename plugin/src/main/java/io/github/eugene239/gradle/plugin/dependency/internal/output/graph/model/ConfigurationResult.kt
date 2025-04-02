package io.github.eugene239.gradle.plugin.dependency.internal.output.graph.model

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.LibVersions

internal data class ConfigurationResult(
    val configuration: String,
    val versions: LibVersions,
    val flatDependencies: Map<LibKey, Set<LibKey>>,
    val topDependencies: Set<LibKey>
)