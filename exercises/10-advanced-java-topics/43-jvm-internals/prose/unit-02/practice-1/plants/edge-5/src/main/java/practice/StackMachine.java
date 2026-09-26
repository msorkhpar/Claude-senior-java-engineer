package practice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class StackMachine {

    private StackMachine() {
    }

    /** Runs {@code program} with {@code args} in the first local variables and returns what ireturn pops. */
    public static int run(List<String> program, int... args) {
        int[] locals = new int[16];
        System.arraycopy(args, 0, locals, 0, args.length);
        Deque<Integer> stack = new ArrayDeque<>();
        for (String instruction : program) {
            String[] parts = instruction.trim().split("[ _]+");
            switch (parts[0]) {
                case "iload" -> stack.push(locals[Integer.parseInt(parts[1])]);
                case "istore" -> locals[Integer.parseInt(parts[1])] = pop(stack);
                case "iconst", "bipush" -> stack.push(Integer.parseInt(parts[1]));
                case "iinc" -> locals[Integer.parseInt(parts[1])] += Integer.parseInt(parts[2]);
                case "iadd", "isub", "imul", "idiv" -> {
                    int right = pop(stack);
                    int left = pop(stack);
                    stack.push(switch (parts[0]) {
                        case "iadd" -> left + right;
                        case "isub" -> left - right;
                        case "imul" -> left * right;
                        default -> left / right;
                    });
                }
                case "ireturn" -> {
                    return pop(stack);
                }
                default -> throw new IllegalArgumentException("unknown instruction: " + instruction);
            }
        }
        throw new IllegalStateException("the program ended without ireturn");
    }

    private static int pop(Deque<Integer> stack) {
        return stack.isEmpty() ? 0 : stack.pop();
    }
}
