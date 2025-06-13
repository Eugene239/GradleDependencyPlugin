package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.di.DynamicModule
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class BaseTask : DefaultTask() {

    companion object {
        const val DEFAULT_LIMIT = 20
        const val DEFAULT_CONNECTION_TIMEOUT: Long = 10_000
    }

    @Input
    @Option(option = "repository-connection-limit", description = "Max api calls to repository at one time")
    var limit: String = "$DEFAULT_LIMIT"

    @Input
    @Option(option = "filter", description = "Regex filter for dependencies")
    var filter: String = ""

    @Input
    @Option(option = "connection-timeout", description = "Ktor client connection timeout")
    var connectionTimeout: String = "$DEFAULT_CONNECTION_TIMEOUT"

    @TaskAction
    fun init() {
        runCatching {
            DynamicModule.register(configuration())
            runBlocking {
                exec()
            }
        }.onFailure {
            logger.error("Task execution failed", it)
            throw it
        }
    }


    open fun configuration(): TaskConfiguration {
        return object : TaskConfiguration {
            override val repositoryConnectionLimit: Int = limit.toIntOrNull() ?: DEFAULT_LIMIT
            override val regexFilter: String? = filter.ifBlank { null }
            override val connectionTimeOut: Long = connectionTimeout.toLongOrNull() ?: DEFAULT_CONNECTION_TIMEOUT
        }
    }

    abstract suspend fun exec()

}