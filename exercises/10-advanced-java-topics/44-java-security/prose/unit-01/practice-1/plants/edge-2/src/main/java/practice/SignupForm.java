package practice;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class SignupForm {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9]{3,19}");
    private static final Pattern AGE = Pattern.compile("[0-9]{1,3}");

    private SignupForm() {
    }

    /** Returns the NFKC-normalized username if it is 3 to 20 ASCII letters or digits. */
    public static String username(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("username is required");
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC);
        if (!USERNAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("username must be 3 to 20 letters or digits");
        }
        return normalized;
    }

    /** Returns the age if {@code raw} is ASCII digits with a value from 0 to 150. */
    public static int age(String raw) {
        if (raw == null || !AGE.matcher(raw).matches()) {
            throw new IllegalArgumentException("age must be a number");
        }
        int age = Integer.parseInt(raw);
        if (age > 150) {
            throw new IllegalArgumentException("age must be 0 to 150");
        }
        return age;
    }
}
