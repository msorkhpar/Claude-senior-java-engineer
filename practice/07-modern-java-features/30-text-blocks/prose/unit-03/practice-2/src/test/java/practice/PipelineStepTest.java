package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineStepTest {

    @Test
    void rendersAStepWithTheScriptUnderRun() {
        String step = PipelineStep.render("build", "./gradlew build\nif [ -d out ]; then\n  ls out\nfi");

        assertThat(step).isEqualTo("""
                  - name: build
                    run: |
                      ./gradlew build
                      if [ -d out ]; then
                        ls out
                      fi
                """);
    }

    @Test
    void aScriptIndentedAsAWholeIsNotIndentedFurther() {
        assertThat(PipelineStep.runBlock("    make test\n    make lint"))
                .isEqualTo("      make test\n      make lint\n");
    }

    @Test
    void aScriptFileThatEndsWithALineBreakIsNotIndentedFurther() {
        assertThat(PipelineStep.runBlock("  cd web\n  npm ci\n  npm test\n"))
                .isEqualTo("      cd web\n      npm ci\n      npm test\n");
    }

    @Test
    void blankLinesOfTheScriptStayEmpty() {
        assertThat(PipelineStep.runBlock("echo start\n\necho middle\n   \necho end"))
                .isEqualTo("      echo start\n\n      echo middle\n\n      echo end\n");
    }

    @Test
    void aScriptWithWindowsLineEndingsGivesTheSameFile() {
        String block = PipelineStep.runBlock("cd api\r\n  go test ./...\r\ncd ..\r\n");

        assertThat(block).doesNotContain("\r");
        assertThat(block).isEqualTo("      cd api\n        go test ./...\n      cd ..\n");
    }

    @Test
    void noLineEndsWithWhitespace() {
        assertThat(PipelineStep.runBlock("  docker build .   \n  docker push"))
                .isEqualTo("      docker build .\n      docker push\n");
    }
}
