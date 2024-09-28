package io.epavlov.gradle.plugin.dependency

import kotlinx.serialization.Serializable


@Serializable
data class DependencyNode(
    val name: String,
    var versions: Versions = Versions(),
    val children: MutableList<DependencyNode> = mutableListOf(),
    val isProject: Boolean? = null
)

@Serializable
data class Versions(
    val actual: String? = null,
    val resolved: String? = null,
    val latest: String? = null
)
