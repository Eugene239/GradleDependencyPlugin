package io.github.eugene239.gradle.plugin.dependency.internal.output.report

import io.github.eugene239.gradle.plugin.dependency.internal.output.Output
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.LatestVersionsUseCaseResult
import org.gradle.internal.cc.base.logger

internal class ConsoleLatestVersionsOutput : Output<LatestVersionsUseCaseResult, Unit> {

    override fun format(data: LatestVersionsUseCaseResult) {
        if (data.dependencies.isNotEmpty()) {
            logger.lifecycle("Latest versions list:")
            data.dependencies
                .forEach { dependency ->
                    logger.lifecycle("- ${dependency.name} : ${dependency.currentVersion} -> ${dependency.latestVersion} ${dependency.status} ")
                }
        } else {
            logger.lifecycle("Everything is up-to-date")
        }
    }
}