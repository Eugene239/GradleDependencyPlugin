package io.github.eugene239.gradle.plugin.dependency.internal.pom

import groovy.namespace.QName
import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.filter.RegexFilter
import org.gradle.api.logging.Logger
import java.io.File

internal class PomXMLParserImpl(
    private val logger: Logger,
    private val filter: RegexFilter?
) : PomXMLParser {
    // todo migrate to MavenResolver?
    // todo Get versions from parent, eg: com.google.guava:guava:31.1-jre
    companion object {
        private const val DEPENDENCIES = "dependencies"
        private const val DEPENDENCY = "dependency"
        private const val GROUP_ID = "groupId"
        private const val ARTIFACT_ID = "artifactId"
        private const val VERSION = "version"
        private const val PROPERTIES = "properties"
        private const val PARENT = "parent"
        private const val UNSPECIFIED = "unspecified"
        private const val SCOPE = "scope"
        private const val TEST = "test"
        private val legalVersionChars = setOf('.', '-')
    }

    private val parser = XmlParser()

    override fun parse(file: File): Set<LibKey> {
        kotlin.runCatching {
            val pomNode = parser.parse(file)
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
                    getPomDependency(child, filter, pomNode)
                }
                .toSet()
        }.onFailure {
            logger.error("Can't parse POM file ${file.path}", it)
        }
        return emptySet()
    }

    private fun getPomDependency(nodeList: NodeList, filter: RegexFilter?, pom: Node): LibKey? {
        var groupId: String? = null
        var artifactId: String? = null
        var version: String? = null

        nodeList
            .mapNotNull { it as? Node }
            .forEach { node ->
                val name = node.fieldName()
                val value = node.getStringValue()

                when (name) {
                    GROUP_ID -> groupId = value
                    ARTIFACT_ID -> artifactId = value
                    VERSION -> version = value
                    SCOPE -> {
                        // Ignore test dependencies
                        if (TEST == value) {
                            logger.info("IGNORE $groupId:$artifactId:$version")
                            return null
                        }
                    }

                    else -> {}
                }
            }

        if (groupId == null || artifactId == null || (filter != null && !filter.matches("$groupId:$artifactId:$version"))) {
            return null
        }
        val vrs = version
        if (vrs != null) {
            if (vrs.isBlank()) {
                return null
            }
            if (vrs.startsWith("\${")) {
                version = getVersionFromProperties(pom, vrs)
            } else if (vrs.startsWith("[")) {
                version = vrs.split(",").first().removePrefix("[").removeSuffix("]").removeSuffix("]")
                    .also { logger.info("Took version from list: $it ($vrs)") }
            } else if (UNSPECIFIED == vrs) {
                version = getUnspecifiedVersion(pom).also {
                    logger.info("Unspecified version -> $it")
                }
            }
        }
        val grp = groupId
        if (grp == "\${pom.groupId}") {
            groupId = getGroupFromProperties(pom).also {
                logger.info("Found real group: $it")
            }
        }



        return if (version != null) {
            LibKey(groupId!!, artifactId!!, escapeVersion(version!!))
        } else {
            throw Exception("Missing fields check $nodeList")
        }
    }

    private fun Node.fieldName(): String? {
        return (name() as? QName)?.qualifiedName
    }

    private fun escapeVersion(version: String): String {
        return if (version.any {
                it.isLetterOrDigit().not() && legalVersionChars.contains(it).not()
            }) {
            return version.filter { it.isLetterOrDigit() || legalVersionChars.contains(it) }.also {
                logger.info("Escaping version: $version -> $it")
            }
        } else {
            version
        }
    }

    private fun getGroupFromProperties(pom: Node): String {
        logger.info("getGroupFromProperties")
        val parent = pom.children().mapNotNull { it as? Node }
            .find { PARENT == it.fieldName() }
        val groupIdNode = parent?.children()
            ?.mapNotNull { it as? Node }
            ?.find { GROUP_ID == it.fieldName() }
        return groupIdNode?.getStringValue() ?: throw Exception("Can't find group property")
    }

    private fun getVersionFromProperties(pom: Node, version: String): String {
        val escaped = version.replace("{", "")
            .replace("\$", "")
            .replace("}", "")

        logger.info("Get version from properties: $escaped")
        val properties = pom.children().mapNotNull { it as? Node }
            .find { PROPERTIES == it.fieldName() }
        logger.info("PROPERTIES: $properties")
        val result = properties?.children()
            ?.mapNotNull {
                logger.info("CHILD NODE: $it")
                it as? Node
            }
            ?.find {
                logger.info("$escaped == ${it.fieldName()} = ${escaped == it.fieldName()}")
                escaped == it.fieldName()
            }?.getStringValue()


        logger.info("Get version from properties: $version --> $result")
        return result ?: throw Exception("Can't find version property: $version")
    }

    private fun getUnspecifiedVersion(pom: Node): String {
        logger.info("getUnspecifiedVersion")
        val version = pom.children().mapNotNull { it as? Node }
            .find { VERSION == it.fieldName() }
        return version?.getStringValue() ?: throw Exception("Can't find version property")
    }

    private fun Node.getStringValue(): String {
        val value = value()
        return (value as NodeList).text()
    }
}