package smoke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AdderTest {
    @Test
    void addsTwoNumbers() {
        assertEquals(2, Adder.add(1, 1));
    }
}
