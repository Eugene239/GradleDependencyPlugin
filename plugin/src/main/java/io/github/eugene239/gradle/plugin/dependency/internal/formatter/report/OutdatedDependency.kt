package io.github.eugene239.gradle.plugin.dependency.internal.formatter.report

internal data class OutdatedDependency(
    val group: String,
    val module: String,
    val versions: OutdatedVersion
)

internal data class OutdatedVersion(
    val current: String,
    val latest: String
)