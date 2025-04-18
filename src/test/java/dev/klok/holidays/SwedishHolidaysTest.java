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

    @Test
    void testEaster() {
        // Easter 2026 is on April 5
        // Fre 3 apr.: Långfredagen  –  Lör 4 apr.: Påskafton  –  Sön 5 apr.: Påskdagen  –  Mån 6 apr. (vecka 15): Annandag påsk

        Holiday easterEve = SwedishHolidays.getEasterEve(2026);
        Holiday easterEveSE = SwedishHolidays.getEasterEve(2026, SwedishHolidays.Lang.SE);
        assertEquals(LocalDate.of(2026, 4, 4), easterEve.getDate());
        assertEquals("Påskafton", easterEveSE.getName());
        assertEquals("Easter Eve", easterEve.getName());

        Holiday easterMonday = SwedishHolidays.getEasterMonday(2026);
        Holiday easterMondaySE = SwedishHolidays.getEasterMonday(2026, SwedishHolidays.Lang.SE);
        assertEquals(LocalDate.of(2026, 4, 6), easterMondaySE.getDate());
        assertEquals("Annandag påsk", easterMondaySE.getName());
        assertEquals("Easter Monday", easterMonday.getName());

        Holiday goodFriday = SwedishHolidays.getGoodFriday(2026);
        Holiday goodFridaySE = SwedishHolidays.getGoodFriday(2026, SwedishHolidays.Lang.SE);
        assertEquals(LocalDate.of(2026, 4, 3), goodFriday.getDate());
        assertEquals("Långfredagen", goodFridaySE.getName());
        assertEquals("Good Friday", goodFriday.getName());

        Holiday easterSunday = SwedishHolidays.getEasterSunday(2026);
        Holiday easterSundaySE = SwedishHolidays.getEasterSunday(2026, SwedishHolidays.Lang.SE);
        assertEquals("Påskdagen", easterSundaySE.getName());
        assertEquals("Easter Sunday", easterSunday.getName());
        assertEquals(LocalDate.of(2026, 4, 5), easterSunday.getDate());

        // Easter 2029
        // Fre 30 mars: Långfredagen  –  Lör 31 mars: Påskafton  –  Sön 1 apr.: Påskdagen  –  Mån 2 apr. (vecka 14): Annandag påsk

        Holiday easterEve2029 = SwedishHolidays.getEasterEve(2029);
        Holiday easterEve2029SE = SwedishHolidays.getEasterEve(2029, SwedishHolidays.Lang.SE);
        assertEquals(LocalDate.of(2029, 3, 31), easterEve2029.getDate());
        assertEquals("Påskafton", easterEve2029SE.getName());
        assertEquals("Easter Eve", easterEve2029.getName());
        Holiday easterMonday2029 = SwedishHolidays.getEasterMonday(2029);
        Holiday easterMonday2029SE = SwedishHolidays.getEasterMonday(2029, SwedishHolidays.Lang.SE);
        assertEquals(LocalDate.of(2029, 4, 2), easterMonday2029.getDate());
        assertEquals("Annandag påsk", easterMonday2029SE.getName());
        assertEquals("Easter Monday", easterMonday2029.getName());
        Holiday goodFriday2029 = SwedishHolidays.getGoodFriday(2029);
        Holiday goodFriday2029SE = SwedishHolidays.getGoodFriday(2029, SwedishHolidays.Lang.SE);
        assertEquals(LocalDate.of(2029, 3, 30), goodFriday2029.getDate());
        assertEquals("Långfredagen", goodFriday2029SE.getName());
        assertEquals("Good Friday", goodFriday2029.getName());
        Holiday easterSunday2029 = SwedishHolidays.getEasterSunday(2029);
        Holiday easterSunday2029SE = SwedishHolidays.getEasterSunday(2029, SwedishHolidays.Lang.SE);
        assertEquals("Påskdagen", easterSunday2029SE.getName());
        assertEquals("Easter Sunday", easterSunday2029.getName());
        assertEquals(LocalDate.of(2029, 4, 1), easterSunday2029.getDate());

    }

    @Test
    void testPentecost() {
        int year = 2030;
        Holiday pentecost = SwedishHolidays.getPentecost(year);
        Holiday pentecostSE = SwedishHolidays.getPentecost(year, SwedishHolidays.Lang.SE);

        // Lör 8 juni: Pingstafton  –  Sön 9 juni: Pingstdagen
        assertEquals(LocalDate.of(2030, 6, 9), pentecost.getDate());
        assertEquals("Pingstdagen", pentecostSE.getName());
        assertEquals("Pentecost", pentecost.getName());

        Holiday whitsunEve = SwedishHolidays.getWhitsunEve(year);
        Holiday whitsunEveSE = SwedishHolidays.getWhitsunEve(year, SwedishHolidays.Lang.SE);
        assertEquals(LocalDate.of(2030, 6, 8), whitsunEveSE.getDate());
        assertEquals("Whitsun Eve", whitsunEve.getName());
        assertEquals("Pingstafton", whitsunEveSE.getName());
    }
    
}
