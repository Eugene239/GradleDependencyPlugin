package io.github.eugene239.gradle.plugin.dependency.internal.formatter.graph.old

import kotlinx.serialization.Serializable
import java.util.Collections


@Serializable
internal data class DependencyNode(
    val name: String,
    var versions: Versions = Versions(),
    val children: MutableList<DependencyNode> = Collections.synchronizedList(mutableListOf()),
    val isProject: Boolean? = null
)

@Serializable
internal data class Versions(
    val actual: String? = null,
    val resolved: String? = null,
    val latest: String? = null
)
