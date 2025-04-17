package dev.klok.holidays;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.TimeZone;

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

    @Test
    void testXmlGregorianCalendar() throws Exception {
        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance()
                .newXMLGregorianCalendarDate(2025, 1, 1, DatatypeConstants.FIELD_UNDEFINED);
        assertTrue(SwedishHolidays.isHoliday(xmlCal));
        XMLGregorianCalendar nonHoliday = DatatypeFactory.newInstance()
                .newXMLGregorianCalendarDate(2025, 1, 2, DatatypeConstants.FIELD_UNDEFINED);
        assertFalse(SwedishHolidays.isHoliday(nonHoliday));
    }

    @Test
    void testUtilDateOverload() {
        // January 1, 2025 is a holiday
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"));
        cal.set(2025, Calendar.JANUARY, 1, 0, 0, 0);
        java.util.Date utilDate = cal.getTime();
        assertTrue(SwedishHolidays.isHoliday(utilDate));
    }

    @Test
    void testCalendarOverload() {
        // June 6, 2025 is National Day
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"));
        cal.set(2025, Calendar.JUNE, 6, 12, 0, 0);
        assertTrue(SwedishHolidays.isHoliday(cal));
    }

    @Test
    void testLocalDateTimeOverload() {
        LocalDateTime dt = LocalDateTime.of(2025, 4, 18, 9, 30);
        assertTrue(SwedishHolidays.isHoliday(dt));
    }

    @Test
    void testZonedDateTimeOverload() {
        ZonedDateTime zdt = ZonedDateTime.of(
                LocalDateTime.of(2025, 4, 18, 15, 0),
                ZoneId.of("Europe/Stockholm")
        );
        assertTrue(SwedishHolidays.isHoliday(zdt));
    }

    @Test
    void testOffsetDateTimeOverload() {
        OffsetDateTime odt = OffsetDateTime.of(
                LocalDateTime.of(2025, 4, 18, 15, 0),
                ZoneOffset.ofHours(2)
        );
        assertTrue(SwedishHolidays.isHoliday(odt));
    }

    @Test
    void testInstantOverload() {
        Instant instant = LocalDate.of(2025, 1, 1)
                .atStartOfDay(ZoneId.of("Europe/Stockholm"))
                .toInstant();
        assertTrue(SwedishHolidays.isHoliday(instant));
    }
}
