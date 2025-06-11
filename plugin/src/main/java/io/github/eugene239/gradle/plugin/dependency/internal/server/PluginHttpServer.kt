package io.github.eugene239.gradle.plugin.dependency.internal.server

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import io.github.eugene239.gradle.plugin.dependency.internal.di.di
import io.github.eugene239.gradle.plugin.dependency.task.WPTaskConfiguration
import org.gradle.api.logging.Logger
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

internal class PluginHttpServer {
    private val logger: Logger by di()
    private val executorService: ExecutorService by di()
    private val httpHandler: HttpHandler by di()
    private val taskConfiguration: WPTaskConfiguration by di()
    private val port: Int? by lazy {
        taskConfiguration.httpPort
    }

    private var server: HttpServer? = null

    fun start() {
        server = HttpServer.create(InetSocketAddress(Inet4Address.getLoopbackAddress(), port ?: 0), 0)
        server?.createContext("/", httpHandler)
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