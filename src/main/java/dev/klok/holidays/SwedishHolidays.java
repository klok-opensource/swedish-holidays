package dev.klok.holidays;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.sql.Date;
import java.sql.Timestamp;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Utility class for checking Swedish public holidays across various date types and retrieving holiday lists.
 */
public final class SwedishHolidays {

    public enum Lang { SE, EN }

    private static final ZoneId SWEDEN = ZoneId.of("Europe/Stockholm");
    private static Lang defaultLang = Lang.EN;

    // Nested cache for holidays per year and language
    private static final ConcurrentMap<Integer, ConcurrentMap<Lang, List<Holiday>>> holidayListCachePerLang = new ConcurrentHashMap<>();
    // Date cache per year for quick holiday lookup
    private static final ConcurrentMap<Integer, Set<LocalDate>> holidayDateCache = new ConcurrentHashMap<>();

    // Prevent instantiation
    private SwedishHolidays() {}

    static {
        // Pre-populate the cache with holidays for the current year
        int currentYear = LocalDate.now(SWEDEN).getYear();
        listHolidays(Lang.SE, currentYear);
        listHolidays(Lang.EN, currentYear);

        // Pre-populate the cache with holidays for the next year
        int nextYear = currentYear + 1;
        listHolidays(Lang.SE, nextYear);
        listHolidays(Lang.EN, nextYear);

        // Pre-populate the cache with holidays for the second next year
        int secondNextYear = currentYear + 2;
        listHolidays(Lang.SE, secondNextYear);
        listHolidays(Lang.EN, secondNextYear);

        // Pre-populate the cache with holidays for the previous year
        int previousYear = currentYear - 1;
        listHolidays(Lang.SE, previousYear);
        listHolidays(Lang.EN, previousYear);
    }

    //
    // IS HOLIDAY CHECK METHODS
    //

    /**
     * Checks if the given LocalDate is a Swedish public holiday (default language: English).
     * @param date the LocalDate to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(LocalDate date) {
        Set<LocalDate> dates = holidayDateCache.get(date.getYear());
        if (dates != null) {
            return dates.contains(date);
        }
        // Trigger population using default language
        listHolidays(defaultLang, date.getYear());
        dates = holidayDateCache.get(date.getYear());
        return dates != null && dates.contains(date);
    }

    /**
     * Checks if the given java.sql.Date is a Swedish public holiday.
     * @param sqlDate the SQL Date to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(Date sqlDate) {
        return isHoliday(sqlDate.toLocalDate());
    }

    /**
     * Checks if the given Timestamp is a Swedish public holiday.
     * @param timestamp the Timestamp to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(Timestamp timestamp) {
        LocalDate d = timestamp.toInstant().atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given Instant is a Swedish public holiday.
     * @param instant the Instant to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(Instant instant) {
        LocalDate d = instant.atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given XMLGregorianCalendar is a Swedish public holiday.
     * @param xmlCal the XMLGregorianCalendar to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(XMLGregorianCalendar xmlCal) {
        GregorianCalendar cal = xmlCal.toGregorianCalendar(TimeZone.getTimeZone(SWEDEN), null, null);
        LocalDate date = cal.toInstant().atZone(SWEDEN).toLocalDate();
        return isHoliday(date);
    }

    /**
     * Checks if the given java.util.Date is a Swedish public holiday.
     * @param date the Date to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(java.util.Date date) {
        LocalDate d = date.toInstant().atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given Calendar is a Swedish public holiday.
     * @param calendar the Calendar to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(Calendar calendar) {
        LocalDate d = calendar.toInstant().atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given LocalDateTime is a Swedish public holiday.
     * @param dateTime the LocalDateTime to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(LocalDateTime dateTime) {
        LocalDate d = dateTime.atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given ZonedDateTime is a Swedish public holiday.
     * @param dateTime the ZonedDateTime to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(ZonedDateTime dateTime) {
        LocalDate d = dateTime.withZoneSameInstant(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given OffsetDateTime is a Swedish public holiday.
     * @param dateTime the OffsetDateTime to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(OffsetDateTime dateTime) {
        LocalDate d = dateTime.atZoneSameInstant(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given TemporalAccessor is a Swedish public holiday.
     * Tries to parse as LocalDate first, then Instant.
     * @param temporal the TemporalAccessor to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(TemporalAccessor temporal) {
        try {
            LocalDate date = LocalDate.from(temporal);
            return isHoliday(date);
        } catch (DateTimeException e) {
            Instant instant = Instant.from(temporal);
            return isHoliday(instant);
        }
    }

    /**
     * Checks if today (Stockholm timezone) is a Swedish public holiday.
     * @return true if today is a holiday, false otherwise
     */
    public static boolean isHolidayToday() {
        LocalDate today = LocalDate.now(SWEDEN);
        return isHoliday(today);
    }

    //
    // LIST HOLIDAY METHODS
    //

    /**
     * Returns a list of holidays for the current year in the specified language.
     * @param lang the language (SE or EN)
     * @return list of holidays
     */
    public static List<Holiday> listHolidays(Lang lang) {
        int year = LocalDate.now(SWEDEN).getYear();
        return listHolidays(lang, year);
    }

    /**
     * Returns a list of holidays for the current year, using language code string.
     * @param langCode "SE" or "EN"
     * @return list of holidays
     */
    public static List<Holiday> listHolidays(String langCode) {
        Lang l = "SE".equalsIgnoreCase(langCode) ? Lang.SE : Lang.EN;
        return listHolidays(l);
    }

    /**
     * Returns a list of holidays for the current year in Swedish.
     * @return list of holidays in Swedish
     */
    public static List<Holiday> listHolidaysSE() {
        return listHolidays(Lang.SE);
    }

    /**
     * Returns a list of holidays for the current year in English.
     * @return list of holidays in English
     */
    public static List<Holiday> listHolidaysEN() {
        return listHolidays(Lang.EN);
    }

    /**
     * Returns a list of holidays for the current year in the default language.
     * @return list of holidays
     */
    public static List<Holiday> listHolidays() {
        return listHolidays(defaultLang);
    }

    /**
     * Returns a list of holidays for the specified year in the default language.
     * @param year the year
     * @return list of holidays
     */
    public static List<Holiday> listHolidays(int year) {
        return listHolidays(defaultLang, year);
    }

    /**
     * Returns a list of holidays for the specified year in the specified language.
     * @param lang the language (SE or EN)
     * @param year the year
     * @return list of holidays
     */
    public static List<Holiday> listHolidays(Lang lang, int year) {
        ConcurrentMap<Lang, List<Holiday>> perLangMap =
            holidayListCachePerLang.computeIfAbsent(year, y -> new ConcurrentHashMap<>());
        return perLangMap.computeIfAbsent(lang, l -> {
            List<Holiday> holidays = calculateHolidaysForYear(year, l);
            // Populate date-only cache
            holidayDateCache.putIfAbsent(year, holidays.stream().map(Holiday::getDate).collect(Collectors.toSet()));
            return Collections.unmodifiableList(holidays);
        });
    }

    //
    // GET SINGLE HOLIDAY METHODS
    //

    /**
     * Returns New Year's Day for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return New Year's Day holiday
     */
    public static Holiday getNewYearsDay(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 1, 1),
            lang==Lang.SE?"Nyårsdagen":"New Year's Day",
            lang==Lang.SE?"Fast datum, 1 januari":"Fixed date, January 1");
    }

    /**
     * Returns Epiphany Eve for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Epiphany Eve holiday
     */
    public static Holiday getEpiphanyEve(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 1, 5),
            lang==Lang.SE?"Trettondagsafton":"Epiphany Eve",
            lang==Lang.SE?"Fast datum, 5 januari":"Fixed date, January 5");
    }

    /**
     * Returns Epiphany for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Epiphany holiday
     */
    public static Holiday getEpiphany(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 1, 6),
            lang==Lang.SE?"Trettondedag jul":"Epiphany",
            lang==Lang.SE?"Fast datum, 6 januari":"Fixed date, January 6");
    }

    /**
     * Returns Walpurgis Eve for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Walpurgis Eve holiday
     */
    public static Holiday getWalpurgisEve(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 4, 30),
            lang==Lang.SE?"Valborgsmässoafton":"Walpurgis Eve",
            lang==Lang.SE?"Fast datum, 30 april":"Fixed date, April 30");
    }

    /**
     * Returns May Day for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return May Day holiday
     */
    public static Holiday getMayDay(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 5, 1),
            lang==Lang.SE?"Första maj":"May Day",
            lang==Lang.SE?"Fast datum, 1 maj":"Fixed date, May 1");
    }

    /**
     * Returns National Day of Sweden for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return National Day holiday
     */
    public static Holiday getNationalDay(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 6, 6),
            lang==Lang.SE?"Sveriges nationaldag":"National Day of Sweden",
            lang==Lang.SE?"Fast datum, 6 juni":"Fixed date, June 6");
    }

    /**
     * Returns Christmas Eve for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Christmas Eve holiday
     */
    public static Holiday getChristmasEve(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 12, 24),
            lang==Lang.SE?"Julafton":"Christmas Eve",
            lang==Lang.SE?"Fast datum, 24 december":"Fixed date, December 24");
    }

    /**
     * Returns Christmas Day for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Christmas Day holiday
     */
    public static Holiday getChristmasDay(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 12, 25),
            lang==Lang.SE?"Juldagen":"Christmas Day",
            lang==Lang.SE?"Fast datum, 25 december":"Fixed date, December 25");
    }

    /**
     * Returns St. Stephen's Day for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return St. Stephen's Day holiday
     */
    public static Holiday getStStephensDay(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 12, 26),
            lang==Lang.SE?"Annandag jul":"St. Stephen's Day",
            lang==Lang.SE?"Fast datum, 26 december":"Fixed date, December 26");
    }

    /**
     * Returns New Year's Eve for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return New Year's Eve holiday
     */
    public static Holiday getNewYearsEve(int year, Lang lang) {
        return new Holiday(LocalDate.of(year, 12, 31),
            lang==Lang.SE?"Nyårsafton":"New Year's Eve",
            lang==Lang.SE?"Fast datum, 31 december":"Fixed date, December 31");
    }

    /**
     * Returns Maundy Thursday for the given year.
     * @param easter the Easter Sunday date
     * @param lang the language (SE or EN)
     * @return Maundy Thursday holiday
     */
    public static Holiday getMaundyThursday(LocalDate easter, Lang lang) {
        return new Holiday(easter.minusDays(3),
            lang==Lang.SE?"Skärtorsdagen":"Maundy Thursday",
            lang==Lang.SE?"Rörligt datum, torsdagen före Påskdagen":"Movable date, Thursday before Easter Sunday");
    }

    /**
     * Returns Maundy Thursday for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Maundy Thursday holiday
     */
    public static Holiday getMaundyThursday(int year, Lang lang) {
        return getMaundyThursday(calculateEaster(year), lang);
    }
    /**
     * Returns Maundy Thursday for the given year (default language).
     * @param year the year
     * @return Maundy Thursday holiday
     */
    public static Holiday getMaundyThursday(int year) {
        return getMaundyThursday(year, defaultLang);
    }

    /**
     * Returns Good Friday for the given year.
     * @param easter the Easter Sunday date
     * @param lang the language (SE or EN)
     * @return Good Friday holiday
     */
    public static Holiday getGoodFriday(LocalDate easter, Lang lang) {
        return new Holiday(easter.minusDays(2),
            lang==Lang.SE?"Långfredagen":"Good Friday",
            lang==Lang.SE?"Rörligt datum, fredagen före Påskdagen":"Movable date, Friday before Easter Sunday");
    }

    /**
     * Returns Good Friday for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Good Friday holiday
     */
    public static Holiday getGoodFriday(int year, Lang lang) {
        return getGoodFriday(calculateEaster(year), lang);
    }
    /**
     * Returns Good Friday for the given year (default language).
     * @param year the year
     * @return Good Friday holiday
     */
    public static Holiday getGoodFriday(int year) {
        return getGoodFriday(year, defaultLang);
    }

    /**
     * Returns Easter Eve for the given year.
     * @param easter the Easter Sunday date
     * @param lang the language (SE or EN)
     * @return Easter Eve holiday
     */
    public static Holiday getEasterEve(LocalDate easter, Lang lang) {
        return new Holiday(easter.minusDays(1),
            lang==Lang.SE?"Påskafton":"Easter Eve",
            lang==Lang.SE?"Rörligt datum, lördagen före Påskdagen":"Movable date, Saturday before Easter Sunday");
    }

    /**
     * Returns Easter Eve for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Easter Eve holiday
     */
    public static Holiday getEasterEve(int year, Lang lang) {
        return getEasterEve(calculateEaster(year), lang);
    }
    /**
     * Returns Easter Eve for the given year (default language).
     * @param year the year
     * @return Easter Eve holiday
     */
    public static Holiday getEasterEve(int year) {
        return getEasterEve(year, defaultLang);
    }

    /**
     * Returns Easter Sunday for the given year.
     * @param easter the Easter Sunday date
     * @param lang the language (SE or EN)
     * @return Easter Sunday holiday
     */
    public static Holiday getEasterSunday(LocalDate easter, Lang lang) {
        return new Holiday(easter,
            lang==Lang.SE?"Påskdagen":"Easter Sunday",
            lang==Lang.SE?"Rörligt datum, första söndagen efter ecklesiastisk fullmåne, efter vårdagjämningen":"Movable date, first Sunday after the ecclesiastical full moon following the vernal equinox");
    }

    /**
     * Returns Easter Sunday for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Easter Sunday holiday
     */
    public static Holiday getEasterSunday(int year, Lang lang) {
        return getEasterSunday(calculateEaster(year), lang);
    }
    /**
     * Returns Easter Sunday for the given year (default language).
     * @param year the year
     * @return Easter Sunday holiday
     */
    public static Holiday getEasterSunday(int year) {
        return getEasterSunday(year, defaultLang);
    }

    /**
     * Returns Easter Monday for the given year.
     * @param easter the Easter Sunday date
     * @param lang the language (SE or EN)
     * @return Easter Monday holiday
     */
    public static Holiday getEasterMonday(LocalDate easter, Lang lang) {
        return new Holiday(easter.plusDays(1),
            lang==Lang.SE?"Annandag påsk":"Easter Monday",
            lang==Lang.SE?"Rörligt datum, dagen efter påskdagen (d.v.s. en måndag)":"Movable date, the day after Easter Sunday (Monday)");
    }

    /**
     * Returns Easter Monday for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Easter Monday holiday
     */
    public static Holiday getEasterMonday(int year, Lang lang) {
        return getEasterMonday(calculateEaster(year), lang);
    }
    /**
     * Returns Easter Monday for the given year (default language).
     * @param year the year
     * @return Easter Monday holiday
     */
    public static Holiday getEasterMonday(int year) {
        return getEasterMonday(year, defaultLang);
    }

    /**
     * Returns Ascension Day for the given year.
     * @param easter the Easter Sunday date
     * @param lang the language (SE or EN)
     * @return Ascension Day holiday
     */
    public static Holiday getAscensionDay(LocalDate easter, Lang lang) {
        return new Holiday(easter.plusDays(39),
            lang==Lang.SE?"Kristi himmelsfärdsdag":"Ascension Day",
            lang==Lang.SE?"Rörligt datum, sjätte torsdagen efter påskdagen":"Movable date, the sixth Thursday after Easter Sunday");
    }

    /**
     * Returns Ascension Day for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Ascension Day holiday
     */
    public static Holiday getAscensionDay(int year, Lang lang) {
        return getAscensionDay(calculateEaster(year), lang);
    }
    /**
     * Returns Ascension Day for the given year (default language).
     * @param year the year
     * @return Ascension Day holiday
     */
    public static Holiday getAscensionDay(int year) {
        return getAscensionDay(year, defaultLang);
    }

    /**
     * Returns Whitsun Eve for the given year.
     * @param pentecost the Pentecost date
     * @param lang the language (SE or EN)
     * @return Whitsun Eve holiday
     */
    public static Holiday getWhitsunEve(LocalDate pentecost, Lang lang) {
        return new Holiday(pentecost.minusDays(1),
            lang==Lang.SE?"Pingstafton":"Whitsun Eve",
            lang==Lang.SE?"Rörligt datum, dagen före pingstdagen (d.v.s. en lördag)":"Movable date, the day before Pentecost (Saturday)");
    }

    /**
     * Returns Whitsun Eve for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Whitsun Eve holiday
     */
    public static Holiday getWhitsunEve(int year, Lang lang) {
        LocalDate p = calculateEaster(year).plusDays(49);
        return getWhitsunEve(p, lang);
    }
    /**
     * Returns Whitsun Eve for the given year (default language).
     * @param year the year
     * @return Whitsun Eve holiday
     */
    public static Holiday getWhitsunEve(int year) {
        return getWhitsunEve(year, defaultLang);
    }

    /**
     * Returns Pentecost for the given year.
     * @param pentecost the Pentecost date
     * @param lang the language (SE or EN)
     * @return Pentecost holiday
     */
    public static Holiday getPentecost(LocalDate pentecost, Lang lang) {
        return new Holiday(pentecost,
            lang==Lang.SE?"Pingstdagen":"Pentecost",
            lang==Lang.SE?"Rörligt datum, sjunde söndagen efter påskdagen":"Movable date, seventh Sunday after Easter Sunday");
    }

    /**
     * Returns Pentecost for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Pentecost holiday
     */
    public static Holiday getPentecost(int year, Lang lang) {
        LocalDate p = calculateEaster(year).plusDays(49);
        return getPentecost(p, lang);
    }
    /**
     * Returns Pentecost for the given year (default language).
     * @param year the year
     * @return Pentecost holiday
     */
    public static Holiday getPentecost(int year) {
        return getPentecost(year, defaultLang);
    }

    /**
     * Returns Midsummer Eve for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Midsummer Eve holiday
     */
    public static Holiday getMidsummerEve(int year, Lang lang) {
        return new Holiday(findDayInRange(year, Month.JUNE, 19, 25, DayOfWeek.FRIDAY),
            lang==Lang.SE?"Midsommarafton":"Midsummer Eve",
            lang==Lang.SE?"Rörligt datum, fredagen mellan 19 juni och 25 juni (fredagen före midsommardagen)":"Movable date, Friday between June 19 and June 25");
    }

    /**
     * Returns Midsummer Day for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return Midsummer Day holiday
     */
    public static Holiday getMidsummerDay(int year, Lang lang) {
        return new Holiday(findDayInRange(year, Month.JUNE, 20, 26, DayOfWeek.SATURDAY),
            lang==Lang.SE?"Midsommardagen":"Midsummer Day",
            lang==Lang.SE?"Rörligt datum, lördagen mellan 20 juni och 26 juni":"Movable date, Saturday between June 20 and June 26");
    }

    /**
     * Returns All Saints' Eve for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return All Saints' Eve holiday
     */
    public static Holiday getAllSaintsEve(int year, Lang lang) {
        return new Holiday(findDayInRange(year, Month.OCTOBER, 30, 5, DayOfWeek.FRIDAY),
            lang==Lang.SE?"Allhelgonaafton":"All Saints' Eve",
            lang==Lang.SE?"Rörligt datum, fredagen mellan 30 oktober och 5 november":"Movable date, Friday between October 30 and November 5");
    }

    /**
     * Returns All Saints' Day for the given year.
     * @param year the year
     * @param lang the language (SE or EN)
     * @return All Saints' Day holiday
     */
    public static Holiday getAllSaintsDay(int year, Lang lang) {
        return new Holiday(findDayInRange(year, Month.OCTOBER, 31, 6, DayOfWeek.SATURDAY),
            lang==Lang.SE?"Alla helgons dag":"All Saints' Day",
            lang==Lang.SE?"Rörligt datum, lördagen mellan 31 oktober och 6 november":"Movable date, Saturday between October 31 and November 6");
    }

    //
    // PRIVATE METHODS
    //


    // Helper method
    private static List<Holiday> calculateHolidaysForYear(int year, Lang lang) {
        List<Holiday> holidays = new ArrayList<>();

        holidays.add(getNewYearsDay(year, lang));
        holidays.add(getEpiphanyEve(year, lang));
        holidays.add(getEpiphany(year, lang));
        holidays.add(getWalpurgisEve(year, lang));
        holidays.add(getMayDay(year, lang));
        holidays.add(getNationalDay(year, lang));
        holidays.add(getChristmasEve(year, lang));
        holidays.add(getChristmasDay(year, lang));
        holidays.add(getStStephensDay(year, lang));
        holidays.add(getNewYearsEve(year, lang));
        LocalDate easter = calculateEaster(year);
        holidays.add(getMaundyThursday(easter, lang));
        holidays.add(getGoodFriday(easter, lang));
        holidays.add(getEasterEve(easter, lang));
        holidays.add(getEasterSunday(easter, lang));
        holidays.add(getEasterMonday(easter, lang));
        holidays.add(getAscensionDay(easter, lang));
        LocalDate pentecost = easter.plusDays(49);
        holidays.add(getWhitsunEve(pentecost, lang));
        holidays.add(getPentecost(pentecost, lang));
        holidays.add(getMidsummerEve(year, lang));
        holidays.add(getMidsummerDay(year, lang));
        holidays.add(getAllSaintsEve(year, lang));
        holidays.add(getAllSaintsDay(year, lang));

        return holidays;
    }

    /**
     * Calculates the date of Easter Sunday for a given year using the
     * Anonymous Gregorian algorithm (Meeus/Jones/Butcher algorithm).
     *
     * @param year the year to calculate Easter for
     * @return Easter Sunday date in the specified year
     */
    private static LocalDate calculateEaster(int year) {
        int goldenNumber = year % 19;                // Position in 19-year Metonic cycle
        int century = year / 100;                    // Century number
        int yearOfCentury = year % 100;              // Year within the century

        int centuryDiv4 = century / 4;               // Century divided by 4
        int centuryRemainder = century % 4;          // Remainder of century/4

        int correction = (century + 8) / 25;         // Solar correction
        int lunarCorrection = (century - correction + 1) / 3; // Lunar correction

        // paschalFullMoon: number of days from March 21 to the paschal full moon
        int paschalFullMoon = (19 * goldenNumber + century - centuryDiv4 - lunarCorrection + 15) % 30;

        int yearOfCenturyDiv4 = yearOfCentury / 4;    // Year-of-century divided by 4
        int yearOfCenturyRemainder = yearOfCentury % 4;

        // weekday: offset to the next Sunday
        int weekday = (32 + 2 * centuryRemainder + 2 * yearOfCenturyDiv4 - paschalFullMoon - yearOfCenturyRemainder) % 7;

        // advance: additional correction based on lunar cycle
        int advance = (goldenNumber + 11 * paschalFullMoon + 22 * weekday) / 451;

        // month and day calculation: March is 3, April is 4
        int month = (paschalFullMoon + weekday - 7 * advance + 114) / 31;
        int day   = ((paschalFullMoon + weekday - 7 * advance + 114) % 31) + 1;

        return LocalDate.of(year, month, day);
    }

    /**
     * Finds the first occurrence of a day of week in a date range within a year.
     * @param year the year
     * @param month the month
     * @param startDay starting day of range
     * @param endDay ending day of range
     * @param dow the DayOfWeek to find
     * @return the matching date
     */
    private static LocalDate findDayInRange(int year, Month month, int startDay, int endDay, DayOfWeek dow) {
        LocalDate date = LocalDate.of(year, month, startDay);
        LocalDate last = (endDay >= startDay)
                ? LocalDate.of(year, month, endDay)
                : LocalDate.of(year, month, 1).withDayOfMonth(1).plusMonths(1).withDayOfMonth(endDay);
        while (!date.isAfter(last)) {
            if (date.getDayOfWeek() == dow) {
                return date;
            }
            date = date.plusDays(1);
        }
        throw new IllegalStateException("No " + dow + " between days " + startDay + " and " + endDay);
    }
}