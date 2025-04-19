package dev.klok.holidays;

/**
 * Interface for defining custom holiday calculation rules.
 * Implementations of this interface can calculate a holiday date
 * and provide its details (name, description) for a given year and language.
 *
 * @see SwedishHolidays#addCustomHolidayRule(HolidayRule)
 */
@FunctionalInterface
public interface HolidayRule {
    /**
     * Calculates the custom holiday for the given year, if applicable.
     *
     * @param year The year for which to calculate the holiday.
     * @param lang The desired language for the holiday's name and description (SE or EN).
     * @return A Holiday object if the rule defines a holiday for this year, otherwise null.
     */
    Holiday calculateHoliday(int year, SwedishHolidays.Lang lang);
}