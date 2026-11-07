package practice;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AccountDirectoryTest {

    private static AccountDirectory directory() {
        AccountDirectory directory = new AccountDirectory();
        directory.open(518L, "Jane Doe");
        directory.open(6_000_000_123L, "Example Shipping Ltd");
        directory.open(90_210L, "John Roe");
        return directory;
    }

    @Test
    void findsTheOwnerOfAnEventFromTheBatchFeed() {
        AccountDirectory directory = directory();

        assertThat(directory.ownerOf(Map.of("accountId", 518L))).contains("Jane Doe");
        assertThat(directory.ownerOf(Map.of("accountId", 90_210L))).contains("John Roe");
        assertThat(directory.ownerOf(Map.of("accountId", 777L))).isEmpty();
        assertThat(directory.isOpen(4_444)).isFalse();
    }

    @Test
    void findsTheOwnerWhenTheDecoderGaveAnInteger() {
        AccountDirectory directory = directory();

        assertThat(directory.ownerOf(Map.of("accountId", 518))).contains("Jane Doe");
        assertThat(directory.ownerOf(Map.of("accountId", 90_210))).contains("John Roe");
    }

    @Test
    void findsTheOwnerOfAnIdBeyondTheIntRange() {
        AccountDirectory directory = directory();

        assertThat(directory.ownerOf(Map.of("accountId", 6_000_000_123L))).contains("Example Shipping Ltd");
        assertThat(directory.ownerOf(Map.of("accountId", 518L + 4_294_967_296L))).isEmpty();
    }

    @Test
    void anOldTerminalFindsAnOpenAccount() {
        AccountDirectory directory = directory();

        assertThat(directory.isOpen(518)).isTrue();
        assertThat(directory.isOpen(90_210)).isTrue();
    }
}
