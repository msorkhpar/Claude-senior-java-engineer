# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a comprehensive Java tutorial repository designed to prepare students for senior Java engineer interviews. The project uses a modular Maven structure with numbered modules covering progressive Java topics from fundamentals to advanced concepts. Each module follows a consistent pattern: README documentation with detailed explanations, Java implementation classes, and comprehensive JUnit 5 test suites.

## Build and Test Commands

### Build the entire project
```bash
mvn clean install
```

### Run all tests
```bash
mvn test
```

### Build a specific module
```bash
mvn clean install -pl 01-java-basics
```

### Run tests for a specific module
```bash
mvn test -pl 05-pattern-matching
```

### Run a single test class
```bash
mvn test -Dtest=PrimitiveTypesTest
```

### Run a single test method
```bash
mvn test -Dtest=PrimitiveTypesTest#testIntegerOverflow
```

### Skip tests during build
```bash
mvn clean install -DskipTests
```

## Project Structure

The repository is organized as a Maven multi-module project. Currently implemented modules cover foundational topics, with plans to expand to **100+ modules** covering the complete senior Java engineer curriculum.

**Current modules** (as examples):
- `00-base/` - Base utilities module (e.g., PerformanceTestUtil)
- `01-java-basics/` - Fundamentals: primitives, references, operators
- `02-control-flow/` - Control structures: if/else, loops, switch
- `03-methods/` - Method declarations, overloading, pass-by-value
- `04-str-literals/` - String handling and manipulation
- `05-pattern-matching/` - Pattern matching (instanceof, records, switch)
- `06-class-obj/` - Classes, objects, constructors
- `07-encapsulation/` - Access modifiers and encapsulation
- `08-object-oriented/` - OOP concepts (inheritance, polymorphism, abstraction)
- `09-records/` - Java records (Java 14+)
- `10-sealed/` - Sealed classes and interfaces (Java 17+)
- `11-try-catch/` - Exception handling with try/catch/finally
- `12-custom-exception/` - Custom exception creation and usage
- *(more modules planned)*

See the "Curriculum Topics" section below for the complete topic hierarchy.

### Module Structure Pattern

Each module follows this consistent structure:
```
XX-module-name/
├── pom.xml
├── README.md                           # Module overview with links to detailed docs
├── README_X.Y.Z.md                     # Detailed documentation for sub-topics
└── src/
    ├── main/java/com/github/msorkhpar/claudejavatutor/[modulename]/
    │   └── [Concept].java              # Implementation classes
    └── test/java/com/github/msorkhpar/claudejavatutor/[modulename]/
        └── [Concept]Test.java          # JUnit 5 test classes
```

## Package Naming Convention

All Java code uses the package structure: `com.github.msorkhpar.claudejavatutor.[modulename]`

Examples:
- `com.github.msorkhpar.claudejavatutor.javabasics.PrimitiveTypes`
- `com.github.msorkhpar.claudejavatutor.controlflow.SwitchExpression`
- `com.github.msorkhpar.claudejavatutor.patternmatching.RecordPattern`

## Testing Libraries and Tools

The project uses a comprehensive testing stack defined in the parent POM:

- **JUnit 5** (5.10.2): Core testing framework
- **AssertJ** (3.26.0): Fluent assertions for readable tests
- **Mockito** (5.12.0): Mocking framework for unit tests
- **DataFaker** (2.2.2): Generate realistic test data
- **WireMock** (3.6.0): Mock HTTP services for integration tests
- **Testcontainers** (1.19.8): Docker-based integration testing
- **Awaitility** (4.2.1): Testing asynchronous code

**Note:** Not all tools will be applicable to every topic. Use discretion in applying them based on the specific requirements of each module.

### Testing Best Practices

1. **Coverage:** Tests must cover happy paths, edge cases, exceptions, and boundary conditions
2. **Readability:** Use AssertJ for fluent, readable assertions
3. **No Mocking Privates:** Do not mock or test private methods
4. **Performance Tests:** Use `PerformanceTestUtil` from `00-base` module for performance measurements
5. **Test Naming:** Use descriptive test method names that explain what is being tested

Example test structure:
```java
@Test
void testIntegerOverflow() {
    // Given
    int max = Integer.MAX_VALUE;

    // When
    int overflow = max + 1;

    // Then
    assertThat(overflow).isEqualTo(Integer.MIN_VALUE);
}
```

## Content Creation Guidelines

When creating or updating content in this repository, follow these guidelines to maintain consistency and quality:

### README Documentation Requirements

Each README file must include:
- **Detailed concept explanation** with real-world context
- **Key points to remember** for quick reference
- **Relevant Java 21 features** or modern practices
- **Common pitfalls** and how to avoid them
- **Best practices** and optimization techniques
- **Edge cases** and their handling
- **Interview-specific insights** (common questions, tricky aspects)
- **References to source code and test files**

### Test Suite Requirements

When creating tests:
- Use **JUnit 5** for all unit testing
- Cover **expected behaviors, edge cases, and error conditions**
- Include **performance considerations** where relevant
- Tests must be **readable and well-structured** using AssertJ assertions
- **Do NOT mock or test private methods**
- Ensure comprehensive coverage to help learning

### Implementation Code Requirements

When writing implementation code:
- Provide **minimal code to demonstrate the concept** clearly
- Refactor for **clarity, efficiency, and best practices**
- Use **modern Java syntax and features** (Java 21)
- Include proper **error handling and logging** where applicable
- Demonstrate **clean, readable, and well-documented code**

### Coding Examples Requirements

When providing code examples:
- **Illustrate real-world usage** scenarios and practical applications
- **Demonstrate modern Java syntax and features** (Java 21)
- **Show common pitfalls and their solutions** with explanations
- **Highlight performance implications** where relevant to the topic

### In-depth Explanations

Provide:
- The concept's **purpose and importance** in Java development
- **Internal workings** (if applicable)
- **Evolution across Java versions** (if relevant)
- **Trade-offs and design decisions**

### Interview Q&A Section

Each topic must include:
- **Common interview questions** related to the topic
- **Detailed answers with explanations**
- **Code examples** where applicable (include a Java code block after each question requiring code)
- **Text-based answers** for conceptual questions (include a text block after each Q&A question)
- **Best practices and pitfalls**
- **Real-world applications**
- **Tips on explaining complex topics concisely** for interview scenarios
- **Whiteboard coding approaches** for applicable topics

### Development Approach

- Use **Test-Driven Development (TDD)** when implementing code
- For conceptual topics (e.g., system design), focus on understanding and best practices
- Demonstrate effective use of testing libraries (AssertJ, Mockito, DataFaker, etc.)
- Cover only **one subject per module/file**
- **DO NOT FORGET**: Cover details comprehensively with examples. Each method should be covered by enough tests covering happy paths, edge cases, exceptions, and anything else that increases code coverage and aids learning

### Teaching Approach Guidelines

When creating content for this repository:
- **Provide clear explanations with real-world analogies** where possible to make concepts accessible
- **Compare and contrast related concepts** to deepen understanding
- **Emphasize modern Java approaches** and best practices (Java 21)
- **Discuss scalability and performance implications** when relevant to the topic
- **Be prepared to provide advanced examples or edge cases** to challenge understanding
- **Demonstrate clean, readable, and well-documented code** for all implementations
- **Show proper error handling and logging practices** where applicable
- **Illustrate effective use of Java 21 features** when relevant
- **Discuss code organization and design patterns** as appropriate to the topic

### Concurrency-Specific Guidelines

When working on concurrency-related topics:
- **Discuss Java's built-in concurrency tools** (synchronized, volatile, locks, etc.)
- **Use Awaitility for asynchronous testing** when relevant
- **Mention JCStress** for stress-testing concurrent data structures if appropriate
- Cover thread safety, race conditions, and synchronization issues
- Provide examples of common concurrency pitfalls and solutions

## Documentation Structure

The README files follow a structured interview-preparation format:

1. **Concept Explanation:** Detailed explanation of the topic
2. **Key Points:** Bullet-point summary of important concepts
3. **Modern Java Features:** Java 21 features and best practices
4. **Common Pitfalls:** Mistakes to avoid
5. **Edge Cases:** Boundary conditions and special scenarios
6. **Best Practices:** Industry-standard approaches
7. **Interview Q&A:** Common interview questions with detailed answers and code examples

## Development Guidelines

### When Adding New Content

1. Follow the TDD (Test-Driven Development) approach
2. Create comprehensive tests BEFORE or alongside implementation code
3. Follow existing module structure patterns
4. Use Java 21 features where appropriate
5. Add detailed documentation in README files with practical examples
6. Include interview Q&A sections with code examples

### Code Quality Standards

- Use Java 21 syntax and modern language features
- Follow clean code principles (readable, maintainable)
- Proper exception handling and logging practices
- Demonstrate best practices in all code examples
- Keep examples focused and minimal (avoid over-engineering)

### Key Java 21 Features to Utilize

- Pattern matching for `instanceof` (JEP 394)
- Pattern matching for `switch` (JEP 441)
- Record patterns (JEP 440)
- Sealed classes (JEP 409)
- Text blocks (JEP 378)
- Records (JEP 395)
- Virtual threads (Project Loom - JEP 444)

## Module Dependencies

The `00-base` module contains shared utilities:
- `PerformanceTestUtil`: For measuring execution time in performance tests
- `MeasurementResult<T>`: Record type for capturing execution time and results

Other modules depend on `00-base` when they need performance testing utilities.

## Interview Preparation Context

This repository is specifically designed for **senior Java engineer interview preparation**. When working on this codebase:

1. Code examples should demonstrate production-ready quality
2. Documentation should address common interview questions
3. Tests should showcase understanding of edge cases and error handling
4. Performance considerations should be highlighted where relevant
5. Modern Java features should be demonstrated and explained
6. Design patterns and SOLID principles should be evident in implementations
7. Include tips on explaining complex topics concisely for interview scenarios
8. Discuss whiteboard coding approaches for applicable topics
9. Mention industry best practices related to each specific topic
10. Highlight common interview questions in Q&A format with detailed answers

## Curriculum Topics

This section outlines the complete curriculum hierarchy that the repository aims to cover. Use this as a reference when adding new modules or understanding the overall structure.

### 1. Java Fundamentals

#### 1.1. Variables, Data Types, Operators
- 1.1.1. Primitive data types
- 1.1.2. Reference data types
- 1.1.3. Type casting and conversion
- 1.1.4. Arithmetic, relational, and logical operators

#### 1.2. Control Flow Statements
- 1.2.1. if/else statements
- 1.2.2. for loops
- 1.2.3. while and do-while loops
- 1.2.4. switch statements
- 1.2.5. switch expressions (Java 14+)

#### 1.3. Methods & Functions
- 1.3.1. Method declaration and invocation
- 1.3.2. Method parameters and return types
- 1.3.3. Pass by value and pass by reference
- 1.3.4. Method overloading

#### 1.4. String Literals and Manipulations
- 1.4.1. String creation and initialization
- 1.4.2. String concatenation
- 1.4.3. String methods (e.g., length(), charAt(), substring())
- 1.4.4. String comparison (equals() vs. ==)
- 1.4.5. StringBuilder and StringBuffer

#### 1.5. Pattern Matching
- 1.5.1. instanceof operator
- 1.5.2. Pattern matching for instanceof (Java 14+)
- 1.5.3. Record patterns (Java 19+)
- 1.5.4. Switch patterns (Java 19+)

### 2. Java Classes & Objects

#### 2.1. Defining Classes & Objects
- 2.1.1. Class structure and components
- 2.1.2. Creating objects and using constructors
- 2.1.3. Instance and static members
- 2.1.4. Inheritance basics

#### 2.2. Encapsulation & Access Modifiers
- 2.2.1. Public, private, protected, and default access modifiers
- 2.2.2. Getter and setter methods
- 2.2.3. Data hiding and protection

#### 2.3. Object Oriented Concepts
- 2.3.1. Inheritance
  - 2.3.1.1. Extending classes and code reusability
  - 2.3.1.2. Method overriding and super keyword
  - 2.3.1.3. Abstract classes and methods
- 2.3.2. Polymorphism
  - 2.3.2.1. Method overloading
  - 2.3.2.2. Dynamic method dispatch and virtual methods
  - 2.3.2.3. Interfaces and implementing classes
- 2.3.3. Abstraction
  - 2.3.3.1. Focusing on essential features and behavior
  - 2.3.3.2. Interfaces as contracts
  - 2.3.3.3. Abstract classes vs. interfaces

#### 2.4. Records
- 2.4.1. Defining and using records
- 2.4.2. Advantages of records over traditional classes
- 2.4.3. Limitations and use cases for records

#### 2.5. Sealed Classes and Interfaces
- 2.5.1. Defining sealed classes and interfaces
- 2.5.2. Permitted and non-permitted subclasses
- 2.5.3. Use cases for sealed classes and interfaces

### 3. Exception Handling

#### 3.1. try/catch/finally blocks
- 3.1.1. Handling exceptions with try/catch
- 3.1.2. Using multiple catch blocks for different exception types
- 3.1.3. The finally block for cleanup operations
- 3.1.4. try-with-resources statement

#### 3.2. Throwing custom exceptions
- 3.2.1. Creating custom exception classes
- 3.2.2. Throwing exceptions using the throw keyword
- 3.2.3. Best practices for custom exceptions
- 3.2.4. Checked vs. unchecked exceptions

### 4. Functional Programming in Java

#### 4.1. Lambda Expressions
- 4.1.1. Introduction to lambda expressions
- 4.1.2. Syntax and structure of lambda expressions
- 4.1.3. Using lambda expressions with functional interfaces
- 4.1.4. Best practices and readability considerations

#### 4.2. Functional Interfaces
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

#### 4.3. Method References
- 4.3.1. Introduction to method references
- 4.3.2. Types of method references (static, instance, constructor)
- 4.3.3. Using method references with functional interfaces

#### 4.4. Streams API
- 4.4.1. Introduction to the Streams API
- 4.4.2. Creating and using streams
- 4.4.3. Intermediate operations (filter, map, flatMap)
- 4.4.4. Terminal operations (forEach, reduce, collect)
- 4.4.5. Parallel streams and performance considerations

### 5. Java Memory Model

#### 5.1. Understanding the Java Memory Model
- 5.1.1. Overview of the Java Memory Model
- 5.1.2. Shared memory and thread-local memory
- 5.1.3. Synchronization and memory consistency
- 5.1.4. Reordering and optimization by the compiler and runtime

#### 5.2. Happens-before relationships and memory visibility
- 5.2.1. Definition of happens-before relationships
- 5.2.2. Synchronization actions and their impact on memory visibility
  - 5.2.2.1. Synchronized methods and blocks
  - 5.2.2.2. Volatile variables
  - 5.2.2.3. Final fields
- 5.2.3. Proper synchronization to ensure memory visibility

#### 5.3. Avoiding common concurrency pitfalls related to memory
- 5.3.1. Data races and how to prevent them
- 5.3.2. Atomicity violations and ensuring atomic operations
- 5.3.3. Visibility issues and proper synchronization techniques
- 5.3.4. Best practices for safe concurrent programming

### 6. Multithreading and Concurrency

#### 6.1. Thread Basics
- 6.1.1. Creating threads (Runnable interface and Thread class)
- 6.1.2. Starting threads (start() method)
- 6.1.3. Stopping threads (interrupt() method and cooperative cancellation)
- 6.1.4. Thread lifecycle and states

#### 6.2. Synchronization
- 6.2.1. Monitors and synchronized blocks
  - 6.2.1.1. Intrinsic locks and the synchronized keyword
  - 6.2.1.2. Avoiding deadlocks and starvation
- 6.2.2. Volatile keyword
  - 6.2.2.1. Visibility and ordering guarantees
  - 6.2.2.2. Atomicity considerations

#### 6.3. Locks and Semaphores
- 6.3.1. Reentrant locks
  - 6.3.1.1. Lock interface and ReentrantLock class
  - 6.3.1.2. Explicit locking and unlocking
  - 6.3.1.3. Condition objects for waiting and signaling
- 6.3.2. Semaphores
  - 6.3.2.1. Semaphore class and its methods (acquire(), release())
  - 6.3.2.2. Controlling access to shared resources
  - 6.3.2.3. Bounded and unbounded semaphores

#### 6.4. Executors and Thread Pools
- 6.4.1. ExecutorService interface and implementations
  - 6.4.1.1. ThreadPoolExecutor class and its configuration
  - 6.4.1.2. Executing tasks with submit() and execute()
- 6.4.2. Callable and Future for returning results
  - 6.4.2.1. Callable interface and its differences from Runnable
  - 6.4.2.2. Future interface for retrieving task results
  - 6.4.2.3. Handling exceptions and timeouts

#### 6.5. Concurrent Collections
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

#### 6.6. Fork/Join Framework
- 6.6.1. ForkJoinPool and RecursiveTask/RecursiveAction
  - 6.6.1.1. Parallelizing tasks with ForkJoinPool
  - 6.6.1.2. Implementing RecursiveTask for results-bearing tasks
  - 6.6.1.3. Implementing RecursiveAction for resultless tasks
  - 6.6.1.4. Splitting and joining tasks for optimal performance

#### 6.7. Virtual Threads (Project Loom)
- 6.7.1. Lightweight threads for high concurrency
  - 6.7.1.1. Overview of Project Loom and virtual threads
  - 6.7.1.2. Creating and managing virtual threads
- 6.7.2. Differences between virtual threads and platform threads
  - 6.7.2.1. Scalability and resource consumption
  - 6.7.2.2. Blocking and synchronization behavior
  - 6.7.2.3. Compatibility with existing code and libraries

### 7. Modern Java Features

#### 7.1. Overview of Changes in Java 8, 11, 15, 17, and 21
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

#### 7.2. Enhanced Enums
- 7.2.1. Limitations of Traditional Enums
- 7.2.2. Declaring Generic Enums
- 7.2.3. Parameterized Enum Constants
- 7.2.4. Generic Methods in Enums
- 7.2.5. Use Cases and Examples

#### 7.3. New Date and Time API
- 7.3.1. Limitations of the Legacy Date and Calendar Classes
- 7.3.2. Overview of the java.time package
  - 7.3.2.1. LocalDate, LocalTime, and LocalDateTime
  - 7.3.2.2. Instant and Timestamps
  - 7.3.2.3. Duration and Period
  - 7.3.2.4. DateTimeFormatter
  - 7.3.2.5. Zoned and Offset Date and Time
- 7.3.3. Interoperability with Legacy Date and Calendar Classes

#### 7.4. Text Blocks
- 7.4.1. Limitations of Traditional String Literals
- 7.4.2. Syntax and Usage of Text Blocks
- 7.4.3. Formatting and Indentation
- 7.4.4. Escape Sequences and Special Characters
- 7.4.5. Use Cases and Benefits

### 8. Principles

#### 8.1. SOLID Principles
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

#### 8.2. KISS (Keep It Simple, Stupid) Principle
- 8.2.1. Definition and purpose of KISS principle
- 8.2.2. Applying KISS in concurrent programming
  - 8.2.2.1. Avoiding unnecessary complexity
  - 8.2.2.2. Favoring simplicity and readability
- 8.2.3. Benefits of KISS for concurrency
  - 8.2.3.1. Easier maintenance and debugging
  - 8.2.3.2. Reduced risk of concurrency bugs

#### 8.3. DRY (Don't Repeat Yourself) Principle
- 8.3.1. Definition and purpose of DRY principle
- 8.3.2. Applying DRY in concurrent programming
  - 8.3.2.1. Extracting reusable concurrency patterns
  - 8.3.2.2. Avoiding duplication of synchronization logic
- 8.3.3. Benefits of DRY for concurrency
  - 8.3.3.1. Improved code maintainability
  - 8.3.3.2. Consistency in concurrent behavior
- 8.3.4. Balancing DRY with other principles (e.g., SRP)

#### 8.4. Composition over Inheritance Principle
- 8.4.1. Definition and purpose of Composition over Inheritance
- 8.4.2. Applying Composition over Inheritance in concurrent programming
  - 8.4.2.1. Favoring object composition for flexibility
  - 8.4.2.2. Avoiding deep inheritance hierarchies
- 8.4.3. Benefits of Composition over Inheritance for concurrency
  - 8.4.3.1. Easier to reason about concurrent behavior
  - 8.4.3.2. Improved modularity and testability

#### 8.5. Fail-Fast vs. Fail-Safe Iterator Principles
- 8.5.1. Definition and purpose of Fail-Fast and Fail-Safe iterators
- 8.5.2. Applying Fail-Fast and Fail-Safe principles in concurrent collections
  - 8.5.2.1. Fail-Fast iterators and ConcurrentModificationException
  - 8.5.2.2. Fail-Safe iterators and snapshot semantics
- 8.5.3. Choosing between Fail-Fast and Fail-Safe iterators
  - 8.5.3.1. Considerations for concurrent modification
  - 8.5.3.2. Trade-offs in performance and consistency

### 9. Design Patterns

#### 9.1. Creational Patterns
- 9.1.1. Singleton Pattern
  - 9.1.1.1. Definition and purpose of the Singleton pattern
  - 9.1.1.2. Implementing thread-safe Singleton in Java
  - 9.1.1.3. Lazy initialization and double-checked locking
  - 9.1.1.4. Enum-based Singleton implementation
- 9.1.2. Factory Method Pattern
  - 9.1.2.1. Definition and purpose of the Factory Method pattern
  - 9.1.2.2. Implementing Factory Method in Java
  - 9.1.2.3. Subclasses overriding the factory method
  - 9.1.2.4. Abstracting object creation process
- 9.1.3. Builder Pattern
  - 9.1.3.1. Definition and purpose of the Builder pattern
  - 9.1.3.2. Implementing Builder in Java
  - 9.1.3.3. Separating object construction from representation
  - 9.1.3.4. Handling complex object creation scenarios

#### 9.2. Structural Patterns
- 9.2.1. Adapter Pattern
  - 9.2.1.1. Definition and purpose of the Adapter pattern
  - 9.2.1.2. Implementing Adapter in Java (class and object adapters)
  - 9.2.1.3. Adapting interfaces for compatibility
  - 9.2.1.4. Real-world examples and use cases
- 9.2.2. Decorator Pattern
  - 9.2.2.1. Definition and purpose of the Decorator pattern
  - 9.2.2.2. Implementing Decorator in Java
  - 9.2.2.3. Wrapping objects to extend functionality
  - 9.2.2.4. Decorator vs. subclassing for behavior extension
- 9.2.3. Proxy Pattern
  - 9.2.3.1. Definition and purpose of the Proxy pattern
  - 9.2.3.2. Implementing Proxy in Java
  - 9.2.3.3. Types of proxies (remote, virtual, protection, smart)
  - 9.2.3.4. Controlling access to the real object

#### 9.3. Behavioral Patterns
- 9.3.1. Strategy Pattern
  - 9.3.1.1. Definition and purpose of the Strategy pattern
  - 9.3.1.2. Implementing Strategy in Java
  - 9.3.1.3. Encapsulating algorithms as separate classes
  - 9.3.1.4. Allowing dynamic selection of algorithms
- 9.3.2. Observer Pattern
  - 9.3.2.1. Definition and purpose of the Observer pattern
  - 9.3.2.2. Implementing Observer in Java
  - 9.3.2.3. Subject (Observable) and Observer interfaces
  - 9.3.2.4. Loose coupling between objects for event notification
- 9.3.3. Command Pattern
  - 9.3.3.1. Definition and purpose of the Command pattern
  - 9.3.3.2. Implementing Command in Java
  - 9.3.3.3. Encapsulating requests as command objects
  - 9.3.3.4. Decoupling sender and receiver of requests

### 10. Advanced Java Topics

#### 10.1. Data Structures and Algorithms
- 10.1.1. Common Data Structures
  - 10.1.1.1. Lists (ArrayList, LinkedList)
  - 10.1.1.2. Sets (HashSet, TreeSet)
  - 10.1.1.3. Maps (HashMap, TreeMap)
  - 10.1.1.4. Trees (Binary Trees, BST, AVL, Red-Black)
  - 10.1.1.5. Graphs (Adjacency List, Adjacency Matrix)
- 10.1.2. Big O Notation
  - 10.1.2.1. Time Complexity (O(1), O(log n), O(n), O(n log n), O(n^2))
  - 10.1.2.2. Space Complexity
  - 10.1.2.3. Best, Average, and Worst Case Analysis

#### 10.2. Java Memory Management and Garbage Collection
- 10.2.1. Heap and Stack Memory Management
  - 10.2.1.1. Heap Memory (Object Allocation)
  - 10.2.1.2. Stack Memory (Method Calls and Local Variables)
  - 10.2.1.3. Memory Allocation and Deallocation
- 10.2.2. Garbage Collection Algorithms
  - 10.2.2.1. Mark-and-Sweep Algorithm
  - 10.2.2.2. Generational Garbage Collection (Young, Old Generations)
  - 10.2.2.3. Garbage Collection Tuning and Optimization

#### 10.3. Reflection
- 10.3.1. Class and Object Introspection
- 10.3.2. Accessing Fields, Methods, and Constructors
- 10.3.3. Dynamic Object Creation and Method Invocation
- 10.3.4. Reflection Performance Considerations

#### 10.4. Annotations
- 10.4.1. Built-in Annotations (@Override, @Deprecated, @SuppressWarnings)
- 10.4.2. Custom Annotation Creation
- 10.4.3. Annotation Retention Policies (SOURCE, CLASS, RUNTIME)
- 10.4.4. Annotation Processing and Code Generation

#### 10.5. JVM Internals
- 10.5.1. JVM Architecture (Class Loader, Execution Engine, Runtime Data Areas)
- 10.5.2. Bytecode Execution and Interpretation
- 10.5.3. JIT (Just-In-Time) Compilation
- 10.5.4. JVM Performance Tuning and Monitoring

#### 10.6. Java Security
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

#### 10.7. Java Persistence and Databases
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


## Output Format for New Content

When creating new content for this repository, follow this format:

1. **README.md content** with Q&A questions and their answers
2. **Maven-based module structure** following the existing pattern
3. **Package naming**: `com.github.msorkhpar.claudejavatutor.[modulename]`
4. **Test file** (if applicable) with comprehensive coverage
5. **Java implementation** (if applicable) with clear examples
6. **Cover only one subject per module** - maintain focused, modular content

### Important Content Requirements

- **DO NOT FORGET**: Cover details as much as needed by providing examples for whatever is mentioned
- When README is fully covered with Q&A and examples, start writing implementation code
- **Each method** should be covered by enough unit or integration tests
- Tests must cover: **happy paths, edge cases, exceptions**, and anything else that helps learning and increases code coverage
- **Use the testing libraries**: AssertJ, Mockito, DataFaker, WireMock, Testcontainers, Awaitility
- **Tests are very important** and should be readable, cover different cases, and even test expected exceptions
- **DO NOT mock or test private methods**

## Common Tasks

### Adding a New Module

1. Create module directory with number prefix (e.g., `13-new-topic/`)
2. Add `pom.xml` with parent reference and proper artifactId
3. Update parent `pom.xml` to include new module
4. Create standard directory structure (`src/main/java`, `src/test/java`)
5. Follow package naming convention: `com.github.msorkhpar.claudejavatutor.[modulename]`
6. Create README.md with detailed concept explanation, Q&A section, and examples
7. Create detailed README_X.Y.Z.md files for sub-topics
8. Implement Java classes with minimal code to demonstrate concepts
9. Create comprehensive test suites covering all scenarios
10. Ensure all content follows the teaching approach and interview preparation guidelines

### Updating Dependency Versions

All dependency versions are managed in the parent `pom.xml` properties section. Update versions there rather than in individual modules.

### Running Specific Test Suites

Use Maven's `-Dtest` flag with wildcards:
```bash
mvn test -Dtest="*Pattern*Test"  # Run all pattern-related tests
mvn test -pl 05-pattern-matching -Dtest="RecordPatternTest#*nested*"  # Run nested tests only
```
