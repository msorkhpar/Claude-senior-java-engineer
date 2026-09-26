package practice;

import java.util.concurrent.ThreadFactory;

public final class CountingFactory implements ThreadFactory {

    public CountingFactory(String prefix) {
    }

    /** An unstarted virtual thread named prefix + n that runs the task and is counted while it does. */
    @Override
    public Thread newThread(Runnable task) {
        throw new UnsupportedOperationException("write newThread");
    }

    /** How many of this factory's threads are running their task. */
    public int running() {
        throw new UnsupportedOperationException("write running");
    }
}
