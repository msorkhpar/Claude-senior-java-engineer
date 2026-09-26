package practice;

import org.junit.jupiter.api.Test;

import practice.Editing.DeleteCommand;
import practice.Editing.InsertCommand;
import practice.Editing.TextEditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EditingTest {

    private static TextEditor holding(String text) {
        TextEditor editor = new TextEditor();
        editor.insert(0, text);
        return editor;
    }

    @Test
    void executesAndUndoesInsertsAndDeletes() {
        TextEditor editor = new TextEditor();
        InsertCommand hello = new InsertCommand(editor, 0, "Hello");
        InsertCommand world = new InsertCommand(editor, 5, " World");

        hello.execute();
        world.execute();
        assertThat(editor.getContent()).isEqualTo("Hello World");
        DeleteCommand cut = new DeleteCommand(editor, 5, 6);
        cut.execute();
        assertThat(editor.getContent()).isEqualTo("Hello");
        cut.undo();
        assertThat(editor.getContent()).isEqualTo("Hello World");
        world.undo();
        assertThat(editor.getContent()).isEqualTo("Hello");

        assertThat(world.description()).isEqualTo("Insert ' World' at position 5");
        assertThat(cut.description()).isEqualTo("Delete 6 chars at position 5");
        assertThatThrownBy(() -> new InsertCommand(null, 0, "x")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InsertCommand(editor, 0, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DeleteCommand(null, 0, 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMiddleInsertUndoesAtItsOwnPosition() {
        TextEditor editor = holding("xyz");
        InsertCommand insert = new InsertCommand(editor, 1, "ab");

        insert.execute();
        assertThat(editor.getContent()).isEqualTo("xabyz");
        insert.undo();

        assertThat(editor.getContent()).isEqualTo("xyz");

        TextEditor repeated = holding("xyz");
        InsertCommand early = new InsertCommand(repeated, 0, "y");
        early.execute();
        assertThat(repeated.getContent()).isEqualTo("yxyz");
        early.undo();
        assertThat(repeated.getContent()).isEqualTo("xyz");
    }

    @Test
    void deleteSavesTheTextWhenItRuns() {
        TextEditor editor = holding("abcdef");
        DeleteCommand delete = new DeleteCommand(editor, 0, 3);
        editor.insert(0, "XYZ");

        delete.execute();
        assertThat(editor.getContent()).isEqualTo("abcdef");
        delete.undo();

        assertThat(editor.getContent()).isEqualTo("XYZabcdef");
    }

    @Test
    void undoBeforeExecuteIsRefused() {
        TextEditor editor = holding("abc");
        DeleteCommand delete = new DeleteCommand(editor, 0, 1);

        assertThatThrownBy(delete::undo).isInstanceOf(IllegalStateException.class);
        assertThat(editor.getContent()).isEqualTo("abc");
    }
}
