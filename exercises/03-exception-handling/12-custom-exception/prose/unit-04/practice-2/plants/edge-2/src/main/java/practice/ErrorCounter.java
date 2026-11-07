package practice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Counts the error lines in each log of a nightly batch. Most hosts write UTF-8; the old
 * warehouse hosts write ISO-8859-1, so a log that isn't valid UTF-8 is read again in that
 * charset. A log that can't be read at all is skipped and reported, and the batch goes on.
 */
public final class ErrorCounter {

    /** Opens one log. In production: {@code cs -> Files.newBufferedReader(path, cs)}. */
    public interface Log {
        BufferedReader open(Charset charset) throws IOException;
    }

    /** Decides whether a line is an error. Rules are written by the teams that own the hosts. */
    public interface Rule {
        boolean isError(String line);
    }

    /** Error counts per log, and the reason each skipped log could not be read. */
    public record Report(Map<String, Long> errors, Map<String, IOException> skipped) {
    }

    private final Rule rule;

    public ErrorCounter(Rule rule) {
        this.rule = rule;
    }

    /** Scans the logs in the map's order. */
    public Report scan(Map<String, Log> logs) {
        Map<String, Long> errors = new LinkedHashMap<>();
        Map<String, IOException> skipped = new LinkedHashMap<>();
        for (Map.Entry<String, Log> log : logs.entrySet()) {
            try {
                errors.put(log.getKey(), countAnyCharset(log.getValue()));
            } catch (IOException e) {
                skipped.put(log.getKey(), e);
            }
        }
        return new Report(errors, skipped);
    }

    long countAnyCharset(Log log) throws IOException {
        try {
            return count(log, StandardCharsets.UTF_8);
        } catch (CharacterCodingException notUtf8) {
            return count(log, StandardCharsets.ISO_8859_1);
        }
    }

    /** The number of lines in the log that the rule calls errors, reading it in the given charset. */
    long count(Log log, Charset charset) throws IOException {
        try (BufferedReader reader = log.open(charset)) {
            return reader.lines().filter(rule::isError).count();
        }
    }
}
