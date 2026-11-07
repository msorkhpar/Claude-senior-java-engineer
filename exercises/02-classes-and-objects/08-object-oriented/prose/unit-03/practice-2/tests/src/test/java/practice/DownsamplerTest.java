package practice;

import org.junit.jupiter.api.Test;

import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class DownsamplerTest {

    /** Readings replayed from disk: a linked list that counts every link it walks. */
    static final class Replayed extends AbstractSequentialList<Integer> {
        private final LinkedList<Integer> links = new LinkedList<>();
        long linksWalked;

        Replayed(int size) {
            for (int i = 0; i < size; i++) links.add(i * 3);
        }

        @Override
        public int size() {
            return links.size();
        }

        @Override
        public ListIterator<Integer> listIterator(int index) {
            linksWalked += Math.min(index, links.size() - index);   // LinkedList walks from the nearer end
            ListIterator<Integer> it = links.listIterator(index);
            return new ListIterator<>() {
                public boolean hasNext() { return it.hasNext(); }
                public Integer next() { linksWalked++; return it.next(); }
                public boolean hasPrevious() { return it.hasPrevious(); }
                public Integer previous() { linksWalked++; return it.previous(); }
                public int nextIndex() { return it.nextIndex(); }
                public int previousIndex() { return it.previousIndex(); }
                public void remove() { throw new UnsupportedOperationException(); }
                public void set(Integer e) { throw new UnsupportedOperationException(); }
                public void add(Integer e) { throw new UnsupportedOperationException(); }
            };
        }
    }

    /** Live readings: millions of them, each one read from a buffer, and every read counted. */
    static final class Live extends AbstractList<Integer> implements RandomAccess {
        private final int size;
        long reads;

        Live(int size) {
            this.size = size;
        }

        @Override
        public Integer get(int index) {
            if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
            reads++;
            return index * 2;
        }

        @Override
        public int size() {
            return size;
        }
    }

    @Test
    void keepsEveryStepThReadingOfAnyList() {
        List<String> readings = List.of("t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "t10");

        assertThat(new Downsampler(4).sample(readings)).containsExactly("t0", "t4", "t8");
        assertThat(new Downsampler(4).sample(new LinkedList<>(readings))).containsExactly("t0", "t4", "t8");
        assertThat(new Downsampler(5).sample(new ArrayList<>(readings))).containsExactly("t0", "t5", "t10");
        assertThat(new Downsampler(1).sample(new LinkedList<>(readings))).isEqualTo(readings);
        assertThat(new Downsampler(20).sample(readings)).containsExactly("t0");
        assertThat(new Downsampler(3).sample(new LinkedList<String>())).isEmpty();
    }

    @Test
    void samplesTheReplayedReadingsCorrectly() {
        List<Integer> sampled = new Downsampler(6).sample(new Replayed(20));

        assertThat(sampled).containsExactly(0, 18, 36, 54);
    }

    @Test
    void walksTheReplayedReadingsOnce() {
        Replayed replayed = new Replayed(30_000);

        List<Integer> sampled = new Downsampler(7).sample(replayed);

        assertThat(sampled).hasSize(4_286);
        assertThat(sampled.get(4_285)).isEqualTo(29_995 * 3);
        assertThat(replayed.linksWalked).isLessThanOrEqualTo(2L * 30_000);
    }

    @Test
    void readsOnlyTheLiveReadingsItKeeps() {
        Live live = new Live(4_000_000);

        List<Integer> sampled = new Downsampler(1_000).sample(live);

        assertThat(sampled).hasSize(4_000);
        assertThat(sampled.get(3_999)).isEqualTo(3_999_000 * 2);
        assertThat(live.reads).isLessThanOrEqualTo(4_000L + 10);
    }
}
