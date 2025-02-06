plugins {
    kotlin("jvm") version PluginDependencies.Versions.kotlin
    kotlin("plugin.serialization") version PluginDependencies.Versions.kotlin
    id("com.github.gmazzo.buildconfig") version "5.4.0"
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.3.1"
    groovy
   // id("java-gradle-plugin")
}

val major = 0
val minor = 1
val patch = 0

val libraryVersion = LibVersions.getLibVersion(
    project = project,
    major = major,
    minor = minor,
    patch = patch
)

group = "io.github.eugene239"
var projectArtifactId = "gradle-plugin-dependency"
version = libraryVersion

publishing {
    println(libraryVersion)
    publications {
        create<MavenPublication>("gradle") {
            groupId = "io.github.eugene239"
            artifactId = projectArtifactId
            version = libraryVersion
            from(components["java"])

            pom {
                url.set("https://github.com/Eugene239/GradleDependencyPlugin")

                scm {
                    url.set("https://github.com/Eugene239/GradleDependencyPlugin")
                }
            }

        }
    }

}


buildConfig {
    buildConfigField("String", "PLUGIN_VERSION", "\"${libraryVersion}\"")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "17" }

gradlePlugin {
    website.set("https://github.com/Eugene239/GradleDependencyPlugin")
    vcsUrl.set("https://github.com/Eugene239/GradleDependencyPlugin")
    plugins {
        create("GradleDependencyPlugin") {

            id = "io.github.eugene239.gradle.plugin.dependency"
            implementationClass = "io.github.eugene239.gradle.plugin.dependency.DependencyPlugin"
            displayName = "Gralde Dependency plugin"
            description = "View project dependecy tree"
            tags.set(listOf("graph", "dependencies"))
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(Dependencies.Libraries.kotlinxSerialization)
    implementation(Dependencies.Libraries.coroutines)
    // DI
    implementation(Dependencies.Libraries.koin)
    testImplementation("junit:junit:4.13.2")
}
