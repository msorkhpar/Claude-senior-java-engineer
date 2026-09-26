package practice;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
public final class LockExecutor {
    private final Lock lock;
    public LockExecutor(Lock lock) { this.lock = Objects.requireNonNull(lock, "lock must not be null"); }
    public <T> T withLock(Supplier<T> action) { Objects.requireNonNull(action, "action"); lock.lock(); try { return action.get(); } finally { lock.unlock(); } }
    public void withLockRun(Runnable action) { Objects.requireNonNull(action, "action"); withLock(() -> { action.run(); return null; }); }
    public static final class Account {
        private final LockExecutor locker; private int balance;
        public Account(LockExecutor locker, int initialBalance) { this.locker = Objects.requireNonNull(locker, "locker must not be null"); this.balance = initialBalance; }
        public int deposit(int amount) { locker.withLockRun(() -> { }); balance += amount; return balance; }
        public int withdraw(int amount) { return locker.withLock(() -> balance -= amount); }
        public int getBalance() { return locker.withLock(() -> balance); }
    }
}
