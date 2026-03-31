package com.github.msorkhpar.claudejavatutor.creationalpatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Builder Pattern Tests")
class BuilderPatternTest {

    @Nested
    @DisplayName("Fluent User Builder")
    class UserBuilderTest {

        @Test
        @DisplayName("Should build user with required parameters only")
        void testRequiredParamsOnly() {
            var user = BuilderPattern.User.builder("Alice", "alice@example.com").build();

            assertThat(user.getName()).isEqualTo("Alice");
            assertThat(user.getEmail()).isEqualTo("alice@example.com");
            assertThat(user.getAge()).isZero();
            assertThat(user.getPhone()).isNull();
            assertThat(user.getAddress()).isNull();
            assertThat(user.isActive()).isTrue();
            assertThat(user.getRoles()).isEmpty();
        }

        @Test
        @DisplayName("Should build user with all parameters")
        void testAllParams() {
            var user = BuilderPattern.User.builder("Bob", "bob@example.com")
                    .age(30)
                    .phone("+1234567890")
                    .address("123 Main St")
                    .active(false)
                    .role("ADMIN")
                    .role("USER")
                    .build();

            assertThat(user.getName()).isEqualTo("Bob");
            assertThat(user.getEmail()).isEqualTo("bob@example.com");
            assertThat(user.getAge()).isEqualTo(30);
            assertThat(user.getPhone()).isEqualTo("+1234567890");
            assertThat(user.getAddress()).isEqualTo("123 Main St");
            assertThat(user.isActive()).isFalse();
            assertThat(user.getRoles()).containsExactly("ADMIN", "USER");
        }

        @Test
        @DisplayName("Should produce immutable roles list")
        void testImmutableRoles() {
            var user = BuilderPattern.User.builder("Alice", "alice@example.com")
                    .role("ADMIN")
                    .build();

            assertThatThrownBy(() -> user.getRoles().add("HACKER"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should throw for null name")
        void testNullName() {
            assertThatThrownBy(() -> BuilderPattern.User.builder(null, "a@b.com"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Name");
        }

        @Test
        @DisplayName("Should throw for null email")
        void testNullEmail() {
            assertThatThrownBy(() -> BuilderPattern.User.builder("Alice", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Email");
        }

        @Test
        @DisplayName("Should throw for blank name")
        void testBlankName() {
            assertThatThrownBy(() -> BuilderPattern.User.builder("  ", "a@b.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");
        }

        @Test
        @DisplayName("Should throw for blank email")
        void testBlankEmail() {
            assertThatThrownBy(() -> BuilderPattern.User.builder("Alice", "  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");
        }

        @Test
        @DisplayName("Should throw for negative age at build time")
        void testNegativeAge() {
            var builder = BuilderPattern.User.builder("Alice", "alice@example.com").age(-1);

            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("Should throw for unrealistically high age")
        void testHighAge() {
            var builder = BuilderPattern.User.builder("Alice", "alice@example.com").age(200);

            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("unrealistically high");
        }

        @Test
        @DisplayName("Should throw for invalid email format")
        void testInvalidEmail() {
            var builder = BuilderPattern.User.builder("Alice", "not-an-email");

            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid email");
        }

        @Test
        @DisplayName("Should accept age of zero")
        void testZeroAge() {
            var user = BuilderPattern.User.builder("Baby", "parent@example.com").age(0).build();

            assertThat(user.getAge()).isZero();
        }

        @Test
        @DisplayName("Should accept age of 150")
        void testMaxAge() {
            var user = BuilderPattern.User.builder("Elder", "e@example.com").age(150).build();

            assertThat(user.getAge()).isEqualTo(150);
        }

        @Test
        @DisplayName("Should throw for null role")
        void testNullRole() {
            assertThatThrownBy(() ->
                    BuilderPattern.User.builder("Alice", "a@b.com").role(null)
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should replace roles via roles() method")
        void testSetRoles() {
            var user = BuilderPattern.User.builder("Alice", "a@b.com")
                    .role("OLD")
                    .roles(List.of("NEW1", "NEW2"))
                    .build();

            assertThat(user.getRoles()).containsExactly("NEW1", "NEW2");
        }

        @Test
        @DisplayName("Should allow builder reuse")
        void testBuilderReuse() {
            var builder = BuilderPattern.User.builder("Alice", "alice@example.com");

            var user1 = builder.age(25).build();
            var user2 = builder.age(30).build();

            assertThat(user1.getAge()).isEqualTo(25);
            assertThat(user2.getAge()).isEqualTo(30);
            assertThat(user1.getName()).isEqualTo(user2.getName());
        }
    }

    @Nested
    @DisplayName("GoF Builder with Director")
    class GoFBuilderTest {

        @Test
        @DisplayName("Should build full HTML document via Director")
        void testFullHtmlDocument() {
            var builder = new BuilderPattern.HtmlDocumentBuilder();
            var director = new BuilderPattern.DocumentDirector();

            var doc = director.constructFullDocument(builder, "My Title", "Hello World");

            assertThat(doc.getTitle()).isEqualTo("<h1>My Title</h1>");
            assertThat(doc.getHeader()).isEqualTo("<header>Generated Document</header>");
            assertThat(doc.getBody()).isEqualTo("<p>Hello World</p>");
            assertThat(doc.getFooter()).isEqualTo("<footer>End of Document</footer>");
        }

        @Test
        @DisplayName("Should build full plain text document via Director")
        void testFullPlainTextDocument() {
            var builder = new BuilderPattern.PlainTextDocumentBuilder();
            var director = new BuilderPattern.DocumentDirector();

            var doc = director.constructFullDocument(builder, "My Title", "Hello World");

            assertThat(doc.getTitle()).isEqualTo("=== My Title ===");
            assertThat(doc.getHeader()).isEqualTo("--- Generated Document ---");
            assertThat(doc.getBody()).isEqualTo("Hello World");
            assertThat(doc.getFooter()).isEqualTo("--- End of Document ---");
        }

        @Test
        @DisplayName("Should build minimal document (no header/footer)")
        void testMinimalDocument() {
            var builder = new BuilderPattern.HtmlDocumentBuilder();
            var director = new BuilderPattern.DocumentDirector();

            var doc = director.constructMinimalDocument(builder, "Quick Note", "Just a note");

            assertThat(doc.getTitle()).isEqualTo("<h1>Quick Note</h1>");
            assertThat(doc.getBody()).isEqualTo("<p>Just a note</p>");
            assertThat(doc.getHeader()).isNull();
            assertThat(doc.getFooter()).isNull();
        }

        @Test
        @DisplayName("Same Director should produce different results with different builders")
        void testDirectorWithDifferentBuilders() {
            var director = new BuilderPattern.DocumentDirector();

            var htmlDoc = director.constructFullDocument(
                    new BuilderPattern.HtmlDocumentBuilder(), "Test", "Content");
            var textDoc = director.constructFullDocument(
                    new BuilderPattern.PlainTextDocumentBuilder(), "Test", "Content");

            assertThat(htmlDoc.getTitle()).startsWith("<h1>");
            assertThat(textDoc.getTitle()).startsWith("===");
            assertThat(htmlDoc.getBody()).startsWith("<p>");
            assertThat(textDoc.getBody()).isEqualTo("Content");
        }

        @Test
        @DisplayName("Document toString should combine all parts")
        void testDocumentToString() {
            var builder = new BuilderPattern.PlainTextDocumentBuilder();
            var director = new BuilderPattern.DocumentDirector();

            var doc = director.constructFullDocument(builder, "Title", "Body");

            assertThat(doc.toString())
                    .contains("=== Title ===")
                    .contains("Body")
                    .contains("--- End of Document ---");
        }

        @Test
        @DisplayName("Document toString should handle null parts gracefully")
        void testDocumentToStringNullParts() {
            var doc = new BuilderPattern.Document();

            assertThat(doc.toString()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Generic Builder for Inheritance (Pizza)")
    class GenericBuilderTest {

        @Test
        @DisplayName("Should build NY pizza with defaults")
        void testNyPizzaDefaults() {
            var pizza = new BuilderPattern.NyPizza.Builder("Large").build();

            assertThat(pizza.getSize()).isEqualTo("Large");
            assertThat(pizza.isThinCrust()).isTrue();
            assertThat(pizza.hasCheese()).isFalse();
            assertThat(pizza.hasPepperoni()).isFalse();
            assertThat(pizza.style()).isEqualTo("New York");
        }

        @Test
        @DisplayName("Should build NY pizza with all options")
        void testNyPizzaAllOptions() {
            var pizza = new BuilderPattern.NyPizza.Builder("Medium")
                    .cheese(true)
                    .pepperoni(true)
                    .thinCrust(false)
                    .build();

            assertThat(pizza.getSize()).isEqualTo("Medium");
            assertThat(pizza.isThinCrust()).isFalse();
            assertThat(pizza.hasCheese()).isTrue();
            assertThat(pizza.hasPepperoni()).isTrue();
        }

        @Test
        @DisplayName("Should build Chicago pizza with defaults")
        void testChicagoPizzaDefaults() {
            var pizza = new BuilderPattern.ChicagoPizza.Builder("Large").build();

            assertThat(pizza.getSize()).isEqualTo("Large");
            assertThat(pizza.isStuffedCrust()).isFalse();
            assertThat(pizza.style()).isEqualTo("Chicago");
        }

        @Test
        @DisplayName("Should build Chicago pizza with stuffed crust")
        void testChicagoPizzaStuffedCrust() {
            var pizza = new BuilderPattern.ChicagoPizza.Builder("Large")
                    .stuffedCrust(true)
                    .cheese(true)
                    .build();

            assertThat(pizza.isStuffedCrust()).isTrue();
            assertThat(pizza.hasCheese()).isTrue();
        }

        @Test
        @DisplayName("Fluent chain should return correct builder type for NY pizza")
        void testFluentChainNy() {
            // This test verifies that cheese() returns NyPizza.Builder, not Pizza.AbstractBuilder
            BuilderPattern.NyPizza pizza = new BuilderPattern.NyPizza.Builder("Small")
                    .cheese(true)           // Returns NyPizza.Builder
                    .thinCrust(true)        // This call proves cheese() returned the right type
                    .pepperoni(true)        // Returns NyPizza.Builder
                    .build();

            assertThat(pizza.hasCheese()).isTrue();
            assertThat(pizza.isThinCrust()).isTrue();
            assertThat(pizza.hasPepperoni()).isTrue();
        }

        @Test
        @DisplayName("Fluent chain should return correct builder type for Chicago pizza")
        void testFluentChainChicago() {
            BuilderPattern.ChicagoPizza pizza = new BuilderPattern.ChicagoPizza.Builder("Large")
                    .pepperoni(true)        // Returns ChicagoPizza.Builder
                    .stuffedCrust(true)     // Proves pepperoni() returned correct type
                    .cheese(true)
                    .build();

            assertThat(pizza.hasPepperoni()).isTrue();
            assertThat(pizza.isStuffedCrust()).isTrue();
        }

        @Test
        @DisplayName("Should throw for null size")
        void testNullSize() {
            assertThatThrownBy(() -> new BuilderPattern.NyPizza.Builder(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Pizza abstract type should work polymorphically")
        void testPolymorphism() {
            BuilderPattern.Pizza ny = new BuilderPattern.NyPizza.Builder("L").build();
            BuilderPattern.Pizza chicago = new BuilderPattern.ChicagoPizza.Builder("L").build();

            assertThat(ny.style()).isEqualTo("New York");
            assertThat(chicago.style()).isEqualTo("Chicago");
        }
    }

    @Nested
    @DisplayName("HttpRequest Builder (Real-World Example)")
    class HttpRequestBuilderTest {

        @Test
        @DisplayName("Should build GET request with required params only")
        void testGetRequest() {
            var request = BuilderPattern.HttpRequest.builder("https://api.example.com", "GET").build();

            assertThat(request.getUrl()).isEqualTo("https://api.example.com");
            assertThat(request.getMethod()).isEqualTo("GET");
            assertThat(request.getHeaders()).isEmpty();
            assertThat(request.getBody()).isNull();
            assertThat(request.getTimeoutMs()).isEqualTo(30_000);
        }

        @Test
        @DisplayName("Should build POST request with headers and body")
        void testPostRequest() {
            var request = BuilderPattern.HttpRequest.builder("https://api.example.com/data", "POST")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer token123")
                    .body("{\"key\": \"value\"}")
                    .timeoutMs(5000)
                    .build();

            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getHeaders())
                    .containsEntry("Content-Type", "application/json")
                    .containsEntry("Authorization", "Bearer token123");
            assertThat(request.getBody()).isEqualTo("{\"key\": \"value\"}");
            assertThat(request.getTimeoutMs()).isEqualTo(5000);
        }

        @Test
        @DisplayName("Should produce immutable headers map")
        void testImmutableHeaders() {
            var request = BuilderPattern.HttpRequest.builder("https://api.com", "GET")
                    .header("Accept", "text/html")
                    .build();

            assertThatThrownBy(() -> request.getHeaders().put("X-Evil", "header"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should throw for null URL")
        void testNullUrl() {
            assertThatThrownBy(() -> BuilderPattern.HttpRequest.builder(null, "GET"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw for null method")
        void testNullMethod() {
            assertThatThrownBy(() -> BuilderPattern.HttpRequest.builder("https://api.com", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw for blank URL")
        void testBlankUrl() {
            assertThatThrownBy(() -> BuilderPattern.HttpRequest.builder("  ", "GET"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw for blank method")
        void testBlankMethod() {
            assertThatThrownBy(() -> BuilderPattern.HttpRequest.builder("https://api.com", "  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw for negative timeout")
        void testNegativeTimeout() {
            var builder = BuilderPattern.HttpRequest.builder("https://api.com", "GET").timeoutMs(-1);

            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("Should throw when GET has a body")
        void testGetWithBody() {
            var builder = BuilderPattern.HttpRequest.builder("https://api.com", "GET").body("data");

            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("GET");
        }

        @Test
        @DisplayName("Should throw when DELETE has a body")
        void testDeleteWithBody() {
            var builder = BuilderPattern.HttpRequest.builder("https://api.com", "DELETE").body("data");

            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("DELETE");
        }

        @Test
        @DisplayName("Should allow POST with body")
        void testPostWithBody() {
            var request = BuilderPattern.HttpRequest.builder("https://api.com", "POST")
                    .body("data")
                    .build();

            assertThat(request.getBody()).isEqualTo("data");
        }

        @Test
        @DisplayName("Should allow PUT with body")
        void testPutWithBody() {
            var request = BuilderPattern.HttpRequest.builder("https://api.com", "PUT")
                    .body("data")
                    .build();

            assertThat(request.getBody()).isEqualTo("data");
        }

        @Test
        @DisplayName("Should throw for null header key")
        void testNullHeaderKey() {
            assertThatThrownBy(() ->
                    BuilderPattern.HttpRequest.builder("https://api.com", "GET")
                            .header(null, "value")
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw for null header value")
        void testNullHeaderValue() {
            assertThatThrownBy(() ->
                    BuilderPattern.HttpRequest.builder("https://api.com", "GET")
                            .header("key", null)
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should allow zero timeout")
        void testZeroTimeout() {
            var request = BuilderPattern.HttpRequest.builder("https://api.com", "GET")
                    .timeoutMs(0)
                    .build();

            assertThat(request.getTimeoutMs()).isZero();
        }
    }
}
