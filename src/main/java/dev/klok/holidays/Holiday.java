package dev.klok.holidays;

import java.time.LocalDate;

/**
 * Represents a Swedish holiday, including official public holidays (helgdagar)
 * as well as other significant non-working days and eves common in Sweden.
 * Each Holiday has a date, a localized name, and a description of its date logic.
 */
public class Holiday {
    private final LocalDate date;
    private final String name;
    private final String description;

    /**
     * Constructs a Holiday instance.
     * @param date the date of the holiday
     * @param name the name of the holiday in the chosen language
     * @param description a description of the holiday date logic
     */
    public Holiday(LocalDate date, String name, String description) {
        this.date = date;
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the date of this holiday.
     * @return the holiday date
     */
    public LocalDate getDate() { return date; }

    /**
     * Gets the localized name of this holiday.
     * @return the holiday name
     */
    public String getName() { return name; }

    /**
     * Gets the localized description of this holiday.
     * @return the holiday description
     */
    public String getDescription() { return description; }
}
