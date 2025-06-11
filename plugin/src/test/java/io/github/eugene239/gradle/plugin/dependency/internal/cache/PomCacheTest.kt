package io.github.eugene239.gradle.plugin.dependency.internal.cache

import io.github.eugene239.gradle.plugin.dependency.internal.LibKey
import io.github.eugene239.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryByNameCache
import io.github.eugene239.gradle.plugin.dependency.internal.cache.repository.RepositoryCache
import io.github.eugene239.gradle.plugin.dependency.internal.service.MavenService
import io.github.eugene239.gradle.plugin.dependency.internal.service.Pom
import io.github.eugene239.gradle.plugin.dependency.internal.service.Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class PomCacheTest {
    private val repositoryByNameCache = mockk<RepositoryByNameCache>()
    private val mavenService = mockk<MavenService>()

    private val pomCache = PomCache()

    @Before
    fun setup() {
        coEvery { repositoryByNameCache.get(any(), any()) } returns mockk<Repository>()
        coEvery { mavenService.getPom(any(), any()) } returns mockk<Pom>()
    }

    @Test
    fun `check getting pom called once`() = runTest {
        // GIVEN
        val repoName = "name"
        val dependency = LibKey(
            group = "com.google.android.gms",
            module = "play-services-basement",
            version = "17.0.0"
        )
        // WHEN
        pomCache.get(dependency, repoName)
        pomCache.get(dependency.copy(), repoName)
        // THEN
        coVerify(exactly = 1) { mavenService.getPom(any(), any()) }
    }
}