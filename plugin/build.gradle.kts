plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.build.config)
    alias(libs.plugins.gradle.publish)
    id("maven-publish")
    groovy
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
        create<MavenPublication>("demolib") {
            groupId = "io.github.eugene239"
            artifactId = projectArtifactId
            version = libraryVersion
            from(components["java"])
            repositories {
                maven {
                    name = "projectLocal"
                    url = uri("${rootDir}/localMavenRepo")
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
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

gradlePlugin {
    website.set("https://github.com/Eugene239/GradleDependencyPlugin")
    vcsUrl.set("https://github.com/Eugene239/GradleDependencyPlugin")
    plugins {
        create("GradleDependencyPlugin") {
            id = "io.github.eugene239.gradle.plugin.dependency"
            implementationClass = "io.github.eugene239.gradle.plugin.dependency.DependencyPlugin"
            displayName = "Gralde Dependency plugin"
            description = "View project dependecy tree"
            @Suppress("UnstableApiUsage")
            tags.set(listOf("graph", "dependencies"))
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.freemarker)
    implementation(libs.ktor.client)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.serialization.xml)

    testImplementation("junit:junit:4.13.2")
}
