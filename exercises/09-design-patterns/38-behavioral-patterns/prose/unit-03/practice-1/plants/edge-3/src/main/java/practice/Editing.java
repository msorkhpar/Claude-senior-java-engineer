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
            return "Insert '" + text + "' at position " + position;
        }
    }

    public static final class DeleteCommand implements Command {
        private final TextEditor editor;
        private final int position;
        private final int length;
        private String deletedText;

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
            deletedText = editor.getContent().substring(position, position + length);
            editor.delete(position, length);
        }

        @Override
        public void undo() {
            editor.insert(position, deletedText);
        }

        @Override
        public String description() {
            return "Delete " + length + " chars at position " + position;
        }
    }
}
