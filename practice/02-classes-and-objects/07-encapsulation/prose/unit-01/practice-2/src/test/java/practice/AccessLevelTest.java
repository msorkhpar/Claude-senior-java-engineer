package practice;

import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessLevelTest {

    @SuppressWarnings("unused")
    static class Example {
        public int publicVariable = 10;
        protected int protectedVariable = 30;
        int defaultVariable = 40;
        private int privateVariable = 20;
        static final int DEFAULT_LIMIT = 5;
    }

    private static int modifiersOf(String field) throws NoSuchFieldException {
        return Example.class.getDeclaredField(field).getModifiers();
    }

    @Test
    void readsEachKeyword() throws Exception {
        assertThat(AccessLevel.of(modifiersOf("publicVariable"))).isEqualTo(AccessLevel.PUBLIC);
        assertThat(AccessLevel.of(modifiersOf("protectedVariable"))).isEqualTo(AccessLevel.PROTECTED);
        assertThat(AccessLevel.of(modifiersOf("privateVariable"))).isEqualTo(AccessLevel.PRIVATE);
        assertThat(AccessLevel.of(Modifier.PUBLIC | Modifier.STATIC)).isEqualTo(AccessLevel.PUBLIC);
    }

    @Test
    void noKeywordMeansPackagePrivate() throws Exception {
        assertThat(AccessLevel.of(modifiersOf("defaultVariable"))).isEqualTo(AccessLevel.PACKAGE);
        assertThat(AccessLevel.of(modifiersOf("DEFAULT_LIMIT"))).isEqualTo(AccessLevel.PACKAGE);
        assertThat(AccessLevel.of(0)).isEqualTo(AccessLevel.PACKAGE);
    }

    @Test
    void privateIsTheMostRestrictive() {
        assertThat(AccessLevel.mostRestrictive(AccessLevel.PUBLIC, AccessLevel.PRIVATE, AccessLevel.PACKAGE))
                .isEqualTo(AccessLevel.PRIVATE);
        assertThat(AccessLevel.mostRestrictive(AccessLevel.PUBLIC)).isEqualTo(AccessLevel.PUBLIC);
        assertThat(AccessLevel.PRIVATE.isMoreRestrictiveThan(AccessLevel.PACKAGE)).isTrue();
        assertThat(AccessLevel.PUBLIC.isMoreRestrictiveThan(AccessLevel.PROTECTED)).isFalse();
        assertThat(AccessLevel.PUBLIC.isMoreRestrictiveThan(AccessLevel.PUBLIC)).isFalse();
    }

    @Test
    void packageIsStricterThanProtected() {
        assertThat(AccessLevel.PACKAGE.isMoreRestrictiveThan(AccessLevel.PROTECTED)).isTrue();
        assertThat(AccessLevel.PROTECTED.isMoreRestrictiveThan(AccessLevel.PACKAGE)).isFalse();
        assertThat(AccessLevel.mostRestrictive(AccessLevel.PROTECTED, AccessLevel.PUBLIC, AccessLevel.PACKAGE))
                .isEqualTo(AccessLevel.PACKAGE);
    }
}
