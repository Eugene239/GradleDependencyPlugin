package io.github.eugene239.gradle.plugin.dependency.internal.cache

internal interface Cache<K, V> {

    suspend fun get(key: K): V
}