package practice;

import java.util.Optional;

public final class User {

    private final String name;
    private final int age;
    private final String email;
    private final boolean active;

    private User(Builder builder) {
        this.name = builder.name;
        this.age = builder.age == null ? 0 : builder.age;
        this.email = builder.email;
        this.active = builder.active;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String name() {
        return name;
    }

    public int age() {
        return age;
    }

    public Optional<String> email() {
        return Optional.ofNullable(email);
    }

    public boolean active() {
        return active;
    }

    public static final class Builder {

        private String name;
        private Integer age;
        private String email;
        private boolean active = true;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public User build() {
            if (name == null || name.isBlank()) {
                throw new IllegalStateException("name is required");
            }
            if (age != null && age < 0) {
                throw new IllegalStateException("age must be >= 0, was " + age);
            }
            return new User(this);
        }
    }
}
