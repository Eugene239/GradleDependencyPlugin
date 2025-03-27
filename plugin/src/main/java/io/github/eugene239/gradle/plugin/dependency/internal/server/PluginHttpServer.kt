package io.github.eugene239.gradle.plugin.dependency.internal.server

import com.sun.net.httpserver.HttpServer
import org.gradle.api.logging.Logger
import java.io.File
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

internal class PluginHttpServer(
    private val rootDir: File,
    private val logger: Logger,
    private val executorService: ExecutorService
) {

    private var server: HttpServer? = null

    fun start(port: Int? = null) {
        server = HttpServer.create(InetSocketAddress(Inet4Address.getLoopbackAddress(), port ?: 0), 0)
        server?.createContext("/", PluginHttpHandler(rootDir, logger))
        server?.executor = executorService
        server?.start()
        logger.lifecycle("")
        logger.lifecycle("Web server started http:/$this")
        kotlin.runCatching {
            executorService.awaitTermination(1000, TimeUnit.DAYS)
        }.onFailure {
            stop()
        }
        executorService.shutdown()
    }

    fun stop() {
        server?.stop(0)
        executorService.shutdown()
        logger.lifecycle("Server stopped")
    }

    override fun toString(): String {
        return server?.address?.toString().orEmpty()
    }
}