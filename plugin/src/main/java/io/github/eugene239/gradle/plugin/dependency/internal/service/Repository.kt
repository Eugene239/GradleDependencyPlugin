package io.github.eugene239.gradle.plugin.dependency.internal.service

import io.ktor.util.StringValues
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.util.Base64

internal data class Repository(
    val url: String,
    val name: String,
    val authorization: RepositoryAuthorization? = null
) {
    override fun toString(): String {
        return "[Repository] name:$name, url:$url"
    }
}

internal data class RepositoryAuthorization(
    val username: String,
    val password: String
)

internal fun MavenArtifactRepository.toRepository(): Repository {
    return Repository(
        url = url.toString(),
        name = name,
        authorization = if (credentials.username.isNullOrBlank().not()
            && credentials.password.isNullOrBlank().not()
        ) {
            RepositoryAuthorization(
                username = credentials.username!!,
                password = credentials.password!!
            )
        } else {
            null
        }
    )
}

internal fun RepositoryAuthorization.toStringValues(): StringValues {
    val token = Base64.getEncoder()
        .encodeToString("${username}:${password}".toByteArray())
    return StringValues.build {
        append("Authorization", "Basic $token")
    }
}
