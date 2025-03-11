package io.github.eugene239.gradle.plugin.dependency.internal.service.serializer

import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader

internal fun EventType.makeString(reader: XmlReader): String {
    return if (isTextElement) {
        "[$this] ${reader.text}"
    } else {
        return if (EventType.END_ELEMENT == this || EventType.START_ELEMENT == this) {
            "[$this] ${reader.localName}"
        } else {
            "[$this]"
        }
    }
}