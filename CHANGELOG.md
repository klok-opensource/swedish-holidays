# Changelog

All notable changes to this project are documented in this file following [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2025-04-20

### Added

-   **Holiday Customization:**
    -   Ability to add custom fixed-date holidays via `SwedishHolidays.addCustomHoliday()`.
    -   Ability to remove standard holidays *by specific date* via `SwedishHolidays.removeStandardHoliday()`.
    -   Ability to add custom rule-based holidays by implementing the new `HolidayRule` interface and using `SwedishHolidays.addCustomHolidayRule()`.
    -   Configuration methods to clear customizations (`clearCustomAdditions`, `clearStandardRemovals`, `clearCustomRules`, `clearAllCustomizations`).
    -   Public `SwedishHolidays.clearCache()` method to manually invalidate the holiday cache.
    -   Public `SwedishHolidays.setDefaultLanguage()` to change the default language used in methods without an explicit language parameter.
-   Added `equals()`, `hashCode()`, and `toString()` methods to the `Holiday` class for better object comparison and debugging.
-   Added comprehensive Javadoc for all public methods, including customization features.
-   Added null checks for required parameters in public methods.

### Changed

-   Internal holiday calculation logic refactored to support customization pipeline (standard calculation -> removals -> fixed additions -> rule additions -> sorting).
-   `isHoliday()` methods now check against the final, potentially customized set of holidays retrieved via the internal cache.
-   `listHolidays()` methods now return the final, potentially customized list of holidays.
-   Individual holiday getters (`getNewYearsDay`, `getGoodFriday`, etc.) now explicitly document that they return the *standard* calculation and do *not* reflect customizations. Added year-only overloads using the default language for all standard getters.
-   Improved robustness of `isHoliday(XMLGregorianCalendar)` timezone handling.
-   Improved robustness of `isHoliday(TemporalAccessor)` type handling.
-   Made `Holiday` class `final`.

### Fixed

-   Ensured thread-safety for customization lists using `CopyOnWriteArrayList`/`Set`.
-   Correctly implemented cache invalidation whenever customization settings are modified.

## [1.0.3] - 2025-04-18

### Added

-   Added descriptive Javadoc comments to all public methods and classes for comprehensive API documentation.
-   Added caching and prepopulates previous, current, next, second next year with both languages.

### Changed

-   Bumped project version to `1.0.3`.

## [1.0.2] - 2025-04-18

### Changed

-   Changed default language to English for all methods without explicit language parameter.
-   Bumped Maven artifact to `swedish-holidays:1.0.2`.

## [1.0.1] - 2025-04-18

### Added

-   Added overloads for Easter-based holiday getters accepting `int year`, calculating Easter internally.
-   Added overloads of individual holiday getters without a language parameter, defaulting to Swedish names (Note: Changed to default English in 1.0.2).
-   Introduced `listHolidays()` and `listHolidays(int year)` overloads defaulting to English language.
-   Updated README with detailed method documentation, usage examples, and translation table.

## [1.0.0] - Initial Release

### Added

-   Core API for checking holidays with `isHoliday(...)` overloads.
-   `listHolidays(Lang, year)` and `isHolidayToday()` methods.
-   Support for multiple date types (`LocalDate`, `Instant`, `Timestamp`, `XMLGregorianCalendar`, etc.).
-   `Lang` enum for SE/EN localization.