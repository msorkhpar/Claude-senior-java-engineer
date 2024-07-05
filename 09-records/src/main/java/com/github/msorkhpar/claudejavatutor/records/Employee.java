package com.github.msorkhpar.claudejavatutor.records;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

public record Employee(String name, int id, LocalDate hireDate) {
    // Compact constructor for validation
    public Employee {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(hireDate, "Hire date cannot be null");
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
    }

    // Custom method
    public boolean isNewHire() {
        System.out.println(LocalDate.now());
        return LocalDate.now(Clock.systemDefaultZone()).minusMonths(6).isBefore(hireDate);
    }

    // Override toString for custom formatting
    @Override
    public String toString() {
        return String.format("Employee(name=%s, id=%d, hired on %s)", name, id, hireDate);
    }
}