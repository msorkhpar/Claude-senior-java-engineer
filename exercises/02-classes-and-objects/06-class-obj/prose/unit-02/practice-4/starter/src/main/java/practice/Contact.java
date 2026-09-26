package practice;

import java.time.LocalDate;
import java.util.List;

public class Contact {

    public Contact(String name, LocalDate dateOfBirth, String email, List<String> phones) {
        throw new UnsupportedOperationException("write the full constructor");
    }

    public Contact(String name, LocalDate dateOfBirth) {
        throw new UnsupportedOperationException("write this constructor");
    }

    public Contact(Contact other) {
        throw new UnsupportedOperationException("write the copy constructor");
    }

    public String getName() {
        throw new UnsupportedOperationException("write getName");
    }

    public LocalDate getDateOfBirth() {
        throw new UnsupportedOperationException("write getDateOfBirth");
    }

    public String getEmail() {
        throw new UnsupportedOperationException("write getEmail");
    }

    public List<String> getPhones() {
        throw new UnsupportedOperationException("write getPhones");
    }

    public void addPhone(String phone) {
        throw new UnsupportedOperationException("write addPhone");
    }
}
