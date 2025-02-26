package io.github.eugene239.gradle.plugin.dependency.internal.service

import kotlinx.serialization.decodeFromString
import org.junit.Assert
import org.junit.Test

internal class MavenPomPropertiesXmlSerializerTest {

    private val xml = XmlFormat.format

    @Test
    fun `parse properties with custom serializer`() {
        // WHEN
        val pom: Pom = xml.decodeFromString(xmlText)
        // THEN
        Assert.assertNotNull(pom.properties?.entries)
        Assert.assertEquals(pom.properties?.entries?.size, 7)
    }

    @Test
    fun `parse pom with and without namespace`() {
        // WHEN
        xml.decodeFromString<Pom>(xmlText).also {
            println(it)
        }
        xml.decodeFromString<Pom>(xmlWithoutNamespace).also {
            println(it)
        }

    }

    private val xmlText = """
        <project xmlns="http://maven.apache.org/POM/4.0.0">
          <properties>
            <test.include>%regex[.*.class]</test.include>
            <truth.version>1.1.2</truth.version>
            <checker-framework.version>3.12.0</checker-framework.version>
            <animal.sniffer.version>1.20</animal.sniffer.version>
            <maven-javadoc-plugin.version>3.1.0</maven-javadoc-plugin.version>
            <maven-javadoc-plugin.additionalJOptions></maven-javadoc-plugin.additionalJOptions>
            <maven-source-plugin.version>3.2.1</maven-source-plugin.version>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
          </properties>
        </project>
    """.trimIndent()

    private val xmlWithoutNamespace = """
        <project>
        <modelVersion>4.0.0</modelVersion>
        <groupId>log4j</groupId>
        <artifactId>log4j</artifactId>
        <version>1.2.12</version>
        </project>
    """.trimIndent()
}