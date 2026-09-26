package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataPipelineTest {

    @Test
    void runsThePagesThreeStages() {
        DataPipeline<Integer> pipeline = new DataPipeline<Integer>()
                .addStage(list -> list.stream().filter(n -> n > 0).toList())
                .addStage(list -> list.stream().distinct().toList())
                .addStage(list -> list.stream().sorted().toList());

        assertThat(pipeline.execute(Arrays.asList(3, -1, 2, 3, 0, 5))).containsExactly(2, 3, 5);
        assertThat(pipeline.stageCount()).isEqualTo(3);
    }

    @Test
    void stagesRunInTheOrderTheyWereAdded() {
        DataPipeline<Integer> firstTwoThenSort = new DataPipeline<Integer>()
                .addStage(list -> list.stream().limit(2).toList())
                .addStage(list -> list.stream().sorted().toList());
        DataPipeline<Integer> sortThenFirstTwo = new DataPipeline<Integer>()
                .addStage(list -> list.stream().sorted().toList())
                .addStage(list -> list.stream().limit(2).toList());

        assertThat(firstTwoThenSort.execute(List.of(5, 1, 4))).containsExactly(1, 5);
        assertThat(sortThenFirstTwo.execute(List.of(5, 1, 4))).containsExactly(1, 4);
    }

    @Test
    void addStageAddsToThisPipeline() {
        DataPipeline<Integer> pipeline = new DataPipeline<>();

        DataPipeline<Integer> returned = pipeline.addStage(list -> list.stream().map(n -> n * 1000).toList());

        assertThat(returned).isSameAs(pipeline);
        assertThat(pipeline.stageCount()).isEqualTo(1);
        assertThat(pipeline.execute(List.of(1, 2))).containsExactly(1000, 2000);
    }

    @Test
    void theAnswerIsNeverTheCallersList() {
        List<Integer> input = new ArrayList<>(List.of(700, 800));

        List<Integer> answer = new DataPipeline<Integer>().execute(input);
        input.add(900);

        assertThat(answer).containsExactly(700, 800);
        answer.add(1000);
        assertThat(input).containsExactly(700, 800, 900);

        List<Integer> unsorted = new ArrayList<>(List.of(3000, 1000, 2000));
        List<Integer> sorted = new DataPipeline<Integer>().addStage(list -> {
            list.sort(null);
            return list;
        }).execute(unsorted);
        assertThat(sorted).containsExactly(1000, 2000, 3000);
        assertThat(unsorted).containsExactly(3000, 1000, 2000);
    }

    @Test
    void nullsAreRefused() {
        DataPipeline<Integer> pipeline = new DataPipeline<>();

        assertThatThrownBy(() -> pipeline.addStage(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(pipeline.stageCount()).isZero();
        assertThatThrownBy(() -> pipeline.execute(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
