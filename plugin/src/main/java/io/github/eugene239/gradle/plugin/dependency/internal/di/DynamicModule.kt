package io.github.eugene239.gradle.plugin.dependency.internal.di

import com.sun.net.httpserver.HttpHandler
import io.github.eugene239.gradle.plugin.dependency.internal.OUTPUT_PATH
import io.github.eugene239.gradle.plugin.dependency.internal.cache.children.ChildrenCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryByNameCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.size.SizeCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.version.LatestVersionCache
import io.github.eugene239.gradle.plugin.dependency.internal.filter.DependencyFilter
import io.github.eugene239.gradle.plugin.dependency.internal.filter.RegexFilter
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.DefaultGraphOutput
import io.github.eugene239.gradle.plugin.dependency.internal.output.graph.GraphOutput
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.MarkdownReportFormatter
import io.github.eugene239.gradle.plugin.dependency.internal.output.report.ReportFormatter
import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultRepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.server.PluginHttpHandler
import io.github.eugene239.gradle.plugin.dependency.internal.server.PluginHttpServer
import io.github.eugene239.gradle.plugin.dependency.internal.service.DefaultMavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.ui.DefaultUiSaver
import io.github.eugene239.gradle.plugin.dependency.internal.ui.UiSaver
import io.github.eugene239.gradle.plugin.dependency.task.TaskConfiguration
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal object DynamicModule {

    fun register(taskConfiguration: TaskConfiguration) {
        val logger = DI.resolve(Logger::class.java)
        val project = DI.resolve(Project::class.java)

        logger.info("[DI] DynamicModule register $taskConfiguration")

        DI.register(TaskConfiguration::class.java, taskConfiguration)

        val rootDir = File("${project.layout.buildDirectory.asFile.get()}${File.separator}$OUTPUT_PATH")
        DI.register(RootDir::class.java, RootDir(rootDir))

        DI.register(RegexFilter::class.java, DependencyFilter())
        DI.register(MavenService::class.java, DefaultMavenService())
        DI.register(RepositoryProvider::class.java, DefaultRepositoryProvider())

        // Cache
        DI.register(PomCache::class.java, PomCache())
        DI.register(LatestVersionCache::class.java, LatestVersionCache())
        DI.register(RepositoryByNameCache::class.java, RepositoryByNameCache())
        DI.register(ChildrenCache::class.java, ChildrenCache())

        // Web Server
        DI.register(ExecutorService::class.java, Executors.newFixedThreadPool(5))
        DI.register(HttpHandler::class.java, PluginHttpHandler())
        DI.register(PluginHttpServer::class.java, PluginHttpServer())
        DI.register(SizeCache::class.java, SizeCache())

        // Output
        DI.register(ReportFormatter::class.java, MarkdownReportFormatter())
        DI.register(GraphOutput::class.java, DefaultGraphOutput())
        DI.register(UiSaver::class.java, DefaultUiSaver())
    }
}