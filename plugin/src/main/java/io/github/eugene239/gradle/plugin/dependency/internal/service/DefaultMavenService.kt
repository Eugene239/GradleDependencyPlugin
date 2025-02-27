package io.github.eugene239.gradle.plugin.dependency.internal.service

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.exception.PomException
import io.github.eugene239.gradle.plugin.dependency.internal.exception.WIPException
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.xml.xml
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.gradle.api.logging.Logger
import java.net.URL

internal class DefaultMavenService(
    private val logger: Logger,
    private val repositoryProvider: RepositoryProvider
) : MavenService {

    private val client: HttpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = 5_000
        }
//        install(HttpRequestRetry) {
//            retryOnExceptionIf(
//                maxRetries = 2
//            ) { request, exception ->
//                if (exception !is CancellationException) {
//                    logger.warn("${request.url} failed with $exception")
//                }
//                exception is IOException
//                //exception is HttpRequestTimeoutException || exception is IOException
//            }
//            delayMillis { retry: Int -> retry * 1000L }
//        }
        install(ContentNegotiation) {
            xml(
                contentType = ContentType.Any,
                format = XmlFormat.format
            )
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
            client.get(getMetadataUrl(libIdentifier, repository)) {
                repository.authorization?.let {
                    headers.appendAll(it.toStringValues())
                }
            }.body<MavenMetadata>()
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
        val head = repository.withPermit {
            client.head(URL("${repository.url.removeSuffix("/")}/$urlPath")) {
                repository.authorization?.let {
                    headers.appendAll(it.toStringValues())
                }
            }
        }
        return head.headers["Content-Length"]?.toLongOrNull() ?: throw Exception("Can't get size for $libKey in repository: $repository with packaging: $packaging, $urlPath")
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