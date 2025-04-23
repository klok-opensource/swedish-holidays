package dev.klok.holidays;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.sql.Date;
import java.sql.Timestamp;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList; // Thread-safe list
import java.util.concurrent.CopyOnWriteArraySet;  // Thread-safe set
import java.util.stream.Collectors;

/**
 * Utility class for checking Swedish public holidays and common non-working days/eves.
 * Provides methods to check specific dates, list holidays for a year, and customize
 * the holiday set by adding or removing dates/rules.
 * <p>
 * This class uses caching for performance. Holiday calculations are performed lazily
 * per year and language and stored. Customizations will clear the cache.
 * </p>
 * <p>
 * All methods are thread-safe.
 * </p>
 */
public final class SwedishHolidays {

    /** Language options for holiday names and descriptions. */
    public enum Lang { SE, EN }

    private static final ZoneId SWEDEN = ZoneId.of("Europe/Stockholm");
    private static Lang defaultLang = Lang.EN;

    // Nested cache for holidays per year and language
    private static final ConcurrentMap<Integer, ConcurrentMap<Lang, List<Holiday>>> holidayListCachePerLang = new ConcurrentHashMap<>();
    // Date cache per year for quick holiday lookup (contains final, customized dates)
    private static final ConcurrentMap<Integer, Set<LocalDate>> holidayDateCache = new ConcurrentHashMap<>();

    // --- Customization Fields ---
    private static final List<Holiday> customAdditions = new CopyOnWriteArrayList<>();
    private static final Set<LocalDate> standardRemovals = new CopyOnWriteArraySet<>();
    private static final List<HolidayRule> customRules = new CopyOnWriteArrayList<>();
    // --- End Customization Fields ---


    // Prevent instantiation
    private SwedishHolidays() {}

    // Static initializer for pre-populating cache
    static {
        // Pre-populate the cache with holidays for common years
        int currentYear = LocalDate.now(SWEDEN).getYear();
        populateCacheForYear(currentYear - 1); // Previous
        populateCacheForYear(currentYear);     // Current
        populateCacheForYear(currentYear + 1); // Next
        populateCacheForYear(currentYear + 2); // Second next
    }

    // Helper to avoid code duplication in static block
    private static void populateCacheForYear(int year) {
        // Getting the list triggers calculation and caching for both languages
        // and the date-only cache
        listHolidays(Lang.SE, year);
        listHolidays(Lang.EN, year);
    }

    //
    // --- CONFIGURATION METHODS ---
    //

    /**
     * Sets the default language used when no language is specified in method calls.
     * Default is {@link Lang#EN}.
     *
     * @param lang The language to set as default (SE or EN).
     */
    public static void setDefaultLanguage(Lang lang) {
        defaultLang = Objects.requireNonNull(lang, "Default language cannot be null");
        // Changing default language doesn't require cache clearing,
        // as caches are keyed by specific language.
    }

    /**
     * Adds a custom holiday definition for a specific fixed date.
     * This holiday will be included in addition to standard and rule-based holidays.
     * <p>
     * Note: Modifying configurations clears the holiday cache.
     * </p>
     *
     * @param date        The specific date of the custom holiday (required).
     * @param name        The name for this custom holiday (required).
     * @param description A description for this custom holiday (required).
     */
    public static void addCustomHoliday(LocalDate date, String name, String description) {
        Objects.requireNonNull(date, "Custom holiday date cannot be null");
        Objects.requireNonNull(name, "Custom holiday name cannot be null");
        Objects.requireNonNull(description, "Custom holiday description cannot be null");
        // Optional: Check for duplicates if needed, though CopyOnWriteArrayList allows them
        customAdditions.add(new Holiday(date, name, description));
        clearCache(); // Crucial: Invalidate cache on config change
    }

    /**
     * Removes a standard holiday that falls on the specified date.
     * If a standard holiday calculation results in this specific date for any year,
     * it will be excluded from the results of `listHolidays` and `isHoliday`.
     * This does *not* remove custom holidays or rule-based holidays on the same date.
     * <p>
     * Note: Modifying configurations clears the holiday cache.
     * </p>
     *
     * @param dateToRemove The exact date of the standard holiday to remove (required).
     */
    public static void removeStandardHoliday(LocalDate dateToRemove) {
        Objects.requireNonNull(dateToRemove, "Date to remove cannot be null");
        standardRemovals.add(dateToRemove);
        clearCache(); // Crucial: Invalidate cache on config change
    }

    /**
     * Adds a custom holiday calculation rule. This rule will be evaluated
     * each time holidays are requested for a specific year, potentially adding
     * a holiday based on the rule's logic (e.g., 'Friday after Ascension').
     * <p>
     * Note: Modifying configurations clears the holiday cache.
     * </p>
     *
     * @param rule The HolidayRule implementation (required).
     * @see HolidayRule
     */
    public static void addCustomHolidayRule(HolidayRule rule) {
        Objects.requireNonNull(rule, "Holiday rule cannot be null");
        customRules.add(rule);
        clearCache(); // Crucial: Invalidate cache on config change
    }

    /**
     * Clears all previously added custom fixed-date holidays.
     * Standard holidays and rule-based holidays remain unaffected.
     * <p>
     * Note: Modifying configurations clears the holiday cache if changes were made.
     * </p>
     */
    public static void clearCustomAdditions() {
        if (!customAdditions.isEmpty()) {
            customAdditions.clear();
            clearCache();
        }
    }

    /**
     * Clears all previously specified standard holiday removals.
     * All standard holidays will be included again unless removed by subsequent calls.
     * <p>
     * Note: Modifying configurations clears the holiday cache if changes were made.
     * </p>
     */
    public static void clearStandardRemovals() {
        if (!standardRemovals.isEmpty()) {
            standardRemovals.clear();
            clearCache();
        }
    }

    /**
     * Clears all previously added custom holiday calculation rules.
     * Standard holidays and fixed-date custom holidays remain unaffected.
     * <p>
     * Note: Modifying configurations clears the holiday cache if changes were made.
     * </p>
     */
    public static void clearCustomRules() {
        if (!customRules.isEmpty()) {
            customRules.clear();
            clearCache();
        }
    }

    /**
     * Clears all custom configurations: fixed additions, standard removals, and custom rules.
     * Resets the holiday calculation to the default Swedish holidays.
     * <p>
     * Note: Modifying configurations clears the holiday cache if changes were made.
     * </p>
     */
    public static void clearAllCustomizations() {
        boolean changed = !customAdditions.isEmpty()
                || !standardRemovals.isEmpty()
                || !customRules.isEmpty();
        customAdditions.clear();
        standardRemovals.clear();
        customRules.clear();
        if (changed) {
            clearCache();
        }
    }

    /**
     * Clears the internal holiday cache. This forces recalculation of holidays
     * upon the next request. Usually called automatically when configurations change,
     * but can be invoked manually if external factors affecting holidays (which this
     * library doesn't account for) have changed.
     */
    public static void clearCache() {
        holidayListCachePerLang.clear();
        holidayDateCache.clear();
        // Optional: Re-pre-populate cache here if desired, but lazy loading is often sufficient.
        // Consider adding back the pre-population calls if clearing is frequent.
        // populateCacheForYear(LocalDate.now(SWEDEN).getYear()); // etc.
    }

    // --- END CONFIGURATION METHODS ---


    //
    // --- IS HOLIDAY CHECK METHODS ---
    //

    /**
     * Checks if the given LocalDate is a Swedish public holiday or registered custom holiday.
     * Uses the default language internally for cache population if needed.
     * @param date the LocalDate to check (required)
     * @return true if the date is a holiday according to current configuration, false otherwise
     */
    public static boolean isHoliday(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");
        int year = date.getYear();
        // Check the date-only cache first for speed
        Set<LocalDate> dates = holidayDateCache.get(year);
        if (dates != null) {
            return dates.contains(date);
        }
        // Cache miss: Trigger calculation and caching using the primary listHolidays method
        // This will populate both holidayListCachePerLang and holidayDateCache
        listHolidays(defaultLang, year); // Use default lang, result is same date set

        // Check cache again after population
        dates = holidayDateCache.get(year);
        return dates != null && dates.contains(date);
    }

    /**
     * Checks if the given java.sql.Date is a Swedish public holiday or registered custom holiday.
     * Converts the SQL Date to LocalDate before checking.
     * @param sqlDate the SQL Date to check (required)
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(Date sqlDate) {
        Objects.requireNonNull(sqlDate, "SQL Date cannot be null");
        return isHoliday(sqlDate.toLocalDate());
    }

    /**
     * Checks if the given Timestamp corresponds to a Swedish public holiday or registered custom holiday
     * in the Stockholm timezone. The time part is ignored.
     * @param timestamp the Timestamp to check (required)
     * @return true if the date part is a holiday, false otherwise
     */
    public static boolean isHoliday(Timestamp timestamp) {
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        LocalDate d = timestamp.toInstant().atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given Instant corresponds to a Swedish public holiday or registered custom holiday
     * in the Stockholm timezone. The time part is ignored.
     * @param instant the Instant to check (required)
     * @return true if the date part is a holiday, false otherwise
     */
    public static boolean isHoliday(Instant instant) {
        Objects.requireNonNull(instant, "Instant cannot be null");
        LocalDate d = instant.atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given XMLGregorianCalendar corresponds to a Swedish public holiday or registered custom holiday.
     * The date is interpreted in the Stockholm timezone. Time part is ignored.
     * @param xmlCal the XMLGregorianCalendar to check (required)
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(XMLGregorianCalendar xmlCal) {
        Objects.requireNonNull(xmlCal, "XMLGregorianCalendar cannot be null");
        // Handle potential timezone information in XMLGregorianCalendar correctly
        TimeZone tz = xmlCal.getTimezone() != DatatypeConstants.FIELD_UNDEFINED
                ? TimeZone.getTimeZone(ZoneOffset.ofTotalSeconds(xmlCal.getTimezone() * 60))
                : TimeZone.getTimeZone(SWEDEN); // Default to SWEDEN if undefined

        GregorianCalendar cal = xmlCal.toGregorianCalendar(tz, null, null);
        LocalDate date = cal.toInstant().atZone(SWEDEN).toLocalDate(); // Convert via Instant ensures correct zone handling
        return isHoliday(date);
    }

    /**
     * Checks if the given java.util.Date corresponds to a Swedish public holiday or registered custom holiday
     * in the Stockholm timezone. The time part is ignored.
     * @param date the Date to check (required)
     * @return true if the date part is a holiday, false otherwise
     */
    public static boolean isHoliday(java.util.Date date) {
        Objects.requireNonNull(date, "java.util.Date cannot be null");
        LocalDate d = date.toInstant().atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given Calendar corresponds to a Swedish public holiday or registered custom holiday.
     * The date is interpreted in the Stockholm timezone. Time part is ignored.
     * @param calendar the Calendar to check (required)
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(Calendar calendar) {
        Objects.requireNonNull(calendar, "Calendar cannot be null");
        LocalDate d = calendar.toInstant().atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given LocalDateTime corresponds to a Swedish public holiday or registered custom holiday.
     * The LocalDateTime is assumed to be in the Stockholm timezone. Time part is ignored.
     * @param dateTime the LocalDateTime to check (required)
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime cannot be null");
        // Assume LocalDateTime is in target timezone (Stockholm)
        LocalDate d = dateTime.toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given ZonedDateTime corresponds to a Swedish public holiday or registered custom holiday.
     * The date is determined based on the Stockholm timezone, regardless of the object's original zone.
     * Time part is ignored.
     * @param dateTime the ZonedDateTime to check (required)
     * @return true if the date in Stockholm is a holiday, false otherwise
     */
    public static boolean isHoliday(ZonedDateTime dateTime) {
        Objects.requireNonNull(dateTime, "ZonedDateTime cannot be null");
        LocalDate d = dateTime.withZoneSameInstant(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given OffsetDateTime corresponds to a Swedish public holiday or registered custom holiday.
     * The date is determined based on the Stockholm timezone. Time part is ignored.
     * @param dateTime the OffsetDateTime to check (required)
     * @return true if the date in Stockholm is a holiday, false otherwise
     */
    public static boolean isHoliday(OffsetDateTime dateTime) {
        Objects.requireNonNull(dateTime, "OffsetDateTime cannot be null");
        LocalDate d = dateTime.atZoneSameInstant(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    /**
     * Checks if the given TemporalAccessor represents a Swedish public holiday or registered custom holiday.
     * Tries to extract LocalDate directly; if that fails, tries to extract an Instant
     * and convert it using the Stockholm timezone.
     * @param temporal the TemporalAccessor to check (required)
     * @return true if the date is a holiday, false otherwise
     * @throws DateTimeException if the TemporalAccessor cannot be resolved to a date or instant.
     */
    public static boolean isHoliday(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "TemporalAccessor cannot be null");
        try {
            // Prioritize direct LocalDate extraction
            if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
                LocalDate date = LocalDate.from(temporal);
                return isHoliday(date);
            }
        } catch (DateTimeException ignored) { /* Fall through */ }

        try {
            // Try extracting an Instant
            if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
                Instant instant = Instant.from(temporal);
                return isHoliday(instant);
            }
        } catch (DateTimeException e) {
            throw new DateTimeException("TemporalAccessor cannot be interpreted as LocalDate or Instant: " + temporal, e);
        }

        throw new DateTimeException("TemporalAccessor does not contain sufficient information for LocalDate or Instant: " + temporal);
    }

    /**
     * Checks if today (in the Stockholm timezone) is a Swedish public holiday or registered custom holiday.
     * @return true if today is a holiday, false otherwise
     */
    public static boolean isHolidayToday() {
        LocalDate today = LocalDate.now(SWEDEN);
        return isHoliday(today);
    }

    // --- END IS HOLIDAY CHECK METHODS ---


    //
    // --- LIST HOLIDAY METHODS ---
    //

    /**
     * Returns a list of all holidays (standard and custom, reflecting removals)
     * for the specified year in the specified language.
     * Results are cached.
     *
     * @param lang the language (SE or EN) (required)
     * @param year the year
     * @return an unmodifiable list of holidays for the year, sorted by date.
     */
    public static List<Holiday> listHolidays(Lang lang, int year) {
        Objects.requireNonNull(lang, "Language cannot be null");
        ConcurrentMap<Lang, List<Holiday>> perLangMap =
                holidayListCachePerLang.computeIfAbsent(year, y -> new ConcurrentHashMap<>());

        // Check cache first
        List<Holiday> cachedHolidays = perLangMap.get(lang);
        if (cachedHolidays != null) {
            return cachedHolidays;
        }

        // Cache miss: Calculate, apply customizations, and cache
        List<Holiday> holidays = calculateAndCustomizeHolidaysForYear(year, lang);

        // Populate date-only cache (using the *final* list of dates)
        // Use computeIfAbsent to avoid race conditions if multiple threads calculate for the same year
        // This should happen only once per year, regardless of language requested first
        holidayDateCache.computeIfAbsent(year, y ->
                holidays.stream()
                        .map(Holiday::getDate)
                        .collect(Collectors.toSet()) // Collect to a Set for fast lookups
        );

        // Populate the list cache for the specific language
        List<Holiday> unmodifiableHolidays = Collections.unmodifiableList(holidays);
        // Use putIfAbsent for the language-specific cache to avoid overwriting if another thread just finished
        List<Holiday> existing = perLangMap.putIfAbsent(lang, unmodifiableHolidays);

        return (existing != null) ? existing : unmodifiableHolidays;
    }

    /**
     * Returns a list of holidays for the specified year, using language code string.
     * Uses English if the code is not "SE" (case-insensitive).
     * @param langCode "SE" or "EN" (case-insensitive).
     * @param year the year
     * @return list of holidays
     */
    public static List<Holiday> listHolidays(String langCode, int year) {
        Objects.requireNonNull(langCode, "Language code (string) cannot be null");
        Lang l = "SE".equalsIgnoreCase(langCode) ? Lang.SE : Lang.EN;
        return listHolidays(l, year);
    }


    /**
     * Returns a list of holidays for the current year in the specified language.
     * @param lang the language (SE or EN) (required)
     * @return list of holidays
     */
    public static List<Holiday> listHolidays(Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        int year = LocalDate.now(SWEDEN).getYear();
        return listHolidays(lang, year);
    }

    /**
     * Returns a list of holidays for the current year, using language code string.
     * Uses English if the code is not "SE" (case-insensitive).
     * @param langCode "SE" or "EN"
     * @return list of holidays
     */
    public static List<Holiday> listHolidays(String langCode) {
        Objects.requireNonNull(langCode, "Language code (string) cannot be null");
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
     * Returns a list of holidays for the current year in the default language (initially English).
     * @return list of holidays
     * @see #setDefaultLanguage(Lang)
     */
    public static List<Holiday> listHolidays() {
        return listHolidays(defaultLang);
    }

    /**
     * Returns a list of holidays for the specified year in the default language (initially English).
     * @param year the year
     * @return list of holidays
     * @see #setDefaultLanguage(Lang)
     */
    public static List<Holiday> listHolidays(int year) {
        return listHolidays(defaultLang, year);
    }

    // --- END LIST HOLIDAY METHODS ---


    //
    // --- GET SINGLE HOLIDAY METHODS ---
    // These methods provide direct access to calculate specific standard holidays.
    // They *do not* reflect custom additions or removals. Use `listHolidays` or `isHoliday`
    // for the final, customized view. They are useful for understanding the base calculations
    // or potentially identifying dates for removal.
    //

    // (Fixed Date Getters - Keep existing methods like getNewYearsDay, getEpiphany, etc.)
    // Add null checks for Lang parameter
    public static Holiday getNewYearsDay(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 1, 1),
                lang==Lang.SE?"Nyårsdagen":"New Year's Day",
                lang==Lang.SE?"Fast datum, 1 januari":"Fixed date, January 1");
    }
    // Add year-only overload using defaultLang for all fixed date getters
    public static Holiday getNewYearsDay(int year) { return getNewYearsDay(year, defaultLang); }

    public static Holiday getEpiphanyEve(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 1, 5),
                lang==Lang.SE?"Trettondagsafton":"Epiphany Eve",
                lang==Lang.SE?"Fast datum, 5 januari":"Fixed date, January 5");
    }
    public static Holiday getEpiphanyEve(int year) { return getEpiphanyEve(year, defaultLang); }


    public static Holiday getEpiphany(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 1, 6),
                lang==Lang.SE?"Trettondedag jul":"Epiphany",
                lang==Lang.SE?"Fast datum, 6 januari":"Fixed date, January 6");
    }
    public static Holiday getEpiphany(int year) { return getEpiphany(year, defaultLang); }

    // ... (Add similar null checks and year-only overloads for ALL fixed date getters)
    // getWalpurgisEve, getMayDay, getNationalDay, getChristmasEve, getChristmasDay,
    // getStStephensDay, getNewYearsEve

    public static Holiday getWalpurgisEve(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 4, 30),
                lang==Lang.SE?"Valborgsmässoafton":"Walpurgis Eve",
                lang==Lang.SE?"Fast datum, 30 april":"Fixed date, April 30");
    }
    public static Holiday getWalpurgisEve(int year) { return getWalpurgisEve(year, defaultLang); }


    public static Holiday getMayDay(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 5, 1),
                lang==Lang.SE?"Första maj":"May Day",
                lang==Lang.SE?"Fast datum, 1 maj":"Fixed date, May 1");
    }
    public static Holiday getMayDay(int year) { return getMayDay(year, defaultLang); }


    public static Holiday getNationalDay(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 6, 6),
                lang==Lang.SE?"Sveriges nationaldag":"National Day of Sweden",
                lang==Lang.SE?"Fast datum, 6 juni":"Fixed date, June 6");
    }
    public static Holiday getNationalDay(int year) { return getNationalDay(year, defaultLang); }


    public static Holiday getChristmasEve(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 12, 24),
                lang==Lang.SE?"Julafton":"Christmas Eve",
                lang==Lang.SE?"Fast datum, 24 december":"Fixed date, December 24");
    }
    public static Holiday getChristmasEve(int year) { return getChristmasEve(year, defaultLang); }


    public static Holiday getChristmasDay(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 12, 25),
                lang==Lang.SE?"Juldagen":"Christmas Day",
                lang==Lang.SE?"Fast datum, 25 december":"Fixed date, December 25");
    }
    public static Holiday getChristmasDay(int year) { return getChristmasDay(year, defaultLang); }


    public static Holiday getStStephensDay(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 12, 26),
                lang==Lang.SE?"Annandag jul":"St. Stephen's Day",
                lang==Lang.SE?"Fast datum, 26 december":"Fixed date, December 26");
    }
    public static Holiday getStStephensDay(int year) { return getStStephensDay(year, defaultLang); }


    public static Holiday getNewYearsEve(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(LocalDate.of(year, 12, 31),
                lang==Lang.SE?"Nyårsafton":"New Year's Eve",
                lang==Lang.SE?"Fast datum, 31 december":"Fixed date, December 31");
    }
    public static Holiday getNewYearsEve(int year) { return getNewYearsEve(year, defaultLang); }


    // (Easter-Based Getters - Keep existing methods)
    // Add null checks for Lang parameter
    public static Holiday getMaundyThursday(LocalDate easter, Lang lang) {
        Objects.requireNonNull(easter, "Easter date cannot be null");
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(easter.minusDays(3),
                lang==Lang.SE?"Skärtorsdagen":"Maundy Thursday",
                lang==Lang.SE?"Rörligt datum, torsdagen före Påskdagen":"Movable date, Thursday before Easter Sunday");
    }
    public static Holiday getMaundyThursday(int year, Lang lang) { return getMaundyThursday(calculateEaster(year), lang); }
    public static Holiday getMaundyThursday(int year) { return getMaundyThursday(year, defaultLang); }

    // ... (Add similar checks/overloads for ALL Easter-based getters)
    // getGoodFriday, getEasterEve, getEasterSunday, getEasterMonday, getAscensionDay,
    // getWhitsunEve, getPentecost

    public static Holiday getGoodFriday(LocalDate easter, Lang lang) {
        Objects.requireNonNull(easter, "Easter date cannot be null");
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(easter.minusDays(2),
                lang==Lang.SE?"Långfredagen":"Good Friday",
                lang==Lang.SE?"Rörligt datum, fredagen före Påskdagen":"Movable date, Friday before Easter Sunday");
    }
    public static Holiday getGoodFriday(int year, Lang lang) { return getGoodFriday(calculateEaster(year), lang); }
    public static Holiday getGoodFriday(int year) { return getGoodFriday(year, defaultLang); }


    public static Holiday getEasterEve(LocalDate easter, Lang lang) {
        Objects.requireNonNull(easter, "Easter date cannot be null");
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(easter.minusDays(1),
                lang==Lang.SE?"Påskafton":"Easter Eve",
                lang==Lang.SE?"Rörligt datum, lördagen före Påskdagen":"Movable date, Saturday before Easter Sunday");
    }
    public static Holiday getEasterEve(int year, Lang lang) { return getEasterEve(calculateEaster(year), lang); }
    public static Holiday getEasterEve(int year) { return getEasterEve(year, defaultLang); }


    public static Holiday getEasterSunday(LocalDate easter, Lang lang) {
        Objects.requireNonNull(easter, "Easter date cannot be null");
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(easter,
                lang==Lang.SE?"Påskdagen":"Easter Sunday",
                lang==Lang.SE?"Rörligt datum, första söndagen efter ecklesiastisk fullmåne, efter vårdagjämningen":"Movable date, first Sunday after the ecclesiastical full moon following the vernal equinox");
    }
    public static Holiday getEasterSunday(int year, Lang lang) { return getEasterSunday(calculateEaster(year), lang); }
    public static Holiday getEasterSunday(int year) { return getEasterSunday(year, defaultLang); }


    public static Holiday getEasterMonday(LocalDate easter, Lang lang) {
        Objects.requireNonNull(easter, "Easter date cannot be null");
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(easter.plusDays(1),
                lang==Lang.SE?"Annandag påsk":"Easter Monday",
                lang==Lang.SE?"Rörligt datum, dagen efter påskdagen (d.v.s. en måndag)":"Movable date, the day after Easter Sunday (Monday)");
    }
    public static Holiday getEasterMonday(int year, Lang lang) { return getEasterMonday(calculateEaster(year), lang); }
    public static Holiday getEasterMonday(int year) { return getEasterMonday(year, defaultLang); }


    public static Holiday getAscensionDay(LocalDate easter, Lang lang) {
        Objects.requireNonNull(easter, "Easter date cannot be null");
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(easter.plusDays(39),
                lang==Lang.SE?"Kristi himmelsfärdsdag":"Ascension Day",
                lang==Lang.SE?"Rörligt datum, sjätte torsdagen efter påskdagen":"Movable date, the sixth Thursday after Easter Sunday");
    }
    public static Holiday getAscensionDay(int year, Lang lang) { return getAscensionDay(calculateEaster(year), lang); }
    public static Holiday getAscensionDay(int year) { return getAscensionDay(year, defaultLang); }


    public static Holiday getWhitsunEve(LocalDate pentecost, Lang lang) {
        Objects.requireNonNull(pentecost, "Pentecost date cannot be null");
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(pentecost.minusDays(1),
                lang==Lang.SE?"Pingstafton":"Whitsun Eve",
                lang==Lang.SE?"Rörligt datum, dagen före pingstdagen (d.v.s. en lördag)":"Movable date, the day before Pentecost (Saturday)");
    }
    public static Holiday getWhitsunEve(int year, Lang lang) {
        LocalDate p = calculateEaster(year).plusDays(49);
        return getWhitsunEve(p, lang);
    }
    public static Holiday getWhitsunEve(int year) { return getWhitsunEve(year, defaultLang); }


    public static Holiday getPentecost(LocalDate pentecost, Lang lang) {
        Objects.requireNonNull(pentecost, "Pentecost date cannot be null");
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(pentecost,
                lang==Lang.SE?"Pingstdagen":"Pentecost",
                lang==Lang.SE?"Rörligt datum, sjunde söndagen efter påskdagen":"Movable date, seventh Sunday after Easter Sunday");
    }
    public static Holiday getPentecost(int year, Lang lang) {
        LocalDate p = calculateEaster(year).plusDays(49);
        return getPentecost(p, lang);
    }
    public static Holiday getPentecost(int year) { return getPentecost(year, defaultLang); }

    // (Other Movable Date Getters - Keep existing methods)
    // Add null checks for Lang parameter
    public static Holiday getMidsummerEve(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(findDayInRange(year, Month.JUNE, 19, 25, DayOfWeek.FRIDAY),
                lang==Lang.SE?"Midsommarafton":"Midsummer Eve",
                lang==Lang.SE?"Rörligt datum, fredagen mellan 19 juni och 25 juni (fredagen före midsommardagen)":"Movable date, Friday between June 19 and June 25");
    }
    public static Holiday getMidsummerEve(int year) { return getMidsummerEve(year, defaultLang); }

    // ... (Add similar checks/overloads for ALL other movable date getters)
    // getMidsummerDay, getAllSaintsEve, getAllSaintsDay

    public static Holiday getMidsummerDay(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(findDayInRange(year, Month.JUNE, 20, 26, DayOfWeek.SATURDAY),
                lang==Lang.SE?"Midsommardagen":"Midsummer Day",
                lang==Lang.SE?"Rörligt datum, lördagen mellan 20 juni och 26 juni":"Movable date, Saturday between June 20 and June 26");
    }
    public static Holiday getMidsummerDay(int year) { return getMidsummerDay(year, defaultLang); }


    public static Holiday getAllSaintsEve(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(findDayInRange(year, Month.OCTOBER, 30, 5, DayOfWeek.FRIDAY),
                lang==Lang.SE?"Allhelgonaafton":"All Saints' Eve",
                lang==Lang.SE?"Rörligt datum, fredagen mellan 30 oktober och 5 november":"Movable date, Friday between October 30 and November 5");
    }
    public static Holiday getAllSaintsEve(int year) { return getAllSaintsEve(year, defaultLang); }


    public static Holiday getAllSaintsDay(int year, Lang lang) {
        Objects.requireNonNull(lang, "Language cannot be null");
        return new Holiday(findDayInRange(year, Month.OCTOBER, 31, 6, DayOfWeek.SATURDAY),
                lang==Lang.SE?"Alla helgons dag":"All Saints' Day",
                lang==Lang.SE?"Rörligt datum, lördagen mellan 31 oktober och 6 november":"Movable date, Saturday between October 31 and November 6");
    }
    public static Holiday getAllSaintsDay(int year) { return getAllSaintsDay(year, defaultLang); }

    // --- END GET SINGLE HOLIDAY METHODS ---


    //
    // --- PRIVATE CALCULATION METHODS ---
    //

    /**
     * Calculates the final list of holidays for a year, including standard holidays,
     * custom additions, and rule-based additions, after applying removals.
     * The returned list is sorted by date.
     *
     * @param year The year to calculate for.
     * @param lang The language for holiday names/descriptions.
     * @return A new, mutable list of Holiday objects for the year.
     */
    private static List<Holiday> calculateAndCustomizeHolidaysForYear(int year, Lang lang) {
        // 1. Calculate Standard Holidays
        List<Holiday> standardHolidays = calculateStandardHolidaysForYear(year, lang);

        // 2. Apply Removals (Filter out standard holidays marked for removal)
        List<Holiday> filteredHolidays = standardHolidays.stream()
                .filter(h -> !standardRemovals.contains(h.getDate()))
                .collect(Collectors.toList()); // Collect into a mutable list

        // 3. Apply Fixed Custom Additions (only those matching the year)
        customAdditions.stream()
                .filter(h -> h.getDate().getYear() == year)
                // Consider language handling for fixed additions if needed
                // (Current implementation just adds them as defined)
                .forEach(filteredHolidays::add);

        // 4. Apply Rule-Based Custom Additions
        customRules.stream()
                .map(rule -> rule.calculateHoliday(year, lang)) // Evaluate rule
                .filter(Objects::nonNull)                      // Filter out null results (rule didn't apply)
                .forEach(filteredHolidays::add);               // Add the calculated holiday

        // 5. Sort the final combined list by date
        filteredHolidays.sort(Comparator.comparing(Holiday::getDate));

        // Optional: Remove duplicates if rules or additions might overlap with standard holidays
        // This requires Holiday to have a good equals/hashCode implementation (added).
        // If duplicates are possible and unwanted:
        // List<Holiday> distinctHolidays = filteredHolidays.stream().distinct().collect(Collectors.toList());
        // return distinctHolidays;

        return filteredHolidays; // Return the final, sorted list
    }


    /**
     * Calculates the base set of standard Swedish holidays for a given year and language.
     * Does not include any customizations or removals.
     *
     * @param year The year.
     * @param lang The language.
     * @return A list of standard Holiday objects.
     */
    private static List<Holiday> calculateStandardHolidaysForYear(int year, Lang lang) {
        List<Holiday> holidays = new ArrayList<>(25); // Initial capacity estimate

        // Fixed Dates
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

        // Easter-based Dates
        LocalDate easter = calculateEaster(year);
        holidays.add(getMaundyThursday(easter, lang));
        holidays.add(getGoodFriday(easter, lang));
        holidays.add(getEasterEve(easter, lang));
        holidays.add(getEasterSunday(easter, lang));
        holidays.add(getEasterMonday(easter, lang));
        holidays.add(getAscensionDay(easter, lang));

        // Pentecost-based Dates (calculated from Easter)
        LocalDate pentecost = easter.plusDays(49); // Pentecost Sunday is 49 days after Easter
        holidays.add(getWhitsunEve(pentecost, lang));
        holidays.add(getPentecost(pentecost, lang)); // Whit Sunday

        // Other Movable Dates
        holidays.add(getMidsummerEve(year, lang));
        holidays.add(getMidsummerDay(year, lang));
        holidays.add(getAllSaintsEve(year, lang));
        holidays.add(getAllSaintsDay(year, lang));

        // Note: No sorting here, sorting happens after customizations are applied.
        return holidays;
    }

    /**
     * Calculates the date of Easter Sunday for a given year using the
     * Anonymous Gregorian algorithm (Meeus/Jones/Butcher algorithm).
     *
     * @param year the year to calculate Easter for (must be positive)
     * @return Easter Sunday date in the specified year
     */
    private static LocalDate calculateEaster(int year) {

        if (year <= 0) {
            throw new IllegalArgumentException("Year must be positive to calculate Easter.");
        }

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
     * Finds the first occurrence of a specific day of the week within a date range
     * (inclusive) spanning potentially across month boundaries (e.g., Oct 31 to Nov 6).
     *
     * @param year      the year
     * @param startMonth the starting month
     * @param startDay  starting day of the range (e.g., 19 for Midsummer Eve)
     * @param endDay    ending day of the range (e.g., 25 for Midsummer Eve). If endDay < startDay,
     *                  it's assumed the range crosses into the next month.
     * @param dow       the DayOfWeek to find
     * @return the matching date
     * @throws IllegalStateException if the day of week is not found in the range.
     */
    private static LocalDate findDayInRange(int year, Month startMonth, int startDay, int endDay, DayOfWeek dow) {
        LocalDate startDate = LocalDate.of(year, startMonth, startDay);
        LocalDate endDate;

        if (endDay >= startDay) {
            // Range within the same month
            endDate = LocalDate.of(year, startMonth, endDay);
        } else {
            // Range crosses into the next month
            Month endMonth = startMonth.plus(1);
            endDate = LocalDate.of(year, endMonth, endDay);
        }

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek() == dow) {
                return currentDate;
            }
            currentDate = currentDate.plusDays(1);
        }

        // Should be unreachable for valid Swedish holiday rules, but included for robustness
        throw new IllegalStateException(String.format("Could not find %s between %s-%d and %s-%d in %d",
                dow, startMonth, startDay, endDate.getMonth(), endDay, year));
    }

    // --- END PRIVATE METHODS ---
}