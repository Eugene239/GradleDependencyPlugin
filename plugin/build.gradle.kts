import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.artifacts.result.DefaultUnresolvedComponentResult
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier

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
val patch = 3

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
//tasks.create("dummy") {
//    println("dummy common")
//    // org.jetbrains.kotlin:kotlin-stdlib:2.0.20
//    // org.jetbrains.kotlin:kotlin-stdlib-common:2.0.20
//    val configuration = project.configurations.detachedConfiguration(
//        project.dependencies.create("org.jetbrains.kotlin","kotlin-stdlib-common", "2.0.20")
//    )
//    val files = configuration.resolve()
//    files.forEach {
//        println("## $it, ${it.parentFile.path}")
//    }
//}

//task("getPom") {
//    println("### getPOM")
//    //androidx.fragment:fragment:[1.6.0]
//    //androidx.fragment:fragment:1.6.0
//    // org.webkit:android-jsc:r250231
//    val version = DefaultModuleVersionIdentifier.newId("androidx.fragment", "fragment", "1.6.0")
//   // val version = DefaultModuleVersionIdentifier.newId("org.webkit", "android-jsc", "r250231")
//    val result = project.dependencies.createArtifactResolutionQuery()
//        .forComponents(DefaultModuleComponentIdentifier.newId(version))
//        .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
//        .execute()
//
//    println("## result size: ${result.components.size}")
//    result.components.forEach {
//        println(it)
//        val failure = (it as? UnresolvedComponentResult)?.failure
//        failure?.printStackTrace()
//    }
//    result.resolvedComponents.forEach {
//        println(it)
//        val poms = it.getArtifacts(MavenPomArtifact::class.java)
//        poms.forEach { pom ->
//            val artifact = pom as ResolvedArtifactResult
//            println(artifact.file.path)
//        }
//    }
//}


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
