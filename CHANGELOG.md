<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Changelog kotlinx.html-plugin 

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.1.1] - 2025-01-17
### Added
- chore: automated deployment to the JetBrains marketplace
- chore: protect JETBRAINS_MARKETPLACE environment (see Github/Settings/Environments) with manual review
- chore: add `libs.versions.toml` for dependency management
- chore: read plugin description from `README.md`
- chore: read version information from `CHANGELOG.md`
- chore: tag version in git after successful release
### Changed
- unset `until-build` for upward compatibility


## [1.1.0] - 2025-01-15
### Fixed
- Fix issue #22: K2 compatibility issue


## [1.0.7] - 2020-05-28
### Fixed
- Fix #20: "classes" should be treated as a special parameter
- Fix #16: label.for attribute should be converted in htmlFor
- Fix #18: Parse error on windows and IntelliJ 2019.2


## [1.0.6] - 2020-05-26


## [1.0.5] - 2020-05-26


## [1.0.4] - 2019-06-10
### Fixed
- Fix #14: class names should be added to tag constructor


## [1.0.3] - 2019-06-10
### Fixed
- Fix #10: split class names
- Fix #9: textArea instead of textarea


## [1.0.2] - 2019-04-26
### Fixed
- Fix #7: attributes should be converted into code inside the block.
- Fix #6: the plugin shouldn't apply for Kotlin code with generics.


## [1.0.1] - 2019-04-05
### Changed
- Align Kotlin version on Intellij 2018


