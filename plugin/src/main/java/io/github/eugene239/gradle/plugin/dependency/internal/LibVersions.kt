package io.github.eugene239.gradle.plugin.dependency.internal

import kotlinx.serialization.Serializable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class LibVersions {

    private val map = HashMap<LibIdentifier, Versions>()
    private val lock = ReentrantLock()

    fun setResolved(libIdentifier: LibIdentifier, version: String) {
        lock.withLock {
            val data = createOrGet(libIdentifier)
            map[libIdentifier] = data.copy(resolved = version)
        }
    }

    fun add(libIdentifier: LibIdentifier, version: String) {
        lock.withLock {
            val data = createOrGet(libIdentifier)
            data.set.add(version)
        }
    }

    fun getConflictData(): Map<LibIdentifier, Versions> {
        return map.filter { entry -> entry.value.set.size > 1 }
    }

    private fun createOrGet(libIdentifier: LibIdentifier): Versions {
        map.computeIfAbsent(libIdentifier) { Versions() }
        return map[libIdentifier]!!
    }
}

@Serializable
internal data class Versions(
    val set: HashSet<String> = HashSet(),
    val resolved: String? = null
)