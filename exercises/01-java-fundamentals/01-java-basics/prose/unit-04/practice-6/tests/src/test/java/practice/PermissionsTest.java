package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.Permissions.EXECUTE;
import static practice.Permissions.READ;
import static practice.Permissions.WRITE;

class PermissionsTest {

    @Test
    void grantsTogglesRevokesAndChecks() {
        int mask = Permissions.grant(0, READ);
        assertThat(mask).isEqualTo(1);
        assertThat(Permissions.has(mask, READ)).isTrue();
        assertThat(Permissions.has(mask, WRITE)).isFalse();
        assertThat(Permissions.grant(mask, WRITE)).isEqualTo(3);
        assertThat(Permissions.grant(mask, READ)).isEqualTo(1);
        assertThat(Permissions.toggle(1, WRITE)).isEqualTo(3);
        assertThat(Permissions.toggle(3, WRITE)).isEqualTo(1);
        assertThat(Permissions.revoke(READ | WRITE | EXECUTE, WRITE)).isEqualTo(5);
    }

    @Test
    void revokingAnAbsentFlagChangesNothing() {
        assertThat(Permissions.revoke(READ, WRITE)).isEqualTo(READ);
        assertThat(Permissions.revoke(0, EXECUTE)).isEqualTo(0);
    }

    @Test
    void hasNeedsEveryBitOfACombinedFlag() {
        assertThat(Permissions.has(READ, READ | WRITE)).isFalse();
        assertThat(Permissions.has(READ | WRITE | EXECUTE, READ | WRITE)).isTrue();
    }
}
