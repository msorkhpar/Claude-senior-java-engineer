package com.github.msorkhpar.claudejavatutor.creationalpatterns;

import java.util.*;

/**
 * Demonstrates the Builder pattern with multiple approaches:
 * fluent builder, builder with required parameters, GoF builder with director,
 * and generic builder for inheritance hierarchies.
 */
public class BuilderPattern {

    // =====================================================
    // Fluent Builder (Effective Java style)
    // =====================================================

    /**
     * Immutable User class constructed via a fluent Builder.
     * Demonstrates required vs. optional parameters, validation, and defensive copies.
     */
    public static final class User {
        private final String name;          // Required
        private final String email;         // Required
        private final int age;              // Optional (default 0)
        private final String phone;         // Optional
        private final String address;       // Optional
        private final boolean active;       // Optional (default true)
        private final List<String> roles;   // Optional (default empty)

        private User(Builder builder) {
            this.name = builder.name;
            this.email = builder.email;
            this.age = builder.age;
            this.phone = builder.phone;
            this.address = builder.address;
            this.active = builder.active;
            this.roles = List.copyOf(builder.roles);
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public int getAge() { return age; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        public boolean isActive() { return active; }
        public List<String> getRoles() { return roles; }

        public static Builder builder(String name, String email) {
            return new Builder(name, email);
        }

        public static final class Builder {
            // Required parameters
            private final String name;
            private final String email;

            // Optional parameters with defaults
            private int age = 0;
            private String phone;
            private String address;
            private boolean active = true;
            private List<String> roles = new ArrayList<>();

            public Builder(String name, String email) {
                this.name = Objects.requireNonNull(name, "Name is required");
                this.email = Objects.requireNonNull(email, "Email is required");
                if (name.isBlank()) {
                    throw new IllegalArgumentException("Name cannot be blank");
                }
                if (email.isBlank()) {
                    throw new IllegalArgumentException("Email cannot be blank");
                }
            }

            public Builder age(int age) {
                this.age = age;
                return this;
            }

            public Builder phone(String phone) {
                this.phone = phone;
                return this;
            }

            public Builder address(String address) {
                this.address = address;
                return this;
            }

            public Builder active(boolean active) {
                this.active = active;
                return this;
            }

            public Builder role(String role) {
                this.roles.add(Objects.requireNonNull(role, "Role cannot be null"));
                return this;
            }

            public Builder roles(List<String> roles) {
                this.roles = new ArrayList<>(Objects.requireNonNull(roles, "Roles cannot be null"));
                return this;
            }

            public User build() {
                if (age < 0) {
                    throw new IllegalStateException("Age cannot be negative: " + age);
                }
                if (age > 150) {
                    throw new IllegalStateException("Age is unrealistically high: " + age);
                }
                if (!email.contains("@")) {
                    throw new IllegalStateException("Invalid email format: " + email);
                }
                return new User(this);
            }
        }
    }

    // =====================================================
    // GoF Builder with Director
    // =====================================================

    /**
     * Product: a structured document with sections.
     */
    public static final class Document {
        private String title;
        private String header;
        private String body;
        private String footer;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getHeader() { return header; }
        public void setHeader(String header) { this.header = header; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public String getFooter() { return footer; }
        public void setFooter(String footer) { this.footer = footer; }

        @Override
        public String toString() {
            return String.join("\n",
                    title != null ? title : "",
                    header != null ? header : "",
                    body != null ? body : "",
                    footer != null ? footer : ""
            );
        }
    }

    /**
     * Builder interface (GoF style) defining construction steps.
     */
    public interface DocumentBuilder {
        void buildTitle(String title);
        void buildHeader();
        void buildBody(String content);
        void buildFooter();
        Document getResult();
    }

    /**
     * Concrete builder that creates an HTML-formatted document.
     */
    public static class HtmlDocumentBuilder implements DocumentBuilder {
        private final Document document = new Document();

        @Override
        public void buildTitle(String title) {
            document.setTitle("<h1>" + title + "</h1>");
        }

        @Override
        public void buildHeader() {
            document.setHeader("<header>Generated Document</header>");
        }

        @Override
        public void buildBody(String content) {
            document.setBody("<p>" + content + "</p>");
        }

        @Override
        public void buildFooter() {
            document.setFooter("<footer>End of Document</footer>");
        }

        @Override
        public Document getResult() {
            return document;
        }
    }

    /**
     * Concrete builder that creates a plain-text document.
     */
    public static class PlainTextDocumentBuilder implements DocumentBuilder {
        private final Document document = new Document();

        @Override
        public void buildTitle(String title) {
            document.setTitle("=== " + title + " ===");
        }

        @Override
        public void buildHeader() {
            document.setHeader("--- Generated Document ---");
        }

        @Override
        public void buildBody(String content) {
            document.setBody(content);
        }

        @Override
        public void buildFooter() {
            document.setFooter("--- End of Document ---");
        }

        @Override
        public Document getResult() {
            return document;
        }
    }

    /**
     * Director that orchestrates the building process.
     * The same director works with any builder implementation.
     */
    public static class DocumentDirector {

        public Document constructFullDocument(DocumentBuilder builder, String title, String content) {
            builder.buildTitle(title);
            builder.buildHeader();
            builder.buildBody(content);
            builder.buildFooter();
            return builder.getResult();
        }

        public Document constructMinimalDocument(DocumentBuilder builder, String title, String content) {
            builder.buildTitle(title);
            builder.buildBody(content);
            return builder.getResult();
        }
    }

    // =====================================================
    // Generic Builder for inheritance (self-type idiom)
    // =====================================================

    /**
     * Base class with a generic builder using the self-type idiom.
     */
    public static abstract class Pizza {
        private final String size;
        private final boolean cheese;
        private final boolean pepperoni;

        protected Pizza(AbstractBuilder<?> builder) {
            this.size = builder.size;
            this.cheese = builder.cheese;
            this.pepperoni = builder.pepperoni;
        }

        public String getSize() { return size; }
        public boolean hasCheese() { return cheese; }
        public boolean hasPepperoni() { return pepperoni; }
        public abstract String style();

        public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
            private final String size;
            private boolean cheese = false;
            private boolean pepperoni = false;

            public AbstractBuilder(String size) {
                this.size = Objects.requireNonNull(size, "Size is required");
            }

            public T cheese(boolean cheese) {
                this.cheese = cheese;
                return self();
            }

            public T pepperoni(boolean pepperoni) {
                this.pepperoni = pepperoni;
                return self();
            }

            protected abstract T self();
            public abstract Pizza build();
        }
    }

    /**
     * New York style pizza with thin crust option.
     */
    public static final class NyPizza extends Pizza {
        private final boolean thinCrust;

        private NyPizza(Builder builder) {
            super(builder);
            this.thinCrust = builder.thinCrust;
        }

        public boolean isThinCrust() { return thinCrust; }

        @Override
        public String style() { return "New York"; }

        public static class Builder extends Pizza.AbstractBuilder<Builder> {
            private boolean thinCrust = true;

            public Builder(String size) {
                super(size);
            }

            public Builder thinCrust(boolean thinCrust) {
                this.thinCrust = thinCrust;
                return this;
            }

            @Override
            protected Builder self() { return this; }

            @Override
            public NyPizza build() { return new NyPizza(this); }
        }
    }

    /**
     * Chicago style pizza with stuffed crust option.
     */
    public static final class ChicagoPizza extends Pizza {
        private final boolean stuffedCrust;

        private ChicagoPizza(Builder builder) {
            super(builder);
            this.stuffedCrust = builder.stuffedCrust;
        }

        public boolean isStuffedCrust() { return stuffedCrust; }

        @Override
        public String style() { return "Chicago"; }

        public static class Builder extends Pizza.AbstractBuilder<Builder> {
            private boolean stuffedCrust = false;

            public Builder(String size) {
                super(size);
            }

            public Builder stuffedCrust(boolean stuffedCrust) {
                this.stuffedCrust = stuffedCrust;
                return this;
            }

            @Override
            protected Builder self() { return this; }

            @Override
            public ChicagoPizza build() { return new ChicagoPizza(this); }
        }
    }

    // =====================================================
    // HttpRequest Builder (real-world example)
    // =====================================================

    /**
     * A realistic HttpRequest class demonstrating Builder with collections,
     * validation, and immutability.
     */
    public static final class HttpRequest {
        private final String url;
        private final String method;
        private final Map<String, String> headers;
        private final String body;
        private final int timeoutMs;

        private HttpRequest(Builder builder) {
            this.url = builder.url;
            this.method = builder.method;
            this.headers = Map.copyOf(builder.headers);
            this.body = builder.body;
            this.timeoutMs = builder.timeoutMs;
        }

        public String getUrl() { return url; }
        public String getMethod() { return method; }
        public Map<String, String> getHeaders() { return headers; }
        public String getBody() { return body; }
        public int getTimeoutMs() { return timeoutMs; }

        public static Builder builder(String url, String method) {
            return new Builder(url, method);
        }

        public static final class Builder {
            private final String url;
            private final String method;
            private final Map<String, String> headers = new LinkedHashMap<>();
            private String body;
            private int timeoutMs = 30_000;

            public Builder(String url, String method) {
                this.url = Objects.requireNonNull(url, "URL is required");
                this.method = Objects.requireNonNull(method, "Method is required");
                if (url.isBlank()) {
                    throw new IllegalArgumentException("URL cannot be blank");
                }
                if (method.isBlank()) {
                    throw new IllegalArgumentException("Method cannot be blank");
                }
            }

            public Builder header(String key, String value) {
                Objects.requireNonNull(key, "Header key cannot be null");
                Objects.requireNonNull(value, "Header value cannot be null");
                headers.put(key, value);
                return this;
            }

            public Builder body(String body) {
                this.body = body;
                return this;
            }

            public Builder timeoutMs(int timeoutMs) {
                this.timeoutMs = timeoutMs;
                return this;
            }

            public HttpRequest build() {
                if (timeoutMs < 0) {
                    throw new IllegalStateException("Timeout cannot be negative: " + timeoutMs);
                }
                if (("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) && body != null) {
                    throw new IllegalStateException(method + " requests should not have a body");
                }
                return new HttpRequest(this);
            }
        }
    }
}
