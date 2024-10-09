# Gradle Dependency Plugin [![](https://jitpack.io/v/Eugene239/GradleDependencyPlugin.svg)](https://jitpack.io/#Eugene239/GradleDependencyPlugin)
Plugin to check dependency versions

## Plugin setup

In `buildSrc/build.gradle`   :
Add Jitpack repository
```groovy
buildscript {
    repositories {
        maven {
            url = uri("https://jitpack.io")
            credentials {
                username = "jp_cbr4gl1v8a77aogv268l7u157j"
            }
        }
    }
}
```

```gradle
dependencies {
     classpath("com.github.Eugene239:GradleDependencyPlugin:${latestVersion}")
}
```

In `app` or other module `build.gradle` apply the plugin and use its dependencies:

```gradle
apply(plugin = "io.epavlov.gradle.plugin.dependency")
```


## Gradle asks
### Make Graph UI
Plugin will create new gradle task, to execute use
```shell
$ gralde app:dependencyGraph
```
To filter dependencies, call
```shell
$ gradle app:dependencyGraph --filter "io\.epavlov\.gradle.*"
```
Task will create new directory `app/build/dependency-ui` with html file, open html file in Android
studio to see graph
You can see dependencies with mismatching versions and can use them as a filter to see usage

(!) opening `index.html` file not from Android studio can cause `CORS` error

### Make Dependency report
```shell
$ gralde app:dependencyReport
```
Will create a MD file with all outdated versions for all flavours
If you need to filter dependencies, you can call task with filter option
```shell
$ gradle app:dependencyReport --filter "io\.epavlov\.gradle.*"
```
You will get report on dependencies which matches regex

## TBD
### Completed: 
- make task run after compile task
- generate different `dependency.json` file for all project configurations (optional)
- make ui pretty (optional)
- make solution for project modules
- make MD dependency report

### Todo:
- refactor code
- library size info
- regex cmd param
- refactor from core to usecase
- remove koin?