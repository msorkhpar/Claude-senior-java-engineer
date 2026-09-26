package practice;

import java.util.Optional;

public final class User {

    private User() {
    }

    public static Builder builder() {
        throw new UnsupportedOperationException("write builder");
    }

    public String name() {
        throw new UnsupportedOperationException("write name");
    }

    public int age() {
        throw new UnsupportedOperationException("write age");
    }

    public Optional<String> email() {
        throw new UnsupportedOperationException("write email");
    }

    public boolean active() {
        throw new UnsupportedOperationException("write active");
    }

    public static final class Builder {

        public Builder name(String name) {
            throw new UnsupportedOperationException("write name");
        }

        public Builder age(int age) {
            throw new UnsupportedOperationException("write age");
        }

        public Builder email(String email) {
            throw new UnsupportedOperationException("write email");
        }

        public Builder active(boolean active) {
            throw new UnsupportedOperationException("write active");
        }

        public User build() {
            throw new UnsupportedOperationException("write build");
        }
    }
}
