package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VirtualsTest {

    @Test
    void instanceMethodsAreDispatched() {
        Virtuals.Base base = new Virtuals.Base();
        Virtuals.Base derived = new Virtuals.Derived();
        assertThat(base.kind()).isEqualTo("base");
        assertThat(derived.kind()).isEqualTo("derived");
        assertThat(Virtuals.Base.label()).isEqualTo("Base");
        assertThat(Virtuals.Derived.label()).isEqualTo("Derived");
        assertThat(Virtuals.kindOf(base)).isEqualTo("base");
        assertThat(base.introduce()).isEqualTo("I am base");
    }

    @Test
    void kindOfFollowsTheObjectNotTheReference() {
        Virtuals.Base b = new Virtuals.Derived();
        assertThat(Virtuals.kindOf(b)).isEqualTo("derived");
    }

    @Test
    void aPrivateHelperIsNotDispatched() {
        Virtuals.Base b = new Virtuals.Derived();
        assertThat(b.introduce()).isEqualTo("I am base");
    }
}
