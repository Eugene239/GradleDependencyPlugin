[![Gradle Plugin](https://img.shields.io/gradle-plugin-portal/v/io.github.eugene239.gradle.plugin.dependency)](https://plugins.gradle.org/plugin/io.github.eugene239.gradle.plugin.dependency)

# Gradle Dependency Plugin

A Gradle plugin for analyzing project dependencies

- detects outdated version
- find conflicts
- visualizes dependency graphs via a local web page
- supports BOM/JAR/AAR publication for dependency management distribution

## Installation

```gradle
apply(plugin = "io.github.eugene239.gradle.plugin.dependency") version $latest
```

## Tasks

### dependencyLatestVersions

Generates a Markdown report listing available updates for dependencies in the selected configuration
Supports filter by Regexp

```shell
$ gradle app:dependencyLatestVersions
```

### dependencyConflict

Produces a conflict report. Support `console` and md `output` formats.
Can specify `conflict-level` of dependency version

- major (default)
- minor
- patch

### dependencyWP

Starts a local web server with an interactive UI:

- dependency usage
- conflict analysis
- graph visualization
- latest available version
- dependency size

```shell
$ gradle app:dependencyWP
```

Cancel task to stop httpServer

## CLI parameters

| Parameter                   | Details                                                 | dependencyWP | dependencyLatestVersions | dependencyConflict | Example                      | Default            | 
|-----------------------------|---------------------------------------------------------|--------------|--------------------------|--------------------|------------------------------|--------------------|
| filter                      | Regex string to filter dependencies by name             | &check;      | &check;                  | &check;            | io\\.github\\.eugene239.*    |                    |
| configuration               | Name of configuration to launch task                    | &check;      | &check;                  | &check;            | defaultDebugRuntimeClasspath |                    |
| repository-connection-limit | Limit of requests to maven repository at once           | &check;      | &check;                  | &cross;            | 10                           | 20                 |
| connection-timeout          | Ktor client connection timeout millis                   | &check;      | &check;                  | &cross;            | 10000                        | 10000              |
| http-port                   | Http port for server                                    | &check;      | &cross;                  | &cross;            | 8080                         | Random unused port |  
| fetch-dependencies-size     | Boolean flag to fetch dependencies size in bytes        | &check;      | &cross;                  | &cross;            | true                         | false              |
| fetch-latest-versions       | Boolean flag to fetch from repositories latest versions | &check;      | &cross;                  | &cross;            | true                         | false              |
| output-format               | Report output format, console or MD file                | &cross;      | &check;                  | &check;            | console                      | md                 |
| conflict-level              | Level to report conflicts from                          | &cross;      | &cross;                  | &check;            | MINOR                        | MAJOR              |
| fail-on-conflict            | Fail task dependencyConflict if conflicts are found     | &cross;      | &cross;                  | &check;            | true                         | false              |


## Examples
### All-in for WP

```shell
gradle app:dependencyWP --filter=io\.github\.eugene239.* --configuration=runtimeClasspath --repository-connection-limit=10  --connection-timeout=30000 --http-port=8080 --fetch-dependencies-size=true --fetch-latest-versions=true 
```
### Latest versions report
```
gradle :app:dependencyLatestVersions --configuration=runtimeClasspath --output-format=console
```
### Conflicts scan
```
gradle :app:dependencyConflict --configuration=runtimeClasspath --output-format=console --conflict-level=minor --fail-on-conflict=true
```

## Publication

The plugin also provides artifact publication capabilities for dependency management:

### BOM publication

Generate a Bill of Materials (BOM) POM file with dependencyManagement for all or filtered
dependencies.
This allows consumers to align versions across multiple modules.

#### Usage:

```
extensions.configure(PublicationExtension::class) {
        bom = BomConfig(
            groupId = "io.github.eugene239",
            artifactId = "androidlib-bom",
            filter = "io\.github\.eugene239.*"
    )
}
```

### JAR publication

Create publication task for Java/Kotlin libraries as JAR artifacts.
Artifact can be published with sources or without

#### Usage:

```
extensions.configure(PublicationExtension::class) {
    jar = JarConfig(
        groupId = "io.github.eugene239",
        artifactId = "demolib",
        version = "2.0.0",
        addSource = true
    )
}
```

### AAR publication

For Android libraries, publish AAR artifacts

```
extensions.configure(PublicationExtension::class) {
    aar = AarConfig(
        groupId = "io.github.eugene239",
        artifactId = "androidlib",
        version = "1.3.0",
        addSource = true
    )
}
```

## License 
Gradle Dependency Plugin is licensed under the [Apache License 2.0](./LICENSE)
