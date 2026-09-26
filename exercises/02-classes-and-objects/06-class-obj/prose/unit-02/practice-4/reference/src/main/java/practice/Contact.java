package practice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Contact {
    private final String name;
    private final LocalDate dateOfBirth;
    private final String email;
    private final List<String> phones;

    public Contact(String name, LocalDate dateOfBirth, String email, List<String> phones) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "Date of birth cannot be null");
        this.email = email;
        this.phones = new ArrayList<>(phones);
    }

    public Contact(String name, LocalDate dateOfBirth) {
        this(name, dateOfBirth, null, List.of());
    }

    public Contact(Contact other) {
        this(other.name, other.dateOfBirth, other.email, other.phones);
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getPhones() {
        return List.copyOf(phones);
    }

    public void addPhone(String phone) {
        phones.add(phone);
    }
}
