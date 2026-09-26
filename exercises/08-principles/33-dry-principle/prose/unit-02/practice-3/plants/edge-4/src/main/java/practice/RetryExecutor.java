package practice;
import java.util.Objects;
import java.util.concurrent.Callable;
public final class RetryExecutor {
    private final int maxRetries; private final long retryDelayMs;
    public RetryExecutor(int maxRetries, long retryDelayMs) { if (maxRetries < 0 || retryDelayMs < 0) throw new IllegalArgumentException("negative"); this.maxRetries = maxRetries; this.retryDelayMs = retryDelayMs; }
    public <T> T executeWithRetry(Callable<T> action) throws Exception { Exception last = null; for (int a = 0; a <= maxRetries; a++) { try { return action.call(); } catch (Exception e) { last = e; } } throw last; }
}
