package practice;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OverridesTest {

    static class Animal {
        public String speak() {
            return "...";
        }

        private String secret() {
            return "animal";
        }
    }

    static class Dog extends Animal {
        @Override
        public String speak() {
            return "Woof!";
        }

        public String speak(String to) {
            return "Woof, " + to + "!";
        }

        public String fetch() {
            return "ball";
        }

        public String secret() {
            return "dog";
        }

        @Override
        public String toString() {
            return "Dog";
        }

        public boolean equals(Dog other) {
            return other != null;
        }
    }

    interface Greetable {
        String greet(String name);

        default String wave() {
            return "wave";
        }
    }

    interface Polite extends Greetable {
    }

    static class FriendlyGreeter implements Greetable {
        @Override
        public String greet(String name) {
            return "Hello, " + name + "!";
        }

        @Override
        public String wave() {
            return "big wave";
        }
    }

    static class PoliteGreeter implements Polite {
        @Override
        public String greet(String name) {
            return "Good day, " + name + ".";
        }
    }

    @Test
    void dogSpeakOverridesAnimalSpeak() throws Exception {
        Method speak = Dog.class.getDeclaredMethod("speak");

        assertThat(speak.isAnnotationPresent(Override.class)).isFalse();
        assertThat(Overrides.isOverride(speak)).isTrue();
        assertThat(Overrides.isOverride(Dog.class.getDeclaredMethod("fetch"))).isFalse();
    }

    @Test
    void anOverloadIsNotAnOverride() throws Exception {
        assertThat(Overrides.isOverride(Dog.class.getDeclaredMethod("speak", String.class))).isFalse();
        assertThat(Overrides.isOverride(Dog.class.getDeclaredMethod("equals", Dog.class))).isFalse();
    }

    @Test
    void theWholeSuperclassChainCounts() throws Exception {
        assertThat(Overrides.isOverride(Dog.class.getDeclaredMethod("toString"))).isTrue();
    }

    @Test
    void interfaceMethodsAreOverriddenToo() throws Exception {
        assertThat(Overrides.isOverride(FriendlyGreeter.class.getDeclaredMethod("greet", String.class))).isTrue();
        assertThat(Overrides.isOverride(FriendlyGreeter.class.getDeclaredMethod("wave"))).isTrue();
        assertThat(Overrides.isOverride(PoliteGreeter.class.getDeclaredMethod("greet", String.class))).isTrue();
    }

    @Test
    void aPrivateMethodIsNotOverridden() throws Exception {
        assertThat(Overrides.isOverride(Dog.class.getDeclaredMethod("secret"))).isFalse();
    }
}
