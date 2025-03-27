[![Gradle Plugin](https://img.shields.io/gradle-plugin-portal/v/io.github.eugene239.gradle.plugin.dependency)](https://plugins.gradle.org/plugin/io.github.eugene239.gradle.plugin.dependency)

# Gradle Dependency Plugin

Plugin to check dependency versions

## Plugin setup

In `app` or other module `build.gradle` apply the plugin and use its dependencies:

```gradle
apply(plugin = "io.github.eugene239.gradle.plugin.dependency") version $latest
```

## Gradle tasks

### Make Dependencies Report

```shell
$ gralde app:dependencyReport
```

Will create a MD file with all outdated versions for all flavours

### Make Web Page

Plugin will create a web site to see dependency usage, conflicts and dependency graph.

```shell
$ gralde app:dependencyWP
```

Cancel task to stop httpServer

## CLI parameters

| Parameter                   | Details                                       | Example                      | Default            | 
|-----------------------------|-----------------------------------------------|------------------------------|--------------------|
| filter                      | Regex string to filter dependencies by name   | io\\.github\\.eugene239.*    |                    |
| configuration               | Name of configuration to launch task          | defaultDebugRuntimeClasspath |                    |
| repository-connection-limit | Limit of requests to maven repository at once | 10                           | 20                 |
| connection-timeout          | Ktor client connection timeout millis         | 10000                        | 10000              |
| http-port                   | Http port for server                          | 8080                         | Random unused port |  
