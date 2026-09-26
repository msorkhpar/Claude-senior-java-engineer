package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetTest {

    @Test
    void widgetsAndButtonsLabelThemselves() {
        Widget plain = new Widget("w1");
        assertThat(plain.getId()).isEqualTo("w1");
        assertThat(plain.label()).isEqualTo("Widget");
        assertThat(plain.summary()).isEqualTo("Widget#w1");
        Widget.Button ok = new Widget.Button("b1", "OK");
        assertThat(ok.getId()).isEqualTo("b1");
        assertThat(ok.label()).isEqualTo("Button:OK");
    }

    @Test
    void aButtonsSummaryUsesItsText() {
        Widget ok = new Widget.Button("b1", "OK");
        assertThat(ok.summary()).isEqualTo("Button:OK#b1");
    }

    @Test
    void aButtonsSummaryFollowsANewText() {
        Widget.Button button = new Widget.Button("b2", "OK");
        button.setText("Cancel");
        assertThat(button.label()).isEqualTo("Button:Cancel");
        assertThat(button.summary()).isEqualTo("Button:Cancel#b2");
    }
}
