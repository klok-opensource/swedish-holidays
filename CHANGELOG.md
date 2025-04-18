# Changelog

All notable changes to this project are documented in this file following [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.0.3] - 2025-04-18
- Added descriptive Javadoc comments to all public methods and classes for comprehensive API documentation.
- Added caching and prepopulates previous, current, next, second next year with both languages.
- Bumped project version to `1.0.3`.

## [1.0.2] - 2025-04-18
- Changed default language to English for all methods without explicit language parameter.
- Bumped Maven artifact to `swedish-holidays:1.0.2`.

## [1.0.1] - 2025-04-18
- Added overloads for Easter-based holiday getters accepting `int year`, calculating Easter internally.
- Added overloads of individual holiday getters without a language parameter, defaulting to Swedish names.
- Introduced `listHolidays()` and `listHolidays(int year)` overloads defaulting to English language.
- Updated README with detailed method documentation, usage examples, and translation table.

## [1.0.0] - initial release
- Core API for checking holidays with `isHoliday(...)` overloads.
- `listHolidays(Lang, year)` and `isHolidayToday()` methods.
- Support for multiple date types (`LocalDate`, `Instant`, `Timestamp`, `XMLGregorianCalendar`, etc.).