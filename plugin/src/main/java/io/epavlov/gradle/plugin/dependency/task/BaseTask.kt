package io.epavlov.gradle.plugin.dependency.task

import io.epavlov.gradle.plugin.dependency.internal.OUTPUT_PATH
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class BaseTask : DefaultTask() {

    @TaskAction
    fun init() {
        runCatching {
            makeDir()
            runBlocking {
                exec()
            }
        }.onFailure {
            logger.error("Task execution failed", it)
            throw it
        }
    }

    private fun makeDir() {
        val rootDir = File(project.buildDir, OUTPUT_PATH)
        if (rootDir.exists()) {
            rootDir.mkdirs()
        } else {
            rootDir.mkdirs()
        }
    }

    abstract suspend fun exec()
}