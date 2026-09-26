package practice;

import java.util.List;

public final class CommandHistory {

    public interface Command {
        void execute();

        void undo();

        String description();
    }

    public CommandHistory(int limit) {
        throw new UnsupportedOperationException("TODO");
    }

    public void executeCommand(Command command) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean undo() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean redo() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean canUndo() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean canRedo() {
        throw new UnsupportedOperationException("TODO");
    }

    public int undoSize() {
        throw new UnsupportedOperationException("TODO");
    }

    public int redoSize() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<String> getUndoHistory() {
        throw new UnsupportedOperationException("TODO");
    }
}
