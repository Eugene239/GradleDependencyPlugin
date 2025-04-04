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
$ gradle app:dependencyReport
```

Will create a MD file with all outdated versions for all flavours

### Make Web Page

Plugin will create a web site to see dependency usage, conflicts and dependency graph.

```shell
$ gradle app:dependencyWP
```

Cancel task to stop httpServer

## CLI parameters

| Parameter                   | Details                                                 | dependencyWP | dependencyReport | Example                      | Default            | 
|-----------------------------|---------------------------------------------------------|--------------|------------------|------------------------------|--------------------|
| filter                      | Regex string to filter dependencies by name             | &check;      | &check;          | io\\.github\\.eugene239.*    |                    |
| configuration               | Name of configuration to launch task                    | &check;      | &check;          | defaultDebugRuntimeClasspath |                    |
| repository-connection-limit | Limit of requests to maven repository at once           | &check;      | &check;          | 10                           | 20                 |
| connection-timeout          | Ktor client connection timeout millis                   | &check;      | &check;          | 10000                        | 10000              |
| http-port                   | Http port for server                                    | &check;      | &cross;          | 8080                         | Random unused port |  
| fetch-dependencies-size     | Boolean flag to fetch dependencies size in bytes        | &check;      | &cross;          | true                         | false              |
| fetch-latest-versions       | Boolean flag to fetch from repositories latest versions | &check;      | &cross;          | true                         | false              |

Example of running dependencyWP with all parameter
```shell
gradle app:dependencyWP --filter=io\.github\.eugene239.* --configuration=runtimeClasspath --repository-connection-limit=10  --connection-timeout=30000 --http-port=8080 --fetch-dependencies-size=true --fetch-latest-versions=true 
```
