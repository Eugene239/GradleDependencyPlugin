# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),

## [Unreleased]

#### Added

Simple DI and refactor

#### Removed

#### Fixed

## [Released]

### [0.5.0] - 2025-04-02

#### Added

- Added lib size logic
- Better error handling and logging
- `fetch-dependencies-size` and `fetch-latest-versions` cli arguments
- added links to gradle portal and github to WP
- added module tab for configuration in WP

#### Updated

- Updated ui and graph drawing
- documentation
- moved flatDependencies to configuration dir

### [0.4.0] - 2025-03-27

#### Added

- http server to launch web page
- BuildConfig isDebug flag

#### Breaking changes

- change task name from `dependencyGraph` to `dependencyWP`

#### Removed

- singleDependency from plugin release version

### [0.3.0] - 2025-03-24

#### Added

- simplexml
- conflicts.json
- lib details to track strict version and submodule libraries

#### Removed

- kotlinx-serialization-xml

#### Fixed

- reduced execution time
- logic for multi module project
- resolved to latest

### [0.2.0] - 2025-03-09

#### Added

- Template to format markdown
- Local maven repo for `demolib`

#### Removed

- Unused and deprecated classes
- Koin

### [0.1.0] - 2025-02-06

- Initial release
