package dev.klok.holidays;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwedishHolidaysTest {

    @Test
    void testFixedDateHoliday() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        assertTrue(SwedishHolidays.isHoliday(date));
        Holiday h = SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN, 2025)
                .stream()
                .filter(x -> x.getDate().equals(date))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Holiday not found"));
        assertEquals("New Year's Day", h.getName());
    }

    @Test
    void testEasterBasedHoliday() {
        LocalDate goodFriday = LocalDate.of(2025, 4, 18);
        assertTrue(SwedishHolidays.isHoliday(goodFriday));
        Holiday h = SwedishHolidays.listHolidaysEN().stream()
                .filter(x -> x.getDate().equals(goodFriday))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Holiday not found"));
        assertEquals("Good Friday", h.getName());
    }

    @Test
    void testSQLDateAndTimestamp() {
        LocalDate date = LocalDate.of(2025, 6, 6);
        Date sqlDate = Date.valueOf(date);
        assertTrue(SwedishHolidays.isHoliday(sqlDate));
        Instant instant = date.atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant();
        Timestamp ts = Timestamp.from(instant);
        assertTrue(SwedishHolidays.isHoliday(ts));
    }

    @Test
    void testListHolidaysCount() {
        List<Holiday> list = SwedishHolidays.listHolidays(SwedishHolidays.Lang.SE, 2025);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }
}
