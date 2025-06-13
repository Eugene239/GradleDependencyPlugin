package io.github.eugene239.gradle.plugin.dependency.internal.output.conflict

import freemarker.template.Configuration
import io.github.eugene239.gradle.plugin.dependency.internal.FAIL
import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.WARN
import io.github.eugene239.gradle.plugin.dependency.internal.di.RootDir
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.output.Output
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ConflictLevel
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ConflictUseCaseResult
import io.github.eugene239.plugin.BuildConfig
import java.io.File

internal class MDConflictOutput : Output<ConflictUseCaseResult, File> {

    private val rootDir: RootDir by di()
    private val outputDirectory by lazy { rootDir.file }
    private val configuration: Configuration by di()

    override fun format(data: ConflictUseCaseResult): File {
        val file = createFile()
        val template = configuration.getTemplate("conflict-report-template.ftl")
        val templateData = convert(data)
        template.process(templateData, file.writer())
        return file
    }

    private fun convert(result: ConflictUseCaseResult): Map<String, Any> {
        return mapOf(
            "data" to result.conflictSet
                .sortedBy { it.level.ordinal }
                .map {
                    OutputData(
                        library = it.library,
                        level = if (ConflictLevel.MAJOR == it.level) FAIL else WARN,
                        versions = it.versions.joinToString(", ")
                    )
                },
            "version" to BuildConfig.PLUGIN_VERSION
        )
    }

    private fun createFile(): File {
        val dir = outputDirectory
        if (dir.exists().not()) {
            dir.mkdirs()
        }
        val file = File(dir, "conflict-report.md")
        file.createNewFile()
        return file
    }

    internal data class OutputData(
        val library: LibIdentifier,
        val level: String,
        val versions: String
    )
}