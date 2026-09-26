package practice;

public record PersonDTO(String name, int age, String email) {

    public PersonDTO {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public boolean isAdult() {
        return age >= 18;
    }

    /** One line "name,age,email", its fields stripped, as a PersonDTO. */
    public static PersonDTO parse(String line) {
        throw new UnsupportedOperationException("write parse");
    }
}
