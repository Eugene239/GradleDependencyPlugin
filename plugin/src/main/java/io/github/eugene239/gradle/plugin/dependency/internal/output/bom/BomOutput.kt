package io.github.eugene239.gradle.plugin.dependency.internal.output.bom

import freemarker.template.Configuration
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.di.RootDir
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.output.Output
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.GenerateBomUseCaseResult
import io.github.eugene239.plugin.BuildConfig
import java.io.File

internal class BomOutput : Output<GenerateBomUseCaseResult, File> {

    private val rootDir: RootDir by di()
    private val outputDirectory by lazy { rootDir.file }
    private val configuration: Configuration by di()

    override fun format(data: GenerateBomUseCaseResult): File {
        val file = createFile(data.library)
        val template = configuration.getTemplate("bom-template.ftl")
        val templateData = convert(data)
        template.process(templateData, file.writer())
        return file
    }

    private fun convert(result: GenerateBomUseCaseResult): Map<String, Any> {
        return mapOf(
            "library" to result.library,
            "dependencies" to result.dependencies,
            "version" to BuildConfig.PLUGIN_VERSION
        )
    }

    private fun createFile(libKey: LibKey): File {
        val dir = outputDirectory
        if (dir.exists().not()) {
            dir.mkdirs()
        }
        val file = File(dir, "${libKey.group}:${libKey.module}.pom")
        file.createNewFile()
        return file
    }
}