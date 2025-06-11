package io.github.eugene239.gradle.plugin.dependency.internal.di

import io.github.eugene239.gradle.plugin.dependency.task.TaskConfiguration
import io.github.eugene239.gradle.plugin.dependency.task.WPTaskConfiguration
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DiTest {

    @Before
    fun init() {
        DI.clear()
    }

    @Test
    fun `check implementation is found`() {
        // GIVEN
        val limit = 3
        val filter = "test"
        val timeout = 3L

        DI.register(TaskConfiguration::class.java, object : TaskConfiguration {
            override val repositoryConnectionLimit: Int = limit
            override val regexFilter: String = filter
            override val connectionTimeOut: Long = timeout
        })
        //WHEN
        val taskConfiguration = DI.resolve(TaskConfiguration::class.java)

        // THEN
        Assert.assertEquals(taskConfiguration.connectionTimeOut, timeout)
        Assert.assertEquals(taskConfiguration.regexFilter, filter)
        Assert.assertEquals(taskConfiguration.repositoryConnectionLimit, limit)
    }

    @Test
    fun `check implementation is found by higher interface`() {
        // GIVEN
        val limit = 3
        val filter = "test"
        val timeout = 3L
        val httpPort = 132312
        val fetchLibrarySize = true
        val fetchLatestVersions = false

        DI.register(TaskConfiguration::class.java, object : WPTaskConfiguration {
            override val httpPort: Int = httpPort
            override val fetchLibrarySize: Boolean = fetchLibrarySize
            override val fetchLatestVersions: Boolean = fetchLatestVersions
            override val repositoryConnectionLimit: Int = limit
            override val regexFilter: String = filter
            override val connectionTimeOut: Long = timeout
        })
        //WHEN
        val taskConfiguration = DI.resolve(WPTaskConfiguration::class.java)

        // THEN
        Assert.assertEquals(taskConfiguration.connectionTimeOut, timeout)
        Assert.assertEquals(taskConfiguration.regexFilter, filter)
        Assert.assertEquals(taskConfiguration.repositoryConnectionLimit, limit)
        Assert.assertEquals(taskConfiguration.httpPort, httpPort)
        Assert.assertEquals(taskConfiguration.fetchLibrarySize, fetchLibrarySize)
        Assert.assertEquals(taskConfiguration.fetchLatestVersions, fetchLatestVersions)
    }
}