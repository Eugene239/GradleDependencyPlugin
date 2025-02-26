package io.github.eugene239.gradle.plugin.dependency.internal.provider

import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository

internal interface RepositoryProvider {

    fun getRepositories(): Set<Repository>
}