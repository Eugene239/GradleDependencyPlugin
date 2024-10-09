package io.epavlov.gradle.plugin.dependency

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class BaseTask : DefaultTask() {
    private val scope = CoroutineScope(Dispatchers.Default + CoroutineName(this.name))

    @TaskAction
    fun init() {
        runCatching {
            runBlocking {
                scope.launch {
                    exec()
                }.join()
            }
        }.onFailure {
            logger.error("Task execution failed", it)
            throw it
        }
    }

    abstract suspend fun exec()
}