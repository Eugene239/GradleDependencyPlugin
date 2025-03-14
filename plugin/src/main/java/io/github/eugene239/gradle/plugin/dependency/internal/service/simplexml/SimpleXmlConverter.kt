package io.github.eugene239.gradle.plugin.dependency.internal.service.simplexml

import io.github.eugene239.gradle.plugin.dependency.internal.service.XmlFormat
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.ContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.toByteArray
import java.io.StringReader
import java.io.StringWriter

class SimpleXmlConverter : ContentConverter {

    private val serializer = XmlFormat.serializer

    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any? {
        val reader = StringReader(String(content.toByteArray()))
        return serializer.read(typeInfo.type.javaObjectType, reader)
    }

    override suspend fun serialize(contentType: ContentType, charset: Charset, typeInfo: TypeInfo, value: Any?): OutgoingContent? {
        val writer = StringWriter()
        serializer.write(value, writer)

        return ByteArrayContent(
            bytes = writer.toString().toByteArray(charset)
        )
    }
}