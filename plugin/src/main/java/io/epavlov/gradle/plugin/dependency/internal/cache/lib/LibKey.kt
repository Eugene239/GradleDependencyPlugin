package io.epavlov.gradle.plugin.dependency.internal.cache.lib

import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier


internal data class LibKey(
    val group: String,
    val module: String,
    val version: String
) {

    fun toIdentifier(): ComponentIdentifier {
        val version = DefaultModuleVersionIdentifier.newId(group, module, version)
        return DefaultModuleComponentIdentifier.newId(version)
    }

    override fun toString(): String {
        return "$group:$module:$version"
    }
}

