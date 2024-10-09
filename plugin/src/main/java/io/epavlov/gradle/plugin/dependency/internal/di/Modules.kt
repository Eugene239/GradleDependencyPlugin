package io.epavlov.gradle.plugin.dependency.internal.di

import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.dsl.koinApplication
import org.koin.dsl.module


internal val diModule = module {
   single {
        VersionCache(
            project = get()
        )
    }
}

internal val koinInstance = koinApplication { }

internal interface PluginComponent : KoinComponent {
    override fun getKoin(): Koin = koinInstance.koin
}