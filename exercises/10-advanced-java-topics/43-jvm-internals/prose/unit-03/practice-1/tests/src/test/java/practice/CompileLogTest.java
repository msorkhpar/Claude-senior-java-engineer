package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompileLogTest {

    private static CompileLog log(String... lines) {
        return new CompileLog(List.of(lines));
    }

    @Test
    void reportsTheTierOfTheNewestCompileAndZeroForAMethodNeverCompiled() {
        CompileLog log = log(
                "    203   41       3       shop.Cart::subtotal (71 bytes)",
                "starting checkout worker 2",
                "    204   43       1       shop.Item::sku (5 bytes)",
                "    206   44       3       java.util.ArrayList::get (15 bytes)",
                "    209   47       4       shop.Cart::subtotal (71 bytes)");

        assertThat(log.currentTier("shop.Cart::subtotal")).isEqualTo(4);
        assertThat(log.currentTier("shop.Item::sku")).isEqualTo(1);
        assertThat(log.currentTier("shop.Cart::total")).isZero();
        assertThat(log.deoptimizations("shop.Cart::subtotal")).isZero();
    }

    @Test
    void countsCompiledCodeThrownAwayWithNothingToTakeItsPlace() {
        CompileLog log = log(
                "   1180  512       4       billing.Invoice::lineTotal (88 bytes)",
                "   1190  530       3       billing.Invoice::taxes (40 bytes)",
                "   5022  512       4       billing.Invoice::lineTotal (88 bytes)   made not entrant");

        assertThat(log.deoptimizations("billing.Invoice::lineTotal")).isEqualTo(1);
        assertThat(log.deoptimizations("billing.Invoice::taxes")).isZero();
    }

    @Test
    void aLoopCompileDoesNotDecideWhereCallsGo() {
        CompileLog log = log(
                "     88   61 %     3       etl.Loader::copyRows @ 12 (140 bytes)",
                "     89   62       3       etl.Loader::copyRows (140 bytes)",
                "     95   70 %     4       etl.Loader::copyRows @ 12 (140 bytes)",
                "     97   61 %     3       etl.Loader::copyRows @ 12 (140 bytes)   made not entrant");

        assertThat(log.currentTier("etl.Loader::copyRows")).isEqualTo(3);
    }

    @Test
    void aLowerTierRetiredByANewerCompileLeavesCallsInTheNewerOne() {
        CompileLog log = log(
                "    310   77       3       geo.Route::distance (52 bytes)",
                "    312   80       4       geo.Route::distance (52 bytes)",
                "    313   77       3       geo.Route::distance (52 bytes)   made not entrant");

        assertThat(log.currentTier("geo.Route::distance")).isEqualTo(4);
    }

    @Test
    void afterTheBestCodeIsThrownAwayCallsRunInWhateverIsNewest() {
        CompileLog log = log(
                "    410  101       3       auth.Token::verify (64 bytes)",
                "    415  108       4       auth.Token::verify (64 bytes)",
                "    416  101       3       auth.Token::verify (64 bytes)   made not entrant",
                "   9031  108       4       auth.Token::verify (64 bytes)   made not entrant",
                "   9040  377       3       auth.Token::verify (64 bytes)");

        assertThat(log.currentTier("auth.Token::verify")).isEqualTo(3);
    }

    @Test
    void movingFromOneTierToTheNextIsNotCounted() {
        CompileLog log = log(
                "    520  140       3       mail.Template::render (210 bytes)",
                "    530  151       4       mail.Template::render (210 bytes)",
                "    531  140       3       mail.Template::render (210 bytes)   made not entrant",
                "    600  160       2       mail.Template::escape (30 bytes)",
                "    640  171       4       mail.Template::escape (30 bytes)",
                "    641  160       2       mail.Template::escape (30 bytes)   made not entrant");

        assertThat(log.deoptimizations("mail.Template::render")).isZero();
        assertThat(log.deoptimizations("mail.Template::escape")).isZero();
    }

    @Test
    void codeThrownAwayBeforeAHigherTierExistedIsCounted() {
        CompileLog log = log(
                "    700  220       3       feed.Ranker::score (95 bytes)",
                "   2400  220       3       feed.Ranker::score (95 bytes)   made not entrant",
                "   2410  611       3       feed.Ranker::score (95 bytes)",
                "   2470  640       4       feed.Ranker::score (95 bytes)",
                "   2471  611       3       feed.Ranker::score (95 bytes)   made not entrant");

        assertThat(log.deoptimizations("feed.Ranker::score")).isEqualTo(1);
    }
}
