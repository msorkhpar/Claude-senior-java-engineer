# 10.5.1. JVM Architecture (Class Loader, Execution Engine, Runtime Data Areas)

## Concept Explanation

The Java Virtual Machine (JVM) is the cornerstone of Java's "write once, run anywhere" philosophy. It is an abstract
computing machine that provides a runtime environment for executing Java bytecode. The JVM architecture consists of three
major subsystems that work together to load, verify, and execute Java programs.

**Real-world analogy**: Think of the JVM as a modern airport. The **Class Loader** is like the check-in and security
system -- it receives passengers (classes), verifies their tickets and identity (bytecode verification), and sends them
to the correct terminal (memory area). The **Runtime Data Areas** are the various terminals and gates -- each serves a
different purpose (method area for flight information, heap for passenger lounges, stack for boarding queues). The
**Execution Engine** is the set of runways and air traffic control -- it actually executes the flights (methods), decides
which planes take off first, and optimizes routes (JIT compilation).

### The Three Major Subsystems

#### 1. Class Loader Subsystem

The Class Loader Subsystem handles the loading, linking, and initialization of classes. It follows a delegation
hierarchy:

- **Bootstrap Class Loader**: Loads core Java classes from `java.base` module (rt.jar in older versions). Written in
  native code (C/C++), it is the parent of all class loaders.
- **Platform (Extension) Class Loader**: Loads classes from the platform extensions. In Java 9+, this is the Platform
  Class Loader.
- **Application (System) Class Loader**: Loads classes from the application classpath.

The class loading process has three phases:
1. **Loading**: Finds the binary representation of a class and creates a `Class` object.
2. **Linking**: Verifies bytecode, prepares static fields, and resolves symbolic references.
3. **Initialization**: Executes static initializers and assigns values to static fields.

#### 2. Runtime Data Areas

The JVM defines several memory areas used during program execution:

- **Method Area (Metaspace in Java 8+)**: Stores class metadata, method data, constant pool, and static variables.
  Shared among all threads.
- **Heap**: The runtime data area from which memory for all class instances and arrays is allocated. Shared among all
  threads and managed by the garbage collector.
- **Java Stack (per thread)**: Each thread has its own stack containing frames. Each frame holds local variables, operand
  stack, and a reference to the runtime constant pool.
- **PC Register (per thread)**: Holds the address of the currently executing JVM instruction.
- **Native Method Stack (per thread)**: Contains native method information.

#### 3. Execution Engine

The Execution Engine reads bytecode and executes it:

- **Interpreter**: Reads and executes bytecode instructions one by one. Fast to start but slow for repeated execution.
- **JIT Compiler**: Compiles frequently executed bytecode ("hot spots") into native machine code for better performance.
- **Garbage Collector**: Automatically manages heap memory by reclaiming objects that are no longer referenced.

## Key Points to Remember

1. The JVM uses a **parent delegation model** for class loading -- a child class loader delegates to its parent first
   before attempting to load a class itself.
2. **Metaspace** replaced PermGen in Java 8 and uses native memory instead of heap memory.
3. Each thread has its own **stack**, **PC register**, and **native method stack** -- these are not shared.
4. The **heap** and **method area** are shared across all threads.
5. The class loading process is **lazy** -- classes are loaded only when first referenced.
6. **LinkageError** occurs when a class has already been loaded by a different class loader.
7. The JVM specification defines the architecture, but implementations (HotSpot, GraalVM, OpenJ9) may differ in
   details.
8. Stack frames are created for each method invocation and destroyed when the method completes.

## Relevant Java 21 Features

- **Metaspace improvements**: Better memory management with elastic Metaspace (JEP 387 in Java 16), reducing memory
  footprint by returning unused memory to the OS more eagerly.
- **CDS (Class Data Sharing)**: Enhanced in Java 21 to speed up startup by sharing class metadata across JVM instances.
- **Virtual threads (JEP 444)**: Virtual threads have lightweight stacks that are stored on the heap rather than
  allocating OS-level thread stacks, fundamentally changing the per-thread memory model.
- **ZGC and Shenandoah**: Modern garbage collectors with sub-millisecond pause times, available as production-ready in
  Java 21.
- **String Deduplication**: Available with G1 and ZGC collectors to reduce heap usage for duplicate strings.

## Common Pitfalls and How to Avoid Them

1. **ClassNotFoundException vs. NoClassDefFoundError**: These are commonly confused.
   ```java
   // ClassNotFoundException: thrown when Class.forName() or ClassLoader.loadClass() fails
   try {
       Class.forName("com.example.NonExistent");
   } catch (ClassNotFoundException e) {
       // Class was not found on classpath at runtime
   }

   // NoClassDefFoundError: class was available at compile time but not at runtime
   // or static initializer failed during first load
   ```
   **Solution**: Understand that `ClassNotFoundException` is a checked exception (class not found during dynamic
   loading), while `NoClassDefFoundError` is an error (class was known at compile time but missing/failed at runtime).

2. **StackOverflowError from deep recursion**:
   ```java
   // Unbounded recursion exhausts the thread stack
   public int factorial(int n) {
       return n * factorial(n - 1); // Missing base case!
   }
   ```
   **Solution**: Always have a base case. Use `-Xss` flag to increase stack size if needed, or convert to iterative.

3. **OutOfMemoryError: Metaspace**: Loading too many classes (common in applications with heavy reflection or dynamic
   proxy generation).
   ```java
   // Dynamically generating classes without limit
   while (true) {
       Proxy.newProxyInstance(loader, interfaces, handler);
   }
   ```
   **Solution**: Set `-XX:MaxMetaspaceSize` and investigate class loader leaks. Use tools like `jmap` to inspect.

4. **Class loader leaks in web applications**: Failing to properly clean up class loaders during hot redeployment.
   **Solution**: Ensure ThreadLocal variables, JDBC drivers, and shutdown hooks are properly cleaned up on undeploy.

## Best Practices and Optimization Techniques

1. **Minimize class loading overhead**: Use CDS (Class Data Sharing) with `-Xshare:dump` and `-Xshare:on` for faster
   startup.
2. **Monitor Metaspace usage**: Set `-XX:MaxMetaspaceSize` to prevent unbounded native memory growth.
3. **Thread stack sizing**: Use `-Xss` to tune thread stack size. The default is typically 512KB-1MB. For applications
   with many threads, reducing this can save memory.
4. **Understand the delegation model**: When writing custom class loaders, respect the parent delegation model unless
   you have a specific reason to break it (e.g., hot-reloading, isolation).
5. **Use `jcmd` and `jmap`** to inspect class loading statistics and memory areas at runtime.
6. **Avoid loading classes in performance-critical paths**: Class loading involves I/O and synchronization.

## Edge Cases and Their Handling

1. **Circular class dependencies**: The JVM handles circular references during class loading through a multi-phase
   linking process, but overly complex circular dependencies can cause `ClassCircularityError`.
2. **Static initializer failures**: If a class's static initializer throws an exception, the class is marked as
   unusable and subsequent access throws `NoClassDefFoundError` (not the original exception).
3. **Thread-safety of class initialization**: The JVM guarantees that a class is initialized by exactly one thread; other
   threads wait until initialization completes.
4. **Custom class loaders with null parent**: Passing null as parent to a ClassLoader makes the bootstrap class loader
   the parent, not the system class loader.

## Interview-specific Insights

Interviewers often focus on:

- Explaining the class loader hierarchy and parent delegation model
- Differentiating between the various memory areas (heap vs. stack vs. method area)
- Understanding when `ClassNotFoundException` vs. `NoClassDefFoundError` occurs
- How static initializers are executed and their thread safety
- The difference between PermGen (pre-Java 8) and Metaspace (Java 8+)
- How the JVM decides when to use the interpreter vs. JIT compiler

Common tricky questions:
- "What happens if the same class is loaded by two different class loaders?"
- "Can you explain what happens step-by-step when `new MyClass()` is executed?"
- "Why was PermGen replaced by Metaspace?"

## Interview Q&A Section

**Q1: Explain the JVM class loader hierarchy and the parent delegation model.**

```text
A1: The JVM uses a hierarchical class loader system with three built-in class loaders:

1. Bootstrap Class Loader: The root of the hierarchy, written in native code. It loads core
   Java classes from the java.base module (e.g., java.lang.Object, java.lang.String).

2. Platform (Extension) Class Loader: Child of Bootstrap. Loads platform extension classes.

3. Application (System) Class Loader: Child of Platform. Loads classes from the application
   classpath (specified by -cp or CLASSPATH environment variable).

The Parent Delegation Model works as follows:
- When a class loader receives a request to load a class, it first delegates to its parent.
- The parent delegates to its parent, all the way up to the Bootstrap Class Loader.
- If the parent cannot load the class, the child attempts to load it.
- This ensures core Java classes are always loaded by the Bootstrap loader, preventing
  malicious code from replacing core classes.

Benefits:
- Security: Prevents untrusted code from replacing core Java classes
- Consistency: Ensures the same class is loaded only once
- Namespace isolation: Classes loaded by different loaders are in different namespaces
```

```java
// Demonstrating class loader hierarchy
public class ClassLoaderHierarchy {
    public static void main(String[] args) {
        // Application class loader
        ClassLoader appLoader = ClassLoaderHierarchy.class.getClassLoader();
        System.out.println("App ClassLoader: " + appLoader);

        // Platform class loader (parent of application)
        ClassLoader platformLoader = appLoader.getParent();
        System.out.println("Platform ClassLoader: " + platformLoader);

        // Bootstrap class loader (parent of platform) - returns null
        ClassLoader bootstrapLoader = platformLoader.getParent();
        System.out.println("Bootstrap ClassLoader: " + bootstrapLoader); // null

        // Core classes are loaded by bootstrap
        System.out.println("String loader: " + String.class.getClassLoader()); // null
    }
}
```

**Q2: What are the different runtime memory areas in the JVM?**

```text
A2: The JVM defines five main memory areas:

1. Method Area (Metaspace since Java 8):
   - Shared across all threads
   - Stores class metadata, method bytecode, constant pool, static variables
   - In Java 8+, uses native memory (not heap) as Metaspace
   - Can be tuned with -XX:MetaspaceSize and -XX:MaxMetaspaceSize

2. Heap:
   - Shared across all threads
   - Where all objects and arrays are allocated
   - Managed by the garbage collector
   - Divided into Young Generation (Eden + Survivor spaces) and Old Generation
   - Tuned with -Xms (initial) and -Xmx (maximum)

3. Java Stack (per thread):
   - Each thread has its own stack
   - Contains stack frames, one per method invocation
   - Each frame holds local variables, operand stack, and frame data
   - Tuned with -Xss flag
   - StackOverflowError when stack is exhausted

4. PC (Program Counter) Register (per thread):
   - Holds address of the currently executing JVM instruction
   - For native methods, the PC register is undefined

5. Native Method Stack (per thread):
   - Holds native method information
   - Used when the JVM invokes native (C/C++) methods via JNI
```

```java
// Demonstrating memory areas through code behavior
public class MemoryAreas {
    // Stored in Method Area (Metaspace) - static field
    static int staticCounter = 0;

    // Object fields stored in Heap
    private String name;

    public void demonstrateStack(int depth) {
        // 'depth' is a local variable on the Java Stack
        int localVar = depth * 2; // Also on the stack
        if (depth > 0) {
            demonstrateStack(depth - 1); // New stack frame created
        }
        // Stack frame destroyed when method returns
    }

    public static void main(String[] args) {
        // 'obj' reference is on the stack; actual object is on the heap
        MemoryAreas obj = new MemoryAreas();
        obj.name = "example"; // String object on heap, reference in object on heap
        staticCounter++;       // Modifies value in Method Area
    }
}
```

**Q3: What is the difference between ClassNotFoundException and NoClassDefFoundError?**

```text
A3: These are fundamentally different despite seeming similar:

ClassNotFoundException:
- It is a checked exception (extends Exception)
- Occurs during dynamic/reflective class loading
- Thrown by methods like Class.forName(), ClassLoader.loadClass()
- Means the class was never found on the classpath at all
- Recoverable -- you can catch and handle it

NoClassDefFoundError:
- It is an error (extends Error -> LinkageError)
- Occurs when the JVM or ClassLoader tries to load a class that was available at
  compile time but is missing at runtime
- Also occurs when static initialization of a class fails
- The class was known to exist (compiled against it) but cannot be loaded now
- Generally not recoverable

A common scenario for NoClassDefFoundError:
1. Class A compiles against class B (B is on compile classpath)
2. At runtime, B's jar is missing from the runtime classpath
3. When A tries to use B, NoClassDefFoundError is thrown

Another scenario:
1. Class B's static initializer throws an exception on first load
2. B is marked as unusable
3. Subsequent attempts to use B throw NoClassDefFoundError
```

```java
// ClassNotFoundException example
public class ClassLoadingErrors {
    public static void classNotFoundExample() {
        try {
            // Dynamic loading -- class doesn't exist
            Class<?> clazz = Class.forName("com.example.DoesNotExist");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        }
    }

    // NoClassDefFoundError from static initializer failure
    static class FailingClass {
        static {
            if (true) throw new RuntimeException("Static init failed!");
        }
    }

    public static void noClassDefFoundExample() {
        try {
            new FailingClass(); // First attempt -- ExceptionInInitializerError
        } catch (ExceptionInInitializerError e) {
            System.out.println("First load failed: " + e.getCause());
        }
        try {
            new FailingClass(); // Second attempt -- NoClassDefFoundError
        } catch (NoClassDefFoundError e) {
            System.out.println("Subsequent load: " + e.getMessage());
        }
    }
}
```

**Q4: Why was PermGen replaced by Metaspace in Java 8?**

```text
A4: PermGen (Permanent Generation) had several significant problems:

1. Fixed Size: PermGen had a fixed maximum size (default 64MB-256MB depending on platform).
   This made it prone to OutOfMemoryError: PermGen space, especially in applications that
   loaded many classes (e.g., application servers with hot deployment).

2. Difficult Tuning: Developers had to manually tune -XX:MaxPermSize, and getting it wrong
   caused either wasted memory or OOM errors.

3. Garbage Collection Complexity: PermGen was collected during Full GC, which could be
   expensive. Class unloading was tightly coupled with GC.

4. Internal Overhead: JVM internal data structures were stored alongside class metadata,
   making memory management complex.

Metaspace (Java 8+) improvements:
1. Uses native memory instead of heap memory -- can grow dynamically.
2. Default max size is effectively unlimited (bounded by available native memory).
3. Automatic tuning: Metaspace grows and shrinks based on demand.
4. Better memory management: Class metadata is allocated in chunks and can be returned
   to the OS when no longer needed (especially with elastic Metaspace in Java 16+).
5. Simpler GC: Class unloading is more efficient.

Tuning options:
- -XX:MetaspaceSize: Initial metaspace size (triggers GC when reached)
- -XX:MaxMetaspaceSize: Maximum metaspace size (safety limit)
```

```java
// Monitoring Metaspace usage
public class MetaspaceMonitoring {
    public static void main(String[] args) {
        // Runtime memory information (heap only)
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Heap - Max: " + runtime.maxMemory() / 1024 / 1024 + "MB");
        System.out.println("Heap - Total: " + runtime.totalMemory() / 1024 / 1024 + "MB");
        System.out.println("Heap - Free: " + runtime.freeMemory() / 1024 / 1024 + "MB");

        // For Metaspace, use ManagementFactory
        java.lang.management.MemoryMXBean memBean =
            java.lang.management.ManagementFactory.getMemoryMXBean();
        System.out.println("Non-Heap (includes Metaspace): " +
            memBean.getNonHeapMemoryUsage());
    }
}
```

**Q5: What happens step-by-step when the JVM encounters `new MyClass()`?**

```text
A5: The JVM performs the following steps when it encounters 'new MyClass()':

1. Class Loading Check:
   - The JVM checks if MyClass has already been loaded (exists in Method Area).
   - If not, triggers the class loading process:
     a. Loading: Finds and reads the .class file
     b. Linking: Verification (bytecode validity), Preparation (default values for
        static fields), Resolution (symbolic to direct references)
     c. Initialization: Runs static initializers and assigns static field values

2. Memory Allocation:
   - Calculates the total memory needed for the object (header + instance fields).
   - Allocates memory on the heap using one of two strategies:
     a. Bump pointer: If heap is compacted, simply moves the pointer forward
     b. Free list: If heap is fragmented, finds a suitable free block
   - Thread safety is ensured via TLAB (Thread Local Allocation Buffer) or CAS.

3. Memory Initialization:
   - Sets all allocated memory to zero (default values).
   - This is why instance fields have default values (0, null, false).

4. Object Header Setup:
   - Sets the object header containing:
     a. Mark Word: Hash code, GC age, lock state
     b. Class Pointer: Reference to the class metadata in Method Area
     c. Array Length (if array): Size of the array

5. Constructor Invocation:
   - Calls the <init> method (constructor bytecode).
   - First invokes the parent constructor (super()).
   - Then executes instance initializer blocks.
   - Finally executes the constructor body.

6. Reference Assignment:
   - The reference to the newly created object is stored (on the stack or in a field).
```

```java
// Demonstrating object creation lifecycle
public class ObjectCreationLifecycle {
    static {
        System.out.println("1. Static initializer (class loading)");
    }

    {
        System.out.println("3. Instance initializer block");
    }

    private String name;

    public ObjectCreationLifecycle(String name) {
        // super() is called implicitly first
        System.out.println("4. Constructor body");
        this.name = name;
    }

    public static void main(String[] args) {
        System.out.println("2. Before new - class already loaded");
        ObjectCreationLifecycle obj = new ObjectCreationLifecycle("test");
        System.out.println("5. Object fully created: " + obj.name);
    }
}
// Output:
// 1. Static initializer (class loading)
// 2. Before new - class already loaded
// 3. Instance initializer block
// 4. Constructor body
// 5. Object fully created: test
```

## Code Examples

- Test: [JvmArchitectureTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/jvminternals/JvmArchitectureTest.java)
- Source: [JvmArchitecture.java](src/main/java/com/github/msorkhpar/claudejavatutor/jvminternals/JvmArchitecture.java)
