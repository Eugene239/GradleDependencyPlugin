package io.github.eugene239.gradle.plugin.dependency.internal.service

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.exception.PomException
import io.github.eugene239.gradle.plugin.dependency.internal.exception.WIPException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.xml.xml
import org.gradle.api.logging.Logger
import java.net.URL

internal class DefaultMavenService(
    private val logger: Logger
) : MavenService {

    private val client: HttpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 10_000
        }
//        install(HttpRequestRetry) {
//            retryOnExceptionIf(
//                maxRetries = 2
//            ) { request, exception ->
//                logger.warn("${request.url} retry on $exception")
//                exception is HttpRequestTimeoutException || exception is IOException
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
        val response = client.head(getMetadataUrl(libIdentifier, repository)) {
            repository.authorization?.let {
                headers.appendAll(it.toStringValues())
            }
        }
        return Result.success(HttpStatusCode.OK == response.status)
    }

    override suspend fun getMetadata(libIdentifier: LibIdentifier, repository: Repository): MavenMetadata {
        return client.get(getMetadataUrl(libIdentifier, repository)) {
            repository.authorization?.let {
                headers.appendAll(it.toStringValues())
            }
        }.body<MavenMetadata>()
    }

    override suspend fun getPom(libKey: LibKey, repository: Repository): Pom {
        val urlPath = "${libKey.group.replace('.', '/')}/${libKey.module}/${libKey.version}/${libKey.module}-${libKey.version}.pom"
        val url = "${repository.url.removeSuffix("/")}/$urlPath"
            .also { logger.info("pomUrl: $it") }
        val response = client.get(URL(url)) {
            repository.authorization?.let {
                headers.appendAll(it.toStringValues())
            }
        }
        when {
            response.status.isSuccess() -> return response.body()
            HttpStatusCode.NotFound == response.status -> throw PomException.PomNotFoundException(url)
            else -> throw WIPException("Unhandled error for pomCache, check $url")
        }
    }

    override suspend fun getSize(libKey: LibKey, repository: Repository, packaging: String): Long {
        val urlPath = "${libKey.group.replace('.', '/')}/${libKey.module}/${libKey.version}/${libKey.module}-${libKey.version}.$packaging"
        val head = client.head(URL("${repository.url.removeSuffix("/")}/$urlPath")) {
            repository.authorization?.let {
                headers.appendAll(it.toStringValues())
            }
        }
        return head.headers["Content-Length"]?.toLongOrNull() ?: throw Exception("Can't get size for $libKey in repository: $repository with packaging: $packaging, $urlPath")
    }

    private fun getMetadataUrl(libIdentifier: LibIdentifier, repository: Repository): String {
        val libPath = "${libIdentifier.group.replace('.', '/')}/${libIdentifier.module}/maven-metadata.xml"
        return "${repository.url.removeSuffix("/")}/$libPath"
            .also { logger.info("metadataUrl: $it") }
    }
}