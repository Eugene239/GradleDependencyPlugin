pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "projectLocal"
            url = uri("${rootDir}/localMavenRepo")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            name = "projectLocal"
            url = uri("${rootDir}/localMavenRepo")
        }
    }
}

rootProject.name = "Gradle Dependency Plugin"
include(":plugin")
include(":demolib")
include(":androidlib")
