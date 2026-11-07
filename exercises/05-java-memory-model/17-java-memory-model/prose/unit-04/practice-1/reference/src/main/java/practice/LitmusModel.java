package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A small model checker for litmus tests: a few threads, each a list of reads and writes of
 * shared variables. It lists every combination of values the threads' reads can end with.
 */
public final class LitmusModel {

    private LitmusModel() {
    }

    /** One operation of a thread. */
    public sealed interface Op permits Write, Read {
    }

    /** Stores {@code value} into the shared variable {@code variable}. */
    public record Write(String variable, int value) implements Op {
    }

    /** Loads the shared variable {@code variable} into {@code register}, private to the thread. */
    public record Read(String variable, String register) implements Op {
    }

    public static Op write(String variable, int value) {
        return new Write(variable, value);
    }

    public static Op read(String variable, String register) {
        return new Read(variable, register);
    }

    /**
     * Every set of register values the program can end with when nothing is reordered: each
     * thread performs its operations in its own program order, and the threads' operations
     * interleave in every possible way. Shared variables start at 0. Register names are unique
     * across the program.
     */
    public static Set<Map<String, Integer>> outcomes(List<List<Op>> threads) {
        Set<Map<String, Integer>> results = new HashSet<>();
        explore(State.start(threads), results);
        return results;
    }

    /**
     * Adds to {@code results} the registers of every finished execution that can continue from
     * {@code state}. The next operation to perform may come from any thread that has one left.
     */
    static void explore(State state, Set<Map<String, Integer>> results) {
        if (state.done()) {
            results.add(state.registers());
            return;
        }
        for (int t = 0; t < state.threads(); t++) {
            List<Integer> pending = state.pending(t);
            if (!pending.isEmpty()) {
                explore(state.run(t, pending.get(0)), results);
            }
        }
    }

    /** A point in one execution. Immutable: {@link #run} returns a new state. */
    static final class State {
        private final List<List<Op>> program;
        private final boolean[][] performed;
        private final Map<String, Integer> memory;
        private final Map<String, Integer> registers;

        private State(List<List<Op>> program, boolean[][] performed,
                      Map<String, Integer> memory, Map<String, Integer> registers) {
            this.program = program;
            this.performed = performed;
            this.memory = memory;
            this.registers = registers;
        }

        static State start(List<List<Op>> program) {
            boolean[][] performed = new boolean[program.size()][];
            for (int t = 0; t < program.size(); t++) {
                performed[t] = new boolean[program.get(t).size()];
            }
            return new State(List.copyOf(program), performed, new HashMap<>(), new HashMap<>());
        }

        /** How many threads the program has. */
        int threads() {
            return program.size();
        }

        /** Whether every operation of every thread has been performed. */
        boolean done() {
            for (int t = 0; t < threads(); t++) {
                if (!pending(t).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        /** The indexes of thread {@code t}'s operations not performed yet, in program order. */
        List<Integer> pending(int t) {
            List<Integer> left = new ArrayList<>();
            for (int i = 0; i < performed[t].length; i++) {
                if (!performed[t][i]) {
                    left.add(i);
                }
            }
            return left;
        }

        /** A copy of this state in which thread {@code t} has performed its operation {@code index}. */
        State run(int t, int index) {
            boolean[][] p = new boolean[performed.length][];
            for (int i = 0; i < performed.length; i++) {
                p[i] = performed[i].clone();
            }
            if (p[t][index]) {
                throw new IllegalArgumentException("operation " + index + " of thread " + t + " already performed");
            }
            p[t][index] = true;
            Map<String, Integer> mem = new HashMap<>(memory);
            Map<String, Integer> regs = new HashMap<>(registers);
            Op op = program.get(t).get(index);
            if (op instanceof Write w) {
                mem.put(w.variable(), w.value());
            } else if (op instanceof Read r) {
                regs.put(r.register(), mem.getOrDefault(r.variable(), 0));
            }
            return new State(program, p, mem, regs);
        }

        /** The registers' values so far. */
        Map<String, Integer> registers() {
            return Map.copyOf(registers);
        }
    }
}
