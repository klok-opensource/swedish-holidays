package dev.klok.holidays;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.logging.Logger;
import java.sql.Date;
import java.sql.Timestamp;

public class SwedishHolidays {
    private static final ZoneId SWEDEN = ZoneId.of("Europe/Stockholm");
    private static final Logger logger = Logger.getLogger(SwedishHolidays.class.getName());

    public enum Lang { SE, EN }

    public static boolean isHoliday(LocalDate date) {
        return listHolidays(Lang.EN).stream()
                .anyMatch(h -> h.getDate().equals(date));
    }

    public static boolean isHoliday(Date sqlDate) {
        return isHoliday(sqlDate.toLocalDate());
    }

    public static boolean isHoliday(Timestamp timestamp) {
        LocalDate d = timestamp.toInstant().atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    public static boolean isHoliday(Instant instant) {
        LocalDate d = instant.atZone(SWEDEN).toLocalDate();
        return isHoliday(d);
    }

    public static boolean isHolidayToday() {
        LocalDate today = LocalDate.now(SWEDEN);
        return isHoliday(today);
    }

    public static List<Holiday> listHolidays(Lang lang) {
        int year = LocalDate.now(SWEDEN).getYear();
        return listHolidays(lang, year);
    }

    public static List<Holiday> listHolidays(String lang) {
        Lang l = "SE".equalsIgnoreCase(lang) ? Lang.SE : Lang.EN;
        return listHolidays(l);
    }

    public static List<Holiday> listHolidaysSE() {
        return listHolidays(Lang.SE);
    }

    public static List<Holiday> listHolidaysEN() {
        return listHolidays(Lang.EN);
    }

    public static List<Holiday> listHolidays(Lang lang, int year) {
        List<Holiday> holidays = new ArrayList<>();
        // fixed dates
        holidays.add(new Holiday(LocalDate.of(year,1,1),
                lang==Lang.SE?"Nyårsdagen":"New Year's Day",
                lang==Lang.SE?"Fast datum, 1 januari":"Fixed date, January 1"));
        holidays.add(new Holiday(LocalDate.of(year,1,5),
                lang==Lang.SE?"Trettondagsafton":"Epiphany Eve",
                lang==Lang.SE?"Fast datum, 5 januari":"Fixed date, January 5"));
        holidays.add(new Holiday(LocalDate.of(year,1,6),
                lang==Lang.SE?"Trettondedag jul":"Epiphany",
                lang==Lang.SE?"Fast datum, 6 januari":"Fixed date, January 6"));
        holidays.add(new Holiday(LocalDate.of(year,4,30),
                lang==Lang.SE?"Valborgsmässoafton":"Walpurgis Eve",
                lang==Lang.SE?"Fast datum, 30 april":"Fixed date, April 30"));
        holidays.add(new Holiday(LocalDate.of(year,5,1),
                lang==Lang.SE?"Första maj":"May Day",
                lang==Lang.SE?"Fast datum, 1 maj":"Fixed date, May 1"));
        holidays.add(new Holiday(LocalDate.of(year,6,6),
                lang==Lang.SE?"Sveriges nationaldag":"National Day of Sweden",
                lang==Lang.SE?"Fast datum, 6 juni":"Fixed date, June 6"));
        holidays.add(new Holiday(LocalDate.of(year,12,24),
                lang==Lang.SE?"Julafton":"Christmas Eve",
                lang==Lang.SE?"Fast datum, 24 december":"Fixed date, December 24"));
        holidays.add(new Holiday(LocalDate.of(year,12,25),
                lang==Lang.SE?"Juldagen":"Christmas Day",
                lang==Lang.SE?"Fast datum, 25 december":"Fixed date, December 25"));
        holidays.add(new Holiday(LocalDate.of(year,12,26),
                lang==Lang.SE?"Annandag jul":"St. Stephen's Day",
                lang==Lang.SE?"Fast datum, 26 december":"Fixed date, December 26"));
        holidays.add(new Holiday(LocalDate.of(year,12,31),
                lang==Lang.SE?"Nyårsafton":"New Year's Eve",
                lang==Lang.SE?"Fast datum, 31 december":"Fixed date, December 31"));
        // movable dates based on Easter
        LocalDate easter = calculateEaster(year);
        holidays.add(new Holiday(easter.minusDays(3),
                lang==Lang.SE?"Skärtorsdagen":"Maundy Thursday",
                lang==Lang.SE?"Rörligt datum, torsdagen före Påskdagen":"Movable date, Thursday before Easter Sunday"));
        holidays.add(new Holiday(easter.minusDays(2),
                lang==Lang.SE?"Långfredagen":"Good Friday",
                lang==Lang.SE?"Rörligt datum, fredagen före Påskdagen":"Movable date, Friday before Easter Sunday"));
        holidays.add(new Holiday(easter.minusDays(1),
                lang==Lang.SE?"Påskafton":"Easter Eve",
                lang==Lang.SE?"Rörligt datum, lördagen före Påskdagen":"Movable date, Saturday before Easter Sunday"));
        holidays.add(new Holiday(easter,
                lang==Lang.SE?"Påskdagen":"Easter Sunday",
                lang==Lang.SE?"Rörligt datum, första söndagen efter ecklesiastisk fullmåne, efter vårdagjämningen":"Movable date, first Sunday after the ecclesiastical full moon following the vernal equinox"));
        holidays.add(new Holiday(easter.plusDays(1),
                lang==Lang.SE?"Annandag påsk":"Easter Monday",
                lang==Lang.SE?"Rörligt datum, dagen efter påskdagen (d.v.s. en måndag)":"Movable date, the day after Easter Sunday (Monday)"));
        holidays.add(new Holiday(easter.plusDays(39),
                lang==Lang.SE?"Kristi himmelsfärdsdag":"Ascension Day",
                lang==Lang.SE?"Rörligt datum, sjätte torsdagen efter påskdagen":"Movable date, the sixth Thursday after Easter Sunday"));
        LocalDate pentecost = easter.plusDays(49);
        holidays.add(new Holiday(pentecost.minusDays(1),
                lang==Lang.SE?"Pingstafton":"Whitsun Eve",
                lang==Lang.SE?"Rörligt datum, dagen före pingstdagen (d.v.s. en lördag)":"Movable date, the day before Pentecost (Saturday)"));
        holidays.add(new Holiday(pentecost,
                lang==Lang.SE?"Pingstdagen":"Pentecost",
                lang==Lang.SE?"Rörligt datum, sjunde söndagen efter påskdagen":"Movable date, seventh Sunday after Easter Sunday"));
        // Midsummer
        holidays.add(new Holiday(findDayInRange(year, Month.JUNE, 19, 25, DayOfWeek.FRIDAY),
                lang==Lang.SE?"Midsommarafton":"Midsummer Eve",
                lang==Lang.SE?"Rörligt datum, fredagen mellan 19 juni och 25 juni (fredagen före midsommardagen)":"Movable date, Friday between June 19 and June 25"));
        holidays.add(new Holiday(findDayInRange(year, Month.JUNE, 20, 26, DayOfWeek.SATURDAY),
                lang==Lang.SE?"Midsommardagen":"Midsummer Day",
                lang==Lang.SE?"Rörligt datum, lördagen mellan 20 juni och 26 juni":"Movable date, Saturday between June 20 and June 26"));
        // All Saints
        holidays.add(new Holiday(findDayInRange(year, Month.OCTOBER, 30, 5, DayOfWeek.FRIDAY),
                lang==Lang.SE?"Allhelgonaafton":"All Saints' Eve",
                lang==Lang.SE?"Rörligt datum, fredagen mellan 30 oktober och 5 november":"Movable date, Friday between October 30 and November 5"));
        holidays.add(new Holiday(findDayInRange(year, Month.OCTOBER, 31, 6, DayOfWeek.SATURDAY),
                lang==Lang.SE?"Alla helgons dag":"All Saints' Day",
                lang==Lang.SE?"Rörligt datum, lördagen mellan 31 oktober och 6 november":"Movable date, Saturday between October 31 and November 6"));

        // log logic in single line
        StringBuilder sb = new StringBuilder();
        for (Holiday h : holidays) {
            sb.append(h.getName()).append("(").append(h.getDescription()).append("); ");
        }
        logger.info(sb.toString());

        return holidays;
    }

    private static LocalDate calculateEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }

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