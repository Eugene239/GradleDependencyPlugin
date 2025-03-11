package io.github.eugene239.gradle.plugin.dependency.internal.cache

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryCache
import io.github.eugene239.gradle.plugin.dependency.internal.provider.RepositoryProvider
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class RepositoryCacheTest {

    private val provider = mockk<RepositoryProvider>()
    private val mavenService = mockk<MavenService>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val repositoryCache = RepositoryCache(
        repositoryProvider = provider,
        mavenService = mavenService,
        ioDispatcher = UnconfinedTestDispatcher()
    )

    @Before
    fun setup() {
        every { provider.getRepositories() } returns setOf(mockk())
        coEvery { mavenService.isMetadataExists(any(), any()) } returns Result.success(true)
    }

    @Test
    fun `check searching called once`() = runTest {
        // GIVEN
        val version1 = "11.0.2"
        val version2 = "17.0.0"

        val dependency1 = LibKey(
            group = "com.google.android.gms",
            module = "play-services-basement",
            version = version1
        )
        val dependency2 = dependency1.copy(version = version2)
        // WHEN
        repositoryCache.get(dependency1)
        repositoryCache.get(dependency2)
        // THEN
        coVerify(exactly = 1) { mavenService.isMetadataExists(any(), any()) }
    }
}