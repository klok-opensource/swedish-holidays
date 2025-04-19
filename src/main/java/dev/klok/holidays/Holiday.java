package dev.klok.holidays;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a Swedish holiday, including official public holidays (helgdagar)
 * as well as other significant non-working days and eves common in Sweden.
 * Each Holiday has a date, a localized name, and a description of its date logic.
 * <p>
 * This class is immutable.
 * </p>
 */
public final class Holiday { // Make final for immutability guarantees
    private final LocalDate date;
    private final String name;
    private final String description;

    /**
     * Constructs an immutable Holiday instance.
     * @param date the date of the holiday (required)
     * @param name the name of the holiday in the chosen language (required)
     * @param description a description of the holiday date logic (required)
     */
    public Holiday(LocalDate date, String name, String description) {
        // Add null checks for required fields
        this.date = Objects.requireNonNull(date, "Holiday date cannot be null");
        this.name = Objects.requireNonNull(name, "Holiday name cannot be null");
        this.description = Objects.requireNonNull(description, "Holiday description cannot be null");
    }

    /**
     * Gets the date of this holiday.
     * @return the holiday date (never null)
     */
    public LocalDate getDate() { return date; }

    /**
     * Gets the localized name of this holiday.
     * @return the holiday name (never null)
     */
    public String getName() { return name; }

    /**
     * Gets the localized description of this holiday.
     * @return the holiday description (never null)
     */
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holiday holiday = (Holiday) o;
        // Primarily compare by date, but include name for potential duplicates on same date
        // If only date uniqueness matters, remove name and description from equals/hashCode
        return Objects.equals(date, holiday.date) &&
                Objects.equals(name, holiday.name) && // Consider if name should be part of equality
                Objects.equals(description, holiday.description); // Consider if desc should be part of equality
    }

    @Override
    public int hashCode() {
        // Match the fields used in equals()
        return Objects.hash(date, name, description);
    }

    @Override
    public String toString() {
        return "Holiday{" +
                "date=" + date +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}