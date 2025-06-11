package io.github.eugene239.gradle.plugin.dependency.task

import io.github.eugene239.gradle.plugin.dependency.internal.di.DynamicModule
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class BaseTask : DefaultTask() {

    companion object {
        const val DEFAULT_LIMIT = 20
        const val DEFAULT_CONNECTION_TIMEOUT: Long = 10_000
    }

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


    abstract fun configuration(): TaskConfiguration

    abstract suspend fun exec()

}