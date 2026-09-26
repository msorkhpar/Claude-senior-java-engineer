package practice;

import java.time.LocalDate;
import java.util.Objects;

public record Employee(String name, int id, LocalDate hireDate) {

    /** Refuse a null name or hire date (NullPointerException) and an id of 0 or less. */
    public Employee {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(hireDate, "Hire date cannot be null");
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
    }

    /** True when the hire date is strictly after the date six months before today. */
    public boolean isNewHire(LocalDate today) {
        return today.minusMonths(6).isBefore(hireDate);
    }

    /** "Employee(name=<name>, id=<id>, hired on <hireDate>)". */
    @Override
    public String toString() {
        return String.format("Employee(name=%s, id=%d, hired on %s)", name, id, hireDate);
    }
}
