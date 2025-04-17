# Klok Holidays Utility Library

A lightweight Java library (Java 8+ compatible) providing utilities to determine Swedish public holidays.

## Features

- Check if a given date is a holiday (`LocalDate`, `java.sql.Date`, `Instant`, `Timestamp` overloads).
- Get a list of all holidays for a specific year or current year (Swedish and English names).
- `isHolidayToday()` to check if today is a Swedish holiday (Stockholm timezone).

## Maven Central

_This library has no external dependencies and can be published to your artifact repository._

### Gradle

```groovy
implementation 'dev.klok.holidays:klok-holidays:1.0.0'
```

### Usage Example

```java
import dev.klok.holidays.SwedishHolidays;
import dev.klok.holidays.Holiday;
import java.time.*;
import java.sql.Date;
import java.sql.Timestamp;

public class HolidayDemo {
    public static void main(String[] args) {
        // 1. Check if today is a holiday
        boolean todayHoliday = SwedishHolidays.isHolidayToday();
        System.out.println("Today is holiday? " + todayHoliday);

        // 2. Check specific date
        LocalDate date = LocalDate.of(2025, Month.APRIL, 17);
        boolean isGoodFriday = SwedishHolidays.isHoliday(date);
        System.out.println("2025-04-17 is Good Friday? " + isGoodFriday);

        // 3. List all holidays in 2025 in English
        for (Holiday h : SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN, 2025)) {
            System.out.printf("%s: %s (%s)\n", h.getDate(), h.getName(), h.getDescription());
        }

        // 4. Using java.sql.Date or Timestamp
        Date sqlDate = Date.valueOf(LocalDate.of(2025, 6, 6));
        System.out.println("National Day (SQL Date): " + SwedishHolidays.isHoliday(sqlDate));

        Timestamp ts = Timestamp.from(LocalDate.of(2025, 12, 25)
                .atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant());
        System.out.println("Christmas (Timestamp): " + SwedishHolidays.isHoliday(ts));
    }
}
```

## Running Tests

```bash
./gradlew test
```
