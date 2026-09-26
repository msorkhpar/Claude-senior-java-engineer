package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ReportBoardTest {

    @Test
    void aPublishedReportIsReadWhole() throws InterruptedException {
        ReportBoard board = new ReportBoard();
        CountDownLatch published = new CountDownLatch(1);
        AtomicReference<Optional<ReportBoard.Report>> seen = new AtomicReference<>();
        Thread reader = new Thread(() -> {
            try {
                if (published.await(20, TimeUnit.SECONDS)) {
                    seen.set(board.read());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        reader.setDaemon(true);
        reader.start();

        board.publish("Q3 sales", 1250);
        assertThat(board.read()).contains(new ReportBoard.Report("Q3 sales", 1250));
        published.countDown();
        reader.join(20_000);
        assertThat(reader.isAlive()).isFalse();
        assertThat(seen.get()).contains(new ReportBoard.Report("Q3 sales", 1250));
    }

    @Test
    void readingBeforePublishIsEmpty() {
        ReportBoard board = new ReportBoard();
        assertThat(board.read()).isEmpty();
        board.publish("Totals", 0);
        assertThat(board.read()).contains(new ReportBoard.Report("Totals", 0));
    }

    @Test
    void aSecondPublishIsRefused() {
        ReportBoard board = new ReportBoard();
        board.publish("Q3 sales", 1250);
        assertThatThrownBy(() -> board.publish("Q4", 7)).isInstanceOf(IllegalStateException.class);
        assertThat(board.read()).contains(new ReportBoard.Report("Q3 sales", 1250));
    }

    @Test
    void theReadyFlagIsVolatile() {
        new ReportBoard().publish("probe", 1);
        List<Field> flags = Arrays.stream(ReportBoard.class.getDeclaredFields())
                .filter(f -> f.getType() == boolean.class && !Modifier.isStatic(f.getModifiers()))
                .toList();
        assertThat(flags).as("the published state is kept in a boolean field").isNotEmpty();
        assertThat(flags).allSatisfy(f -> assertThat(Modifier.isVolatile(f.getModifiers()))
                .as("field %s is volatile", f.getName()).isTrue());
    }
}
