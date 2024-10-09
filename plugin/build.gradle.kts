plugins {
    kotlin("jvm") version PluginDependencies.Versions.kotlin
    kotlin("plugin.serialization") version PluginDependencies.Versions.kotlin
    id("com.github.gmazzo.buildconfig") version "5.4.0"
    id("maven-publish")
    groovy
    id("java-gradle-plugin")
}

val major = 1
val minor = 3
val patch = 0

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
    }
}

buildConfig {
    buildConfigField("String", "PLUGIN_VERSION", "\"${libraryVersion}\"")
}

//artifactory {
//    setContextUrl("https://artifactory.TBD.com/artifactory")
//    publish {
//        repository {
//            if (libraryVersion.endsWith("SNAPSHOT")) {
//                setRepoKey("maven-snapshots")
//            } else {
//                setRepoKey("maven")
//            }
//            setUsername("${project.properties["ARTIFACTORY_USERNAME"]}")
//            setPassword("${project.properties["ARTIFACTORY_PASSWORD"]}")
//        }
//        defaults {
//            publications("maven")
//            setPublishArtifacts(true)
//            setPublishPom(true)
//        }
//    }
//}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "17" }

//
//gradle.projectsEvaluated {
//    tasks.named("artifactoryPublish") {
//        dependsOn("assemble")
//    }
//    println(libraryVersion)
//}

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
