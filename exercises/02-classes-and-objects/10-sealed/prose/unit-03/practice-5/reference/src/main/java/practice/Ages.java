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
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return new Err("not a number: " + text);
        }
        if (value < 0 || value > 150) {
            return new Err("out of range: " + value);
        }
        return new Ok(value);
    }

    public static Result sumAges(List<String> texts) {
        int total = 0;
        for (String text : texts) {
            switch (parseAge(text)) {
                case Ok ok -> total += ok.value();
                case Err err -> {
                    return err;
                }
            }
        }
        return new Ok(total);
    }
}
