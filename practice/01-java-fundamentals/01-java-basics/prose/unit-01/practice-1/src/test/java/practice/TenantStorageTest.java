package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TenantStorageTest {

    private static final long MB = 1024L * 1024;

    @Test
    void placesASmallTenantOnAnExistingShardWithItsQuota() {
        TenantStorage storage = new TenantStorage(7);
        for (String key : List.of("orders/2024/0001", "invoices/acme", "zebra-key-991", "x")) {
            TenantStorage.Placement placement = storage.place(key, 3, 500);

            assertThat(placement.shard()).isBetween(0, 6);
            assertThat(storage.place(key, 3, 500).shard()).isEqualTo(placement.shard());
            assertThat(placement.quotaBytes()).isEqualTo(1_500 * MB);
        }
    }

    @Test
    void aQuotaAboveTwoGigabytesIsExact() {
        TenantStorage storage = new TenantStorage(7);

        assertThat(storage.place("invoices/acme", 4, 1_000).quotaBytes()).isEqualTo(4_000 * MB);
    }

    @Test
    void aTenantWithManyUsersAndLargeAllowancesGetsItsExactQuota() {
        TenantStorage storage = new TenantStorage(7);

        assertThat(storage.place("invoices/acme", 50_000, 100_000).quotaBytes())
                .isEqualTo(5_000_000_000L * MB);
    }

    @Test
    void everyKeyLandsOnAShardThatExists() {
        TenantStorage storage = new TenantStorage(7);
        String key = "polygenelubricants";            // its hashCode() is Integer.MIN_VALUE

        int shard = storage.place(key, 3, 500).shard();

        assertThat(shard).isBetween(0, 6);
        assertThat(storage.place(key, 3, 500).shard()).isEqualTo(shard);
    }
}
