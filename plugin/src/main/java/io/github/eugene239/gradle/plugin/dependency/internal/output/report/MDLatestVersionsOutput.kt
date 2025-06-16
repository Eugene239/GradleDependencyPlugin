package io.github.eugene239.gradle.plugin.dependency.internal.output.report

import freemarker.template.Configuration
import io.github.eugene239.gradle.plugin.dependency.internal.di.RootDir
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.output.Output
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.LatestVersionsUseCaseResult
import io.github.eugene239.plugin.BuildConfig
import java.io.File


internal class MDLatestVersionsOutput : Output<LatestVersionsUseCaseResult, File> {
    private val rootDir: RootDir by di()
    private val outputDirectory by lazy { rootDir.file }
    private val configuration: Configuration by di()

    override fun format(data: LatestVersionsUseCaseResult): File {
        val file = createFile()
        val template = configuration.getTemplate("latest-versions-report-template.ftl")
        val templateData = mapOf(
            "dependencies" to data.dependencies.sortedByDescending { it.status },
            "version" to BuildConfig.PLUGIN_VERSION
        )
        template.process(templateData, file.writer())
        return file
    }

    private fun createFile(): File {
        val dir = outputDirectory
        if (dir.exists().not()) {
            dir.mkdirs()
        }
        val file = File(dir, "latest-versions-report.md")
        file.createNewFile()
        return file
    }
}