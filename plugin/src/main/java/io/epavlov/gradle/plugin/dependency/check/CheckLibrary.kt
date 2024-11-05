package io.epavlov.gradle.plugin.dependency.check

import io.epavlov.gradle.plugin.dependency.internal.LibKey
import io.epavlov.gradle.plugin.dependency.internal.cache.dependency.DependencyCache
import io.epavlov.gradle.plugin.dependency.internal.cache.pom.PomCache
import io.epavlov.gradle.plugin.dependency.internal.cache.version.VersionCache
import io.epavlov.gradle.plugin.dependency.internal.pom.PomXMLParserImpl
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project

class CheckLibrary(
    private val project: Project
) {

    private val dependencyCache = DependencyCache(
        xmlParser = PomXMLParserImpl(
            logger = project.logger,
            filter = null
        ),
        dependencyFilter = null,
        logger = project.logger,
        pomCache = PomCache(
            project = project,
            versionCache = VersionCache(
                project = project
            )
        )
    )

    fun check(dependency: String) {
        runBlocking {
            val split = dependency.split(":")
            dependencyCache.limit = 1
            dependencyCache.fillRealChildren(
                libKey = LibKey(split[0], split[1], split[2]),
                parent = null,
            )
        }
    }
}