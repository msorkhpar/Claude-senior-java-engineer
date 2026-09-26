package practice;

public final class Launch {

    private Launch() {
    }

    /** Runs {@code task} on a new thread named {@code name}, and returns that thread without waiting. */
    public static Thread launch(String name, Runnable task) {
        Thread thread = new Thread(task, name);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return thread;
    }
}
