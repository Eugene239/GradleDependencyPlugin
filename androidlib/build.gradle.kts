plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "io.github.eugene239.androidlib"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

buildscript {
    dependencies {
        val pluginClasspath = PluginProvider.getPluginClasspath(project)
        if (pluginClasspath != null) {
            classpath(pluginClasspath)
        }
    }
}

if (PluginProvider.getPluginClasspath(project) != null) {
    apply(plugin = "io.github.eugene239.gradle.plugin.dependency")

    extensions.configure(io.github.eugene239.gradle.plugin.dependency.publication.PublicationExtension::class) {
        aar = io.github.eugene239.gradle.plugin.dependency.publication.AarConfig(
            groupId = "io.github.eugene239",
            artifactId = "androidlib",
            version = "1.3.0",
            addSource = true
        )
        bom = io.github.eugene239.gradle.plugin.dependency.publication.BomConfig(
            groupId = "io.github.eugene239",
            artifactId = "androidlib-bom",
        )
    }

    publishing {
        repositories {
            maven {
                name = "projectLocal"
                url = uri("${rootDir}/localMavenRepo")
            }
        }
    }
}

dependencies {
    implementation(libs.appcompat.v7)
    testImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
}