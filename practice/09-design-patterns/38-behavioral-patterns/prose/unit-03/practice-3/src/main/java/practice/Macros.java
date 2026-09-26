package practice;

import java.util.List;

public final class Macros {

    private Macros() {
    }

    /** The receiver: a text buffer. */
    public static final class TextEditor {
        private final StringBuilder content = new StringBuilder();

        public void insert(int position, String text) {
            if (position < 0 || position > content.length()) {
                throw new IndexOutOfBoundsException("Position " + position + " is out of bounds for length " + content.length());
            }
            content.insert(position, text);
        }

        public void delete(int position, int length) {
            if (position < 0 || length < 0 || position + length > content.length()) {
                throw new IndexOutOfBoundsException("Cannot delete " + length + " chars at position " + position);
            }
            content.delete(position, position + length);
        }

        public String getContent() {
            return content.toString();
        }
    }

    public interface Command {
        void execute();

        void undo();

        String description();
    }

    /** Given: inserts text at a position; undo removes it again. */
    public static final class InsertCommand implements Command {
        private final TextEditor editor;
        private final int position;
        private final String text;

        public InsertCommand(TextEditor editor, int position, String text) {
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
            return "Insert '" + text + "' at position " + position;
        }
    }

    public static final class MacroCommand implements Command {

        public MacroCommand(String name, List<Command> commands) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public void execute() {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public void undo() {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public String description() {
            throw new UnsupportedOperationException("TODO");
        }

        public int commandCount() {
            throw new UnsupportedOperationException("TODO");
        }
    }
}
