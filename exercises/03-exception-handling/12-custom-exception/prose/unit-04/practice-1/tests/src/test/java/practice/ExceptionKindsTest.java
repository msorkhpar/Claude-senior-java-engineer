package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionKindsTest {

    @Test
    void classifiesTheCommonTypes() {
        assertThat(ExceptionKinds.isChecked(IOException.class)).isTrue();
        assertThat(ExceptionKinds.isChecked(SQLException.class)).isTrue();
        assertThat(ExceptionKinds.isChecked(ClassNotFoundException.class)).isTrue();
        assertThat(ExceptionKinds.isChecked(IllegalArgumentException.class)).isFalse();
        assertThat(ExceptionKinds.isChecked(NullPointerException.class)).isFalse();
        assertThat(ExceptionKinds.isChecked(RuntimeException.class)).isFalse();
    }

    @Test
    void errorsAreUnchecked() {
        assertThat(ExceptionKinds.isChecked(StackOverflowError.class)).isFalse();
        assertThat(ExceptionKinds.isChecked(OutOfMemoryError.class)).isFalse();
    }

    @Test
    void deepRuntimeSubclassesAreUnchecked() {
        assertThat(ExceptionKinds.isChecked(NumberFormatException.class)).isFalse();
        assertThat(ExceptionKinds.isChecked(ArrayIndexOutOfBoundsException.class)).isFalse();
    }
}
