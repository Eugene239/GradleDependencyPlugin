package io.github.eugene239.gradle.plugin.dependency.internal.service

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlElement(true)
@XmlSerialName("metadata")
internal data class MavenMetadata(
    @XmlElement(true)
    @XmlSerialName("groupId")
    var groupId: String? = null,
    @XmlElement(true)
    @XmlSerialName("artifactId")
    val artifactId: String?,
    @XmlElement(true)
    @XmlSerialName("versioning")
    val versioning: Versioning
)

@Serializable
internal data class Versioning(
    @XmlElement(true)
    @XmlSerialName("latest")
    val latest: String?,
    @XmlElement(true)
    @XmlSerialName("release")
    val release: String?
)

