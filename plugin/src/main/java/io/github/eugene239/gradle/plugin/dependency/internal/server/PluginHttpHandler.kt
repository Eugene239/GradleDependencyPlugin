package io.github.eugene239.gradle.plugin.dependency.internal.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.gradle.api.logging.Logger
import java.io.File

class PluginHttpHandler(
    private val rootDir: File,
    private val logger: Logger,
) : HttpHandler {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun handle(exchange: HttpExchange) {
        scope.launch {
            logger.info("exchange:  ${exchange.requestURI.path}")
            when {
                exchange.requestURI.path == "/" || exchange.requestURI.path.isNullOrBlank() -> {
                    val file = File(rootDir, "index.html")
                    logger.info("file: $file")
                    exchange.sendResponseHeaders(200, file.length())
                    exchange.responseBody.write(file.inputStream().readAllBytes())
                }

                File(rootDir, exchange.requestURI.path).exists() -> {
                    val file = File(rootDir, exchange.requestURI.path)
                    logger.info("file: $file, ${file.extension}")
                    when (file.extension) {
                        "js" -> {
                            exchange.responseHeaders.add("Content-Type", "application/javascript")
                        }

                        "json" -> {
                            exchange.responseHeaders.add("Content-Type", "application/json")
                        }
                    }
                    exchange.sendResponseHeaders(200, file.length())
                    exchange.responseBody.write(file.inputStream().readAllBytes())
                }

                else -> {
                    logger.warn("unknown request: ${exchange.requestURI.path}")
                    exchange.sendResponseHeaders(404, 0L)
                }

            }
            exchange.close()
        }
    }
}