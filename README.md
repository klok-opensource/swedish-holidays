# Klok Swedish Holidays Utility Library

A lightweight Java library (Java 8+ compatible) providing utilities to determine Swedish public holidays and common non-working days/eves, with support for customization.

## Features

-   Check if a given date is a holiday (`LocalDate`, `java.sql.Date`, `Instant`, `Timestamp`, `XMLGregorianCalendar`, `Calendar`, `LocalDateTime`, `ZonedDateTime`, `OffsetDateTime`, `TemporalAccessor` overloads).
-   Get a list of all holidays for a specific year or current year (Swedish and English names).
-   `isHolidayToday()` to check if today is a Swedish holiday (Stockholm timezone).
-   **Customization**: Add custom fixed holidays, remove specific standard holidays by date, or add custom rule-based holidays (e.g., "day after X").
-   Caching for performance.
-   Thread-safe.

## Maven Central

_This library has no external dependencies and can be published to your artifact repository._

### Gradle

```groovy
implementation 'dev.klok.holidays:swedish-holidays:1.1.0' // <-- Updated Version
```

### Maven

```xml
<dependency>
    <groupId>dev.klok.holidays</groupId>
    <artifactId>swedish-holidays</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Usage Example

```java
import dev.klok.holidays.SwedishHolidays;
import dev.klok.holidays.Holiday;
import dev.klok.holidays.HolidayRule; // Import the new rule interface
import java.time.*;
import java.sql.Date;
import java.sql.Timestamp;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class HolidayDemo {
    public static void main(String[] args) throws Exception {
        // --- Basic Usage ---

        // 1. Check if today is a holiday
        boolean todayHoliday = SwedishHolidays.isHolidayToday();
        System.out.println("Today is holiday? " + todayHoliday);

        // 2. Check specific date
        LocalDate date = LocalDate.of(2025, 1, 1);
        boolean isNewYear = SwedishHolidays.isHoliday(date);
        System.out.println("2025-01-01 is New Year's Day? " + isNewYear); // true

        // 3. List all holidays in 2025 in English (default language)
        System.out.println("\n--- Standard Holidays 2025 (EN) ---");
        for (Holiday h : SwedishHolidays.listHolidays(2025)) {
            System.out.printf("%s: %s (%s)\n", h.getDate(), h.getName(), h.getDescription());
        }

        // 4. List all holidays in 2025 in Swedish
        System.out.println("\n--- Standard Holidays 2025 (SE) ---");
        for (Holiday h : SwedishHolidays.listHolidays(SwedishHolidays.Lang.SE, 2025)) {
            System.out.printf("%s: %s (%s)\n", h.getDate(), h.getName(), h.getDescription());
        }

        // --- Customization ---

        // 5. Add a custom fixed company holiday
        SwedishHolidays.addCustomHoliday(
                LocalDate.of(2025, 8, 15),
                "Company Kick-off",
                "Annual company event (custom)"
        );
        System.out.println("\nIs 2025-08-15 a holiday now? " + SwedishHolidays.isHoliday(LocalDate.of(2025, 8, 15))); // true

        // 6. Remove a specific standard holiday *by date* (e.g., Epiphany Eve 2025)
        // Note: This removes only this specific date instance.
        LocalDate epiphanyEve2025 = SwedishHolidays.getEpiphanyEve(2025).getDate();
        SwedishHolidays.removeStandardHoliday(epiphanyEve2025);
        System.out.println("Is Epiphany Eve 2025 (" + epiphanyEve2025 + ") still a holiday? " + SwedishHolidays.isHoliday(epiphanyEve2025)); // false

        // 7. Add a custom rule: The Friday after Ascension Day ("Klämdag")
        HolidayRule ascensionBridgeDay = (year, lang) -> {
            Holiday ascension = SwedishHolidays.getAscensionDay(year, lang);
            LocalDate bridgeDayDate = ascension.getDate().plusDays(1);
            String name = lang == SwedishHolidays.Lang.SE ? "Klämdag (Kristi himmelsfärd)" : "Bridging Day (Ascension)";
            String desc = lang == SwedishHolidays.Lang.SE ? "Fredagen efter Kristi himmelsfärdsdag" : "Friday after Ascension Day";
            return new Holiday(bridgeDayDate, name, desc);
        };
        SwedishHolidays.addCustomHolidayRule(ascensionBridgeDay);

        // 8. List holidays for 2025 again, showing customizations
        System.out.println("\n--- Customized Holidays 2025 (EN) ---");
        List<Holiday> customizedHolidays = SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN, 2025);
        for (Holiday h : customizedHolidays) {
            System.out.printf("%s: %s (%s)\n", h.getDate(), h.getName(), h.getDescription());
            // Look for "Company Kick-off", the missing Epiphany Eve, and "Bridging Day (Ascension)"
        }

        // 9. Check the calculated bridging day
        LocalDate bridgeDay2025 = SwedishHolidays.getAscensionDay(2025).getDate().plusDays(1);
        System.out.println("\nIs the Ascension bridging day (" + bridgeDay2025 + ") a holiday? " + SwedishHolidays.isHoliday(bridgeDay2025)); // true

        // 10. Clear all customizations to reset
        SwedishHolidays.clearAllCustomizations();
        System.out.println("\nCustomizations cleared.");
        System.out.println("Is 2025-08-15 a holiday now? " + SwedishHolidays.isHoliday(LocalDate.of(2025, 8, 15))); // false
        System.out.println("Is Epiphany Eve 2025 (" + epiphanyEve2025 + ") a holiday again? " + SwedishHolidays.isHoliday(epiphanyEve2025)); // true
        System.out.println("Is the Ascension bridging day (" + bridgeDay2025 + ") still a holiday? " + SwedishHolidays.isHoliday(bridgeDay2025)); // false
    }
}
```

*Note: Methods without explicit language parameters default to English (`Lang.EN`). You can change this using `SwedishHolidays.setDefaultLanguage(SwedishHolidays.Lang.SE)`.*

## Customization

The library allows customization of the holiday list:

### Adding Fixed Custom Holidays

You can add specific dates as holidays, often used for company-specific non-working days.

```java
SwedishHolidays.addCustomHoliday(
        LocalDate.of(2025, 8, 15),
    "Company Kick-off",
            "Annual company event"
            );
```

These additions apply only to the specified year in the `LocalDate`.

### Removing Standard Holidays by Date

If a standard calculated holiday should not be considered a holiday for your use case, you can remove it *by its specific date*.

```java
// Calculate the date of the holiday you want to remove for a specific year
LocalDate midsummerEve2025 = SwedishHolidays.getMidsummerEve(2025).getDate();

// Remove that specific date instance
SwedishHolidays.removeStandardHoliday(midsummerEve2025);

// Now, isHoliday(midsummerEve2025) will return false.
// Midsummer Eve in other years remains unaffected unless explicitly removed.
```

**Important:** `removeStandardHoliday` works on the exact `LocalDate`. It does not remove the *concept* of a holiday (like "Midsummer Eve") for all years.

### Adding Custom Holiday Rules

For holidays based on logic (e.g., "day after X", "third Monday of Month Y"), you can implement the `HolidayRule` interface and add it.

```java
import dev.klok.holidays.HolidayRule;
import dev.klok.holidays.Holiday;
import dev.klok.holidays.SwedishHolidays;
import java.time.LocalDate;

// Example: Rule for the Friday after Ascension Day ("Klämdag")
HolidayRule ascensionBridgeDay = (year, lang) -> {
    // Use existing getters to find the base holiday
    Holiday ascension = SwedishHolidays.getAscensionDay(year, lang);
    LocalDate bridgeDayDate = ascension.getDate().plusDays(1); // Friday

    // Provide localized names/descriptions
    String name = lang == SwedishHolidays.Lang.SE ? "Klämdag (Kristi himmelsfärd)" : "Bridging Day (Ascension)";
    String desc = lang == SwedishHolidays.Lang.SE ? "Fredagen efter Kristi himmelsfärdsdag" : "Friday after Ascension Day";

    // Return the new Holiday object
    return new Holiday(bridgeDayDate, name, desc);
};

// Add the rule to the system
SwedishHolidays.addCustomHolidayRule(ascensionBridgeDay);

// Now, the Friday after Ascension will be included in listHolidays and isHoliday checks.
```

The `calculateHoliday` method in your `HolidayRule` receives the `year` and `Lang` and should return a `Holiday` object if the rule applies for that year, or `null` otherwise.

### Clearing Customizations

You can clear specific types of customizations or all of them:

```java
SwedishHolidays.clearCustomAdditions();   // Remove only fixed additions
SwedishHolidays.clearStandardRemovals();  // Remove only standard date removals
SwedishHolidays.clearCustomRules();       // Remove only custom rules
SwedishHolidays.clearAllCustomizations(); // Remove all additions, removals, and rules
```

### Important Notes on Customization

-   **Call Early:** Apply customizations early in your application's lifecycle, before extensive use of `isHoliday` or `listHolidays`.
-   **Cache Invalidation:** All configuration methods (`add*`, `remove*`, `clear*`) automatically clear the internal holiday cache. Recalculation occurs on the next holiday request.
-   **Global Scope:** These customizations are static and apply globally within the JVM. If you need different holiday sets concurrently, you would need to refactor the library to be instance-based.

## Public Methods

This library provides several categories of public methods:

1.  **`isHoliday(...)` overloads:** Check if a specific point in time corresponds to a holiday date in Stockholm.
    -   `isHoliday(LocalDate date)`
    -   `isHoliday(java.sql.Date date)`
    -   `isHoliday(Timestamp timestamp)`
    -   `isHoliday(Instant instant)`
    -   `isHoliday(XMLGregorianCalendar xmlCal)`
    -   `isHoliday(java.util.Date date)`
    -   `isHoliday(Calendar calendar)`
    -   `isHoliday(LocalDateTime dateTime)` (Assumes Stockholm time)
    -   `isHoliday(ZonedDateTime dateTime)` (Converts to Stockholm time)
    -   `isHoliday(OffsetDateTime dateTime)` (Converts to Stockholm time)
    -   `isHoliday(TemporalAccessor temporal)` (Attempts LocalDate or Instant extraction)

2.  **`isHolidayToday()`:** Checks if the current date (Stockholm timezone) is a holiday.

3.  **`listHolidays(...)` overloads:** Retrieve the final list of holidays for a year (includes standard & custom, reflects removals), sorted by date.
    -   `listHolidays(Lang lang, int year)` – Specify language and year.
    -   `listHolidays(String langCode, int year)` – Use string code (`"SE"` or `"EN"`).
    -   `listHolidays(Lang lang)` – Current year, specify language.
    -   `listHolidays(String langCode)` – Current year, use string code.
    -   `listHolidays(int year)` – Specify year, use default language.
    -   `listHolidays()` – Current year, use default language.
    -   `listHolidaysSE()` – Current year, Swedish language.
    -   `listHolidaysEN()` – Current year, English language.

4.  **Individual Standard Holiday Getters:** Calculate specific *standard* holidays. **Note:** These getters *do not* reflect customizations (additions/removals). They are useful for getting the base date (e.g., to use with `removeStandardHoliday`).
    -   Fixed-date holidays: `getNewYearsDay(...)`, `getEpiphany(...)`, `getMayDay(...)`, `getNationalDay(...)`, `getChristmasEve(...)`, etc.
    -   Easter-based holidays: `getMaundyThursday(...)`, `getGoodFriday(...)`, `getEasterSunday(...)`, `getAscensionDay(...)`, `getPentecost(...)`, etc.
    -   Other movable holidays: `getMidsummerEve(...)`, `getMidsummerDay(...)`, `getAllSaintsEve(...)`, `getAllSaintsDay(...)`
    -   Each getter has overloads:
        *   `getHolidayName(int year)` – Uses default language.
        *   `getHolidayName(int year, Lang lang)` – Specifies language.

5.  **Configuration Methods:** Manage customizations and library settings.
    -   `addCustomHoliday(LocalDate date, String name, String description)`
    -   `removeStandardHoliday(LocalDate dateToRemove)`
    -   `addCustomHolidayRule(HolidayRule rule)`
    -   `clearCustomAdditions()`
    -   `clearStandardRemovals()`
    -   `clearCustomRules()`
    -   `clearAllCustomizations()`
    -   `clearCache()`
    -   `setDefaultLanguage(Lang lang)`

## Method Naming & Holiday Translation

All public method names are in English. Below is the translation table for standard holidays:

| Swedish                | English                |
| ---------------------- | ---------------------- |
| Nyårsdagen             | New Year's Day         |
| Trettondagsafton       | Epiphany Eve           |
| Trettondedag jul       | Epiphany               |
| Valborgsmässoafton     | Walpurgis Eve          |
| Första maj             | May Day                |
| Sveriges nationaldag   | National Day of Sweden |
| Julafton               | Christmas Eve          |
| Juldagen               | Christmas Day          |
| Annandag jul           | St. Stephen's Day      |
| Nyårsafton             | New Year's Eve         |
| Skärtorsdagen          | Maundy Thursday        |
| Långfredagen           | Good Friday            |
| Påskafton              | Easter Eve             |
| Påskdagen              | Easter Sunday          |
| Annandag påsk          | Easter Monday          |
| Kristi himmelsfärdsdag | Ascension Day          |
| Pingstafton            | Whitsun Eve            |
| Pingstdagen            | Pentecost              |
| Midsommarafton         | Midsummer Eve          |
| Midsommardagen         | Midsummer Day          |
| Allhelgonaafton        | All Saints' Eve        |
| Alla helgons dag       | All Saints' Day        |

## Building & Testing

Use the Gradle wrapper:

```bash
# Build the library
./gradlew build

# Run tests
./gradlew test
```