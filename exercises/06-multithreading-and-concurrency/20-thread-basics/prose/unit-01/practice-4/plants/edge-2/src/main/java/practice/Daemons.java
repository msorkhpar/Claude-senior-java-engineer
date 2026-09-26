package practice;

public final class Daemons {

    private Daemons() {
    }

    /** Makes a never-started thread a daemon and answers true; answers false, changing nothing, for a started one. */
    public static boolean markBackground(Thread thread) {
        if (thread.isAlive()) {
            return false;
        }
        thread.setDaemon(true);
        return true;
    }
}
