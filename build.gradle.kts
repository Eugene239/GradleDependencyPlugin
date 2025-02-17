import java.net.URI

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = URI("https://maven.google.com/")
        }
    }
}