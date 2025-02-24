package io.github.eugene239.gradle.plugin.dependency.internal

import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier

internal open class LibIdentifier(
    open val group: String,
    open val module: String
) {
    override fun toString(): String {
        return "$group:$module"
    }
}

internal class LibKey(
    override val group: String,
    override val module: String,
    val version: String
) : LibIdentifier(group = group, module = module) {

    fun toIdentifier(): ComponentIdentifier {
        val version = DefaultModuleVersionIdentifier.newId(group, module, version)
        return DefaultModuleComponentIdentifier.newId(version)
    }

    override fun toString(): String {
        return "$group:$module:$version"
    }
}

