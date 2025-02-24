package io.github.eugene239.gradle.plugin.dependency.internal.service

import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultFormatCache
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalXmlUtilApi::class)
internal class MavenPomPropertiesXmlSerializerTest {

    private val xml = XML {
        policy = DefaultXmlSerializationPolicy(formatCache = DefaultFormatCache()) {
            unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
        }
        autoPolymorphic = true
    }

    @Test
    fun `parse properties with custom serializer`() {
        val pom: Pom = xml.decodeFromString(xmlText)
        Assert.assertNotNull(pom.properties?.entries)
        Assert.assertEquals(pom.properties?.entries?.size, 7)
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
}