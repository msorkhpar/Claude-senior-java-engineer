package practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ImagesTest {

    /** The real image: expensive to create, so the test counts how often it is. */
    record RealImage(String filename) implements Images.ImageService {
        RealImage {
            if (filename.isBlank()) {
                throw new IllegalArgumentException("filename must not be blank");
            }
        }

        @Override
        public String display() {
            return "Displaying image: " + filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public long getSize() {
            return filename.length() * 1024L;
        }
    }

    private static Function<String, Images.ImageService> counting(AtomicInteger loads) {
        return name -> {
            loads.incrementAndGet();
            return new RealImage(name);
        };
    }

    @Test
    void loadsOnFirstDisplayAndDelegates() {
        AtomicInteger loads = new AtomicInteger();
        Images.VirtualImageProxy proxy = new Images.VirtualImageProxy("photo.png", counting(loads));

        assertThat(loads).hasValue(0);
        assertThat(proxy.isLoaded()).isFalse();

        assertThat(proxy.display()).isEqualTo("Displaying image: photo.png");
        assertThat(loads).hasValue(1);
        assertThat(proxy.isLoaded()).isTrue();
    }

    @Test
    void theFilenameNeedsNoLoad() {
        AtomicInteger loads = new AtomicInteger();
        Images.VirtualImageProxy proxy = new Images.VirtualImageProxy("photo.png", counting(loads));

        assertThat(proxy.getFilename()).isEqualTo("photo.png");
        assertThat(loads).hasValue(0);
        assertThat(proxy.isLoaded()).isFalse();
    }

    @Test
    void theImageIsLoadedOnce() {
        AtomicInteger loads = new AtomicInteger();
        Images.VirtualImageProxy proxy = new Images.VirtualImageProxy("photo.png", counting(loads));

        proxy.display();
        assertThat(proxy.getSize()).isEqualTo(9 * 1024L);
        proxy.display();

        assertThat(loads).hasValue(1);
    }

    @Test
    void racingFirstCallsLoadOnce() throws Exception {
        CountDownLatch firstInside = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger loads = new AtomicInteger();
        Images.VirtualImageProxy proxy = new Images.VirtualImageProxy("photo.png", name -> {
            if (loads.incrementAndGet() == 1) {
                firstInside.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return new RealImage(name);
        });
        AtomicReference<String> seenByA = new AtomicReference<>();
        AtomicReference<String> seenByB = new AtomicReference<>();

        Thread a = new Thread(() -> seenByA.set(proxy.display()));
        a.start();
        firstInside.await();
        Thread b = new Thread(() -> seenByB.set(proxy.display()));
        b.start();
        while (loads.get() < 2 && !waitsForALock(b) && b.isAlive()) {
            Thread.onSpinWait();
        }
        release.countDown();
        a.join();
        b.join();

        assertThat(loads).hasValue(1);
        assertThat(seenByB.get()).isEqualTo("Displaying image: photo.png");
        assertThat(seenByA.get()).isEqualTo("Displaying image: photo.png");
    }

    @Test
    void aBlankFilenameIsRefusedAtOnce() {
        AtomicInteger loads = new AtomicInteger();

        assertThatIllegalArgumentException().isThrownBy(() -> new Images.VirtualImageProxy("  ", counting(loads)));
        assertThat(loads).hasValue(0);
    }

    private static boolean waitsForALock(Thread t) {
        Thread.State s = t.getState();
        return s == Thread.State.BLOCKED || s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING;
    }
}
