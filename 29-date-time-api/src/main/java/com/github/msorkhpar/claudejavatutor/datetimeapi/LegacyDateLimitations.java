package com.github.msorkhpar.claudejavatutor.datetimeapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Demonstrates limitations of the legacy java.util.Date and java.util.Calendar classes.
 * These issues motivated the introduction of java.time in Java 8.
 */
public class LegacyDateLimitations {

    /**
     * Demonstrates that java.util.Date is mutable - dates can be changed after creation.
     */
    public static Date createAndMutateDate() {
        Date date = new Date(2024 - 1900, Calendar.JANUARY, 1); // Year offset from 1900
        date.setYear(2025 - 1900); // Mutated!
        return date;
    }

    /**
     * Demonstrates the confusing year offset (years since 1900) in java.util.Date.
     */
    @SuppressWarnings("deprecation")
    public static int getYearFromLegacyDate(Date date) {
        return date.getYear() + 1900; // Must add 1900 to get the actual year
    }

    /**
     * Demonstrates the confusing zero-based month indexing in Calendar.
     * January = 0, February = 1, ..., December = 11
     */
    public static int getMonthFromCalendar(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day); // Must subtract 1 for correct month
        return calendar.get(Calendar.MONTH); // Returns 0-based month
    }

    /**
     * Formats with a formatter the caller may share. Correct on one thread; see
     * {@link #wrongParsesWithSharedSimpleDateFormat(int, int, int)} for what sharing does.
     */
    public static String formatDateUnsafe(Date date, SimpleDateFormat sharedFormatter) {
        return sharedFormatter.format(date);
    }

    /**
     * Demonstrates that SimpleDateFormat is NOT thread-safe: ONE instance is shared by
     * {@code threads} threads, each parsing {@code parsesPerThread} different dates.
     * Every result is checked against the date that was parsed; a wrong date or an
     * exception (NumberFormatException, ArrayIndexOutOfBoundsException, ...) counts as
     * one corrupted parse. The formatter keeps its intermediate state in a shared
     * Calendar, so concurrent parses overwrite each other's fields.
     *
     * <p>A race only shows when the threads really overlap, which a single short run
     * does not guarantee (on a busy or small machine one thread may finish before the
     * next starts). So the workload is repeated, up to {@code maxRounds} rounds and at
     * most {@value #TIMEOUT_SECONDS} seconds in total, until a round shows corruption.</p>
     *
     * @return the number of corrupted parses in the first round that showed any
     *         (0 means no corruption was observed within the bounds)
     */
    public static int wrongParsesWithSharedSimpleDateFormat(int threads, int parsesPerThread, int maxRounds) {
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("maxRounds must be positive");
        }
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        for (int round = 0; round < maxRounds && System.nanoTime() < deadline; round++) {
            SimpleDateFormat shared = new SimpleDateFormat("yyyy-MM-dd");
            shared.setTimeZone(TimeZone.getTimeZone("UTC"));
            int wrong = countWrongParses(threads, parsesPerThread, text -> {
                Date parsed = shared.parse(text);
                return parsed.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            });
            if (wrong > 0) {
                return wrong;
            }
        }
        return 0;
    }

    /**
     * The same workload with ONE shared {@link DateTimeFormatter}, which is immutable and
     * thread-safe: every one of the {@code rounds} rounds runs, and the total is always 0.
     */
    public static int wrongParsesWithSharedDateTimeFormatter(int threads, int parsesPerThread, int rounds) {
        if (rounds <= 0) {
            throw new IllegalArgumentException("rounds must be positive");
        }
        DateTimeFormatter shared = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        int total = 0;
        for (int round = 0; round < rounds; round++) {
            total += countWrongParses(threads, parsesPerThread, text -> LocalDate.parse(text, shared));
        }
        return total;
    }

    private interface DateParser {
        LocalDate parse(String text) throws Exception;
    }

    /** Upper bound on the whole run, so the demonstration can never hang. */
    private static final long TIMEOUT_SECONDS = 30;

    private static int countWrongParses(int threads, int parsesPerThread, DateParser parser) {
        if (threads <= 0 || parsesPerThread <= 0) {
            throw new IllegalArgumentException("threads and parsesPerThread must be positive");
        }
        // Daemon threads: even a worker stuck inside a corrupted formatter cannot keep the JVM alive
        ExecutorService pool = Executors.newFixedThreadPool(threads, runnable -> {
            Thread t = new Thread(runnable, "shared-formatter-demo");
            t.setDaemon(true);
            return t;
        });
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                LocalDate first = LocalDate.of(2000, 1, 1).plusDays(t * 1000L);
                results.add(pool.submit(() -> {
                    start.await();
                    int wrong = 0;
                    for (int i = 0; i < parsesPerThread; i++) {
                        LocalDate expected = first.plusDays(i % 1000);
                        try {
                            if (!expected.equals(parser.parse(expected.toString()))) {
                                wrong++;
                            }
                        } catch (Exception e) {
                            wrong++; // corrupted state surfaced as an exception
                        }
                    }
                    return wrong;
                }));
            }
            start.countDown(); // release all threads at once to maximise overlap
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
            int total = 0;
            for (Future<Integer> result : results) {
                total += result.get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
            }
            return total;
        } catch (TimeoutException e) {
            throw new IllegalStateException("Demonstration did not finish within " + TIMEOUT_SECONDS + "s", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * Demonstrates Calendar's lenient mode allowing invalid dates silently.
     */
    public static Date createLenientDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(true); // default behavior
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    /**
     * Demonstrates strict mode that can reject invalid dates.
     */
    public static Date createStrictDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        calendar.set(year, month - 1, day);
        return calendar.getTime(); // May throw if invalid
    }

    /**
     * Demonstrates what a java.util.Date actually carries: a single instant (milliseconds
     * since 1970-01-01T00:00:00Z) and nothing else -- no calendar date, no time zone.
     * A time of day is therefore always present, and the calendar day and time you "see"
     * depend on the zone you choose to look at it in: the same Date is a different day in
     * different zones. There is no clean way to represent just a date.
     *
     * @return the Date as "yyyy-MM-dd HH:mm" in the given zone
     */
    public static String dateAndTimeIn(Date date, TimeZone zone) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        formatter.setTimeZone(zone);
        return formatter.format(date);
    }

    /**
     * Demonstrates that java.sql.Date extends java.util.Date but has different semantics,
     * leading to confusion in the API.
     */
    public static java.sql.Date convertToSqlDate(Date utilDate) {
        return new java.sql.Date(utilDate.getTime());
    }

    /**
     * Demonstrates that Date.toString() uses the system default timezone,
     * making the output unpredictable across environments.
     */
    public static String formatWithDefaultTimezone(Date date) {
        return date.toString(); // Output depends on JVM timezone
    }

    /**
     * Demonstrates parsing fragility with SimpleDateFormat.
     */
    public static Date parseDateString(String dateStr, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        return sdf.parse(dateStr);
    }

    /**
     * Demonstrates that Calendar silently overflows months/days in lenient mode.
     */
    public static Date overflowingCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setLenient(true);
        cal.set(2024, 12, 1); // Month 12 = January of next year (0-indexed)
        return cal.getTime();
    }
}
