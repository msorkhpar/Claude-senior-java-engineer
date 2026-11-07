package practice;

import java.lang.reflect.Method;

/** Runs an operator's command by name against a handler object, for the ops console. */
public final class Dispatcher {

    /** What the console shows: an HTTP-like status and a line of text. */
    public record Reply(int status, String body) {
    }

    private final Object handler;

    public Dispatcher(Object handler) {
        this.handler = handler;
    }

    /** Runs the handler's public method called {@code command} with {@code args}. */
    public Reply call(String command, Object... args) {
        for (Method method : handler.getClass().getMethods()) {
            if (method.getName().equals(command) && method.getDeclaringClass() != Object.class) {
                return run(method, args);
            }
        }
        return new Reply(404, "no command " + command);
    }

    /**
     * 200 with {@code String.valueOf} of the result when the command completes; 422 with the
     * command's own message when the command rejects its input with an
     * {@code IllegalArgumentException}; 400, starting "bad arguments", when the arguments don't
     * fit the command's parameters; 500 with the simple class name of whatever else the command
     * threw.
     */
    Reply run(Method method, Object... args) {
        // TODO: call the method on the handler and turn what happens into a Reply.
        throw new IllegalStateException("TODO: run " + method.getName());
    }
}
