# Gradle Dependency Graph UI Plugin

Spike to check dependency versions

### Gradle

In `buildSrc/build.gradle`   :

```gradle
dependencies {
    implementation("io.epavlov:gradle-plugin-dependency:${latestVersion}")
    ...
}
```

In `app` or other module `build.gradle` apply the plugin and use its dependencies:

```gradle
apply(plugin = "io.epavlov.gradle.plugin.dependency")
```

Add configuration

``` groovy
dependencyGraphOptions {
    appConfigurationNames = [
        "worldDebugCompileClasspath",
        "chinaDebugCompileClasspath""
    ]
    dependencyNameRegex = "^io\\.epavlov\\.(?!android).*",
    printConfigurations = false
}
```
Also you can pass a postfix of configuration name, it will fetch all configuration contains this name
``` groovy
dependencyGraphOptions {
    appConfigurationNames = [
        "runtimeClasspath",
    ]
    dependencyNameRegex = "^io\\.epavlov\\..*",
    printConfigurations = false
}
```


### Task

Plugin will create new gradle task, to execute use
``
$ gralde app:dependencyUI
``
Task will create new directory `app/build/dependencyUI` with html file, open html file in Android
studio to see graph
You can see dependencies with mismatching versions and can use them as a filter to see usage

(!) opening `dep.html` file not from Android studio can cause `CORS` error

### Completed: 
- make task run after compile task
### Todo:
- refactor code
- generate different `dependency.json` file for all project configurations (optional)
- make ui pretty (optional)
- make MD reports
- library size info
- make solution for project modules