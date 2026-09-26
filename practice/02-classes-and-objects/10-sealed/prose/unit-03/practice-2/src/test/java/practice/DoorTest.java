package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoorTest {

    @Test
    void aDoorOpensClosesLocksAndUnlocks() {
        Door.State state = new Door.Closed();
        state = Door.open(state);
        assertThat(state).isEqualTo(new Door.Opened());
        state = Door.close(state);
        assertThat(state).isEqualTo(new Door.Closed());
        state = Door.lock(state, "1234");
        assertThat(state).isEqualTo(new Door.Locked("1234"));
        state = Door.unlock(state, "1234");
        assertThat(state).isEqualTo(new Door.Closed());
    }

    @Test
    void aLockedDoorDoesNotOpen() {
        assertThatThrownBy(() -> Door.open(new Door.Locked("1234")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anOpenDoorCannotBeLocked() {
        assertThatThrownBy(() -> Door.lock(new Door.Opened(), "1234"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theWrongCodeKeepsItLocked() {
        assertThat(Door.unlock(new Door.Locked("1234"), "0000")).isEqualTo(new Door.Locked("1234"));
        assertThat(Door.close(new Door.Locked("1234"))).isEqualTo(new Door.Locked("1234"));
    }

    @Test
    void theCodeIsComparedByItsText() {
        Door.State locked = Door.lock(new Door.Closed(), new String("1234"));
        assertThat(Door.unlock(locked, new String("1234"))).isEqualTo(new Door.Closed());
    }
}
