package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SampleBufferTest {

    @Test
    void keepsEverySampleInOrderAsItGrows() {
        SampleBuffer buffer = new SampleBuffer(6);
        for (int i = 0; i < 2_500; i++) buffer.add(i * 0.25);

        assertThat(buffer.size()).isEqualTo(2_500);
        assertThat(buffer.capacity()).isGreaterThanOrEqualTo(2_500);
        for (int i = 0; i < 2_500; i++) assertThat(buffer.get(i)).isEqualTo(i * 0.25);
    }

    @Test
    void aSmallBatchIsAppendedAfterTheSamplesAlreadyThere() {
        SampleBuffer buffer = new SampleBuffer(4);
        buffer.add(3.5);
        buffer.add(7.0);
        buffer.add(9.25);
        buffer.addAll(new double[] {11.0, 0.5});

        assertThat(buffer.size()).isEqualTo(5);
        assertThat(buffer.get(0)).isEqualTo(3.5);
        assertThat(buffer.get(3)).isEqualTo(11.0);
        assertThat(buffer.get(4)).isEqualTo(0.5);
    }

    @Test
    void fillingTheBufferOneSampleAtATimeCopiesEachSampleOnlyAFewTimes() {
        SampleBuffer buffer = new SampleBuffer(6);
        int n = 400_000;
        for (int i = 0; i < n; i++) buffer.add(i);

        assertThat(buffer.size()).isEqualTo(n);
        assertThat(buffer.copied()).as("elements copied while adding %d samples", n).isLessThan(12L * n);
    }

    @Test
    void aBufferCreatedWithNoRoomTakesSamples() {
        SampleBuffer buffer = new SampleBuffer(0);
        buffer.add(1.5);
        buffer.add(2.5);
        buffer.add(4.5);

        assertThat(buffer.size()).isEqualTo(3);
        assertThat(buffer.get(2)).isEqualTo(4.5);
    }

    @Test
    void aBatchLargerThanTheFreeRoomFitsInOneAppend() {
        SampleBuffer buffer = new SampleBuffer(10);
        for (int i = 0; i < 9; i++) buffer.add(i);
        double[] batch = new double[150];
        for (int i = 0; i < batch.length; i++) batch[i] = 1_000 + i;

        buffer.addAll(batch);

        assertThat(buffer.size()).isEqualTo(159);
        assertThat(buffer.get(8)).isEqualTo(8.0);
        assertThat(buffer.get(158)).isEqualTo(1_149.0);
    }
}
