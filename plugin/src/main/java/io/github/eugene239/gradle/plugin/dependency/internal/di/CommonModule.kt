package io.github.eugene239.gradle.plugin.dependency.internal.di

import io.github.eugene239.gradle.plugin.dependency.internal.provider.DefaultIsSubmoduleProvider
import io.github.eugene239.gradle.plugin.dependency.internal.provider.IsSubmoduleProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.gradle.api.Project
import org.gradle.api.logging.Logger

object CommonModule {

    fun register(project: Project) {
        project.logger.warn("[DI] Register CommonModule")
        DI.register(Logger::class.java, project.logger)
        DI.register(Project::class.java, project)
        DI.register(IsSubmoduleProvider::class.java, DefaultIsSubmoduleProvider())
        DI.register(CoroutineDispatcher::class.java, Dispatchers.IO)
    }
}