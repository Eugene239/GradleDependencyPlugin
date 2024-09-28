object PluginDependencies {
    object Versions {
        const val jfrog = "4.25.0"
        const val kotlin = "1.9.22"
    }
}

object Dependencies {
    object Versions {
        const val kotlinxSerialization = "1.6.2"
        const val koin = "3.4.3"
    }

    object Libraries {
        const val kotlinxSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerialization}"
        const val koin = "io.insert-koin:koin-core:${Versions.koin}"
    }
}
