package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The chat-ops bot's command table. Commands are filed by their hashCode() when the table is
 * built, and indexOf turns a typed name into the command's position, which the bot switches on.
 */
public final class CommandTable {

    private final List<String> commands;
    private final Map<Integer, List<String>> byHash = new HashMap<>();

    public CommandTable(String... commands) {
        this.commands = List.of(commands);
        for (String command : commands) {
            file(command);
        }
    }

    // TODO: file one command in byHash, under its hashCode(), so indexOf can find it.
    void file(String command) {
        throw new IllegalStateException("TODO: file");
    }

    // TODO: the command's position in the list given to the constructor, or -1 when the typed name
    // is not a command. Looks the name up in byHash.
    public int indexOf(String name) {
        throw new IllegalStateException("TODO: indexOf");
    }

    /** What the bot replies to a typed command. */
    public String reply(String typed) {
        return switch (indexOf(typed)) {
            case -1 -> "unknown command: " + typed;
            default -> "running " + commands.get(indexOf(typed));
        };
    }
}
