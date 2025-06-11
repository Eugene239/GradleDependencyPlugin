package io.github.eugene239.gradle.plugin.dependency.internal.output.report

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import io.github.eugene239.gradle.plugin.dependency.internal.di.RootDir
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.plugin.BuildConfig
import java.io.File

internal class MarkdownReportFormatter : ReportFormatter {

    private val rootDir: RootDir by di()
    private val outputDirectory by lazy { rootDir.file }

    override fun format(outdated: Collection<OutdatedDependency>): File {
        val file = createFile()
        val config = Configuration(Configuration.VERSION_2_3_33).apply {
            defaultEncoding = "UTF-8"
            logTemplateExceptions = true
            templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
            setClassForTemplateLoading(this::class.java, "/")
        }
        val template = config.getTemplate("ReportTemplate.ftl")
        val data = mapOf(
            "dependencies" to outdated.sortedByDescending { it.status.ordinal },
            "version" to BuildConfig.PLUGIN_VERSION
        )
        template.process(data, file.writer())
        return file
    }


    private fun createFile(): File {
        val dir = outputDirectory
        if (dir.exists().not()) {
            dir.mkdirs()
        }
        val file = File(dir, "Report.md")
        file.createNewFile()
        return file
    }
}