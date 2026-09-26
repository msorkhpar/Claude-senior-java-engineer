package practice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class CommandHistory {

    public interface Command {
        void execute();

        void undo();

        String description();
    }

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();
    private final int limit;

    public CommandHistory(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be at least 1");
        }
        this.limit = limit;
    }

    public void executeCommand(Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        command.execute();
        pushUndo(command);
        redoStack.clear();
    }

    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        Command command = undoStack.removeLast();
        command.undo();
        redoStack.push(command);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        Command command = redoStack.pop();
        command.execute();
        pushUndo(command);
        return true;
    }

    private void pushUndo(Command command) {
        undoStack.addLast(command);
        if (undoStack.size() > limit) {
            undoStack.removeFirst();
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public int undoSize() {
        return undoStack.size();
    }

    public int redoSize() {
        return redoStack.size();
    }

    public List<String> getUndoHistory() {
        return undoStack.stream().map(Command::description).toList();
    }
}
