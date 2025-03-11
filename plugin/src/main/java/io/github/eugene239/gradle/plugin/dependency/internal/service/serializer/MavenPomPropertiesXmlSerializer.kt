package io.github.eugene239.gradle.plugin.dependency.internal.service.serializer

import io.github.eugene239.gradle.plugin.dependency.internal.service.Properties
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.serialization.XML

internal object MavenPomPropertiesXmlSerializer : KSerializer<Properties> {

    private const val NAME = "properties"

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(NAME) {
        element<String>("entries", isOptional = true)
    }

    private fun readProperties(reader: XmlReader): Map<String, String> {
        val map = mutableMapOf<String, String>()
        var element: EventType?
        while (reader.hasNext() && !(reader.eventType == EventType.END_ELEMENT && NAME == reader.localName)) {
            if (EventType.START_ELEMENT == reader.eventType && NAME != reader.localName) {
                readProperty(reader)?.let {
                    map.put(it.first, it.second)
                }

            }
            element = reader.next()
        }
        return map
    }

    private fun readProperty(reader: XmlReader): Pair<String, String>? {
        val name = reader.localName
        var element: EventType?
        while (reader.hasNext() && reader.eventType != EventType.END_ELEMENT) {
            element = reader.next()
            if (element.isTextElement) {
                return name to reader.text
            }
        }
        return null
    }

    private fun decode(decoder: Decoder): Properties {
        val reader = (decoder as? XML.XmlInput)?.input ?: return Properties()
        return Properties(entries = readProperties(reader))
    }

    override fun deserialize(decoder: Decoder) = decode(decoder)

    override fun serialize(encoder: Encoder, value: Properties) = Unit
}