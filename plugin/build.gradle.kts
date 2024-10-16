plugins {
    kotlin("jvm") version PluginDependencies.Versions.kotlin
    kotlin("plugin.serialization") version PluginDependencies.Versions.kotlin
    id("com.github.gmazzo.buildconfig") version "5.4.0"
    id("maven-publish")
    groovy
    id("java-gradle-plugin")
}

val major = 1
val minor = 5
val patch = 2

val libraryVersion = LibVersions.getLibVersion(
    project = project,
    major = major,
    minor = minor,
    patch = patch
)

group = "io.epavlov"
var projectArtifactId = "gradle-plugin-dependency"
version = libraryVersion

publishing {
    println(libraryVersion)
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.epavlov"
            artifactId = projectArtifactId
            version = libraryVersion
            from(components["java"])
        }
        repositories {
            maven {
                name = "repsy"
                url = uri("https://repo.repsy.io/mvn/eugene239/poc")
                credentials {
                    username = "eugene239"
                    password = "4P@ixcZ2xbf6jNF"
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
    plugins {
        create("GradleDependencyPlugin") {
            id = "io.epavlov.gradle.plugin.dependency"
            implementationClass = "io.epavlov.gradle.plugin.dependency.DependencyPlugin"
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
