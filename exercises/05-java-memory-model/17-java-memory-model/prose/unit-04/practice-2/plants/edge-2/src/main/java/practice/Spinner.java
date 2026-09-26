package practice;

public final class Spinner {

    private boolean stop;

    public void requestStop() {
        stop = true;
    }

    public boolean stopRequested() {
        return stop;
    }

    public int runUntilStopped(Runnable step) {
        int steps = 0;
        while (!stop) {
            step.run();
            steps++;
        }
        return steps;
    }
}
