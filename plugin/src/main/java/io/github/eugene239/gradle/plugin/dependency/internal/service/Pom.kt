package io.github.eugene239.gradle.plugin.dependency.internal.service

import io.github.eugene239.gradle.plugin.dependency.internal.service.simplexml.PropertiesConverter
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.convert.Convert

@Root(name = "project", strict = false)
internal data class Pom(

    @field:Element(name = "groupId", required = false)
    var groupId: String? = null,

    @field:Element(name = "artifactId", required = false)
    var artifactId: String? = null,

    @field:Element(name = "version", required = false)
    var version: String? = null,

    @field:Element(name = "packaging", required = false)
    var packaging: String? = null,

    @field:Element(name = "dependencies", required = false)
    var dependencies: Dependencies? = null,

    @field:Element(name = "dependencyManagement", required = false)
    var dependencyManagement: DependencyManagement? = null,

    @field:Element(name = "parent", required = false)
    var parent: Parent? = null,


    @field:Element(name = "properties", required = false)
    var properties: Properties? = null
)

@Root(name = "properties", strict = false)
@Convert(PropertiesConverter::class)
internal data class Properties(
    var entries: Map<String, String>? = null
)

@Root(name = "parent", strict = false)
internal data class Parent(

    @field:Element(name = "groupId", required = false)
    var groupId: String? = null,

    @field:Element(name = "artifactId", required = false)
    var artifactId: String? = null,

    @field:Element(name = "version", required = false)
    var version: String? = null
)

@Root(name = "dependencyManagement", strict = false)
internal data class DependencyManagement(

    @field:Element(name = "dependencies", required = false)
    var dependencies: Dependencies? = null
)

@Root(name = "dependencies", strict = false)
internal data class Dependencies(

    @field:ElementList(entry = "dependency", inline = true, required = false)
    var dependency: List<Dependency> = mutableListOf()
)

@Root(name = "dependency", strict = false)
internal data class Dependency(

    @field:Element(name = "groupId")
    var groupId: String? = null,

    @field:Element(name = "artifactId")
    var artifactId: String? = null,

    @field:Element(name = "version", required = false)
    var version: String? = null,

    @field:Element(name = "scope", required = false)
    var scope: String? = null
)