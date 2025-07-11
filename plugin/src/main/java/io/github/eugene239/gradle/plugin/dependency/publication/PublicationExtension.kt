package io.github.eugene239.gradle.plugin.dependency.publication

import org.gradle.api.Action

open class PublicationExtension {
    companion object {
        const val EXTENSION_NAME = "dependencyPublication"
    }

    var bom: BomConfig? = null
    var aar: AarConfig? = null
    var jar: JarConfig? = null

    fun bom(configure: Action<BomConfig>) {
        bom = BomConfig().apply(configure::execute)
    }

    fun aar(configure: Action<AarConfig>) {
        aar = AarConfig().apply(configure::execute)
    }

    fun jar(configure: Action<JarConfig>) {
        jar = JarConfig().apply(configure::execute)
    }
}

data class BomConfig(
    var groupId: String? = null,
    var artifactId: String? = null,
    var version: String? = null,
    var filter: String? = null
)

data class AarConfig(
    var groupId: String? = null,
    var artifactId: String? = null,
    var version: String? = null,
    var addSource: Boolean = false,
    var srcDirs: List<String> = listOf("src/main/java", "src/main/kotlin"),
    var artifacts: List<Any> = emptyList()
)

data class JarConfig(
    var groupId: String? = null,
    var artifactId: String? = null,
    var version: String? = null,
    var addSource: Boolean = false,
    var srcDirs: List<String> = listOf("src/main/java", "src/main/kotlin"),
    var artifacts: List<Any> = emptyList()
)
