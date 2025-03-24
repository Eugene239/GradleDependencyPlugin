package io.github.eugene239.gradle.plugin.dependency.internal.service

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "metadata", strict = false)
internal data class MavenMetadata(
    @field:Element(name = "groupId", required = false)
    var groupId: String? = null,
    @field:Element(name = "artifactId", required = false)
    var artifactId: String? = null,
    @field:Element(name = "versioning", required = false)
    var versioning: Versioning? = null,

    // Path of metadata to validate
    var url: String? = null
)

@Root(name = "versioning", strict = false)
internal data class Versioning(
    @field:Element(name = "latest", required = false)
    var latest: String? = null,
    @field:Element(name = "release", required = false)
    var release: String? = null,

    @field:Element(name = "versions", required = false)
    var versions: Versions? = null
)

@Root(name = "versions", strict = false)
internal data class Versions(
    @field:ElementList(entry = "version", inline = true, required = false)
    var version: List<String> = mutableListOf()
)

