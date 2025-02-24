package io.github.eugene239.gradle.plugin.dependency.internal.service

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("project", "http://maven.apache.org/POM/4.0.0")
internal data class Pom(
    @XmlElement(true)
    @XmlSerialName("groupId")
    var groupId: String? = null,
    @XmlElement(true)
    @XmlSerialName("artifactId")
    val artifactId: String?,
    @XmlElement(true)
    @XmlSerialName("version")
    val version: String?,
    @XmlElement(true)
    @XmlSerialName("packaging")
    val packaging: String? = null,
    @XmlElement(true)
    @XmlSerialName("dependencies")
    val dependencies: Dependencies? = null,
    @XmlElement(true)
    @XmlSerialName("dependencyManagement")
    val dependencyManagement: DependencyManagement? = null,

    @XmlElement(true)
    @XmlSerialName("parent")
    val parent: Parent?,

    @XmlElement(true)
    @XmlSerialName("properties")
    val properties: Properties? = null
)

@Serializable(with = MavenPomPropertiesXmlSerializer::class)
internal data class Properties(
    val entries: Map<String, String>? = null
)

@Serializable
internal data class Parent(
    @XmlElement(true)
    @XmlSerialName("groupId")
    val groupId: String,
    @XmlElement(true)
    @XmlSerialName("artifactId")
    val artifactId: String,
    @XmlElement(true)
    @XmlSerialName("version")
    val version: String
)

@Serializable
internal data class DependencyManagement(
    @XmlElement(true)
    @XmlSerialName("dependencies")
    val dependencies: Dependencies?
)


@Serializable
internal data class Dependencies(
    @XmlElement(true)
    @XmlSerialName("dependency")
    val dependency: List<Dependency>
)

@Serializable
internal data class Dependency(
    @XmlElement(true)
    @XmlSerialName("groupId")
    val groupId: String,
    @XmlElement(true)
    @XmlSerialName("artifactId")
    val artifactId: String,
    @XmlElement(true)
    @XmlSerialName("version")
    val version: String?,
    @XmlElement(true)
    @XmlSerialName("scope")
    val scope: String?
)