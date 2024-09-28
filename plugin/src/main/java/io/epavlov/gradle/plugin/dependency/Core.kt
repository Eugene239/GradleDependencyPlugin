package io.epavlov.gradle.plugin.dependency

import org.gradle.api.artifacts.Configuration

interface Core {

    fun execute(
        configurations: List<Configuration>,
    )
}