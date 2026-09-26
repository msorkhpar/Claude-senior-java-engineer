package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class RequestContextTest {

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        return t;
    }

    @Test
    void runsTheWorkUnderItsId() {
        assertThat(RequestContext.process("req-1", RequestContext::currentRequestId)).isEqualTo("req-1");
        assertThat(RequestContext.process("req-2", () -> RequestContext.currentRequestId().length())).isEqualTo(5);
        assertThat(RequestContext.format(new GregorianCalendar(2024, Calendar.MARCH, 5).getTime()))
                .isEqualTo("2024-03-05");
    }

    @Test
    void eachThreadSeesItsOwnId() throws Exception {
        CyclicBarrier bothInside = new CyclicBarrier(2);
        AtomicReference<String> seenByOne = new AtomicReference<>("not run");
        AtomicReference<String> seenByTwo = new AtomicReference<>("not run");
        Thread one = daemon(() -> seenByOne.set(RequestContext.process("one", () -> {
            awaitQuietly(bothInside);
            return RequestContext.currentRequestId();
        })));
        Thread two = daemon(() -> seenByTwo.set(RequestContext.process("two", () -> {
            awaitQuietly(bothInside);
            return RequestContext.currentRequestId();
        })));
        one.start();
        two.start();
        one.join(5000);
        two.join(5000);
        assertThat(one.isAlive() || two.isAlive()).as("both threads finished").isFalse();
        assertThat(seenByOne.get()).isEqualTo("one");
        assertThat(seenByTwo.get()).isEqualTo("two");
    }

    private static void awaitQuietly(CyclicBarrier barrier) {
        try {
            barrier.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void theIdIsClearedEvenWhenTheWorkFails() {
        RequestContext.process("done", () -> "ok");
        assertThat(RequestContext.currentRequestId()).isNull();
        assertThatThrownBy(() -> RequestContext.process("broken", () -> {
            throw new IllegalStateException("work failed");
        })).isInstanceOf(IllegalStateException.class).hasMessage("work failed");
        assertThat(RequestContext.currentRequestId()).isNull();
    }

    @Test
    void eachThreadGetsItsOwnFormatter() throws Exception {
        SimpleDateFormat mine = RequestContext.formatter();
        assertThat(RequestContext.formatter()).isSameAs(mine);
        AtomicReference<SimpleDateFormat> theirs = new AtomicReference<>();
        Thread other = daemon(() -> theirs.set(RequestContext.formatter()));
        other.start();
        other.join(5000);
        assertThat(other.isAlive()).isFalse();
        assertThat(theirs.get()).isNotNull().isNotSameAs(mine);
        assertThat(theirs.get().toPattern()).isEqualTo("yyyy-MM-dd");
    }
}
