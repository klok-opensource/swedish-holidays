package dev.klok.holidays;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.stream.Collectors;


@BenchmarkMode(Mode.AverageTime) // Measure average time per operation
@OutputTimeUnit(TimeUnit.NANOSECONDS) // Output results in nanoseconds
@State(Scope.Benchmark) // State shared across all threads running the benchmark
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS) // 5 warmup iterations, 1 sec each
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS) // 10 measurement iterations, 1 sec each
@Fork(value = 1, jvmArgsAppend = {"-Xms2g", "-Xmx2g"}) // Run in 1 separate JVM process with 2GB heap
public class SwedishHolidaysPerformanceTest {

    // --- Benchmark State ---

    @Param({"2024", "2025", "2026"}) // Test across a few years
    private int year;

    private LocalDate[] testDates; // Array of dates to check
    private int dateIndex; // To cycle through dates

    // Configure the number of test dates
    private static final int NUM_DATES_TO_TEST = 500;

    @Setup(Level.Trial) // Run once per JMH trial (group of forks/iterations)
    public void setupTrial() {
        System.out.printf("Setting up trial for year %d...%n", year);

        // Generate test dates: Mix of holidays and non-holidays for the specific year
        testDates = new LocalDate[NUM_DATES_TO_TEST];
        List<LocalDate> holidaysOfYear = SwedishHolidays.listHolidays(year).stream()
                .map(Holiday::getDate)
                .collect(Collectors.toList());
        Random random = new Random(12345); // Seeded for reproducibility
        LocalDate startOfYear = LocalDate.of(year, 1, 1);

        for (int i = 0; i < NUM_DATES_TO_TEST; i++) {
            // Simple strategy: ~30% chance of picking a known holiday, 70% chance of random day
            if (!holidaysOfYear.isEmpty() && random.nextDouble() < 0.3) {
                testDates[i] = holidaysOfYear.get(random.nextInt(holidaysOfYear.size()));
            } else {
                // Generate a random day within the year
                int randomDay = random.nextInt(startOfYear.lengthOfYear()); // 0 to 364/365
                testDates[i] = startOfYear.plusDays(randomDay);
            }
        }
        dateIndex = 0; // Reset index for cycling
        System.out.println("Setup complete. Generated " + NUM_DATES_TO_TEST + " test dates.");
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() {
        // Reset customizations after the trial
        SwedishHolidays.clearAllCustomizations();
        System.out.println("Trial teardown complete.");
    }


    // Helper method to get the next date cyclically
    private LocalDate getNextDate() {
        LocalDate date = testDates[dateIndex];
        dateIndex = (dateIndex + 1) % NUM_DATES_TO_TEST;
        return date;
    }

    // --- Benchmarks ---

    @Benchmark
    public void isHoliday(Blackhole bh) {
        LocalDate dateToCheck = getNextDate();
        boolean result = SwedishHolidays.isHoliday(dateToCheck);
        bh.consume(result); // Consume result to prevent dead code elimination
    }

    @Benchmark
    public void listHolidays(Blackhole bh) {
        // Listing involves calculation/cache access but less frequently than isHoliday
        List<Holiday> result = SwedishHolidays.listHolidays(SwedishHolidays.Lang.EN, year);
        bh.consume(result);
    }


    // --- Main method to run the benchmark from IDE ---
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SwedishHolidaysPerformanceTest.class.getSimpleName())
                // .mode(Mode.Throughput) // Optionally change mode
                // .timeUnit(TimeUnit.MICROSECONDS) // Optionally change time unit
                // .addProfiler("gc") // Optional: Add GC profiler
                .build();

        new Runner(opt).run();
    }
}
