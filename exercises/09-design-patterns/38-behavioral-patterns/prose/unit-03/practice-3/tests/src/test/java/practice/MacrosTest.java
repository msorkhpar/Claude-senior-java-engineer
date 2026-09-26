package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import practice.Macros.Command;
import practice.Macros.InsertCommand;
import practice.Macros.MacroCommand;
import practice.Macros.TextEditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MacrosTest {

    @Test
    void runsItsCommandsAsOne() {
        TextEditor editor = new TextEditor();
        MacroCommand helloWorld = new MacroCommand("hello-world", List.of(
                new InsertCommand(editor, 0, "Hello"),
                new InsertCommand(editor, 5, " World")));

        helloWorld.execute();

        assertThat(editor.getContent()).isEqualTo("Hello World");
        assertThat(helloWorld.description()).isEqualTo("Macro[hello-world]: 2 commands");
        assertThat(helloWorld.commandCount()).isEqualTo(2);
    }

    @Test
    void undoRunsInReverseOrder() {
        TextEditor editor = new TextEditor();
        MacroCommand helloWorld = new MacroCommand("hello-world", List.of(
                new InsertCommand(editor, 0, "Hello"),
                new InsertCommand(editor, 5, " World")));

        helloWorld.execute();
        helloWorld.undo();

        assertThat(editor.getContent()).isEmpty();
        helloWorld.execute();
        assertThat(editor.getContent()).isEqualTo("Hello World");
    }

    @Test
    void theMacroKeepsItsOwnCopyOfTheList() {
        TextEditor editor = new TextEditor();
        List<Command> steps = new ArrayList<>();
        steps.add(new InsertCommand(editor, 0, "a"));
        steps.add(new InsertCommand(editor, 1, "b"));
        MacroCommand macro = new MacroCommand("ab", steps);

        steps.clear();
        steps.add(new InsertCommand(editor, 0, "zzz"));
        macro.execute();

        assertThat(editor.getContent()).isEqualTo("ab");
        assertThat(macro.commandCount()).isEqualTo(2);
    }

    @Test
    void anEmptyOrBlankMacroIsRefused() {
        TextEditor editor = new TextEditor();
        List<Command> one = List.of(new InsertCommand(editor, 0, "x"));

        assertThatThrownBy(() -> new MacroCommand("empty", List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MacroCommand("none", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MacroCommand("   ", one)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MacroCommand(null, one)).isInstanceOf(IllegalArgumentException.class);
    }
}
