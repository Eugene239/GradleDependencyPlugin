# Gradle Dependency Plugin
Plugin to check dependency versions

## Plugin setup


In `app` or other module `build.gradle` apply the plugin and use its dependencies:

```gradle
apply(plugin = "io.github.eugene239.gradle.plugin.dependency")
```


## Gradle asks
### Make Graph UI
Plugin will create new gradle task, to execute use
```shell
$ gralde app:dependencyGraph
```
To filter dependencies, call
```shell
$ gradle app:dependencyGraph --filter io\.github\.eugene239.*
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
$ gradle app:dependencyReport --filter io\.github\.eugene239.*
```
You will get report on dependencies which matches regex