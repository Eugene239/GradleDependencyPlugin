package io.epavlov.gradle.plugin.dependency.internal.di

import io.epavlov.gradle.plugin.dependency.Core
import io.epavlov.gradle.plugin.dependency.internal.CoreImpl
import io.epavlov.gradle.plugin.dependency.internal.cache.lib.LibCache
import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.epavlov.gradle.plugin.dependency.internal.dependency.DependencyFetcher
import io.epavlov.gradle.plugin.dependency.internal.dependency.IncomingDependencyFetcher
import io.epavlov.gradle.plugin.dependency.internal.formatter.DependencyFormatter
import io.epavlov.gradle.plugin.dependency.internal.formatter.Formatter
import io.epavlov.gradle.plugin.dependency.internal.formatter.report.MarkdownReportFormatter
import io.epavlov.gradle.plugin.dependency.internal.formatter.report.ReportFormatter
import io.epavlov.gradle.plugin.dependency.internal.pom.PomXMLParser
import io.epavlov.gradle.plugin.dependency.internal.pom.PomXMLParserImpl
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.dsl.koinApplication
import org.koin.dsl.module


internal val diModule = module {

    factory<Formatter> {
        DependencyFormatter(
            startupFlags = get()
        )
    }
    factory<PomXMLParser> {
        PomXMLParserImpl(
            filter = get()
        )
    }
    single {
        VersionCache(
            project = get()
        )
    }
    factory {
        LibCache(
            versionCache = get(),
            startupFlags = get(),
            project = get()
        )
    }
    factory<ReportFormatter> {
        MarkdownReportFormatter(
            project = get()
        )
    }

    factory<DependencyFetcher> {
        IncomingDependencyFetcher(
            project = get(),
            regexFilter = get(),
            libCache = get(),
            pomXMLParser = get()
        )
    }

    factory<Core> {
        CoreImpl(
            formatter = get(),
            project = get(),
            versionCache = get(),
            startupFlags = get(),
            dependencyFetcher = get()
        )
    }

}

internal val koinInstance = koinApplication { }

internal interface PluginComponent : KoinComponent {
    override fun getKoin(): Koin = koinInstance.koin
}