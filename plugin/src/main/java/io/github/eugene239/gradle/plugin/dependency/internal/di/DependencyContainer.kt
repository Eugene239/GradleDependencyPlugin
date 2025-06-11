package io.github.eugene239.gradle.plugin.dependency.internal.di

/**
 * Simple DI feels like service discovery
 * Helps to remove duplicated code and initialization boilerplate
 */
internal class DependencyContainer {

    private val instances = mutableMapOf<Class<*>, Any>()

    fun <T : Any> register(clazz: Class<T>, instance: T) {
        instances[clazz] = instance
    }

    fun <T : Any> resolve(clazz: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return instances[clazz] as? T ?: throw IllegalStateException("Can't resolve instance of $clazz").also {
            it.printStackTrace()
        }
    }

    fun clear() {
        instances.clear()
    }
}

internal val DI = DependencyContainer()
internal fun <T : Any> inject(clazz: Class<T>): Lazy<T> = lazy { DI.resolve(clazz) }
internal inline fun <reified T : Any> di(): Lazy<T> = inject(T::class.java)