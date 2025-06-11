package io.github.eugene239.gradle.plugin.dependency.task

// Base Configuration for tasks
interface TaskConfiguration {

    val repositoryConnectionLimit: Int
    val regexFilter: String?
    val connectionTimeOut: Long
}

interface WPTaskConfiguration : TaskConfiguration {
    val httpPort: Int?
    val fetchLibrarySize: Boolean
    val fetchLatestVersions: Boolean
}
