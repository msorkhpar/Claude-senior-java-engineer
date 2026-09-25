package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketDispenserTest {

    @Test
    void handsOutTicketsInOrder() {
        TicketDispenser dispenser = new TicketDispenser();
        assertThat(dispenser.peek()).isEqualTo(1);
        assertThat(dispenser.take()).isEqualTo(1);
        assertThat(dispenser.take()).isEqualTo(2);
        assertThat(dispenser.peek()).isEqualTo(3);
        assertThat(dispenser.take()).isEqualTo(3);
    }

    @Test
    void skipReturnsTheNumberAfterTheSkip() {
        TicketDispenser dispenser = new TicketDispenser();
        assertThat(dispenser.skip()).isEqualTo(2);
        assertThat(dispenser.take()).isEqualTo(2);
        assertThat(dispenser.skip()).isEqualTo(4);
    }
}
