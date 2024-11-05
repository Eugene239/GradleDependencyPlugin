import java.net.URI

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = URI("https://maven.google.com/")
        }
//        maven {
//            url = URI("https://dl.cloudsmith.io/public/cometchat/call-team/maven/")
//        }

    }
}