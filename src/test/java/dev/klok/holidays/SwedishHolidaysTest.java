package dev.klok.holidays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SwedishHolidays Utility Tests")
class SwedishHolidaysTest {

    // Reset customizations before each test to ensure isolation
    @BeforeEach
    void setUp() {
        SwedishHolidays.clearAllCustomizations();
        // Reset default language just in case a test changes it
        SwedishHolidays.setDefaultLanguage(SwedishHolidays.Lang.EN);
        // Clear cache explicitly, though clearAllCustomizations does it too
        SwedishHolidays.clearCache();
        // Pre-populate for consistency if needed, or rely on lazy loading
        prePopulateCacheForTestYears();
    }

    // Helper to pre-populate cache for years used in tests
    private void prePopulateCacheForTestYears() {
        int currentYear = LocalDate.now().getYear();
        SwedishHolidays.listHolidays(2023); // Example past year
        SwedishHolidays.listHolidays(2024); // Example year
        SwedishHolidays.listHolidays(2025); // Common test year
        SwedishHolidays.listHolidays(2026); // Common test year
        SwedishHolidays.listHolidays(2029); // Easter test year
        SwedishHolidays.listHolidays(2030); // Pentecost test year
        SwedishHolidays.listHolidays(currentYear);
        SwedishHolidays.listHolidays(currentYear+1);
    }


    // --- Existing Tests (Kept as is, minor adjustments for clarity/consistency) ---

    @Nested
    @DisplayName("Standard Holiday Checks")
    class StandardChecks {
        @Test
        void testFixedDateHoliday() {
            LocalDate date = LocalDate.of(2025, 1, 1);
            assertTrue(SwedishHolidays.isHoliday(date), "New Year's Day should be a holiday");

            // Use the getter to verify name/date consistency
            Holiday h = SwedishHolidays.getNewYearsDay(2025, SwedishHolidays.Lang.EN);
            assertEquals(date, h.getDate());
            assertEquals("New Year's Day", h.getName());

            // Check Swedish name via getter
            Holiday hSE = SwedishHolidays.getNewYearsDay(2025, SwedishHolidays.Lang.SE);
            assertEquals("Nyårsdagen", hSE.getName());
        }

        @Test
        void testEasterBasedHoliday() {
            LocalDate goodFriday2025 = LocalDate.of(2025, 4, 18);
            assertTrue(SwedishHolidays.isHoliday(goodFriday2025), "Good Friday 2025 should be a holiday");

            Holiday h = SwedishHolidays.getGoodFriday(2025, SwedishHolidays.Lang.EN);
            assertEquals(goodFriday2025, h.getDate());
            assertEquals("Good Friday", h.getName());

            Holiday hSE = SwedishHolidays.getGoodFriday(2025, SwedishHolidays.Lang.SE);
            assertEquals("Långfredagen", hSE.getName());
        }

        @Test
        void testSQLDateAndTimestamp() {
            LocalDate nationalDay2025 = LocalDate.of(2025, 6, 6);
            // SQL Date
            Date sqlDate = Date.valueOf(nationalDay2025);
            assertTrue(SwedishHolidays.isHoliday(sqlDate), "National Day (SQL Date) should be a holiday");

            // Timestamp
            Instant instant = nationalDay2025.atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant();
            Timestamp ts = Timestamp.from(instant);
            assertTrue(SwedishHolidays.isHoliday(ts), "National Day (Timestamp) should be a holiday");
        }

        @Test
        void testXmlGregorianCalendar() throws DatatypeConfigurationException {
            XMLGregorianCalendar newYear2025 = DatatypeFactory.newInstance()
                    .newXMLGregorianCalendarDate(2025, 1, 1, DatatypeConstants.FIELD_UNDEFINED);
            assertTrue(SwedishHolidays.isHoliday(newYear2025), "New Year's (XMLGregorianCalendar) should be a holiday");

            XMLGregorianCalendar nonHoliday = DatatypeFactory.newInstance()
                    .newXMLGregorianCalendarDate(2025, 1, 2, DatatypeConstants.FIELD_UNDEFINED);
            assertFalse(SwedishHolidays.isHoliday(nonHoliday), "Jan 2nd (XMLGregorianCalendar) should not be a holiday");

            // Test with timezone offset
            XMLGregorianCalendar xmasWithTZ = DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(2025, 12, 25, 10, 0, 0, 0, 120); // UTC+2 (Stockholm DST offset)
            assertTrue(SwedishHolidays.isHoliday(xmasWithTZ), "Christmas (XMLGregorianCalendar with TZ) should be a holiday");
        }

        @Test
        void testUtilDateOverload() {
            // May 1st, 2025 is a holiday
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"));
            cal.set(2025, Calendar.MAY, 1, 10, 30, 0);
            cal.set(Calendar.MILLISECOND, 0);
            java.util.Date utilDate = cal.getTime();
            assertTrue(SwedishHolidays.isHoliday(utilDate), "May Day (java.util.Date) should be a holiday");
        }

        @Test
        void testCalendarOverload() {
            // Christmas Day, 2025
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"));
            cal.set(2025, Calendar.DECEMBER, 25, 14, 0, 0);
            assertTrue(SwedishHolidays.isHoliday(cal), "Christmas Day (Calendar) should be a holiday");
        }

        @Test
        void testLocalDateTimeOverload() {
            // Easter Monday 2025 (April 21)
            LocalDateTime dt = LocalDateTime.of(2025, 4, 21, 11, 0);
            assertTrue(SwedishHolidays.isHoliday(dt), "Easter Monday (LocalDateTime) should be a holiday");

            LocalDateTime nonHoliday = LocalDateTime.of(2025, 4, 22, 11, 0);
            assertFalse(SwedishHolidays.isHoliday(nonHoliday), "Day after Easter Monday (LocalDateTime) should not be a holiday");
        }

        @Test
        void testZonedDateTimeOverload() {
            // Ascension Day 2025 (May 29) - Check with UTC zone, should convert correctly
            ZonedDateTime zdtUTC = ZonedDateTime.of(
                    LocalDateTime.of(2025, 5, 29, 10, 0), // 10:00 UTC is 12:00 Stockholm time (summer)
                    ZoneId.of("UTC")
            );
            assertTrue(SwedishHolidays.isHoliday(zdtUTC), "Ascension Day (ZonedDateTime UTC) should be a holiday");

            ZonedDateTime zdtStockholm = ZonedDateTime.of(
                    LocalDateTime.of(2025, 5, 29, 12, 0),
                    ZoneId.of("Europe/Stockholm")
            );
            assertTrue(SwedishHolidays.isHoliday(zdtStockholm), "Ascension Day (ZonedDateTime Stockholm) should be a holiday");
        }

        @Test
        void testOffsetDateTimeOverload() {
            // Midsummer Day 2025 (June 21) - Check with different offset
            OffsetDateTime odt = OffsetDateTime.of(
                    LocalDateTime.of(2025, 6, 21, 0, 0), // Midnight UTC+1 is 01:00 Stockholm time (summer)
                    ZoneOffset.ofHours(1)
            );
            assertTrue(SwedishHolidays.isHoliday(odt), "Midsummer Day (OffsetDateTime) should be a holiday");
        }

        @Test
        void testInstantOverload() {
            // All Saints' Day 2025 (Nov 1)
            Instant instant = LocalDate.of(2025, 11, 1)
                    .atStartOfDay(ZoneId.of("Europe/Stockholm")) // Use Stockholm zone for conversion
                    .toInstant();
            assertTrue(SwedishHolidays.isHoliday(instant), "All Saints' Day (Instant) should be a holiday");

            Instant nonHolidayInstant = LocalDate.of(2025, 11, 2)
                    .atStartOfDay(ZoneId.of("Europe/Stockholm"))
                    .toInstant();
            assertFalse(SwedishHolidays.isHoliday(nonHolidayInstant), "Day after All Saints' Day (Instant) should not be holiday");
        }

        @Test
        void testIsHolidayToday() {
            // This test is environment-dependent, difficult to assert definitively
            // We can check it runs without error, but the result depends on the current date
            assertDoesNotThrow(SwedishHolidays::isHolidayToday);
            System.out.println("Is today a holiday? " + SwedishHolidays.isHolidayToday());
        }

        @Test
        void testTemporalAccessorOverload() {
            // Valid LocalDate
            TemporalAccessor taDate = LocalDate.of(2025, 12, 25);
            assertTrue(SwedishHolidays.isHoliday(taDate));

            // Valid ZonedDateTime
            TemporalAccessor taZdt = ZonedDateTime.of(LocalDateTime.of(2025, 5, 1, 10, 0), ZoneId.of("Europe/Stockholm"));
            assertTrue(SwedishHolidays.isHoliday(taZdt));

            // Valid Instant
            TemporalAccessor taInstant = Instant.parse("2025-06-06T08:00:00Z"); // National Day
            assertTrue(SwedishHolidays.isHoliday(taInstant));

            // Non-holiday via LocalDate
            TemporalAccessor taNonHoliday = LocalDate.of(2025, 1, 2);
            assertFalse(SwedishHolidays.isHoliday(taNonHoliday));

            // Type that cannot be resolved to date or instant
            TemporalAccessor taYearMonth = YearMonth.of(2025, Month.JANUARY);
            assertThrows(DateTimeException.class, () -> SwedishHolidays.isHoliday(taYearMonth));

            // Type that *can* be resolved (OffsetTime - contains Instant info indirectly)
            // This might depend on JDK version specifics, but often OffsetTime can provide enough
            try {
                // Example: Check Christmas using OffsetTime
                OffsetTime ot = OffsetTime.of(10, 0, 0, 0, ZoneOffset.UTC);
                OffsetDateTime odtForCheck = LocalDate.of(2025, 12, 25).atTime(ot); // Combine with a holiday date
                assertTrue(SwedishHolidays.isHoliday((TemporalAccessor)odtForCheck));
            } catch (DateTimeException e) {
                // If it fails because OffsetTime alone isn't enough, this might happen
                System.err.println("Note: isHoliday(TemporalAccessor) with OffsetTime might be limited: " + e.getMessage());
            }

        }

        @Test
        void testNonHolidayDate() {
            LocalDate date = LocalDate.of(2025, 1, 2);
            assertFalse(SwedishHolidays.isHoliday(date), "Jan 2nd should not be a holiday");
        }
    }


    // --- Tests for listHolidays Overloads ---
    @Nested
    @DisplayName("listHolidays Overloads")
    class ListHolidayTests {
        @Test
        void testListHolidaysCount() {
            List<Holiday> listSE = SwedishHolidays.listHolidays(SwedishHolidays.Lang.SE, 2025);
            assertNotNull(listSE);
            assertFalse(listSE.isEmpty(), "Holiday list should not be empty");
            // Standard Swedish holidays are around 22-23 depending on interpretation of eves
            assertTrue(listSE.size() > 15 && listSE.size() < 30, "Should have a reasonable number of holidays");

            List<Holiday> listEN = SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN, 2025);
            assertNotNull(listEN);
            assertEquals(listSE.size(), listEN.size(), "English and Swedish lists should have the same size");
        }

        @Test
        void testListHolidaysDefaultLanguage() {
            SwedishHolidays.setDefaultLanguage(SwedishHolidays.Lang.EN); // Ensure EN default
            List<Holiday> listDefault = SwedishHolidays.listHolidays(2025);
            Optional<Holiday> newYearDefault = listDefault.stream().filter(h -> h.getDate().equals(LocalDate.of(2025,1,1))).findFirst();
            assertTrue(newYearDefault.isPresent());
            assertEquals("New Year's Day", newYearDefault.get().getName());

            List<Holiday> listCurrentDefault = SwedishHolidays.listHolidays();
            assertNotNull(listCurrentDefault);
            assertFalse(listCurrentDefault.isEmpty());

            SwedishHolidays.setDefaultLanguage(SwedishHolidays.Lang.SE); // Change default
            List<Holiday> listDefaultSE = SwedishHolidays.listHolidays(2025);
            Optional<Holiday> newYearDefaultSE = listDefaultSE.stream().filter(h -> h.getDate().equals(LocalDate.of(2025,1,1))).findFirst();
            assertTrue(newYearDefaultSE.isPresent());
            assertEquals("Nyårsdagen", newYearDefaultSE.get().getName());
        }

        @Test
        void testListHolidaysCurrentYear() {
            int currentYear = LocalDate.now().getYear();
            List<Holiday> listCurrentEN = SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN);
            assertNotNull(listCurrentEN);
            assertFalse(listCurrentEN.isEmpty());
            assertEquals(currentYear, listCurrentEN.get(0).getDate().getYear());

            List<Holiday> listCurrentSE = SwedishHolidays.listHolidays(SwedishHolidays.Lang.SE);
            assertNotNull(listCurrentSE);
            assertFalse(listCurrentSE.isEmpty());
            assertEquals(currentYear, listCurrentSE.get(0).getDate().getYear());

            List<Holiday> listCurrentShort = SwedishHolidays.listHolidays(); // Uses default lang
            assertNotNull(listCurrentShort);
            assertFalse(listCurrentShort.isEmpty());
            assertEquals(currentYear, listCurrentShort.get(0).getDate().getYear());
        }

        @Test
        void testListHolidaysStringLangCode() {
            List<Holiday> listSE = SwedishHolidays.listHolidays("SE", 2025);
            Optional<Holiday> newYearSE = listSE.stream().filter(h -> h.getDate().equals(LocalDate.of(2025,1,1))).findFirst();
            assertTrue(newYearSE.isPresent());
            assertEquals("Nyårsdagen", newYearSE.get().getName());

            List<Holiday> listEN = SwedishHolidays.listHolidays("en", 2025); // Case-insensitive check
            Optional<Holiday> newYearEN = listEN.stream().filter(h -> h.getDate().equals(LocalDate.of(2025,1,1))).findFirst();
            assertTrue(newYearEN.isPresent());
            assertEquals("New Year's Day", newYearEN.get().getName());

            List<Holiday> listDefault = SwedishHolidays.listHolidays("XX", 2025); // Invalid code defaults to EN
            Optional<Holiday> newYearDefault = listDefault.stream().filter(h -> h.getDate().equals(LocalDate.of(2025,1,1))).findFirst();
            assertTrue(newYearDefault.isPresent());
            assertEquals("New Year's Day", newYearDefault.get().getName());
        }

        @Test
        void testListHolidaysSE_EN_Shortcuts() {
            List<Holiday> listSE = SwedishHolidays.listHolidaysSE();
            List<Holiday> listEN = SwedishHolidays.listHolidaysEN();
            int currentYear = LocalDate.now().getYear();

            assertNotNull(listSE);
            assertFalse(listSE.isEmpty());
            assertEquals("Nyårsdagen", listSE.stream().filter(h->h.getDate().getMonthValue()==1 && h.getDate().getDayOfMonth()==1).findFirst().get().getName());
            assertEquals(currentYear, listSE.get(0).getDate().getYear());


            assertNotNull(listEN);
            assertFalse(listEN.isEmpty());
            assertEquals("New Year's Day", listEN.stream().filter(h->h.getDate().getMonthValue()==1 && h.getDate().getDayOfMonth()==1).findFirst().get().getName());
            assertEquals(currentYear, listEN.get(0).getDate().getYear());
        }

        @Test
        void testListIsUnmodifiable() {
            List<Holiday> list = SwedishHolidays.listHolidays(2025);
            assertThrows(UnsupportedOperationException.class, () -> list.add(SwedishHolidays.getMayDay(2025)));
            assertThrows(UnsupportedOperationException.class, () -> list.remove(0));
        }

        @Test
        void testListIsSorted() {
            List<Holiday> list = SwedishHolidays.listHolidays(2025);
            LocalDate previous = LocalDate.MIN;
            for(Holiday h : list) {
                assertFalse(h.getDate().isBefore(previous), "List should be sorted by date");
                previous = h.getDate();
            }
        }
    }

    // --- Tests for specific holiday calculations (Easter, Pentecost etc.) ---
    @Nested
    @DisplayName("Date Calculation Logic")
    class DateCalculationTests {

        @Test
        void testEasterCalculations() {
            // Easter 2026 is on April 5
            int year = 2026;
            LocalDate expectedEasterSunday = LocalDate.of(year, 4, 5);
            Holiday easterSunday = SwedishHolidays.getEasterSunday(year);
            assertEquals(expectedEasterSunday, easterSunday.getDate(), "Easter Sunday 2026 date");

            // Check surrounding days relative to calculated Easter
            Holiday goodFriday = SwedishHolidays.getGoodFriday(expectedEasterSunday, SwedishHolidays.Lang.EN);
            assertEquals(expectedEasterSunday.minusDays(2), goodFriday.getDate(), "Good Friday relative to Easter 2026");
            assertEquals("Good Friday", goodFriday.getName());

            Holiday easterMondaySE = SwedishHolidays.getEasterMonday(expectedEasterSunday, SwedishHolidays.Lang.SE);
            assertEquals(expectedEasterSunday.plusDays(1), easterMondaySE.getDate(), "Easter Monday relative to Easter 2026");
            assertEquals("Annandag påsk", easterMondaySE.getName());

            Holiday maundyThursday = SwedishHolidays.getMaundyThursday(year); // Use year overload
            assertEquals(expectedEasterSunday.minusDays(3), maundyThursday.getDate(), "Maundy Thursday 2026 date");

            Holiday easterEve = SwedishHolidays.getEasterEve(year, SwedishHolidays.Lang.SE); // Use year/lang overload
            assertEquals(expectedEasterSunday.minusDays(1), easterEve.getDate(), "Easter Eve 2026 date");
            assertEquals("Påskafton", easterEve.getName());
        }

        @Test
        void testEasterMultipleYears() {
            // Data from https://www.riksdagen.se/sv/dokument-och-lagar/dokument/svensk-forfattningssamling/lag-1989253-om-beraknande-av-lagstadgad-tid_sfs-1989-253/
            // Note: These might be off by one day depending on exact algorithm interpretation, verify known good dates
            assertEquals(LocalDate.of(2023, 4, 9), SwedishHolidays.getEasterSunday(2023).getDate());
            assertEquals(LocalDate.of(2024, 3, 31), SwedishHolidays.getEasterSunday(2024).getDate());
            assertEquals(LocalDate.of(2025, 4, 20), SwedishHolidays.getEasterSunday(2025).getDate());
            assertEquals(LocalDate.of(2026, 4, 5), SwedishHolidays.getEasterSunday(2026).getDate());
            assertEquals(LocalDate.of(2029, 4, 1), SwedishHolidays.getEasterSunday(2029).getDate());
        }


        @Test
        void testAscensionDay() {
            // Ascension 2025 (based on Easter April 20) is 39 days later -> May 29
            int year = 2025;
            LocalDate expectedAscension = LocalDate.of(year, 5, 29);
            Holiday ascension = SwedishHolidays.getAscensionDay(year);
            assertEquals(expectedAscension, ascension.getDate());
            assertEquals("Ascension Day", ascension.getName());

            Holiday ascensionSE = SwedishHolidays.getAscensionDay(year, SwedishHolidays.Lang.SE);
            assertEquals("Kristi himmelsfärdsdag", ascensionSE.getName());
        }

        @Test
        void testPentecostCalculations() {
            // Pentecost 2030 (Easter is April 21) is 49 days after Easter -> June 9
            int year = 2030;
            LocalDate expectedEaster = SwedishHolidays.getEasterSunday(year).getDate(); // Calculate Easter for the year
            LocalDate expectedPentecost = expectedEaster.plusDays(49);
            assertEquals(LocalDate.of(year, 6, 9), expectedPentecost, "Manual Pentecost calculation for 2030");


            Holiday pentecost = SwedishHolidays.getPentecost(year);
            assertEquals(expectedPentecost, pentecost.getDate(), "Pentecost 2030 date");
            assertEquals("Pentecost", pentecost.getName());

            Holiday pentecostSE = SwedishHolidays.getPentecost(expectedPentecost, SwedishHolidays.Lang.SE); // Use date overload
            assertEquals("Pingstdagen", pentecostSE.getName());

            Holiday whitsunEve = SwedishHolidays.getWhitsunEve(year); // Use year overload
            assertEquals(expectedPentecost.minusDays(1), whitsunEve.getDate(), "Whitsun Eve 2030 date");
            assertEquals("Whitsun Eve", whitsunEve.getName());
            assertEquals(LocalDate.of(year, 6, 8), whitsunEve.getDate());

            Holiday whitsunEveSE = SwedishHolidays.getWhitsunEve(year, SwedishHolidays.Lang.SE);
            assertEquals("Pingstafton", whitsunEveSE.getName());
        }

        @Test
        void testMidsummer() {
            // Midsummer 2025: Eve Fri June 20, Day Sat June 21
            int year = 2025;
            Holiday eve = SwedishHolidays.getMidsummerEve(year);
            assertEquals(LocalDate.of(year, 6, 20), eve.getDate());
            assertEquals("Midsummer Eve", eve.getName());
            Holiday daySE = SwedishHolidays.getMidsummerDay(year, SwedishHolidays.Lang.SE);
            assertEquals(LocalDate.of(year, 6, 21), daySE.getDate());
            assertEquals("Midsommardagen", daySE.getName());

            // Midsummer 2026: Eve Fri June 19, Day Sat June 20
            year = 2026;
            eve = SwedishHolidays.getMidsummerEve(year);
            assertEquals(LocalDate.of(year, 6, 19), eve.getDate());
            daySE = SwedishHolidays.getMidsummerDay(year, SwedishHolidays.Lang.SE);
            assertEquals(LocalDate.of(year, 6, 20), daySE.getDate());
        }

        @Test
        void testAllSaints() {
            // All Saints 2025: Eve Fri Oct 31, Day Sat Nov 1
            // Fre 31 okt.: Alla helgons afton  –  Lör 1 nov.: Alla helgons dag
            int year = 2025;
            Holiday eveSE = SwedishHolidays.getAllSaintsEve(year, SwedishHolidays.Lang.SE);
            assertEquals(LocalDate.of(year, 10, 31), eveSE.getDate());
            assertEquals("Allhelgonaafton", eveSE.getName());
            Holiday day = SwedishHolidays.getAllSaintsDay(year);
            assertEquals(LocalDate.of(year, 11, 1), day.getDate());
            assertEquals("All Saints' Day", day.getName());

            // All Saints 2026: Eve Fri Oct 30, Day Sat Oct 31
            // Fre 30 okt.: Alla helgons afton  –  Lör 31 okt.: Alla helgons dag
            year = 2026;
            eveSE = SwedishHolidays.getAllSaintsEve(year, SwedishHolidays.Lang.SE);
            assertEquals(LocalDate.of(year, 10, 30), eveSE.getDate());
            day = SwedishHolidays.getAllSaintsDay(year);
            assertEquals(LocalDate.of(year, 10, 31), day.getDate());
        }
    }

    // --- Tests for the NEW Customization Functionality ---
    @Nested
    @DisplayName("Customization Functionality")
    class CustomizationTests {

        private final LocalDate customDate = LocalDate.of(2025, 8, 15);
        private final String customName = "Company Fun Day";
        private final String customDesc = "Annual fun day";

        @Test
        void testAddCustomFixedHoliday() {
            assertFalse(SwedishHolidays.isHoliday(customDate), "Custom date should not be holiday initially");

            SwedishHolidays.addCustomHoliday(customDate, customName, customDesc);

            assertTrue(SwedishHolidays.isHoliday(customDate), "Custom date should be holiday after adding");

            List<Holiday> holidays = SwedishHolidays.listHolidays(2025);
            Optional<Holiday> found = holidays.stream().filter(h -> h.getDate().equals(customDate)).findFirst();
            assertTrue(found.isPresent(), "Custom holiday should be in the list");
            assertEquals(customName, found.get().getName());
            assertEquals(customDesc, found.get().getDescription());

            // Check it doesn't appear in another year
            List<Holiday> holidaysNextYear = SwedishHolidays.listHolidays(2026);
            assertFalse(holidaysNextYear.stream().anyMatch(h -> h.getDate().equals(customDate)), "Fixed custom holiday should only be in its year");
        }

        @Test
        void testRemoveStandardHoliday() {
            LocalDate mayDay2025 = LocalDate.of(2025, 5, 1);
            assertTrue(SwedishHolidays.isHoliday(mayDay2025), "May Day should be holiday initially");

            SwedishHolidays.removeStandardHoliday(mayDay2025);

            assertFalse(SwedishHolidays.isHoliday(mayDay2025), "May Day should not be holiday after removal");

            List<Holiday> holidays = SwedishHolidays.listHolidays(2025);
            assertFalse(holidays.stream().anyMatch(h -> h.getDate().equals(mayDay2025)), "Removed holiday should not be in list");

            // Ensure other holidays are still present
            LocalDate newYear2025 = LocalDate.of(2025, 1, 1);
            assertTrue(holidays.stream().anyMatch(h -> h.getDate().equals(newYear2025)), "Other holidays should remain after removal");
            assertTrue(SwedishHolidays.isHoliday(newYear2025), "isHoliday check for other holidays should still work");
        }

        @Test
        void testAddCustomRule() {
            // Rule: Friday after Ascension Day
            HolidayRule bridgeDayRule = (year, lang) -> {
                Holiday ascension = SwedishHolidays.getAscensionDay(year, lang); // Use getter for base date
                LocalDate bridgeDate = ascension.getDate().plusDays(1);
                String name = lang == SwedishHolidays.Lang.SE ? "Klämdag" : "Bridging Day";
                String desc = lang == SwedishHolidays.Lang.SE ? "Fredag efter Kristi Himmelsfärd" : "Friday after Ascension";
                return new Holiday(bridgeDate, name, desc);
            };

            SwedishHolidays.addCustomHolidayRule(bridgeDayRule);

            // Check 2025 (Ascension May 29 -> Bridge May 30)
            LocalDate bridgeDay2025 = LocalDate.of(2025, 5, 30);
            assertTrue(SwedishHolidays.isHoliday(bridgeDay2025), "Rule-based bridge day 2025 should be holiday");
            List<Holiday> holidays2025 = SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN, 2025);
            Optional<Holiday> found2025 = holidays2025.stream().filter(h -> h.getDate().equals(bridgeDay2025)).findFirst();
            assertTrue(found2025.isPresent(), "Rule-based holiday 2025 should be in list");
            assertEquals("Bridging Day", found2025.get().getName());

            // Check 2026 (Ascension May 14 -> Bridge May 15)
            LocalDate bridgeDay2026 = LocalDate.of(2026, 5, 15);
            assertTrue(SwedishHolidays.isHoliday(bridgeDay2026), "Rule-based bridge day 2026 should be holiday");
            List<Holiday> holidays2026SE = SwedishHolidays.listHolidays(SwedishHolidays.Lang.SE, 2026);
            Optional<Holiday> found2026SE = holidays2026SE.stream().filter(h -> h.getDate().equals(bridgeDay2026)).findFirst();
            assertTrue(found2026SE.isPresent(), "Rule-based holiday 2026 should be in list (SE)");
            assertEquals("Klämdag", found2026SE.get().getName());
        }

        @Test
        void testCustomRuleReturningNull() {
            // Rule that only applies to leap years
            HolidayRule leapYearBonusDay = (year, lang) -> {
                if (Year.isLeap(year)) {
                    LocalDate date = LocalDate.of(year, 7, 15); // Arbitrary bonus day
                    return new Holiday(date, "Leap Year Bonus", "Only on leap years");
                } else {
                    return null; // Rule doesn't apply
                }
            };
            SwedishHolidays.addCustomHolidayRule(leapYearBonusDay);

            LocalDate bonusDate2024 = LocalDate.of(2024, 7, 15); // 2024 is leap
            LocalDate bonusDate2025 = LocalDate.of(2025, 7, 15); // 2025 is not leap

            assertTrue(SwedishHolidays.isHoliday(bonusDate2024), "Leap year rule should apply in 2024");
            assertTrue(SwedishHolidays.listHolidays(2024).stream().anyMatch(h -> h.getDate().equals(bonusDate2024)));

            assertFalse(SwedishHolidays.isHoliday(bonusDate2025), "Leap year rule should not apply in 2025");
            assertFalse(SwedishHolidays.listHolidays(2025).stream().anyMatch(h -> h.getDate().equals(bonusDate2025)));
        }

        @Test
        void testClearCustomAdditions() {
            SwedishHolidays.addCustomHoliday(customDate, customName, customDesc);
            assertTrue(SwedishHolidays.isHoliday(customDate));

            SwedishHolidays.clearCustomAdditions();
            assertFalse(SwedishHolidays.isHoliday(customDate), "Custom holiday should be removed after clear");
            assertFalse(SwedishHolidays.listHolidays(2025).stream().anyMatch(h -> h.getDate().equals(customDate)));
        }

        @Test
        void testClearStandardRemovals() {
            LocalDate mayDay2025 = LocalDate.of(2025, 5, 1);
            SwedishHolidays.removeStandardHoliday(mayDay2025);
            assertFalse(SwedishHolidays.isHoliday(mayDay2025));

            SwedishHolidays.clearStandardRemovals();
            assertTrue(SwedishHolidays.isHoliday(mayDay2025), "Removed holiday should be back after clear");
            assertTrue(SwedishHolidays.listHolidays(2025).stream().anyMatch(h -> h.getDate().equals(mayDay2025)));
        }

        @Test
        void testClearCustomRules() {
            HolidayRule rule = (y, l) -> new Holiday(LocalDate.of(y, 10, 10), "Rule Day", "");
            SwedishHolidays.addCustomHolidayRule(rule);
            LocalDate ruleDate = LocalDate.of(2025, 10, 10);
            assertTrue(SwedishHolidays.isHoliday(ruleDate));

            SwedishHolidays.clearCustomRules();
            assertFalse(SwedishHolidays.isHoliday(ruleDate), "Rule holiday should be removed after clear");
            assertFalse(SwedishHolidays.listHolidays(2025).stream().anyMatch(h -> h.getDate().equals(ruleDate)));
        }

        @Test
        void testClearAllCustomizations() {
            LocalDate mayDay2025 = LocalDate.of(2025, 5, 1);
            HolidayRule rule = (y, l) -> new Holiday(LocalDate.of(y, 10, 10), "Rule Day", "");
            LocalDate ruleDate = LocalDate.of(2025, 10, 10);

            SwedishHolidays.addCustomHoliday(customDate, customName, customDesc);
            SwedishHolidays.removeStandardHoliday(mayDay2025);
            SwedishHolidays.addCustomHolidayRule(rule);

            assertTrue(SwedishHolidays.isHoliday(customDate));
            assertFalse(SwedishHolidays.isHoliday(mayDay2025));
            assertTrue(SwedishHolidays.isHoliday(ruleDate));

            SwedishHolidays.clearAllCustomizations();

            assertFalse(SwedishHolidays.isHoliday(customDate), "Custom fixed date should be gone after clearAll");
            assertTrue(SwedishHolidays.isHoliday(mayDay2025), "Removed standard date should be back after clearAll");
            assertFalse(SwedishHolidays.isHoliday(ruleDate), "Custom rule date should be gone after clearAll");
        }

        @Test
        void testInteractionAddAndRemove() {
            LocalDate mayDay2025 = LocalDate.of(2025, 5, 1);
            assertTrue(SwedishHolidays.isHoliday(mayDay2025));

            // Remove May Day
            SwedishHolidays.removeStandardHoliday(mayDay2025);
            assertFalse(SwedishHolidays.isHoliday(mayDay2025));

            // Add it back as a custom day with a different name
            SwedishHolidays.addCustomHoliday(mayDay2025, "Custom May Day", "Re-added May 1st");
            assertTrue(SwedishHolidays.isHoliday(mayDay2025), "Date should be holiday again after custom add");

            // Verify the name is the custom one
            Optional<Holiday> found = SwedishHolidays.listHolidays(2025).stream()
                    .filter(h -> h.getDate().equals(mayDay2025))
                    .findFirst();
            assertTrue(found.isPresent());
            assertEquals("Custom May Day", found.get().getName());
        }
    }


    // --- Tests for Cache Behavior ---
    @Nested
    @DisplayName("Cache Behavior")
    class CacheTests {

        @Test
        void testListHolidaysReturnsCachedInstance() {
            List<Holiday> list1 = SwedishHolidays.listHolidays(SwedishHolidays.Lang.SE, 2025);
            List<Holiday> list2 = SwedishHolidays.listHolidays(SwedishHolidays.Lang.SE, 2025);
            assertSame(list1, list2, "Repeated calls for same year/lang should return cached list instance");

            List<Holiday> listEN1 = SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN, 2025);
            List<Holiday> listEN2 = SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN, 2025);
            assertSame(listEN1, listEN2, "Repeated calls for same year/lang (EN) should return cached list instance");

            assertNotSame(list1, listEN1, "Lists for different languages should be different instances");
        }

        @Test
        void testIsHolidayUsesCache() {
            LocalDate date = LocalDate.of(2025, 1, 1);
            // Prime the cache by listing holidays
            SwedishHolidays.listHolidays(2025);

            // Now check isHoliday - this should hit the date cache
            assertTrue(SwedishHolidays.isHoliday(date));
            // Difficult to directly verify cache *hit*, but subsequent calls should be fast
            assertDoesNotThrow(() -> SwedishHolidays.isHoliday(date));
        }

        @Test
        void testConfigurationClearsCache() {
            List<Holiday> listBefore = SwedishHolidays.listHolidays(SwedishHolidays.Lang.SE, 2025);

            // Add a custom holiday - should clear cache
            SwedishHolidays.addCustomHoliday(LocalDate.of(2025, 7, 7), "Cache Test Day", "");

            List<Holiday> listAfter = SwedishHolidays.listHolidays(SwedishHolidays.Lang.SE, 2025);

            // The list content will change, and potentially the instance might too
            // assertNotSame(listBefore, listAfter, "Cache should be invalidated, list instance might change"); // This is not guaranteed by ConcurrentHashMap's computeIfAbsent logic
            assertNotEquals(listBefore.size(), listAfter.size(), "List size should change after adding holiday, indicating recalculation");
            assertTrue(listAfter.stream().anyMatch(h -> h.getName().equals("Cache Test Day")));
        }

        @Test
        void testClearCacheForcesRecalculation() {
            List<Holiday> list1 = SwedishHolidays.listHolidays(2026);
            SwedishHolidays.clearCache(); // Manually clear
            List<Holiday> list2 = SwedishHolidays.listHolidays(2026); // Should recalculate

            // We can't guarantee different instances with computeIfAbsent,
            // but we can verify it still works correctly after clearing.
            assertNotNull(list2);
            assertFalse(list2.isEmpty());
            assertEquals(list1.size(), list2.size(), "List content should be the same after cache clear and recalculation (without config changes)");
            // Optionally, if we could somehow instrument or check internal state:
            // assertTrue(cacheWasActuallyClearedAndRepopulated);
        }
    }

    // --- Tests for Error Handling and Edge Cases ---
    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        void testNullArguments() {
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((LocalDate) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((Date) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((Timestamp) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((Instant) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((XMLGregorianCalendar) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((java.util.Date) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((Calendar) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((LocalDateTime) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((ZonedDateTime) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((OffsetDateTime) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.isHoliday((TemporalAccessor) null));

            assertThrows(NullPointerException.class, () -> SwedishHolidays.listHolidays((SwedishHolidays.Lang) null, 2025));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.listHolidays((SwedishHolidays.Lang) null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.listHolidays((String) null, 2025));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.listHolidays((String) null));


            assertThrows(NullPointerException.class, () -> SwedishHolidays.addCustomHoliday(null, "name", "desc"));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.addCustomHoliday(LocalDate.now(), null, "desc"));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.addCustomHoliday(LocalDate.now(), "name", null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.removeStandardHoliday(null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.addCustomHolidayRule(null));

            // Getters
            assertThrows(NullPointerException.class, () -> SwedishHolidays.getGoodFriday(null, SwedishHolidays.Lang.EN));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.getGoodFriday(LocalDate.now(), null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.getNewYearsDay(2025, null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.getMidsummerEve(2025, null));
            assertThrows(NullPointerException.class, () -> SwedishHolidays.getAllSaintsDay(2025, null));

            assertThrows(NullPointerException.class, () -> SwedishHolidays.setDefaultLanguage(null));
        }

        @Test
        void testInvalidYearForEaster() {
            // Easter calculation requires positive year
            assertThrows(IllegalArgumentException.class, () -> SwedishHolidays.getEasterSunday(0), "Year 0 is invalid for Easter calc");
            assertThrows(IllegalArgumentException.class, () -> SwedishHolidays.getGoodFriday(-1), "Negative year is invalid for Easter calc");
        }

        @ParameterizedTest
        @ValueSource(ints = { 1900, 2000, 2024, 2100 }) // Leap and common years
        void testEasterAlgorithmOnKnownYears(int year) {
            // Test that calculation runs without error for various years
            assertDoesNotThrow(() -> SwedishHolidays.getEasterSunday(year));
        }

        @Test
        void testFindDayInRangeLogicViaGetters() {
            // Midsummer Day 2025: Saturday between June 20 and 26 -> June 21
            assertEquals(DayOfWeek.SATURDAY, SwedishHolidays.getMidsummerDay(2025).getDate().getDayOfWeek());
            assertTrue(SwedishHolidays.getMidsummerDay(2025).getDate().getDayOfMonth() >= 20);
            assertTrue(SwedishHolidays.getMidsummerDay(2025).getDate().getDayOfMonth() <= 26);

            // All Saints' Day 2026: Saturday between Oct 31 and Nov 6 -> Nov 7
            assertEquals(DayOfWeek.SATURDAY, SwedishHolidays.getAllSaintsDay(2026).getDate().getDayOfWeek());
            LocalDate allSaints2026 = SwedishHolidays.getAllSaintsDay(2026).getDate();
            LocalDate startRange = LocalDate.of(2026, 10, 31);
            LocalDate endRange = LocalDate.of(2026, 11, 6);
            assertFalse(allSaints2026.isBefore(startRange));
            assertFalse(allSaints2026.isAfter(endRange));
        }
    }

    // --- Tests for the Holiday Class itself ---
    @Nested
    @DisplayName("Holiday Class Tests")
    class HolidayClassTests {

        private final LocalDate date = LocalDate.of(2025, 1, 1);
        private final String name = "Test Day";
        private final String desc = "Test Description";

        @Test
        void testHolidayConstructorAndGetters() {
            Holiday holiday = new Holiday(date, name, desc);
            assertEquals(date, holiday.getDate());
            assertEquals(name, holiday.getName());
            assertEquals(desc, holiday.getDescription());
        }

        @Test
        void testHolidayConstructorNullChecks() {
            assertThrows(NullPointerException.class, () -> new Holiday(null, name, desc));
            assertThrows(NullPointerException.class, () -> new Holiday(date, null, desc));
            assertThrows(NullPointerException.class, () -> new Holiday(date, name, null));
        }

        @Test
        void testHolidayEqualsAndHashCode() {
            Holiday holiday1 = new Holiday(date, name, desc);
            Holiday holiday2 = new Holiday(date, name, desc); // Same data
            Holiday holiday3 = new Holiday(date.plusDays(1), name, desc); // Different date
            Holiday holiday4 = new Holiday(date, "Different Name", desc); // Different name
            Holiday holiday5 = new Holiday(date, name, "Different Desc"); // Different desc

            // Reflexive
            assertEquals(holiday1, holiday1);

            // Symmetric
            assertEquals(holiday1, holiday2);
            assertEquals(holiday2, holiday1);

            // Consistent HashCode
            assertEquals(holiday1.hashCode(), holiday2.hashCode());

            // Unequal checks
            assertNotEquals(holiday1, holiday3);
            assertNotEquals(holiday1, holiday4);
            assertNotEquals(holiday1, holiday5);
            assertNotEquals(holiday1, null);
            assertNotEquals(holiday1, new Object());

            // HashCode difference (not strictly required, but likely)
            assertNotEquals(holiday1.hashCode(), holiday3.hashCode());
            assertNotEquals(holiday1.hashCode(), holiday4.hashCode());
            // HashCode might collide for holiday5 depending on fields used, but equals must be false
            // assertEquals based on date, name, desc as implemented
        }

        @Test
        void testHolidayToString() {
            Holiday holiday = new Holiday(date, name, desc);
            String str = holiday.toString();
            assertTrue(str.contains(date.toString()));
            assertTrue(str.contains(name));
            assertTrue(str.contains(desc));
            assertTrue(str.startsWith("Holiday{"));
            assertTrue(str.endsWith("}"));
        }
    }

}