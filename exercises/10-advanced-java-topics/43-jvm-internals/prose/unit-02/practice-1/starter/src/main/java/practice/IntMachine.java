package practice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Runs the int instructions of one static method, written as javap -c prints them
 * ("iload_0", "iinc          0, 1", "bipush        10"), and returns what ireturn pops.
 */
public final class IntMachine {

    private final int[] locals = new int[8];
    private final Deque<Integer> stack = new ArrayDeque<>();

    private IntMachine(int... args) {
        System.arraycopy(args, 0, locals, 0, args.length);
    }

    /** The arguments fill locals 0, 1, 2, ... as they do for a static method. */
    public static int run(List<String> program, int... args) {
        return new IntMachine(args).execute(program);
    }

    private int execute(List<String> program) {
        for (String line : program) {
            String[] parts = line.trim().split("[\\s,_]+");
            String op = parts[0];
            switch (op) {
                case "iload" -> stack.push(locals[Integer.parseInt(parts[1])]);
                case "istore" -> locals[Integer.parseInt(parts[1])] = pop();
                case "iconst" -> stack.push(parts[1].equals("m1") ? -1 : Integer.parseInt(parts[1]));
                case "bipush", "sipush" -> stack.push(Integer.parseInt(parts[1]));
                case "iinc" -> increment(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                case "iadd", "isub", "imul", "idiv" -> arithmetic(op);
                case "ireturn" -> {
                    return pop();
                }
                default -> throw new IllegalArgumentException("not supported: " + line);
            }
        }
        throw new IllegalStateException("the program ended without ireturn");
    }

    // TODO: what iinc does with its two operands, as javac uses it for i++, ++i, i += 5 and i--.
    void increment(int local, int delta) {
        throw new IllegalStateException("TODO: increment");
    }

    // TODO: pop the two operands of iadd, isub, imul or idiv and push the int result.
    // pop() and push(int) are below.
    void arithmetic(String op) {
        throw new IllegalStateException("TODO: arithmetic");
    }

    int pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("operand stack is empty");
        }
        return stack.pop();
    }

    void push(int value) {
        stack.push(value);
    }
}
