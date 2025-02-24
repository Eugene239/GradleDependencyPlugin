package io.github.eugene239.gradle.plugin.dependency.internal.service

import io.github.eugene239.gradle.plugin.dependency.internal.LibIdentifier
import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.xml.xml
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultFormatCache
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.gradle.api.logging.Logger
import java.net.URL

internal class DefaultMavenService(
    private val logger: Logger
) : MavenService {

    @OptIn(ExperimentalXmlUtilApi::class)
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            xml(
                contentType = ContentType.Text.Xml,
                format = XML {
                    policy = DefaultXmlSerializationPolicy(formatCache = DefaultFormatCache()) {
                        unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                    }
                    autoPolymorphic= true
                },
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
        return client.get(URL("${repository.url.removeSuffix("/")}/$urlPath".also { logger.info("pomUrl: $it") })) {
            repository.authorization?.let {
                headers.appendAll(it.toStringValues())
            }
        }.body<Pom>()
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