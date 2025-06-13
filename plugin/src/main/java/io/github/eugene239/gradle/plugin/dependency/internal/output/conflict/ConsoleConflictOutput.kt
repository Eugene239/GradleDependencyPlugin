package io.github.eugene239.gradle.plugin.dependency.internal.output.conflict

import io.github.eugene239.gradle.plugin.dependency.internal.output.Output
import io.github.eugene239.gradle.plugin.dependency.internal.usecase.ConflictUseCaseResult
import org.gradle.internal.cc.base.logger

internal class ConsoleConflictOutput : Output<ConflictUseCaseResult, Unit> {

    override fun format(data: ConflictUseCaseResult) {
        if (data.conflictSet.isNotEmpty()) {
            logger.lifecycle("Conflicts list:")
            data.conflictSet
                .sortedBy { it.level.ordinal }
                .forEach { conflict ->
                    logger.lifecycle("- [${conflict.level.name}]  ${conflict.library} : ${conflict.versions}")
                }
        } else {
            logger.lifecycle("No conflicts found")
        }
    }
}