package io.epavlov.gradle.plugin.dependency.internal.formatter.flat

import io.epavlov.gradle.plugin.dependency.internal.LibKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

internal class FlatFormatter {
    private val prettyEncoder = Json {
        prettyPrint = true
        explicitNulls = false
    }

    fun format(outputDir: File, map: Map<LibKey, Collection<LibKey>>): File {
        val data = map
            .mapKeys { it.key.toString() }
            .mapValues { it.value.map { lib -> lib.toString() } }
            .toSortedMap()
            .toMap()
        val jsonElement = prettyEncoder.encodeToJsonElement(data)
        val json = prettyEncoder.encodeToString(jsonElement)
        val file = File(outputDir, "flat-dependencies.json")
        file.createNewFile()
        file.writeText(json)
        return file
    }
}