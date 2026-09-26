package practice;

import java.time.LocalDate;
import java.util.Objects;

public record Employee(String name, int id, LocalDate hireDate) {

    /** Refuse a null name or hire date (NullPointerException) and an id of 0 or less. */
    public Employee {
        throw new UnsupportedOperationException("write the compact constructor");
    }

    /** True when the hire date is strictly after the date six months before today. */
    public boolean isNewHire(LocalDate today) {
        throw new UnsupportedOperationException("write isNewHire");
    }

    /** "Employee(name=<name>, id=<id>, hired on <hireDate>)". */
    @Override
    public String toString() {
        throw new UnsupportedOperationException("write toString");
    }
}
