package io.github.eugene239.gradle.plugin.dependency.internal.ui

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileNotFoundException
import java.net.URL

internal class DefaultUiSaver() : UiSaver {
    companion object {
        private const val BASE_SITE_FILE = "index.html"
    }

    private val prettyEncoder = Json {
        prettyPrint = true
        explicitNulls = false
    }


    override fun save(outputDir: File): File {
        val site = copyFile(
            this::class.java.classLoader.getResource(BASE_SITE_FILE),
            BASE_SITE_FILE,
            outputDir
        )
        getFilesFromManifest().forEach {
            copyFile(it, it.getName(), outputDir)
        }
        return site
    }

    private fun getFilesFromManifest(): List<URL> {
        val manifest = this::class.java.classLoader.getResource(".vite/manifest.json")
            ?: throw Exception("Vite manifest not found")
        val json: JsonElement = prettyEncoder.decodeFromString(manifest.readText())
        return json.jsonObject.mapNotNull { (_, jsonElement) ->
            val array = mutableListOf<URL>()

            val filePath = jsonElement.jsonObject["file"]?.jsonPrimitive?.content
            if (filePath != null) {
                this::class.java.classLoader.getResource(filePath)?.let {
                    array.add(it)
                }
            }
            val cssList = jsonElement.jsonObject["css"]?.jsonArray
                ?.map { it.jsonPrimitive.content }.orEmpty()

            array.addAll(
                cssList.mapNotNull { cssPath ->
                    this::class.java.classLoader.getResource(cssPath)
                }
            )
            return@mapNotNull array
        }.flatten()
    }

    private fun copyFile(resource: URL?, name: String, outputDir: File): File {
        if (resource == null) throw FileNotFoundException("Can't copy resources: $name, resource is null")
        val file = File(outputDir, resource.getPathAfterResource())
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeBytes(resource.readBytes())
        return file
    }

    private fun URL.getName(): String {
        return this.path.substringAfterLast("/")
    }

    private fun URL.getPathAfterResource(): String {
        return this.path.substringAfterLast("!/")
    }
}