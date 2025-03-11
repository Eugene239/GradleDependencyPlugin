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
        xml.decodeFromString<Pom>(xmlText)
        xml.decodeFromString<Pom>(xmlWithoutNamespace)
    }

    @Test
    fun `parse maven metadata with versions`() {
        // WHEN
        val metadata = xml.decodeFromString<MavenMetadata>(metadata)
        // THEN
        Assert.assertEquals(metadata.versioning.versions?.version?.contains("4.3.0-7b54459-SNAPSHOT"), true)
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

    private val metadata = """
        <metadata modelVersion="1.1.0">
        <groupId>com.github.eugene239</groupId>
        <artifactId>plugin</artifactId>
        <version>4.4.0</version>
        <versioning>
        <latest>4.4.0-92882c1-SNAPSHOT</latest>
        <release>4.4.0</release>
        <versions>
        <version>2.0.0-b8db2f9-SNAPSHOT</version>
        <version>2.0.0</version>
        <version>2.0.0-cea55da-SNAPSHOT</version>
        <version>2.0.0-f075330-SNAPSHOT</version>
        <version>2.0.0-65e3717-SNAPSHOT</version>
        <version>3.0.0</version>
        <version>3.0.0-ef84d33-SNAPSHOT</version>
        <version>4.0.0</version>
        <version>4.0.0-5b0d5fa-SNAPSHOT</version>
        <version>4.0.0-05c53c7-SNAPSHOT</version>
        <version>4.0.0-32a3139-SNAPSHOT</version>
        <version>4.1.0</version>
        <version>4.1.0-cf78521-SNAPSHOT</version>
        <version>4.1.0-2d42109-SNAPSHOT</version>
        <version>4.1.0-7c5e335-SNAPSHOT</version>
        <version>4.1.0-7c14f6a-SNAPSHOT</version>
        <version>4.1.0-9e59b14-SNAPSHOT</version>
        <version>4.1.0-20ac51a-SNAPSHOT</version>
        <version>4.1.0-8315005-SNAPSHOT</version>
        <version>4.2.0</version>
        <version>4.2.0-5d6be0c-SNAPSHOT</version>
        <version>4.3.0</version>
        <version>4.3.0-0b348d9-SNAPSHOT</version>
        <version>4.3.0-7b54459-SNAPSHOT</version>
        <version>4.3.0-746ac84-SNAPSHOT</version>
        <version>4.4.0-b3a17d2-SNAPSHOT</version>
        <version>4.4.0</version>
        <version>4.4.0-cbd47b3-SNAPSHOT</version>
        <version>4.4.0-4733e1b-SNAPSHOT</version>
        <version>4.4.0-aaaaaaa-SNAPSHOT</version>
        </versions>
        <lastUpdated>20250221070717</lastUpdated>
        </versioning>
        </metadata>
    """.trimIndent()
}