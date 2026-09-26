package practice;

public final class Spinner {

    private volatile boolean stop;

    public void requestStop() {
        stop = true;
    }

    public boolean stopRequested() {
        return stop;
    }

    public int runUntilStopped(Runnable step) {
        int steps = 0;
        do {
            step.run();
            steps++;
        } while (!stop);
        return steps;
    }
}
