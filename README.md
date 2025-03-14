[![Gradle Plugin](https://img.shields.io/gradle-plugin-portal/v/io.github.eugene239.gradle.plugin.dependency)](https://plugins.gradle.org/plugin/io.github.eugene239.gradle.plugin.dependency)

# Gradle Dependency Plugin

Plugin to check dependency versions

## Plugin setup

In `app` or other module `build.gradle` apply the plugin and use its dependencies:

```gradle
apply(plugin = "io.github.eugene239.gradle.plugin.dependency") version $latest
```

## Gradle asks

### Make Graph UI

Plugin will create new gradle task, to execute use

```shell
$ gralde app:dependencyGraph
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


## CLI parameters

| Parameter                   | Details                                       | Example                      | Default | 
|-----------------------------|-----------------------------------------------|------------------------------|---------|
| filter                      | Regex string to filter dependencies by name   | io\.github\.eugene239.*      |         |
| configuration               | Name of configuration to launch task          | defaultDebugRuntimeClasspath |         |
| repository-connection-limit | Limit of requests to maven repository at once | 10                           | 20      |
| connection-timeout          | Ktor client connection timeout millis         | 10_000                       | 10_000  |
