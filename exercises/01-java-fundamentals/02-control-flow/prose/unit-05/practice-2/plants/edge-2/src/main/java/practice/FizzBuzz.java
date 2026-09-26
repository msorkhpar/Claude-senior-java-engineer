package practice;

public final class FizzBuzz {

    private FizzBuzz() {
    }

    /** Returns FizzBuzz, Fizz, Buzz or the number itself. */
    public static String say(int n) {
        return switch (n % 15) {
            case 0 -> "FizzBuzz";
            case 3, 6, 9, 12 -> "Fizz";
            case 5, 10 -> "Buzz";
            default -> String.valueOf(n);
        };
    }
}
