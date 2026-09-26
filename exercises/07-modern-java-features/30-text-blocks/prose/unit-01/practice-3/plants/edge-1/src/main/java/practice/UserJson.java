package practice;

public final class UserJson {

    private UserJson() {
    }

    /** Returns the four-line JSON object holding {@code name} and {@code age}. */
    public static String of(String name, int age) {
        String escaped = name.replace("\\", "\\\\");
        return "{\n"
                + "    \"name\": \"" + escaped + "\",\n"
                + "    \"age\": " + age + "\n"
                + "}";
    }
}
