package io.github.eugene239.gradle.plugin.dependency.internal.provider

import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import kotlinx.coroutines.sync.Semaphore

internal interface RepositoryProvider {

    fun getRepositories(): Set<Repository>

    fun getConnectionLimit(repository: Repository): Semaphore
}