package com.github.msorkhpar.claudejavatutor.behavioralpatterns;

import java.util.*;
import java.util.function.Consumer;

/**
 * Demonstrates the Command design pattern in Java.
 * The Command pattern encapsulates a request as an object, thereby letting you parameterize
 * clients with different requests, queue or log requests, and support undoable operations.
 */
public class CommandPattern {

    // ===================== Classic Command (Interface-based) =====================

    /**
     * Command interface with execute and optional undo.
     */
    public interface Command {
        void execute();

        void undo();

        String description();
    }

    /**
     * Receiver: a simple text editor that holds a buffer of text.
     */
    public static class TextEditor {
        private final StringBuilder content = new StringBuilder();

        public void insert(int position, String text) {
            if (position < 0 || position > content.length()) {
                throw new IndexOutOfBoundsException(
                        "Position %d is out of bounds for length %d".formatted(position, content.length()));
            }
            content.insert(position, text);
        }

        public void delete(int position, int length) {
            if (position < 0 || position + length > content.length()) {
                throw new IndexOutOfBoundsException(
                        "Cannot delete %d chars at position %d (content length: %d)".formatted(
                                length, position, content.length()));
            }
            content.delete(position, position + length);
        }

        public String getContent() {
            return content.toString();
        }

        public int length() {
            return content.length();
        }
    }

    /**
     * Concrete command: insert text at a position.
     */
    public static class InsertCommand implements Command {
        private final TextEditor editor;
        private final int position;
        private final String text;

        public InsertCommand(TextEditor editor, int position, String text) {
            if (editor == null) {
                throw new IllegalArgumentException("Editor cannot be null");
            }
            if (text == null) {
                throw new IllegalArgumentException("Text cannot be null");
            }
            this.editor = editor;
            this.position = position;
            this.text = text;
        }

        @Override
        public void execute() {
            editor.insert(position, text);
        }

        @Override
        public void undo() {
            editor.delete(position, text.length());
        }

        @Override
        public String description() {
            return "Insert '%s' at position %d".formatted(text, position);
        }
    }

    /**
     * Concrete command: delete text from a position.
     */
    public static class DeleteCommand implements Command {
        private final TextEditor editor;
        private final int position;
        private final int length;
        private String deletedText; // stored for undo

        public DeleteCommand(TextEditor editor, int position, int length) {
            if (editor == null) {
                throw new IllegalArgumentException("Editor cannot be null");
            }
            this.editor = editor;
            this.position = position;
            this.length = length;
        }

        @Override
        public void execute() {
            // Store text for undo before deleting
            deletedText = editor.getContent().substring(position, position + length);
            editor.delete(position, length);
        }

        @Override
        public void undo() {
            if (deletedText == null) {
                throw new IllegalStateException("Cannot undo a command that was never executed");
            }
            editor.insert(position, deletedText);
        }

        @Override
        public String description() {
            return "Delete %d chars at position %d".formatted(length, position);
        }
    }

    // ===================== Invoker: Command History with Undo/Redo =====================

    /**
     * Invoker that manages command execution and supports undo/redo.
     */
    public static class CommandHistory {
        private final Deque<Command> undoStack = new ArrayDeque<>();
        private final Deque<Command> redoStack = new ArrayDeque<>();

        public void executeCommand(Command command) {
            if (command == null) {
                throw new IllegalArgumentException("Command cannot be null");
            }
            command.execute();
            undoStack.push(command);
            redoStack.clear(); // new command invalidates redo history
        }

        public boolean undo() {
            if (undoStack.isEmpty()) {
                return false;
            }
            Command command = undoStack.pop();
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
            undoStack.push(command);
            return true;
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

    // ===================== Macro Command (Composite) =====================

    /**
     * A macro command that groups multiple commands into one.
     */
    public static class MacroCommand implements Command {
        private final List<Command> commands;
        private final String macroName;

        public MacroCommand(String macroName, List<Command> commands) {
            if (macroName == null || macroName.isBlank()) {
                throw new IllegalArgumentException("Macro name cannot be null or blank");
            }
            if (commands == null || commands.isEmpty()) {
                throw new IllegalArgumentException("Commands list cannot be null or empty");
            }
            this.macroName = macroName;
            this.commands = new ArrayList<>(commands);
        }

        @Override
        public void execute() {
            for (Command command : commands) {
                command.execute();
            }
        }

        @Override
        public void undo() {
            // Undo in reverse order
            ListIterator<Command> it = commands.listIterator(commands.size());
            while (it.hasPrevious()) {
                it.previous().undo();
            }
        }

        @Override
        public String description() {
            return "Macro[%s]: %d commands".formatted(macroName, commands.size());
        }

        public int commandCount() {
            return commands.size();
        }
    }

    // ===================== Lambda-based Command (Modern Java) =====================

    /**
     * A lightweight command using lambdas (Runnable) for execute and undo.
     */
    public static class LambdaCommand implements Command {
        private final Runnable executeAction;
        private final Runnable undoAction;
        private final String desc;

        public LambdaCommand(String description, Runnable executeAction, Runnable undoAction) {
            if (executeAction == null) {
                throw new IllegalArgumentException("Execute action cannot be null");
            }
            if (undoAction == null) {
                throw new IllegalArgumentException("Undo action cannot be null");
            }
            this.desc = description != null ? description : "LambdaCommand";
            this.executeAction = executeAction;
            this.undoAction = undoAction;
        }

        @Override
        public void execute() {
            executeAction.run();
        }

        @Override
        public void undo() {
            undoAction.run();
        }

        @Override
        public String description() {
            return desc;
        }
    }

    // ===================== Command Queue (Deferred Execution) =====================

    /**
     * A command queue for deferred/batch execution of commands.
     */
    public static class CommandQueue {
        private final Queue<Command> queue = new LinkedList<>();

        public void enqueue(Command command) {
            if (command == null) {
                throw new IllegalArgumentException("Command cannot be null");
            }
            queue.add(command);
        }

        public int size() {
            return queue.size();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }

        /**
         * Executes all queued commands in order and returns the count executed.
         */
        public int executeAll() {
            int count = 0;
            while (!queue.isEmpty()) {
                queue.poll().execute();
                count++;
            }
            return count;
        }

        /**
         * Executes a single command from the queue, returns false if empty.
         */
        public boolean executeNext() {
            Command command = queue.poll();
            if (command == null) {
                return false;
            }
            command.execute();
            return true;
        }
    }
}
