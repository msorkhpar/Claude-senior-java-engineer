package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLineTest {

    @Test
    void signedInRequestStartsWithTheUserAndEndsWithThePath() {
        String line = AuditLine.line(new String("t.ishikawa"), new String[] {"auditor"}, 'R', '+',
                "/ledger/2031");

        assertThat(line).startsWith("t.ishikawa [").endsWith(" /ledger/2031");
    }

    @Test
    void anonymousRequestIsLoggedWithADash() {
        assertThat(AuditLine.who(null)).isEqualTo("-");
        assertThat(AuditLine.line(null, new String[] {}, 'R', '-', "/admin"))
                .startsWith("- [").endsWith(" /admin");
    }

    @Test
    void rolesAreListedByName() {
        assertThat(AuditLine.roles(new String[] {"warehouse", "returns", "night-shift"}))
                .isEqualTo("warehouse,returns,night-shift");
        assertThat(AuditLine.roles(new String[] {"viewer"})).isEqualTo("viewer");
        assertThat(AuditLine.roles(new String[] {})).isEmpty();
    }

    @Test
    void codeIsTheTwoCharactersSideBySide() {
        assertThat(AuditLine.code('D', '-')).isEqualTo("D-");
        assertThat(AuditLine.code('R', '+')).isEqualTo("R+");
        assertThat(AuditLine.line("o.bello", new String[] {"ops"}, 'W', '-', "/stock/7"))
                .isEqualTo("o.bello [ops] W- /stock/7");
    }
}
