package practice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CommandTableTest {

    // Each typed name is built at run time, as text read from a chat message would be.
    private static String typed(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void findsEachCommandAndRejectsAnUnknownOne() {
        CommandTable table = new CommandTable("deploy", "status", "rollback");
        assertThat(table.indexOf(typed("deploy"))).isZero();
        assertThat(table.indexOf(typed("status"))).isEqualTo(1);
        assertThat(table.indexOf(typed("rollback"))).isEqualTo(2);
        assertThat(table.indexOf(typed("restart"))).isEqualTo(-1);
        assertThat(table.reply(typed("status"))).isEqualTo("running status");
    }

    @Test
    void commandsWithTheSameHashCodeEachReachTheirOwnPosition() {
        assertThat("Ea".hashCode()).isEqualTo("FB".hashCode());
        assertThat("AaBB".hashCode()).isEqualTo("BBAa".hashCode());
        CommandTable table = new CommandTable("deploy", "Ea", "FB", "AaBB", "BBAa", "status");
        assertThat(table.indexOf(typed("Ea"))).isEqualTo(1);
        assertThat(table.indexOf(typed("FB"))).isEqualTo(2);
        assertThat(table.indexOf(typed("AaBB"))).isEqualTo(3);
        assertThat(table.indexOf(typed("BBAa"))).isEqualTo(4);
        assertThat(table.reply(typed("BBAa"))).isEqualTo("running BBAa");
    }

    @Test
    void aNameThatOnlySharesACommandsHashCodeIsUnknown() {
        CommandTable table = new CommandTable("deploy", "Ea", "status");
        assertThat(table.indexOf(typed("FB"))).isEqualTo(-1);
        assertThat(table.reply(typed("FB"))).isEqualTo("unknown command: FB");
    }

    @Test
    void aMessageWithNoCommandIsUnknown() {
        CommandTable table = new CommandTable("deploy", "status");
        assertThat(table.indexOf(null)).isEqualTo(-1);
        assertThat(table.reply(null)).isEqualTo("unknown command: null");
    }
}
