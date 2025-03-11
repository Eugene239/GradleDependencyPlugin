package io.github.eugene239.gradle.plugin.dependency.internal.cache

import kotlinx.coroutines.CancellationException

fun <T> Result<T>.rethrowCancellationException(): Result<T> {
    return fold(
        onFailure = { exception ->
            when (exception) {
                is CancellationException -> throw exception
                else -> this
            }
        },
        onSuccess = { this }
    )
}