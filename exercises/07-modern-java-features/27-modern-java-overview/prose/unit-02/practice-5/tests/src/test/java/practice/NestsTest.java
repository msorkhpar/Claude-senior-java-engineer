package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NestsTest {

    @Test
    void findsTheHostAndNestmates() {
        assertThat(Nests.host(Outer.Inner.class)).isEqualTo(Outer.class);
        assertThat(Nests.host(Outer.Sibling.class)).isEqualTo(Outer.class);
        assertThat(Nests.nestmates(Outer.class, Outer.Inner.class)).isTrue();
        assertThat(Nests.nestmates(Outer.Inner.class, Outer.class)).isTrue();
        assertThat(Nests.nestmates(Outer.class, Stranger.class)).isFalse();
        assertThat(Nests.nestmates(Outer.Inner.class, Stranger.class)).isFalse();
    }

    @Test
    void deeplyNestedHostIsTheTopLevelClass() {
        assertThat(Nests.host(Outer.Inner.Deeper.class)).isEqualTo(Outer.class);
        class Local {
        }
        Runnable anonymous = new Runnable() {
            @Override
            public void run() {
            }
        };
        assertThat(Nests.host(Local.class)).isEqualTo(NestsTest.class);
        assertThat(Nests.host(anonymous.getClass())).isEqualTo(NestsTest.class);
    }

    @Test
    void aTopLevelClassHostsItself() {
        assertThat(Nests.host(Outer.class)).isEqualTo(Outer.class);
        assertThat(Nests.host(Stranger.class)).isEqualTo(Stranger.class);
    }

    @Test
    void aClassIsItsOwnNestmate() {
        assertThat(Nests.nestmates(Outer.class, Outer.class)).isTrue();
        assertThat(Nests.nestmates(Stranger.class, Stranger.class)).isTrue();
    }

    @Test
    void siblingsAreNestmates() {
        assertThat(Nests.nestmates(Outer.Inner.class, Outer.Sibling.class)).isTrue();
        assertThat(Nests.nestmates(Outer.Inner.Deeper.class, Outer.Sibling.class)).isTrue();
    }
}

class Outer {
    class Inner {
        class Deeper {
        }
    }

    static class Sibling {
    }
}

class Stranger {
}
