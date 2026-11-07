package practice;

/**
 * Places a new tenant: how many bytes it may store, and which shard holds each of its keys.
 */
public class TenantStorage {

    /** Where a tenant's key lives and how much the tenant may store. */
    public record Placement(int shard, long quotaBytes) {}

    private static final int BYTES_PER_MB = 1024 * 1024;

    private final int shards;

    public TenantStorage(int shards) {
        if (shards <= 0) throw new IllegalArgumentException("shards must be positive: " + shards);
        this.shards = shards;
    }

    /** Places one key of a tenant that has {@code users} users with {@code mbPerUser} megabytes each. */
    public Placement place(String key, int users, int mbPerUser) {
        return new Placement(shardFor(key), quotaBytes(users, mbPerUser));
    }

    /** The tenant's quota in bytes: users times megabytes per user times BYTES_PER_MB. */
    long quotaBytes(int users, int mbPerUser) {
        return (long) (users * mbPerUser * BYTES_PER_MB);
    }

    /** The shard, from 0 to shards - 1, that holds this key; the same key always gets the same shard. */
    int shardFor(String key) {
        return Math.floorMod(key.hashCode(), shards);
    }
}
