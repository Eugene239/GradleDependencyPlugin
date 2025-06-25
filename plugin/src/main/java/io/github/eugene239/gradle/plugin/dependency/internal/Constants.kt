package io.github.eugene239.gradle.plugin.dependency.internal


internal const val UNSPECIFIED_VERSION = "unspecified"
internal const val OUTPUT_PATH = "dependency-ui"
internal const val CONSOLE = "console"
internal val PREP_RELEASE_KEYS = setOf(
    "rc", "alpha", "beta", "snapshot",
    "preview", "dev", "incubating",
    // adding keys m1, b9, p2, a3... too
    *setOf(
        IntRange(0, 10).map { setOf("m$it", "b$it", "p$it", "a$it") }
    ).flatten()
        .flatten()
        .toTypedArray()
)


internal const val WARN = "⚠\uFE0F"
internal const val FAIL = "❗"
internal const val OK = "✅"

internal const val ARTIFACT_ID = "artifactId"
internal const val GROUP_ID = "groupId"
internal const val VERSION = "version"
internal const val VERSION_DATE_FORMAT = "YYYY-MM-dd"
internal const val BOM_DEFAULT_ARTIFACT_ID = "bom"