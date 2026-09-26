package practice;

import java.util.List;

public class Ages {

    public sealed interface Result permits Ok, Err {
    }

    public record Ok(int value) implements Result {
    }

    public record Err(String message) implements Result {
    }

    public static Result parseAge(String text) {
        throw new UnsupportedOperationException("write parseAge");
    }

    public static Result sumAges(List<String> texts) {
        throw new UnsupportedOperationException("write sumAges");
    }
}
