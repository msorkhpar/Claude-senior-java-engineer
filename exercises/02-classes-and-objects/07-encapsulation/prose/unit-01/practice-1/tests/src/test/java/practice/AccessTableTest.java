package practice;

import org.junit.jupiter.api.Test;

import practice.AccessTable.From;
import practice.AccessTable.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.AccessTable.canAccess;

class AccessTableTest {

    @Test
    void publicIsEverywhereAndPrivateStaysInTheClass() {
        for (From from : From.values()) {
            assertThat(canAccess(Level.PUBLIC, from)).as("public from %s", from).isTrue();
        }
        assertThat(canAccess(Level.PRIVATE, From.SAME_CLASS)).isTrue();
        assertThat(canAccess(Level.PRIVATE, From.SAME_PACKAGE)).isFalse();
        assertThat(canAccess(Level.PRIVATE, From.SUBCLASS_OTHER_PACKAGE)).isFalse();
        assertThat(canAccess(Level.PRIVATE, From.OTHER_PACKAGE)).isFalse();
    }

    @Test
    void protectedIsVisibleInTheSamePackage() {
        assertThat(canAccess(Level.PROTECTED, From.SAME_PACKAGE)).isTrue();
        assertThat(canAccess(Level.PROTECTED, From.SUBCLASS_OTHER_PACKAGE)).isTrue();
        assertThat(canAccess(Level.PROTECTED, From.OTHER_PACKAGE)).isFalse();
    }

    @Test
    void packageAccessStopsAtAnotherPackage() {
        assertThat(canAccess(Level.PACKAGE, From.SAME_PACKAGE)).isTrue();
        assertThat(canAccess(Level.PACKAGE, From.SUBCLASS_OTHER_PACKAGE)).isFalse();
        assertThat(canAccess(Level.PACKAGE, From.OTHER_PACKAGE)).isFalse();
    }

    @Test
    void aNestedClassSeesPrivateMembers() {
        assertThat(canAccess(Level.PRIVATE, From.NESTED_CLASS)).isTrue();
        assertThat(canAccess(Level.PACKAGE, From.NESTED_CLASS)).isTrue();
    }
}
