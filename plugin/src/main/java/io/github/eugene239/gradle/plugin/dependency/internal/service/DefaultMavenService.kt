package io.github.eugene239.gradle.plugin.dependency.internal.service

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.internal.exception.PomException
import io.github.eugene239.gradle.plugin.dependency.internal.exception.WIPException
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.simplexml.SimpleXmlConverter
import io.github.eugene239.gradle.plugin.dependency.task.TaskConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.withPermit
import org.gradle.api.logging.Logger
import java.net.URL

internal class DefaultMavenService : MavenService {

    private val logger: Logger by di()
    private val repositoryProvider: RepositoryProvider by di()
    private val taskConfiguration: TaskConfiguration by di()
    private val timeoutMillis: Long by lazy {
        taskConfiguration.connectionTimeOut
    }

    private val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = timeoutMillis
                connectTimeoutMillis = timeoutMillis
                socketTimeoutMillis = timeoutMillis
            }

            install(ContentNegotiation) {
                register(ContentType.Any, SimpleXmlConverter())
            }
        }
    }

    override suspend fun isMetadataExists(libIdentifier: LibIdentifier, repository: Repository): Result<Boolean> = kotlin.runCatching {
        repository.withPermit {
            val response = client.head(getMetadataUrl(libIdentifier, repository)) {
                repository.authorization?.let {
                    headers.appendAll(it.toStringValues())
                }
            }
            HttpStatusCode.OK == response.status
        }
    }

    override suspend fun getMetadata(libIdentifier: LibIdentifier, repository: Repository): MavenMetadata {
        return repository.withPermit {
            val url = getMetadataUrl(libIdentifier, repository)
            client.get(getMetadataUrl(libIdentifier, repository)) {
                repository.authorization?.let {
                    headers.appendAll(it.toStringValues())
                }
            }.body<MavenMetadata>()
                .copy(url = url)
        }
    }

    override suspend fun getPom(libKey: LibKey, repository: Repository): Pom {
        val urlPath = "${libKey.group.replace('.', '/')}/${libKey.module}/${libKey.version}/${libKey.module}-${libKey.version}.pom"
        val url = "${repository.url.removeSuffix("/")}/$urlPath"
            .also { logger.debug("pomUrl: $it") }
        val response = repository.withPermit {
            client.get(URL(url)) {
                repository.authorization?.let {
                    headers.appendAll(it.toStringValues())
                }
            }
        }
        when {
            response.status.isSuccess() -> return response.body()
            HttpStatusCode.NotFound == response.status -> throw PomException.PomNotFoundException(url, response.status.value)
            else -> throw WIPException("Unhandled error for pomCache, check $url")
        }
    }

    override suspend fun getSize(libKey: LibKey, repository: Repository, packaging: String): Long {
        val urlPath = "${libKey.group.replace('.', '/')}/${libKey.module}/${libKey.version}/${libKey.module}-${libKey.version}.$packaging"
        val url = "${repository.url.removeSuffix("/")}/$urlPath"
        val head = repository.withPermit {
            client.head(URL(url)) {
                repository.authorization?.let {
                    headers.appendAll(it.toStringValues())
                }
            }
        }
        return head.headers["Content-Length"]?.toLongOrNull() ?: throw Exception("Can't get size for $libKey in repository: $repository with packaging: $packaging, $url")
    }

    private fun getMetadataUrl(libIdentifier: LibIdentifier, repository: Repository): String {
        val libPath = "${libIdentifier.group.replace('.', '/')}/${libIdentifier.module}/maven-metadata.xml"
        return "${repository.url.removeSuffix("/")}/$libPath"
            .also { logger.debug("metadataUrl: $it") }
    }

    private suspend fun <T> Repository.withPermit(block: suspend () -> T): T {
        return repositoryProvider.getConnectionLimit(this).withPermit {
            block()
        }
    }
}