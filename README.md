# Klok Swedish Holidays Utility Library

A lightweight Java library (Java 8+ compatible) providing utilities to determine Swedish public holidays.

## Features

- Check if a given date is a holiday (`LocalDate`, `java.sql.Date`, `Instant`, `Timestamp`, `XMLGregorianCalendar` overloads).
- Get a list of all holidays for a specific year or current year (Swedish and English names).
- `isHolidayToday()` to check if today is a Swedish holiday (Stockholm timezone).

## Maven Central

_This library has no external dependencies and can be published to your artifact repository._

### Gradle

```groovy
implementation 'dev.klok.holidays:swedish-holidays:1.0.2'
```

### Usage Example

```java
import dev.klok.holidays.SwedishHolidays;
import dev.klok.holidays.Holiday;
import java.time.*;
import java.sql.Date;
import java.sql.Timestamp;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class HolidayDemo {
    public static void main(String[] args) throws Exception {
        // 1. Check if today is a holiday
        boolean todayHoliday = SwedishHolidays.isHolidayToday();
        System.out.println("Today is holiday? " + todayHoliday);

        // 2. Check specific date
        LocalDate date = LocalDate.of(2025, Month.APRIL, 17);
        boolean isGoodFriday = SwedishHolidays.isHoliday(date);
        System.out.println("2025-04-17 is Good Friday? " + isGoodFriday);

        // 3. List all holidays in 2025 in English (explicit language variant)
        for (Holiday h : SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN, 2025)) {
            System.out.printf("%s: %s (%s)\n", h.getDate(), h.getName(), h.getDescription());
        }

        // 4. Using java.sql.Date or Timestamp
        Date sqlDate = Date.valueOf(LocalDate.of(2025, 6, 6));
        System.out.println("National Day (SQL Date): " + SwedishHolidays.isHoliday(sqlDate));

        Timestamp ts = Timestamp.from(LocalDate.of(2025, 12, 25)
                .atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant());
        System.out.println("Christmas (Timestamp): " + SwedishHolidays.isHoliday(ts));

        // 5. Using XMLGregorianCalendar
        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance()
                .newXMLGregorianCalendarDate(2025, 1, 1, DatatypeConstants.FIELD_UNDEFINED);
        System.out.println("New Year's Day (XMLGregorianCalendar): " + SwedishHolidays.isHoliday(xmlCal));

        // 6. List all holidays in 2025 with default language (English)
        for (Holiday h : SwedishHolidays.listHolidays(2025)) {
            System.out.printf("%s: %s (%s)\n", h.getDate(), h.getName(), h.getDescription());
        }

        // 7. List holidays for current year with default language (English)
        List<Holiday> currentYear = SwedishHolidays.listHolidays();
        currentYear.forEach(h -> System.out.printf("%s: %s (%s)\n", h.getDate(), h.getName(), h.getDescription()));

        // 8. Easter-based getter overload: year-only variant uses default language (English)
        Holiday goodFri = SwedishHolidays.getGoodFriday(2025);
        System.out.println("Good Friday 2025: " + goodFri);

        // 9. Easter-based getter overload: specify language when needed
        Holiday langFri = SwedishHolidays.getGoodFriday(2025, SwedishHolidays.Lang.SE);
        System.out.println("Långfredagen 2025: " + langFri);
    }
}
```

*Note: Methods without explicit language parameters default to English for convenience.*

## Public Methods

This library provides several categories of public methods for checking and retrieving Swedish holidays:

1. `isHoliday(...)` overloads
    - `isHoliday(LocalDate date)`
    - `isHoliday(java.sql.Date date)`
    - `isHoliday(Timestamp timestamp)`
    - `isHoliday(Instant instant)`
    - `isHoliday(XMLGregorianCalendar xmlCal)`
    - `isHoliday(LocalDateTime dateTime)`
    - `isHoliday(ZonedDateTime dateTime)`
    - `isHoliday(OffsetDateTime dateTime)`
    - `isHoliday(Calendar calendar)`
    - Default language: English

2. `isHolidayToday()`
    - Checks if the current date (Stockholm timezone) is a holiday

3. `listHolidays(...)` overloads for retrieving all holidays:
    - `listHolidays(Lang lang, int year)` &ndash; specify language and year
    - `listHolidays(String langCode, int year)` &ndash; use string code (`"SE"` or `"EN"`)
    - `listHolidays(Lang lang)` &ndash; current year, specify language
    - `listHolidays(int year)` &ndash; specify year, default language is English
    - `listHolidays()` &ndash; current year, default language is English
    - `listHolidaysSE()` &ndash; current year, Swedish language
    - `listHolidaysEN()` &ndash; current year, English language

4. Individual holiday getters
    - Fixed-date holidays: `getNewYearsDay(...)`, `getEpiphany(...)`, `getMayDay(...)`, `getNationalDay(...)`, `getChristmasEve(...)`, etc.
        * Overloads:
            + `getHolidayName(int year)` &ndash; year only, default language (English)
            + `getHolidayName(int year, Lang lang)` &ndash; specify language
    - Easter-based holidays: `getMaundyThursday(...)`, `getGoodFriday(...)`, `getEasterSunday(...)`, `getAscensionDay(...)`, `getPentecost(...)`, etc.
        * Overloads:
            + `getHolidayName(int year)` &ndash; year only, default language
            + `getHolidayName(int year, Lang lang)` &ndash; specify language
            + `getHolidayName(LocalDate easterDate, Lang lang)` &ndash; internal use for date-manipulation
    - Movable-date holidays: `getMidsummerEve(...)`, `getMidsummerDay(...)`, `getAllSaintsEve(...)`, `getAllSaintsDay(...)`
        * Overloads:
            + `getHolidayName(int year)` &ndash; year only, default language
            + `getHolidayName(int year, Lang lang)` &ndash; specify language

*These helpers allow fetching a single `Holiday` instance for any specific holiday by name.*

## Method Naming & Holiday Translation

All public method names are in English for consistency. Below is a translation table between the Swedish holiday names and their English counterparts as used by the library:

| Swedish                          | English                       |
|----------------------------------|-------------------------------|
| Nyårsdagen                       | New Year's Day                |
| Trettondagsafton                 | Epiphany Eve                  |
| Trettondedag jul                 | Epiphany                      |
| Valborgsmässoafton               | Walpurgis Eve                 |
| Första maj                       | May Day                       |
| Sveriges nationaldag             | National Day of Sweden        |
| Julafton                         | Christmas Eve                 |
| Juldagen                         | Christmas Day                 |
| Annandag jul                     | St. Stephen's Day             |
| Nyårsafton                       | New Year's Eve                |
| Skärtorsdagen                    | Maundy Thursday               |
| Långfredagen                     | Good Friday                   |
| Påskafton                        | Easter Eve                    |
| Påskdagen                        | Easter Sunday                 |
| Annandag påsk                    | Easter Monday                 |
| Kristi himmelsfärdsdag           | Ascension Day                 |
| Pingstafton                      | Whitsun Eve                   |
| Pingstdagen                      | Pentecost                     |
| Midsommarafton                   | Midsummer Eve                 |
| Midsommardagen                   | Midsummer Day                 |
| Allhelgonaafton                  | All Saints' Eve               |
| Alla helgons dag                 | All Saints' Day               |

## Running Tests

```bash
./gradlew test
```
