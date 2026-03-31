# 9.3.3. Command Pattern

## Concept Explanation

The Command pattern is a behavioral design pattern that turns a request into a stand-alone object that contains all information about the request. This transformation lets you pass requests as method arguments, delay or queue a request's execution, and support undoable operations.

**Real-world analogy**: Think of ordering food at a restaurant. You (the client) tell the waiter (invoker) what you want. The waiter writes the order on a slip (command object) and places it on the kitchen counter. The chef (receiver) reads the slip and prepares the food. The order slip decouples you from the chef -- you never interact with the kitchen directly. If you change your mind, you can cancel the order (undo). Multiple orders can be queued and processed in sequence.

The Command pattern consists of these participants:
- **Command** (interface): declares the `execute()` method, and optionally `undo()`.
- **ConcreteCommand**: implements the Command interface, holds a reference to the receiver and the parameters needed for the action.
- **Receiver**: the object that performs the actual work when a command is executed.
- **Invoker**: asks the command to carry out the request. It does not know the concrete command type or the receiver.
- **Client**: creates the concrete command and sets its receiver.

### When to Use the Command Pattern

1. When you need to **parameterize objects** with operations (pass actions as arguments).
2. When you need to **queue, schedule, or log** operations for later execution.
3. When you need to support **undo/redo** functionality.
4. When you need to implement **transactional systems** where operations can be rolled back.
5. When you want to **decouple the sender** of a request from the object that handles it.
6. When you need to implement **macro recording** (grouping multiple operations into one).

## Key Points to Remember

1. The Command pattern **encapsulates all request details** (receiver, method, arguments) in a single object.
2. It enables **undo/redo** by storing the state needed to reverse an operation.
3. Commands can be **composed into macros** using the Composite pattern.
4. Commands can be **queued and executed later** (deferred execution).
5. The invoker is **completely decoupled** from the receiver -- it only knows the Command interface.
6. In Java, **lambdas and Runnable** can serve as lightweight commands when undo is not needed.
7. The command **stores the state needed for undo** at execution time, not at construction time.

## Relevant Java 21 Features

- **Functional interfaces (Java 8+)**: `Runnable`, `Consumer<T>`, and `Callable<T>` can act as simple commands. For fire-and-forget operations without undo, lambdas are sufficient.
- **Records (Java 16+)**: Immutable command data (parameters, metadata) can be expressed as records.
- **Sealed interfaces (Java 17+)**: When the set of commands is fixed, sealed interfaces enable exhaustive handling via pattern matching.
- **Virtual threads (Java 21)**: Commands queued for asynchronous execution can use virtual threads for lightweight concurrency.
- **Pattern matching for switch (Java 21)**: When processing different command types, pattern matching provides clean dispatching logic.

### Evolution Across Java Versions

| Version | Impact on Command Pattern |
|---------|----------------------------|
| Pre-Java 8 | Required separate classes for every command |
| Java 8 | Lambdas enabled lightweight commands for simple operations |
| Java 8 | `CompletableFuture` enabled async command execution with chaining |
| Java 16+ | Records for immutable command parameters |
| Java 17+ | Sealed interfaces for closed command hierarchies |
| Java 21 | Virtual threads for scalable async command processing |

## Common Pitfalls and How to Avoid Them

1. **Forgetting to save state for undo**: The `undo()` method needs the state that existed before `execute()` was called. If you don't capture it during `execute()`, undo will fail.
   ```java
   // Problem: no state saved for undo
   class DeleteCommand implements Command {
       private TextEditor editor;
       private int position, length;
       
       public void execute() {
           editor.delete(position, length); // text is gone!
       }
       
       public void undo() {
           // Can't undo -- we don't know what was deleted!
       }
   }
   
   // Fix: save deleted text during execute
   class DeleteCommand implements Command {
       private String deletedText; // saved for undo
       
       public void execute() {
           deletedText = editor.getContent().substring(position, position + length);
           editor.delete(position, length);
       }
       
       public void undo() {
           editor.insert(position, deletedText);
       }
   }
   ```

2. **Calling undo on a command that was never executed**: Guard against this with a state check.
   ```java
   public void undo() {
       if (deletedText == null) {
           throw new IllegalStateException("Cannot undo: command was never executed");
       }
       editor.insert(position, deletedText);
   }
   ```

3. **Not clearing the redo stack after a new command**: When a user executes a new command after undoing some commands, the redo stack should be cleared, otherwise the history becomes inconsistent.
   ```java
   public void executeCommand(Command cmd) {
       cmd.execute();
       undoStack.push(cmd);
       redoStack.clear(); // critical!
   }
   ```

4. **Macro undo in the wrong order**: When undoing a macro (composite command), commands must be undone in **reverse order** to maintain consistency.

5. **Shared mutable state in commands**: If multiple commands reference the same receiver and are executed concurrently, race conditions can occur. Ensure commands are either executed sequentially or the receiver is thread-safe.

## Best Practices and Optimization Techniques

1. **Keep commands small and focused**: Each command should encapsulate exactly one operation. Complex workflows should use macro commands.

2. **Make commands immutable where possible**: Command parameters set at construction should be final. Only undo state captured during execution should be mutable.

3. **Use lambdas for simple fire-and-forget commands**: When undo is not needed, `Runnable` or `Consumer<T>` is sufficient.
   ```java
   CommandQueue queue = new CommandQueue();
   queue.enqueue(() -> System.out.println("Hello")); // simple lambda command
   ```

4. **Implement the Memento pattern for complex undo**: For receivers with complex state, use the Memento pattern to save and restore snapshots rather than trying to reverse individual operations.

5. **Limit undo history size**: Unbounded undo stacks can consume excessive memory. Set a maximum size and discard oldest commands.

6. **Serialize commands for persistence**: Commands can be serialized to a log for replay, audit trails, or crash recovery (event sourcing).

7. **Use a command queue for batch/deferred execution**: Queue commands and execute them later, in batches, or on a specific thread.

## Edge Cases and Their Handling

1. **Empty command queue**: `executeAll()` on an empty queue should return 0 or be a no-op, not throw.
2. **Undo on empty undo stack**: Return false or throw a descriptive exception -- do not corrupt state.
3. **Redo after new command**: After a new command is executed, the redo stack must be cleared.
4. **Macro with zero commands**: Reject empty command lists in macro construction.
5. **Command that fails mid-execution**: If `execute()` fails partway, the receiver may be in an inconsistent state. Consider transaction-like rollback semantics.
6. **Null command references**: Always validate that commands are non-null before execution or enqueuing.
7. **Undo of a delete command that was never executed**: The saved text is null; throw `IllegalStateException`.

## Interview-specific Insights

Interviewers often focus on:
- How to implement undo/redo with the Command pattern
- The difference between Command and Strategy patterns (Command: encapsulates a request including receiver; Strategy: encapsulates an algorithm without receiver)
- Real-world uses: text editors, transaction systems, task schedulers, macro recording, event sourcing
- How lambdas simplify the Command pattern in modern Java
- The relationship between Command and Memento patterns (both deal with state capture)
- How Command supports the Open/Closed Principle

Common tricky questions:
- "How is Command different from Strategy?" (Command encapsulates a request with a receiver; Strategy encapsulates an algorithm that the context delegates to)
- "How do you implement redo?" (Maintain separate undo and redo stacks; redo re-executes a previously undone command)
- "Can a command be executed more than once?" (Yes, for replay/retry scenarios, but be careful about idempotency)

## Interview Q&A Section

**Q1: What is the Command pattern and what problem does it solve?**

```text
A1: The Command pattern encapsulates a request as an object, containing all the
information needed to perform an action. It decouples the object that invokes the
operation from the object that knows how to perform it.

Problems it solves:
1. Undo/Redo: By storing executed commands, you can reverse (undo) and replay
   (redo) operations.
2. Deferred execution: Commands can be queued and executed later.
3. Decoupling: The invoker doesn't need to know anything about the receiver
   or the operation details.
4. Logging/Auditing: Commands can be logged for audit trails or replayed
   for crash recovery.
5. Macro recording: Multiple commands can be grouped into a composite (macro)
   command.
6. Transaction rollback: Failed operations can be undone by reversing the
   command sequence.

Participants:
- Command interface: execute() and undo() methods
- ConcreteCommand: binds a receiver to an action with parameters
- Receiver: performs the actual work
- Invoker: stores and triggers commands
- Client: creates commands and sets their receivers
```

```java
// Command interface
interface Command {
    void execute();
    void undo();
    String description();
}

// Receiver
class TextEditor {
    private StringBuilder content = new StringBuilder();

    public void insert(int pos, String text) { content.insert(pos, text); }
    public void delete(int pos, int len) { content.delete(pos, pos + len); }
    public String getContent() { return content.toString(); }
}

// Concrete Command
class InsertCommand implements Command {
    private final TextEditor editor;
    private final int position;
    private final String text;

    InsertCommand(TextEditor editor, int position, String text) {
        this.editor = editor;
        this.position = position;
        this.text = text;
    }

    @Override public void execute() { editor.insert(position, text); }
    @Override public void undo() { editor.delete(position, text.length()); }
    @Override public String description() { return "Insert '" + text + "'"; }
}
```

**Q2: How do you implement undo/redo with the Command pattern?**

```text
A2: Undo/redo is implemented using two stacks:

1. Undo stack: stores commands that have been executed (most recent on top)
2. Redo stack: stores commands that have been undone (most recent on top)

Operations:
- Execute: run the command, push it onto the undo stack, CLEAR the redo stack
- Undo: pop from undo stack, call undo(), push onto redo stack
- Redo: pop from redo stack, call execute(), push onto undo stack

Critical rules:
- When a NEW command is executed, the redo stack MUST be cleared.
  This prevents an inconsistent state where old redo commands conflict
  with the new command.
- Each command must store enough state in execute() to reverse itself in undo().
  For example, a DeleteCommand must save the deleted text so it can re-insert it.
- Macro (composite) commands must undo in REVERSE order.

The undo/redo stacks can optionally have a maximum size to limit memory usage.
```

```java
class CommandHistory {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // new command invalidates redo history
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        return true;
    }
}

// Usage
TextEditor editor = new TextEditor();
CommandHistory history = new CommandHistory();

history.executeCommand(new InsertCommand(editor, 0, "Hello"));
history.executeCommand(new InsertCommand(editor, 5, " World"));
// editor: "Hello World"

history.undo();  // editor: "Hello"
history.undo();  // editor: ""
history.redo();  // editor: "Hello"
```

**Q3: What is the difference between the Command and Strategy patterns?**

```text
A3: While both patterns encapsulate behavior, they differ fundamentally:

Command Pattern:
- Encapsulates a REQUEST (action + receiver + parameters)
- The command KNOWS its receiver and BINDS to it
- Supports undo/redo by storing execution state
- Commands are typically stored, queued, or logged
- Used for: transactions, undo/redo, task scheduling
- Example: "Delete 5 characters from position 3 in this specific document"

Strategy Pattern:
- Encapsulates an ALGORITHM (a way to do something)
- The strategy does NOT know the context -- it's passed data to process
- No concept of undo (algorithms are stateless transformations)
- Strategies are interchangeable at runtime
- Used for: algorithm selection, behavior customization
- Example: "Sort this list using bubble sort" vs. "Sort using quicksort"

Structural difference:
- Command: Command -> Receiver (command holds reference to receiver)
- Strategy: Context -> Strategy (context holds reference to strategy)

Lifecycle difference:
- Command: typically created, executed once (or undo/redo), then discarded
- Strategy: typically set once and reused for multiple operations

Memory model:
- Command: may hold mutable state (for undo)
- Strategy: should be stateless or immutable
```

```java
// Command: encapsulates a specific request
class InsertCommand implements Command {
    private final TextEditor editor; // bound to a specific receiver
    private final String text;
    private final int position;

    public void execute() { editor.insert(position, text); }
    public void undo() { editor.delete(position, text.length()); }
}

// Strategy: encapsulates an algorithm, no receiver binding
interface SortStrategy<T extends Comparable<T>> {
    List<T> sort(List<T> data); // works on any data passed to it
}

class BubbleSortStrategy<T extends Comparable<T>> implements SortStrategy<T> {
    public List<T> sort(List<T> data) {
        // algorithm implementation -- no specific receiver
    }
}
```

**Q4: How do lambda expressions simplify the Command pattern in modern Java?**

```text
A4: Lambda expressions provide a lightweight way to create commands when you
don't need undo functionality or complex state management.

Since Command interfaces with a single method (like Runnable or Consumer) are
functional interfaces, any lambda can serve as a command.

When to use lambdas:
- Fire-and-forget commands (no undo needed)
- Simple one-line operations
- Callback-style commands
- Event handlers

When to use full Command classes:
- When undo/redo is required (need to store state)
- When the command has multiple methods (execute, undo, description)
- When the command needs complex initialization or validation
- When commands need to be serialized or persisted

Hybrid approach: Use a LambdaCommand wrapper that accepts lambdas for both
execute and undo, providing the best of both worlds.
```

```java
// Simple lambda commands (no undo)
Runnable printCommand = () -> System.out.println("Hello");
printCommand.run();

// LambdaCommand with undo support
class LambdaCommand implements Command {
    private final Runnable executeAction;
    private final Runnable undoAction;
    private final String description;

    LambdaCommand(String description, Runnable execute, Runnable undo) {
        this.description = description;
        this.executeAction = execute;
        this.undoAction = undo;
    }

    public void execute() { executeAction.run(); }
    public void undo() { undoAction.run(); }
    public String description() { return description; }
}

// Usage
AtomicInteger counter = new AtomicInteger(0);
Command increment = new LambdaCommand(
    "increment counter",
    counter::incrementAndGet,
    counter::decrementAndGet
);

CommandHistory history = new CommandHistory();
history.executeCommand(increment); // counter = 1
history.undo();                    // counter = 0
history.redo();                    // counter = 1
```

**Q5: What is a Macro Command and how does it work?**

```text
A5: A Macro Command (also called Composite Command) groups multiple commands into
a single command object. When the macro is executed, all its sub-commands execute in
sequence. When the macro is undone, all sub-commands are undone in REVERSE order.

This is a combination of the Command pattern and the Composite pattern.

Key characteristics:
1. Execute: runs all commands in order (first to last)
2. Undo: undoes all commands in reverse order (last to first)
3. Atomic intent: the macro represents a single logical operation composed of steps
4. Can be nested: a macro can contain other macros

Use cases:
- Text editor macros (record and replay a sequence of edits)
- Database transactions (group of SQL operations)
- Build systems (compile, test, package as a single "build" command)
- Batch processing (apply a series of transformations to data)

Important: undo order matters! If you insert "Hello" at position 0 then insert
" World" at position 5, undoing must remove " World" first, then "Hello" --
otherwise positions would be wrong.
```

```java
class MacroCommand implements Command {
    private final List<Command> commands;
    private final String name;

    MacroCommand(String name, List<Command> commands) {
        this.name = name;
        this.commands = new ArrayList<>(commands);
    }

    @Override
    public void execute() {
        for (Command cmd : commands) {
            cmd.execute();
        }
    }

    @Override
    public void undo() {
        // Undo in REVERSE order
        ListIterator<Command> it = commands.listIterator(commands.size());
        while (it.hasPrevious()) {
            it.previous().undo();
        }
    }

    @Override
    public String description() {
        return "Macro[%s]: %d commands".formatted(name, commands.size());
    }
}

// Usage
TextEditor editor = new TextEditor();
MacroCommand helloWorld = new MacroCommand("hello-world", List.of(
    new InsertCommand(editor, 0, "Hello"),
    new InsertCommand(editor, 5, " World")
));

CommandHistory history = new CommandHistory();
history.executeCommand(helloWorld);  // editor: "Hello World"
history.undo();                       // editor: "" (both inserts undone)
history.redo();                       // editor: "Hello World"
```

**Q6: How does the Command pattern support event sourcing and CQRS?**

```text
A6: Event sourcing is an architectural pattern where state changes are stored as
a sequence of events (commands) rather than storing the current state directly.
The Command pattern is the foundation of event sourcing.

How they relate:
1. Each state change is represented as a Command object
2. Commands are stored in an append-only event log
3. Current state can be rebuilt by replaying all commands from the beginning
4. The event log serves as a complete audit trail

CQRS (Command Query Responsibility Segregation) separates read and write models:
- Write side: accepts commands that modify state
- Read side: serves queries from a denormalized read model
- Commands update the write model and publish events
- Read model subscribes to events and updates its projections

Benefits of Command pattern in event sourcing:
- Complete audit trail of all changes
- Time travel: rebuild state at any point in time
- Debugging: replay events to reproduce bugs
- Scalability: commands can be processed asynchronously
- Recovery: replay events after a crash to rebuild state

Implementation considerations:
- Commands must be serializable for persistence
- Commands should be idempotent for safe replay
- Consider snapshots to avoid replaying entire history
```

```java
// Command as an event for event sourcing
interface DomainCommand {
    void execute();
    String toJson(); // serializable for persistence
    long timestamp();
}

// Event store
class EventStore {
    private final List<DomainCommand> events = new ArrayList<>();

    public void append(DomainCommand command) {
        command.execute();
        events.add(command);
    }

    // Rebuild state by replaying all events
    public void replay(Consumer<DomainCommand> handler) {
        events.forEach(handler);
    }

    // Get events after a specific point
    public List<DomainCommand> getEventsSince(long timestamp) {
        return events.stream()
            .filter(e -> e.timestamp() > timestamp)
            .toList();
    }
}

// Command queue for deferred/batch execution
class CommandQueue {
    private final Queue<Command> queue = new LinkedList<>();

    public void enqueue(Command command) { queue.add(command); }

    public int executeAll() {
        int count = 0;
        while (!queue.isEmpty()) {
            queue.poll().execute();
            count++;
        }
        return count;
    }
}
```

## Code Examples

- Test: [CommandPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/behavioralpatterns/CommandPatternTest.java)
- Source: [CommandPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/behavioralpatterns/CommandPattern.java)
