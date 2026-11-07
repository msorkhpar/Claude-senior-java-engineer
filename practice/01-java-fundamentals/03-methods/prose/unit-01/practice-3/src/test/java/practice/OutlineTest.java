package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OutlineTest {

    /** ines opens; omar, sol and uma answer her; pia and quinn answer omar; rui answers quinn; tara answers sol. */
    private static Outline.Post discussion() {
        Outline.Post ines = new Outline.Post("ines");
        Outline.Post omar = ines.reply("omar");
        omar.reply("pia");
        omar.reply("quinn").reply("rui");
        ines.reply("sol").reply("tara");
        ines.reply("uma");
        return ines;
    }

    private static List<String> authors(List<String> lines) {
        return lines.stream().map(l -> l.substring(l.indexOf(' ') + 1)).toList();
    }

    private static Map<String, Integer> depths(List<String> lines) {
        Map<String, Integer> depths = new LinkedHashMap<>();
        for (String l : lines) {
            int space = l.indexOf(' ');
            depths.put(l.substring(space + 1), Integer.parseInt(l.substring(0, space)));
        }
        return depths;
    }

    @Test
    void aShortChainOfRepliesIsOutlined() {
        Outline.Post opening = new Outline.Post("vera");
        opening.reply("wim").reply("xia").reply("yann");
        assertThat(Outline.lines(opening)).containsExactly("0 vera", "1 wim", "2 xia", "3 yann");
    }

    @Test
    void aVeryDeepChainIsOutlinedOnASmallStack() throws Exception {
        int length = 200_000;
        Outline.Post opening = new Outline.Post("p0");
        Outline.Post last = opening;
        for (int i = 1; i < length; i++) last = last.reply("p" + i);

        FutureTask<List<String>> outline = new FutureTask<>(() -> Outline.lines(opening));
        Thread worker = new Thread(null, outline, "outline", 512 * 1024);
        worker.setDaemon(true);
        worker.start();
        List<String> lines = outline.get(60, TimeUnit.SECONDS);

        assertThat(lines).hasSize(length);
        assertThat(lines.get(0)).isEqualTo("0 p0");
        assertThat(lines.get(length - 1)).isEqualTo((length - 1) + " p" + (length - 1));
    }

    @Test
    void repliesAreListedInReadingOrder() {
        assertThat(authors(Outline.lines(discussion())))
                .containsExactly("ines", "omar", "pia", "quinn", "rui", "sol", "tara", "uma");
    }

    @Test
    void eachPostIsAsDeepAsItsPlaceInTheThread() {
        assertThat(depths(Outline.lines(discussion()))).isEqualTo(Map.of(
                "ines", 0, "omar", 1, "pia", 2, "quinn", 2, "rui", 3, "sol", 1, "tara", 2, "uma", 1));
    }
}
