package dev.klok.holidays;

import java.time.LocalDate;

public class Holiday {
    private final LocalDate date;
    private final String name;
    private final String description;

    public Holiday(LocalDate date, String name, String description) {
        this.date = date;
        this.name = name;
        this.description = description;
    }

    public LocalDate getDate() { return date; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
