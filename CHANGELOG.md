# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

The major and minor version numbers of this repository (but not patch numbers) match the version numbers of the
[`kjson`](https://github.com/pwall567/kjson) library used by this Spring library.

## [7.0] - 2023-07-31
### Changed
- `pom.xml`: Updated version of kjson

## [6.1] - 2023-07-25
### Changed
- `pom.xml`: Updated version of kjson

## [6.0] - 2023-07-09
### Changed
- `pom.xml`: Updated version of kjson

## [5.0] - 2023-07-07
### Changed
- `pom.xml`: Updated dependency versions

## [4.4.4] - 2023-05-08
### Changed
- `JSONMockServer`: added `mockPatch` function
- `JSONContentResultMatchersDSL`: added `string` function taking a lambda
- `JSONMockMvcResultMatchersDSL`: added `response` and `contentAsString` accessors

## [4.4.3] - 2023-05-03
### Changed
- many files: revised handling of MockMvc

## [4.4.2] - 2023-05-01
### Changed
- `JSONMockMvc`: fixed bug in URI variable substitution

## [4.4.1] - 2023-05-01
### Changed
- `JSONMockMvcResultMatchersDSL`: added `header` (missed in major update)
### Added
- `JSONHeaderResultMatchersDSL`

## [4.4] - 2023-04-30
### Changed
- many files: major re-work to solve problem of auto-wiring `JSONConfig`

## [3.2.5] - 2022-08-04
### Changed
- `JSONMockServerDSL`: improved on previous bug fix
- `UUIDMatcher`: added function taking string parameter

## [3.2.4] - 2022-08-04
### Changed
- `JSONMockServerDSL`: fixed bug in use of `ExpectedCount` other than `once()`

## [3.2.3] - 2022-08-03
### Changed
- `JSONMockServerDSL`: added `respondTextPlain` and `respondBytes` functions, improved response handling
### Added
- `JSONMockClientRequest`: improves ability to create dynamic responses

## [3.2.2] - 2022-08-01
### Changed
- `JSONMockServerDSL`: reorganised code, added use of lambdas for tests and deprecated use of `Matcher`
- `JSONMockServerDSL`: added `respond` and `respondJSON` functions

## [3.2.1] - 2022-07-12
### Added
- `JSONMockServerDSL`: fixed bug in URL matching, revised other matchers

## [3.2] - 2022-07-10
### Added
- all files: initial version
