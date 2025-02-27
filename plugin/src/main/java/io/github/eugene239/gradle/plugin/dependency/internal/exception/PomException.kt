package io.github.eugene239.gradle.plugin.dependency.internal.exception

internal sealed class PomException(override val message: String) : Exception(message) {
    data class PomNotFoundException(
        val url: String,
        val httpCode: Int
    ) : PomException("Pom not found in $url, httpCode: $httpCode")
}