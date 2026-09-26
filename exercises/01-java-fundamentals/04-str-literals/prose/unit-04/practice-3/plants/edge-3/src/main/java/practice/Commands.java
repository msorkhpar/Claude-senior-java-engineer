package practice;

public final class Commands {

    private Commands() {
    }

    /** Says whether input is the quit command, in any case and with spaces around it. */
    public static boolean isQuit(String input) {
        return input.trim().equalsIgnoreCase("quit");
    }
}
