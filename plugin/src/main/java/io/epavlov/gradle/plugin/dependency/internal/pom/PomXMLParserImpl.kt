package io.epavlov.gradle.plugin.dependency.internal.pom

import groovy.namespace.QName
import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import io.epavlov.gradle.plugin.dependency.internal.filter.RegexFilter
import org.gradle.api.internal.artifacts.repositories.resolver.MavenResolver
import java.io.File

internal class PomXMLParserImpl(
    private val filter: RegexFilter?
) : PomXMLParser {
    // todo migrate to MavenResolver?
    companion object {
        private const val DEPENDENCIES = "dependencies"
        private const val DEPENDENCY = "dependency"
        private const val GROUP_ID = "groupId"
        private const val ARTIFACT_ID = "artifactId"
        private const val VERSION = "version"
    }

    private val parser = XmlParser()

    override fun parse(file: File): Set<PomDependency> {
        val pomNode = parser.parse(file)
        kotlin.runCatching {
            val pomDependencies = pomNode.children()
                .mapNotNull { it as? Node }
                .find {
                    DEPENDENCIES == it.fieldName()
                } ?: return emptySet()

            return pomDependencies.children()
                .asSequence()
                .mapNotNull { it as? Node }
                .filter { DEPENDENCY == it.fieldName() }
                .mapNotNull { it.value() as? NodeList }
                .mapNotNull { child ->
                    getPomDependency(child, filter)
                }
                .toSet()
        }.onFailure {
            it.printStackTrace()
        }
        return emptySet()
    }

    private fun getPomDependency(nodeList: NodeList, filter: RegexFilter?): PomDependency? {
        var groupId: String? = null
        var artifactId: String? = null
        var version: String? = null

        nodeList
            .mapNotNull { it as? Node }
            .forEach { node ->
                val name = node.fieldName()
                val value = (node.value() as NodeList).firstOrNull() as? String? ?: return@forEach

                when (name) {
                    GROUP_ID -> groupId = value
                    ARTIFACT_ID -> artifactId = value
                    VERSION -> version = value
                    else -> {}
                }
            }

        if (groupId == null || artifactId == null || (filter!= null && !filter.matches("$groupId:$artifactId:$version"))) {
            return null
        }

        return if (version != null) {
            PomDependency(groupId!!, artifactId!!, version!!)
        } else {
            throw Exception("Missing fields check $nodeList")
        }
    }

    private fun Node.fieldName(): String? {
        return (name() as? QName)?.qualifiedName
    }
}