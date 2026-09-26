package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ValidationTest {

    @Test
    void validatesAnOrderAndAnEmployee() {
        assertThat(Validation.validateOrder("c-1001", 25.0, 3)).isEmpty();
        assertThat(Validation.validateOrder("", 25.0, 3)).containsExactly("Customer ID is required");
        assertThat(Validation.validateOrder("c-1001", 0, 3)).containsExactly("Amount must be positive");
        assertThat(Validation.validateOrder("c-1001", 25.0, 0)).containsExactly("Quantity must be positive");
        assertThat(Validation.validateOrder("c-1001", 25.0, 20000)).containsExactly("Quantity exceeds maximum allowed (10000)");
        assertThat(Validation.validateEmployee("Ann", 30, 5000)).isEmpty();
        assertThat(Validation.validateEmployee("Bob", 15, 5000)).containsExactly("Age must be between 18 and 120");
        assertThat(Validation.validateEmployee("Bob", 30, -1)).containsExactly("Salary must be positive");
        assertThat(Validation.isNonBlank("x")).isTrue();
        assertThat(Validation.isPositive(2.5)).isTrue();
        assertThat(Validation.isPositive(0.5)).isTrue();
        assertThat(Validation.validateOrder("c-1001", 0.5, 3)).isEmpty();
        assertThat(Validation.isInRange(5, 1, 10)).isTrue();
    }

    @Test
    void nullIsInvalidNotAnError() {
        assertThat(Validation.isNonBlank(null)).isFalse();
        assertThat(Validation.isPositive(null)).isFalse();
        assertThat(Validation.validateOrder(null, 25.0, 3)).containsExactly("Customer ID is required");
        assertThat(Validation.validateEmployee(null, 30, 5000)).containsExactly("Name is required");
    }

    @Test
    void theEndsOfARangeAreInside() {
        assertThat(Validation.isInRange(1.0, 1.0, 2.0)).isTrue();
        assertThat(Validation.isInRange(2.0, 1.0, 2.0)).isTrue();
        assertThat(Validation.validateEmployee("Ann", 18, 5000)).isEmpty();
        assertThat(Validation.validateEmployee("Ann", 120, 5000)).isEmpty();
        assertThat(Validation.validateEmployee("Ann", 17, 5000)).containsExactly("Age must be between 18 and 120");
        assertThat(Validation.validateEmployee("Ann", 121, 5000)).containsExactly("Age must be between 18 and 120");
    }

    @Test
    void everyProblemIsListed() {
        assertThat(Validation.validateOrder("   ", -5, 20000))
                .containsExactly("Customer ID is required", "Amount must be positive", "Quantity exceeds maximum allowed (10000)");
        assertThat(Validation.validateEmployee(" ", 200, 0))
                .containsExactly("Name is required", "Age must be between 18 and 120", "Salary must be positive");
    }

    @Test
    void aNegativeQuantityIsNotAlsoTooLarge() throws Exception {
        assertThat(Validation.validateOrder("c-1", 10.0, -5)).containsExactly("Quantity must be positive");
    }

    @Test
    void theRangeHasExactEnds() throws Exception {
        assertThat(Validation.isInRange(18, 18, 120)).isTrue();
        assertThat(Validation.isInRange(17.9999999, 18, 120)).isFalse();
        assertThat(Validation.isInRange(120.0000001, 18, 120)).isFalse();
    }

    @Test
    void theMaximumItselfIsAllowed() throws Exception {
        assertThat(Validation.validateOrder("c-1", 10.0, 10000)).isEmpty();
        assertThat(Validation.validateOrder("c-1", 10.0, 10001)).containsExactly("Quantity exceeds maximum allowed (10000)");
    }

    @Test
    void notANumberIsNotPositive() throws Exception {
        assertThat(Validation.isPositive(Double.NaN)).isFalse();
        assertThat(Validation.validateOrder("c-1", Double.NaN, 1)).containsExactly("Amount must be positive");
    }
}
