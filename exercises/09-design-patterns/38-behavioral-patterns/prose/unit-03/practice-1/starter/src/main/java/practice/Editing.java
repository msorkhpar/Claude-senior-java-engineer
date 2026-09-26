package practice;

public final class Editing {

    private Editing() {
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

        public int length() {
            return content.length();
        }
    }

    public interface Command {
        void execute();

        void undo();

        String description();
    }

    public static final class InsertCommand implements Command {

        public InsertCommand(TextEditor editor, int position, String text) {
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
    }

    public static final class DeleteCommand implements Command {

        public DeleteCommand(TextEditor editor, int position, int length) {
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
    }
}
