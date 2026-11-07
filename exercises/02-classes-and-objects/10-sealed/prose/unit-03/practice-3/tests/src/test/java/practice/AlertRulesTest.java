package practice;

import org.junit.jupiter.api.Test;
import practice.AlertRules.Above;
import practice.AlertRules.And;
import practice.AlertRules.Const;
import practice.AlertRules.Not;
import practice.AlertRules.Or;
import practice.AlertRules.Rule;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertRulesTest {

    static final Rule DISK = new Above("disk.used", 0.85);
    static final Rule QUEUE = new Above("queue.depth", 5_000);

    @Test
    void leavesARuleWithNothingToFoldAsItIs() {
        Rule rule = new And(DISK, new Not(QUEUE));
        assertThat(AlertRules.simplify(rule)).isEqualTo(rule);
    }

    @Test
    void foldsConstantsAndDoubleNegationsAtTheTop() {
        assertThat(AlertRules.simplify(new And(new Const(true), DISK))).isEqualTo(DISK);
        assertThat(AlertRules.simplify(new Or(new Const(true), DISK))).isEqualTo(new Const(true));
        assertThat(AlertRules.simplify(new Not(new Not(QUEUE)))).isEqualTo(QUEUE);
        assertThat(AlertRules.simplify(new Not(new Const(false)))).isEqualTo(new Const(true));
    }

    @Test
    void simplifiesInsideANegation() {
        Rule rule = new Not(new Or(new Const(false), DISK));
        assertThat(AlertRules.simplify(rule)).isEqualTo(new Not(DISK));
    }

    @Test
    void foldsWhatSimplifyingTheChildrenUncovers() {
        Rule rule = new Or(new And(QUEUE, new Not(new Const(true))), DISK);
        Rule simplified = AlertRules.simplify(rule);

        assertThat(simplified).isEqualTo(DISK);
        Map<String, Double> readings = Map.of("disk.used", 0.91, "queue.depth", 7_200.0);
        assertThat(AlertRules.fires(simplified, readings)).isEqualTo(AlertRules.fires(rule, readings));
    }

    @Test
    void foldsAConstantOnEitherSide() {
        assertThat(AlertRules.simplify(new And(QUEUE, new Const(false)))).isEqualTo(new Const(false));
        assertThat(AlertRules.simplify(new Or(DISK, new Const(false)))).isEqualTo(DISK);
    }
}
