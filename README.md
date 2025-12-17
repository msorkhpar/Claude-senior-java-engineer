# Claude Senior Java Engineer Interview Preparation

A comprehensive Java tutorial repository designed to prepare students for senior Java engineer interviews. This project
covers Java fundamentals through advanced topics including concurrency, design patterns, and modern Java features.

## Overview

This repository contains a structured curriculum organized as Maven modules, with each module focusing on a specific
Java topic. Each module includes:

- **Detailed README documentation** with concept explanations, best practices, and common pitfalls
- **Comprehensive test suites** using JUnit 5, AssertJ, Mockito, and other modern testing tools
- **Working code examples** demonstrating Java 21 features and best practices
- **Interview Q&A sections** with common questions and detailed answers

**Target:** Java 21
**Build Tool:** Maven (multi-module project)
**Testing Stack:** JUnit 5, AssertJ, Mockito, DataFaker, WireMock, Testcontainers, Awaitility

> **For Claude Code users:** See [CLAUDE.md](CLAUDE.md) for detailed guidelines on contributing to this repository.

## Build and Test

```bash
# Build entire project
mvn clean install

# Run all tests
mvn test

# Build specific module
mvn clean install -pl 01-java-basics

# Run tests for specific module
mvn test -pl 05-pattern-matching
```

## Curriculum

1. Java Fundamentals

- [1.1. Variables, Data Types, Operators](01-java-basics/README.md)
    - [1.1.1. Primitive data types](01-java-basics/README_1.1.1.md)
    - [1.1.2. Reference data types](01-java-basics/README_1.1.2.md)
    - [1.1.3. Type casting and conversion](01-java-basics/README_1.1.3.md)
    - [1.1.4. Arithmetic, relational, and logical operators](01-java-basics/README_1.1.4.md)
- [1.2. Control Flow Statements (if/else, for, while, switch, switch expressions)](02-control-flow/README.md)
    - [1.2.1. if/else statements](02-control-flow/README_1.2.1.md)
    - [1.2.2. for loops](02-control-flow/README_1.2.2.md)
    - [1.2.3. while and do-while loops](02-control-flow/README_1.2.3.md)
    - [1.2.4. switch statements](02-control-flow/README_1.2.4.md)
    - [1.2.5. switch expressions (Java 14+)](02-control-flow/README_1.2.5.md)
- [1.3. Methods & Functions (focusing on passing by value/reference)](03-methods/README.md)
    - [1.3.1. Method declaration and invocation](03-methods/README_1.3.1.md)
    - [1.3.2. Method parameters and return types](03-methods/README_1.3.2.md)
    - [1.3.3. Pass by value and pass by reference](03-methods/README_1.3.3.md)
    - [1.3.4. Method overloading](03-methods/README_1.3.4.md)
- [1.4. String Literals and Manipulations (Strings are immutable)](04-str-literals/README.md)
    - [1.4.1. String creation and initialization](04-str-literals/README_1.4.1.md)
    - [1.4.2. String concatenation](04-str-literals/README_1.4.2.md)
    - [1.4.3. String methods (e.g., length(), charAt(), substring())](04-str-literals/README_1.4.3.md)
    - [1.4.4. String comparison (equals() vs. ==)](04-str-literals/README_1.4.4.md)
    - [1.4.5. StringBuilder and StringBuffer](04-str-literals/README_1.4.5.md)
- 1.5. [Pattern Matching (instanceof, record patterns, switch patterns)](05-pattern-matching/README.md)
    - [1.5.1. instanceof operator](05-pattern-matching/README_1.5.1.md)
    - [1.5.2. Pattern matching for instanceof (Java 14+)](05-pattern-matching/README_1.5.2.md)
    - [1.5.3. Record patterns (Java 19+)](05-pattern-matching/README_1.5.3.md)
    - [1.5.4. Switch patterns (Java 19+)](05-pattern-matching/README_1.5.4.md)

2. Java Classes & Objects

- 2.1. [Defining Classes & Objects](06-class-obj/README.md)
    - [2.1.1. Class structure and components](06-class-obj/README_2.1.1.md)
    - [2.1.2. Creating objects and using constructors](06-class-obj/README_2.1.1.md)
    - [2.1.3. Instance and static members](06-class-obj/README_2.1.1.md)
    - [2.1.4. Inheritance basics](06-class-obj/README_2.1.1.md)
- 2.2. [Encapsulation & Access Modifiers](07-encapsulation/README.md)
    - [2.2.1. Public, private, protected, and default access modifiers](07-encapsulation/README_2.2.1.md)
    - [2.2.2. Getter and setter methods](07-encapsulation/README_2.2.2.md)
    - [2.2.3. Data hiding and protection](07-encapsulation/README_2.2.3.md)
- [2.3. Object Oriented Concepts](08-object-oriented/README.md)
    - [2.3.1. Inheritance](08-object-oriented/README_2.3.1.md)
        - 2.3.1.1. Extending classes and code reusability
        - 2.3.1.2. Method overriding and super keyword
        - 2.3.1.3. Abstract classes and methods
    - [2.3.2. Polymorphism](08-object-oriented/README_2.3.2.md)
        - 2.3.2.1. Method overloading
        - 2.3.2.2. Dynamic method dispatch and virtual methods
        - 2.3.2.3. Interfaces and implementing classes
    - [2.3.3. Abstraction](08-object-oriented/README_2.3.3.md)
        - 2.3.3.1. Focusing on essential features and behavior
        - 2.3.3.2. Interfaces as contracts
        - 2.3.3.3. Abstract classes vs. interfaces
- 2.4. [Records](09-records/README.md)
    - [2.4.1. Defining and using records](09-records/README_2.4.1.md)
    - [2.4.2. Advantages of records over traditional classes](09-records/README_2.4.2.md)
    - [2.4.3. Limitations and use cases for records](09-records/README_2.4.3.md)
- 2.5. [Sealed Classes and Interfaces](10-sealed/README.md)
    - [2.5.1. Defining sealed classes and interfaces](10-sealed/README_2.5.1.md)
    - [2.5.2. Permitted and non-permitted subclasses](10-sealed/README_2.5.2.md)
    - [2.5.3. Use cases for sealed classes and interfaces](10-sealed/README_2.5.3.md)

3. Exception Handling

- [3.1. try/catch/finally blocks](11-try-catch/README.md)
    - [3.1.1. Handling exceptions with try/catch](11-try-catch/README_3.1.1.md)
    - [3.1.2. Using multiple catch blocks for different exception types](11-try-catch/README_3.1.2.md)
    - [3.1.3. The finally block for cleanup operations](11-try-catch/README_3.1.3.md)
    - [3.1.4. try-with-resources statement](11-try-catch/README_3.1.4.md)
- 3.2. [Throwing custom exceptions](12-custom-exception/README.md)
    - [3.2.1. Creating custom exception classes](12-custom-exception/README_3.2.1.md)
    - [3.2.2. Throwing exceptions using the throw keyword](12-custom-exception/README_3.2.2.md)
    - [3.2.3. Best practices for custom exceptions](12-custom-exception/README_3.2.3.md)
    - [3.2.4. Checked vs. unchecked exceptions](12-custom-exception/README_3.2.4.md)

4. Functional Programming in Java

- [4.1. Lambda Expressions](13-lambda-expressions/README.md)
    - [4.1.1. Introduction to lambda expressions](13-lambda-expressions/README_4.1.1.md)
    - [4.1.2. Syntax and structure of lambda expressions](13-lambda-expressions/README_4.1.2.md)
    - [4.1.3. Using lambda expressions with functional interfaces](13-lambda-expressions/README_4.1.3.md)
    - [4.1.4. Best practices and readability considerations](13-lambda-expressions/README_4.1.4.md)
- 4.2. Functional Interfaces
    - 4.2.1. Consumer
        - 4.2.1.1. Accepting and consuming values
        - 4.2.1.2. Using Consumer with lambda expressions
    - 4.2.2. Supplier
        - 4.2.2.1. Supplying values on-demand
        - 4.2.2.2. Using Supplier with lambda expressions
    - 4.2.3. Function
        - 4.2.3.1. Transforming values and returning results
        - 4.2.3.2. Using Function with lambda expressions
    - 4.2.4. Predicate
        - 4.2.4.1. Testing values and returning boolean results
        - 4.2.4.2. Using Predicate with lambda expressions
- 4.3. Method References
    - 4.3.1. Introduction to method references
    - 4.3.2. Types of method references (static, instance, constructor)
    - 4.3.3. Using method references with functional interfaces
- 4.4. Streams API
    - 4.4.1. Introduction to the Streams API
    - 4.4.2. Creating and using streams
    - 4.4.3. Intermediate operations (filter, map, flatMap)
    - 4.4.4. Terminal operations (forEach, reduce, collect)
    - 4.4.5. Parallel streams and performance considerations

5. Java Memory Model

- 5.1. Understanding the Java Memory Model
    - 5.1.1. Overview of the Java Memory Model
    - 5.1.2. Shared memory and thread-local memory
    - 5.1.3. Synchronization and memory consistency
    - 5.1.4. Reordering and optimization by the compiler and runtime
- 5.2. Happens-before relationships and memory visibility
    - 5.2.1. Definition of happens-before relationships
    - 5.2.2. Synchronization actions and their impact on memory visibility
        - 5.2.2.1. Synchronized methods and blocks
        - 5.2.2.2. Volatile variables
        - 5.2.2.3. Final fields
    - 5.2.3. Proper synchronization to ensure memory visibility
- 5.3. Avoiding common concurrency pitfalls related to memory
    - 5.3.1. Data races and how to prevent them
    - 5.3.2. Atomicity violations and ensuring atomic operations
    - 5.3.3. Visibility issues and proper synchronization techniques
    - 5.3.4. Best practices for safe concurrent programming

6. Multithreading and Concurrency

- 6.1. Thread Basics
    - 6.1.1. Creating threads (Runnable interface and Thread class)
    - 6.1.2. Starting threads (start() method)
    - 6.1.3. Stopping threads (interrupt() method and cooperative cancellation)
    - 6.1.4. Thread lifecycle and states
- 6.2. Synchronization (ensuring thread safety)
    - 6.2.1. Monitors and synchronized blocks
        - 6.2.1.1. Intrinsic locks and the synchronized keyword
        - 6.2.1.2. Avoiding deadlocks and starvation
    - 6.2.2. Volatile keyword
        - 6.2.2.1. Visibility and ordering guarantees
        - 6.2.2.2. Atomicity considerations
- 6.3. Locks and Semaphores (advanced synchronization)
    - 6.3.1. Reentrant locks
        - 6.3.1.1. Lock interface and ReentrantLock class
        - 6.3.1.2. Explicit locking and unlocking
        - 6.3.1.3. Condition objects for waiting and signaling
    - 6.3.2. Semaphores
        - 6.3.2.1. Semaphore class and its methods (acquire(), release())
        - 6.3.2.2. Controlling access to shared resources
        - 6.3.2.3. Bounded and unbounded semaphores
- 6.4. Executors and Thread Pools (managing threads efficiently)
    - 6.4.1. ExecutorService interface and implementations
        - 6.4.1.1. ThreadPoolExecutor class and its configuration
        - 6.4.1.2. Executing tasks with submit() and execute()
    - 6.4.2. Callable and Future for returning results
        - 6.4.2.1. Callable interface and its differences from Runnable
        - 6.4.2.2. Future interface for retrieving task results
        - 6.4.2.3. Handling exceptions and timeouts
- 6.5. Concurrent Collections (thread-safe data structures)
    - 6.5.1. Common Collections and their Implementations
        - 6.5.1.1. List (ArrayList, LinkedList, CopyOnWriteArrayList)
        - 6.5.1.2. Set (HashSet, ConcurrentSkipListSet)
        - 6.5.1.3. Map (HashMap, ConcurrentHashMap)
    - 6.5.2. Thread Safety of Common Data Structures
        - 6.5.2.1. Synchronization wrappers (Collections.synchronizedXXX())
        - 6.5.2.2. Concurrent collections (ConcurrentHashMap, CopyOnWriteArrayList)
    - 6.5.3. Order of Operations in Concurrent Collections
        - 6.5.3.1. Iteration and modification in non-concurrent collections
        - 6.5.3.2. Fail-fast behavior and ConcurrentModificationException
        - 6.5.3.3. Concurrent collections and their iteration guarantees
    - 6.5.4. Collection Capacity and Resizing
        - 6.5.4.1. Initial capacity and load factor in HashMap
        - 6.5.4.2. Resizing and rehashing in HashMap
        - 6.5.4.3. Capacity considerations in other collections
- 6.6. Fork/Join Framework (parallel processing)
    - 6.6.1. ForkJoinPool and RecursiveTask/RecursiveAction
        - 6.6.1.1. Parallelizing tasks with ForkJoinPool
        - 6.6.1.2. Implementing RecursiveTask for results-bearing tasks
        - 6.6.1.3. Implementing RecursiveAction for resultless tasks
        - 6.6.1.4. Splitting and joining tasks for optimal performance
- 6.7. Virtual Threads (Project Loom)
    - 6.7.1. Lightweight threads for high concurrency
        - 6.7.1.1. Overview of Project Loom and virtual threads
        - 6.7.1.2. Creating and managing virtual threads
    - 6.7.2. Differences between virtual threads and platform threads
        - 6.7.2.1. Scalability and resource consumption
        - 6.7.2.2. Blocking and synchronization behavior
        - 6.7.2.3. Compatibility with existing code and libraries

7. Modern Java Features

- 7.1. Overview of Changes in Java 8, 11, 15, 17, and 21
    - 7.1.1. Java 8 (LTS)
        - 7.1.1.1. Lambda Expressions and Functional Interfaces
        - 7.1.1.2. Stream API
        - 7.1.1.3. Default and Static Methods in Interfaces
        - 7.1.1.4. Optional Class
        - 7.1.1.5. New Date and Time API
    - 7.1.2. Java 11 (LTS)
        - 7.1.2.1. Local-Variable Syntax for Lambda Parameters
        - 7.1.2.2. HTTP Client API
        - 7.1.2.3. String API Enhancements
        - 7.1.2.4. Nested Based Access Control
        - 7.1.2.5. Running Java Files Directly
    - 7.1.3. Java 15
        - 7.1.3.1. Text Blocks (Standard)
        - 7.1.3.2. Pattern Matching for instanceof (Second Preview)
        - 7.1.3.3. Hidden Classes
    - 7.1.4. Java 17 (LTS)
        - 7.1.4.1. Enhanced Pseudo-Random Number Generators
        - 7.1.4.2. Deprecation of the Security Manager
    - 7.1.5. Java 21 (LTS)
        - 7.1.5.1. Pattern Matching for switch (Fourth Preview)
        - 7.1.5.2. Record Patterns (Preview)
        - 7.1.5.3. Virtual Threads (Second Preview)
        - 7.1.5.4. Structured Concurrency (Incubator)
        - 7.1.5.5. Foreign Function & Memory API (Second Preview)
- 7.2. Enhanced Enums (adding generics support)
    - 7.2.1. Limitations of Traditional Enums
    - 7.2.2. Declaring Generic Enums
    - 7.2.3. Parameterized Enum Constants
    - 7.2.4. Generic Methods in Enums
    - 7.2.5. Use Cases and Examples
- 7.3. New Date and Time API
    - 7.3.1. Limitations of the Legacy Date and Calendar Classes
    - 7.3.2. Overview of the java.time package
        - 7.3.2.1. LocalDate, LocalTime, and LocalDateTime
        - 7.3.2.2. Instant and Timestamps
        - 7.3.2.3. Duration and Period
        - 7.3.2.4. DateTimeFormatter
        - 7.3.2.5. Zoned and Offset Date and Time
    - 7.3.3. Interoperability with Legacy Date and Calendar Classes
- 7.4. Text Blocks (multi-line string literals)
    - 7.4.1. Limitations of Traditional String Literals
    - 7.4.2. Syntax and Usage of Text Blocks
    - 7.4.3. Formatting and Indentation
    - 7.4.4. Escape Sequences and Special Characters
    - 7.4.5. Use Cases and Benefits

8. Principles

- 8.1. SOLID Principles (focusing on relevance to concurrency)
    - 8.1.1. Single Responsibility Principle (SRP)
        - 8.1.1.1. Definition and purpose of SRP
        - 8.1.1.2. Applying SRP in concurrent programming
        - 8.1.1.3. Benefits of SRP for concurrency
    - 8.1.2. Open/Closed Principle (OCP)
        - 8.1.2.1. Definition and purpose of OCP
        - 8.1.2.2. Applying OCP in concurrent programming
        - 8.1.2.3. Extending behavior without modifying existing code
        - 8.1.2.4. OCP and thread safety
    - 8.1.3. Liskov Substitution Principle (LSP)
        - 8.1.3.1. Definition and purpose of LSP
        - 8.1.3.2. Applying LSP in concurrent programming
        - 8.1.3.3. Subtype behavioral consistency
        - 8.1.3.4. LSP and thread safety in inheritance hierarchies
    - 8.1.4. Interface Segregation Principle (ISP)
        - 8.1.4.1. Definition and purpose of ISP
        - 8.1.4.2. Applying ISP in concurrent programming
        - 8.1.4.3. Designing fine-grained interfaces for concurrency
        - 8.1.4.4. ISP and minimizing dependencies in concurrent systems
    - 8.1.5. Dependency Inversion Principle (DIP)
        - 8.1.5.1. Definition and purpose of DIP
        - 8.1.5.2. Applying DIP in concurrent programming
        - 8.1.5.3. Decoupling modules using abstractions
        - 8.1.5.4. DIP and testability of concurrent code
    - 8.2. KISS (Keep It Simple, Stupid) Principle
        - 8.2.1. Definition and purpose of KISS principle
        - 8.2.2. Applying KISS in concurrent programming
        - 8.2.2.1. Avoiding unnecessary complexity
        - 8.2.2.2. Favoring simplicity and readability
        - 8.2.3. Benefits of KISS for concurrency
        - 8.2.3.1. Easier maintenance and debugging
        - 8.2.3.2. Reduced risk of concurrency bugs
    - 8.3. DRY (Don't Repeat Yourself) Principle
        - 8.3.1. Definition and purpose of DRY principle
        - 8.3.2. Applying DRY in concurrent programming
        - 8.3.2.1. Extracting reusable concurrency patterns
        - 8.3.2.2. Avoiding duplication of synchronization logic
        - 8.3.3. Benefits of DRY for concurrency
        - 8.3.3.1. Improved code maintainability
        - 8.3.3.2. Consistency in concurrent behavior
        - 8.3.4. Balancing DRY with other principles (e.g., SRP)
    - 8.4. Composition over Inheritance Principle
        - 8.4.1. Definition and purpose of Composition over Inheritance
        - 8.4.2. Applying Composition over Inheritance in concurrent programming
        - 8.4.2.1. Favoring object composition for flexibility
        - 8.4.2.2. Avoiding deep inheritance hierarchies
        - 8.4.3. Benefits of Composition over Inheritance for concurrency
        - 8.4.3.1. Easier to reason about concurrent behavior
        - 8.4.3.2. Improved modularity and testability
    - 8.5. Fail-Fast vs. Fail-Safe Iterator Principles
        - 8.5.1. Definition and purpose of Fail-Fast and Fail-Safe iterators
        - 8.5.2. Applying Fail-Fast and Fail-Safe principles in concurrent collections
            - 8.5.2.1. Fail-Fast iterators and ConcurrentModificationException
            - 8.5.2.2. Fail-Safe iterators and snapshot semantics
        - 8.5.3. Choosing between Fail-Fast and Fail-Safe iterators
            - 8.5.3.1. Considerations for concurrent modification
            - 8.5.3.2. Trade-offs in performance and consistency

9. Design Patterns

- 9.1. Creational Patterns
    - 9.1.1. Singleton Pattern (ensuring a single instance)
        - 9.1.1.1. Definition and purpose of the Singleton pattern
        - 9.1.1.2. Implementing thread-safe Singleton in Java
        - 9.1.1.3. Lazy initialization and double-checked locking
        - 9.1.1.4. Enum-based Singleton implementation
    - 9.1.2. Factory Method Pattern (creating objects without specifying the exact class)
        - 9.1.2.1. Definition and purpose of the Factory Method pattern
        - 9.1.2.2. Implementing Factory Method in Java
        - 9.1.2.3. Subclasses overriding the factory method
        - 9.1.2.4. Abstracting object creation process
    - 9.1.3. Builder Pattern (step-by-step object construction)
        - 9.1.3.1. Definition and purpose of the Builder pattern
        - 9.1.3.2. Implementing Builder in Java
        - 9.1.3.3. Separating object construction from representation
        - 9.1.3.4. Handling complex object creation scenarios
- 9.2. Structural Patterns
    - 9.2.1. Adapter Pattern (making incompatible interfaces work together)
        - 9.2.1.1. Definition and purpose of the Adapter pattern
        - 9.2.1.2. Implementing Adapter in Java (class and object adapters)
        - 9.2.1.3. Adapting interfaces for compatibility
        - 9.2.1.4. Real-world examples and use cases
    - 9.2.2. Decorator Pattern (adding behavior dynamically)
        - 9.2.2.1. Definition and purpose of the Decorator pattern
        - 9.2.2.2. Implementing Decorator in Java
        - 9.2.2.3. Wrapping objects to extend functionality
        - 9.2.2.4. Decorator vs. subclassing for behavior extension
    - 9.2.3. Proxy Pattern (providing a controlled interface)
        - 9.2.3.1. Definition and purpose of the Proxy pattern
        - 9.2.3.2. Implementing Proxy in Java
        - 9.2.3.3. Types of proxies (remote, virtual, protection, smart)
        - 9.2.3.4. Controlling access to the real object
- 9.3. Behavioral Patterns
    - 9.3.1. Strategy Pattern (switching between algorithms at runtime)
        - 9.3.1.1. Definition and purpose of the Strategy pattern
        - 9.3.1.2. Implementing Strategy in Java
        - 9.3.1.3. Encapsulating algorithms as separate classes
        - 9.3.1.4. Allowing dynamic selection of algorithms
    - 9.3.2. Observer Pattern (loose coupling for event handling)
        - 9.3.2.1. Definition and purpose of the Observer pattern
        - 9.3.2.2. Implementing Observer in Java
        - 9.3.2.3. Subject (Observable) and Observer interfaces
        - 9.3.2.4. Loose coupling between objects for event notification
    - 9.3.3. Command Pattern (encapsulating a request as an object)
        - 9.3.3.1. Definition and purpose of the Command pattern
        - 9.3.3.2. Implementing Command in Java
        - 9.3.3.3. Encapsulating requests as command objects
        - 9.3.3.4. Decoupling sender and receiver of requests

10. Advanced Java Topics

- 10.1. Data Structures and Algorithms (High-level overview)
    - 10.1.1. Common Data Structures
        - 10.1.1.1. Lists (ArrayList, LinkedList)
        - 10.1.1.2. Sets (HashSet, TreeSet)
        - 10.1.1.3. Maps (HashMap, TreeMap)
        - 10.1.1.4. Trees (Binary Trees, BST, AVL, Red-Black)
        - 10.1.1.5. Graphs (Adjacency List, Adjacency Matrix)
    - 10.1.2. Big O Notation (analyzing algorithm complexity)
        - 10.1.2.1. Time Complexity (O(1), O(log n), O(n), O(n log n), O(n^2))
        - 10.1.2.2. Space Complexity
        - 10.1.2.3. Best, Average, and Worst Case Analysis
- 10.2. Java Memory Management and Garbage Collection
    - 10.2.1. Heap and Stack Memory Management
        - 10.2.1.1. Heap Memory (Object Allocation)
        - 10.2.1.2. Stack Memory (Method Calls and Local Variables)
        - 10.2.1.3. Memory Allocation and Deallocation
    - 10.2.2. Garbage Collection Algorithms
        - 10.2.2.1. Mark-and-Sweep Algorithm
        - 10.2.2.2. Generational Garbage Collection (Young, Old Generations)
        - 10.2.2.3. Garbage Collection Tuning and Optimization
- 10.3. Reflection (accessing class information at runtime)
    - 10.3.1. Class and Object Introspection
    - 10.3.2. Accessing Fields, Methods, and Constructors
    - 10.3.3. Dynamic Object Creation and Method Invocation
    - 10.3.4. Reflection Performance Considerations
- 10.4. Annotations (meta-data for code)
    - 10.4.1. Built-in Annotations (@Override, @Deprecated, @SuppressWarnings)
    - 10.4.2. Custom Annotation Creation
    - 10.4.3. Annotation Retention Policies (SOURCE, CLASS, RUNTIME)
    - 10.4.4. Annotation Processing and Code Generation
- 10.5. JVM Internals (high-level overview)
    - 10.5.1. JVM Architecture (Class Loader, Execution Engine, Runtime Data Areas)
    - 10.5.2. Bytecode Execution and Interpretation
    - 10.5.3. JIT (Just-In-Time) Compilation
    - 10.5.4. JVM Performance Tuning and Monitoring
- 10.6. Java Security
    - 10.6.1. Security Best Practices in Java
        - 10.6.1.1. Secure Coding Guidelines
        - 10.6.1.2. Input Validation and Sanitization
        - 10.6.1.3. Secure Configuration and Deployment
    - 10.6.2. Common Vulnerabilities
        - 10.6.2.1. Injection Flaws (SQL Injection, XSS)
        - 10.6.2.2. Authentication and Access Control Flaws
        - 10.6.2.3. Sensitive Data Exposure
    - 10.6.3. Cryptography and Encryption in Java
        - 10.6.3.1. Java Cryptography Architecture (JCA)
        - 10.6.3.2. Symmetric and Asymmetric Encryption
        - 10.6.3.3. Hashing and Digital Signatures
- 10.7. Java Persistence and Databases
    - 10.7.1. JDBC (Java Database Connectivity)
        - 10.7.1.1. Connecting to Databases
        - 10.7.1.2. Executing SQL Statements (Statement, PreparedStatement)
        - 10.7.1.3. ResultSet Processing and Mapping
    - 10.7.2. ORM (Object-Relational Mapping) Frameworks
        - 10.7.2.1. Hibernate and JPA (Java Persistence API)
        - 10.7.2.2. Mapping Objects to Database Tables
        - 10.7.2.3. Query Languages (HQL, JPQL)
    - 10.7.3. Transactions and Database Concurrency Control
        - 10.7.3.1. ACID Properties (Atomicity, Consistency, Isolation, Durability)
        - 10.7.3.2. Transaction Isolation Levels
        - 10.7.3.3. Optimistic and Pessimistic Locking

## License

This project and its released datasets are licensed under the CC BY 4.0 License. See the [LICENSE](LICENSE)
file for details.