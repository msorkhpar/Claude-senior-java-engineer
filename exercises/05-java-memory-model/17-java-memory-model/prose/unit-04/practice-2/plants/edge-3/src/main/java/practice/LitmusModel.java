package practice;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A small model checker for litmus tests: a few threads, each a list of reads and writes of
 * shared variables. It lists every combination of values the threads' reads can end with,
 * first with nothing reordered, then on a machine where every thread has a store buffer.
 */
public final class LitmusModel {

    private LitmusModel() {
    }

    /** One operation of a thread. */
    public sealed interface Op permits Write, Read, Fence {
    }

    /** Stores {@code value} into the shared variable {@code variable}. */
    public record Write(String variable, int value) implements Op {
    }

    /** Loads the shared variable {@code variable} into {@code register}, private to the thread. */
    public record Read(String variable, String register) implements Op {
    }

    /** A full fence: on the store-buffer machine, waits until the thread's own buffer is empty. */
    public record Fence() implements Op {
    }

    public static Op write(String variable, int value) {
        return new Write(variable, value);
    }

    public static Op read(String variable, String register) {
        return new Read(variable, register);
    }

    public static Op fence() {
        return new Fence();
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

    /**
     * Every set of register values the program can end with on a machine where each thread's
     * writes go into its own store buffer, a queue that reaches shared memory later, one write
     * at a time. Shared variables start at 0.
     */
    public static Set<Map<String, Integer>> outcomesWithStoreBuffers(List<List<Op>> threads) {
        Set<Map<String, Integer>> results = new HashSet<>();
        exploreBuffered(Buffered.start(threads), results);
        return results;
    }

    /** At each point, a thread may perform its next operation, or move one buffered write to memory. */
    static void exploreBuffered(Buffered state, Set<Map<String, Integer>> results) {
        if (state.done()) {
            results.add(state.registers());
            return;
        }
        for (int t = 0; t < state.threads(); t++) {
            if (state.hasNext(t) && state.canPerformNext(t)) {
                exploreBuffered(state.performNext(t), results);
            }
            if (!state.buffer(t).isEmpty()) {
                exploreBuffered(state.drainOne(t), results);
            }
        }
    }

    /** One buffered write: {@code value} on its way to {@code variable}. */
    record Pending(String variable, int value) {
    }

    /** A point in one execution of the store-buffer machine. Immutable. */
    static final class Buffered {
        private final List<List<Op>> program;
        private final int[] next;
        private final List<Deque<Pending>> buffers;
        private final Map<String, Integer> memory;
        private final Map<String, Integer> registers;

        private Buffered(List<List<Op>> program, int[] next, List<Deque<Pending>> buffers,
                         Map<String, Integer> memory, Map<String, Integer> registers) {
            this.program = program;
            this.next = next;
            this.buffers = buffers;
            this.memory = memory;
            this.registers = registers;
        }

        static Buffered start(List<List<Op>> program) {
            List<Deque<Pending>> buffers = new ArrayList<>();
            for (int t = 0; t < program.size(); t++) {
                buffers.add(new ArrayDeque<>());
            }
            return new Buffered(List.copyOf(program), new int[program.size()], buffers,
                    new HashMap<>(), new HashMap<>());
        }

        private Buffered copy() {
            List<Deque<Pending>> b = new ArrayList<>();
            for (Deque<Pending> d : buffers) {
                b.add(new ArrayDeque<>(d));
            }
            return new Buffered(program, next.clone(), b, new HashMap<>(memory), new HashMap<>(registers));
        }

        int threads() {
            return program.size();
        }

        /** Whether every operation has been performed and every buffer has reached memory. */
        boolean done() {
            for (int t = 0; t < threads(); t++) {
                if (hasNext(t) || !buffers.get(t).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        /** Whether thread {@code t} has an operation left. */
        boolean hasNext(int t) {
            return next[t] < program.get(t).size();
        }

        /** Thread {@code t}'s next operation in program order. */
        Op nextOp(int t) {
            return program.get(t).get(next[t]);
        }

        /** Thread {@code t}'s buffered writes, oldest first. Read-only. */
        List<Pending> buffer(int t) {
            return List.copyOf(buffers.get(t));
        }

        /** The value shared memory holds for {@code variable}. */
        int memory(String variable) {
            return memory.getOrDefault(variable, 0);
        }

        /** A copy in which thread {@code t} has performed its next operation. */
        Buffered performNext(int t) {
            Buffered s = copy();
            Op op = nextOp(t);
            s.next[t]++;
            if (op instanceof Write w) {
                s.buffers.get(t).addLast(new Pending(w.variable(), w.value()));
            } else if (op instanceof Read r) {
                s.registers.put(r.register(), load(t, r.variable()));
            }
            return s;
        }

        /** The value thread {@code t} gets when it reads {@code variable} now. */
        int load(int t, String variable) {
            Iterator<Pending> newestFirst = buffers.get(t).descendingIterator();
            while (newestFirst.hasNext()) {
                Pending p = newestFirst.next();
                if (p.variable().equals(variable)) {
                    return p.value();
                }
            }
            return memory(variable);
        }

        /** Whether thread {@code t} may perform its next operation now. */
        boolean canPerformNext(int t) {
            return !(nextOp(t) instanceof Fence) || buffers.get(t).isEmpty();
        }

        /** A copy in which one of thread {@code t}'s buffered writes has reached shared memory. */
        Buffered drainOne(int t) {
            Buffered s = copy();
            Pending p = s.buffers.get(t).removeLast();
            s.memory.put(p.variable(), p.value());
            return s;
        }

        Map<String, Integer> registers() {
            return Map.copyOf(registers);
        }
    }
}
