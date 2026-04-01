# 10.7.2. ORM (Object-Relational Mapping) Frameworks

## Concept Explanation

Object-Relational Mapping (ORM) is a programming technique that bridges the gap between object-oriented programming languages and relational databases. In Java, the primary ORM standard is the **Java Persistence API (JPA)**, with **Hibernate** being the most popular implementation. An ORM framework automatically maps Java objects (entities) to database tables and translates method calls into SQL queries, freeing developers from writing repetitive JDBC boilerplate.

**Real-world analogy**: Imagine you are a diplomat who speaks English (Java objects) negotiating with officials who speak French (relational SQL). An ORM is your interpreter: you express your intent in your native language, and the interpreter translates your words into the other language, handles the nuances of grammar (SQL dialects), and relays the responses back to you in English. You never need to learn French, but the interpreter must understand both languages deeply.

### The Object-Relational Impedance Mismatch

The fundamental problem ORM solves is the "impedance mismatch" between objects and relational tables:

| Object World | Relational World |
|---|---|
| Inheritance hierarchies | Flat tables, no inheritance |
| Object references (graphs) | Foreign keys (flat relationships) |
| Identity via `==` / `equals()` | Identity via primary key |
| Encapsulation (private fields) | All columns public to SQL |
| Polymorphism | No polymorphism |
| Collections (List, Set) | Join tables / foreign keys |

### JPA Architecture

JPA defines a standard set of interfaces and annotations:

- **`@Entity`** - Marks a class as a persistent entity mapped to a table.
- **`EntityManager`** - The central API for CRUD operations and querying.
- **`EntityManagerFactory`** - Creates `EntityManager` instances (one per application).
- **`@Id`** / **`@GeneratedValue`** - Marks the primary key and its generation strategy.
- **`@Column`**, **`@Table`** - Fine-tune the mapping between fields and columns/tables.
- **`@OneToMany`**, **`@ManyToOne`**, **`@ManyToMany`** - Define relationships.
- **`@Version`** - Enables optimistic locking.

### JPQL and HQL

- **JPQL (Java Persistence Query Language)** is a portable, object-oriented query language defined by JPA. Queries reference entity classes and their fields, not table names and columns.
- **HQL (Hibernate Query Language)** is Hibernate's superset of JPQL, adding proprietary extensions.
- Both compile down to native SQL at runtime.

```
-- SQL (table-oriented)
SELECT e.name FROM employees e WHERE e.department = 'Engineering'

-- JPQL (entity-oriented)
SELECT e.name FROM Employee e WHERE e.department = 'Engineering'
```

## Key Points to Remember

- JPA is a **specification** (set of interfaces and annotations); Hibernate, EclipseLink, and OpenJPA are **implementations**.
- The `EntityManager` manages the **persistence context** - a first-level cache of managed entities.
- Entity lifecycle states: **New** (transient), **Managed** (attached), **Detached**, **Removed**.
- **Lazy loading** defers related-entity fetching until accessed; **eager loading** fetches immediately. Default: `@ManyToOne` is EAGER, `@OneToMany` is LAZY.
- The **N+1 problem** is the most common ORM performance issue: 1 query for the parent + N queries for each child.
- **`@Version`** annotation enables optimistic locking, causing an `OptimisticLockException` on stale writes.
- **Dirty checking** allows Hibernate to detect changes to managed entities and automatically generate UPDATE statements at flush time.
- **`persistence.xml`** (JPA) or `hibernate.cfg.xml` (Hibernate) is the configuration file for connection settings and entity registration.
- JPQL supports joins, aggregates, subqueries, and named parameters (`:paramName`).

## Relevant Java 21 Features

- **Records** cannot be JPA entities (entities need a no-arg constructor, mutable state, and non-final fields), but records are excellent as **DTO projections** in JPQL: `SELECT new com.example.EmployeeDTO(e.name, e.salary) FROM Employee e`.
- **Sealed classes** can model entity hierarchies with compile-time restrictions on which subclasses exist.
- **Pattern matching for switch** (Java 21) integrates well with entity type hierarchies for polymorphic processing.
- **Text blocks** make JPQL/HQL queries significantly more readable.
- **Virtual threads** (Java 21) improve scalability for ORM-heavy applications by making blocking database calls cheap.

```java
// Record as a DTO projection (not an entity)
public record EmployeeDTO(String name, double salary) {}

// JPQL with text block (conceptual)
String jpql = """
    SELECT new com.example.EmployeeDTO(e.name, e.salary)
    FROM Employee e
    WHERE e.department.name = :deptName
      AND e.active = true
    ORDER BY e.salary DESC
    """;
```

## Common Pitfalls and How to Avoid Them

### 1. The N+1 Select Problem

```java
// PROBLEM: fetching a list of Categories, then accessing products for each one
List<Category> categories = em.createQuery("SELECT c FROM Category c", Category.class).getResultList();
for (Category c : categories) {
    System.out.println(c.getProducts().size()); // triggers a separate SQL per category!
}
// Results in: 1 query for categories + N queries for products = N+1

// SOLUTION: use JOIN FETCH
List<Category> categories = em.createQuery(
    "SELECT c FROM Category c JOIN FETCH c.products", Category.class).getResultList();
// Results in: 1 query with a JOIN
```

### 2. Detached Entity Modifications Not Persisted

```java
// PROBLEM: modifying an entity outside a transaction
Employee emp = em.find(Employee.class, 1); // managed
em.getTransaction().commit();              // now detached
emp.setSalary(100000);                     // change is LOST - entity is detached

// SOLUTION: merge the detached entity back
em.getTransaction().begin();
Employee managed = em.merge(emp);          // returns a new managed instance
managed.setSalary(100000);                 // this change will be flushed
em.getTransaction().commit();
```

### 3. LazyInitializationException

```java
// PROBLEM: accessing a lazy collection after the session/EntityManager is closed
Employee emp = em.find(Employee.class, 1);
em.close();
emp.getProjects().size(); // LazyInitializationException!

// SOLUTION: fetch within an open EntityManager, use JOIN FETCH, or use a DTO projection
```

### 4. Using Entity Objects as API Responses

```java
// PROBLEM: serializing entities to JSON can trigger lazy loading, expose internal IDs,
// and create circular references with bidirectional relationships.

// SOLUTION: map entities to DTOs before returning from the service layer
public EmployeeDTO toDto(Employee entity) {
    return new EmployeeDTO(entity.getName(), entity.getSalary());
}
```

### 5. Ignoring the First-Level Cache

```java
// PROBLEM: querying the same entity multiple times in one transaction
Employee e1 = em.find(Employee.class, 1);
Employee e2 = em.find(Employee.class, 1);
// e1 == e2 is true! The EntityManager returns the same managed instance.
// This is a feature, not a bug - it guarantees identity consistency.
```

## Best Practices and Optimization Techniques

1. **Use DTO projections** for read-only queries to avoid the overhead of entity lifecycle management.
2. **Prefer `JOIN FETCH`** over lazy loading when you know the related data will be needed.
3. **Use `@BatchSize`** annotation on collections to mitigate N+1 by loading related entities in batches.
4. **Configure second-level cache** (EhCache, Caffeine) for frequently read, rarely modified entities.
5. **Use pagination** (`setFirstResult()`, `setMaxResults()`) for large result sets.
6. **Prefer named queries** (`@NamedQuery`) which are validated at deployment time.
7. **Avoid cascading deletes** unless the relationship truly requires it (CascadeType.REMOVE).
8. **Use `@Embeddable`** for value objects (Address, Money) instead of creating separate entities.
9. **Keep entities focused** - don't add business logic to entity classes; use service layer instead.
10. **Use `@NaturalId`** for business keys and `@Id` for surrogate keys.

## Edge Cases and Their Handling

1. **Bidirectional relationships**: Always maintain both sides of the relationship. Use a helper method:
   ```java
   public void addProduct(Product p) {
       products.add(p);
       p.setCategory(this);
   }
   ```
2. **Null associations**: A `@ManyToOne` field can be null unless annotated `@JoinColumn(nullable = false)`.
3. **Empty collections**: An uninitialized `@OneToMany` collection should default to an empty list, not null.
4. **Inheritance mapping strategies**: `SINGLE_TABLE` (one table, discriminator column), `JOINED` (normalized), `TABLE_PER_CLASS` (union). Each has trade-offs in query performance vs. storage.
5. **Composite keys**: Use `@IdClass` or `@EmbeddedId`. Composite keys must properly implement `equals()` and `hashCode()`.
6. **Large text/blob fields**: Mark as `@Lob` and consider `@Basic(fetch = FetchType.LAZY)` to avoid loading large data unnecessarily.

## Interview-specific Insights

Interviewers commonly focus on:

- **JPA vs. Hibernate** - the specification vs. implementation distinction
- **Entity lifecycle** - transient, managed, detached, removed states
- **Lazy vs. eager loading** - defaults, implications, N+1 problem
- **JPQL vs. native SQL** - when to use each, portability trade-offs
- **Caching levels** - first-level (persistence context) vs. second-level (shared cache)
- **Inheritance mapping strategies** - trade-offs between SINGLE_TABLE, JOINED, TABLE_PER_CLASS
- **Optimistic vs. pessimistic locking** in JPA context

Tricky areas:
- "What happens if you call `persist()` on a detached entity?" (throws `EntityExistsException` or `PersistenceException`)
- "What is the difference between `persist()` and `merge()`?" (`persist` attaches new, `merge` copies state)
- "How does dirty checking work in Hibernate?" (snapshot comparison at flush time)

## Interview Q&A Section

**Q1: What is JPA and how does it relate to Hibernate?**

```text
A1: JPA (Java Persistence API) is a specification (JSR 338) that defines a standard interface for
object-relational mapping in Java. It provides annotations (@Entity, @Table, @Column, etc.) and
interfaces (EntityManager, EntityManagerFactory, etc.) but contains NO implementation code.

Hibernate is the most popular implementation of JPA. Other implementations include EclipseLink
(reference implementation) and OpenJPA. The relationship is similar to JDBC (specification) vs.
MySQL Connector (implementation).

Key distinction:
- JPA code: import jakarta.persistence.EntityManager;
- Hibernate-specific code: import org.hibernate.Session;

Best practice: Program to the JPA interfaces to maintain portability. Only use Hibernate-specific
APIs when you need Hibernate-exclusive features (like @NaturalId, Criteria API extensions, etc.).

Historical evolution:
- Hibernate 1.0 (2001) - predates JPA
- JPA 1.0 (2006, JSR 220) - standardized ORM concepts from Hibernate
- JPA 2.0 (2009) - added Criteria API, metamodel
- JPA 2.1 (2013) - entity graphs, stored procedures
- JPA 2.2 (2017) - Stream results, date/time API support
- JPA 3.0 (2020) - moved to jakarta.persistence namespace
- JPA 3.1 (2022) - UUID generation, JPQL enhancements
```

```java
// JPA standard code (portable across implementations)
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // getters, setters, equals, hashCode
}

// Usage with EntityManager (JPA standard)
EntityManager em = emf.createEntityManager();
em.getTransaction().begin();
Employee emp = new Employee();
emp.setName("Alice");
em.persist(emp);  // INSERT
em.getTransaction().commit();
```

**Q2: Explain the entity lifecycle states in JPA.**

```text
A2: JPA defines four entity lifecycle states that track how an entity relates to the
persistence context (EntityManager):

1. NEW (Transient):
   - Created with 'new' but not yet associated with any EntityManager.
   - Has no database representation.
   - Garbage collected when no longer referenced.

2. MANAGED (Persistent):
   - Associated with an active persistence context (EntityManager).
   - Changes are automatically detected (dirty checking) and synchronized to DB on flush/commit.
   - Obtained via: persist(), find(), JPQL query, or merge() return value.

3. DETACHED:
   - Was previously managed but the EntityManager was closed or clear() was called.
   - Still has a database representation (has an ID).
   - Changes are NOT automatically persisted.
   - Must be re-attached via merge() to persist changes.

4. REMOVED:
   - Scheduled for deletion from the database.
   - Transition via em.remove(entity).
   - Actually deleted on flush/commit.

State transitions:
  new Entity() -> NEW
  em.persist(entity) -> MANAGED
  em.find(id) -> MANAGED
  em.close() -> DETACHED
  em.detach(entity) -> DETACHED
  em.merge(detached) -> returns MANAGED copy
  em.remove(managed) -> REMOVED
  em.persist(removed) -> MANAGED again
```

```java
// Demonstrating entity lifecycle states
EntityManager em = emf.createEntityManager();
em.getTransaction().begin();

// 1. NEW state
Employee emp = new Employee();
emp.setName("Alice");
// emp is transient - no database row exists

// 2. MANAGED state
em.persist(emp);
// emp is now managed - will be inserted on commit
emp.setName("Alice Smith"); // change is automatically detected (dirty checking)

// 3. Flush to database (explicit or automatic at commit)
em.flush(); // INSERT executed, then UPDATE for name change

// 4. DETACHED state
em.getTransaction().commit();
em.close();
// emp is now detached - changes won't be persisted

emp.setSalary(100000); // this change is LOST unless we merge

// 5. Re-attach via merge
EntityManager em2 = emf.createEntityManager();
em2.getTransaction().begin();
Employee managedEmp = em2.merge(emp); // returns a NEW managed instance
// managedEmp is managed, emp is still detached
managedEmp.setSalary(110000); // this will be persisted
em2.getTransaction().commit();

// 6. REMOVED state
em2.getTransaction().begin();
em2.remove(managedEmp); // scheduled for deletion
em2.getTransaction().commit(); // DELETE executed
```

**Q3: What is the N+1 select problem and how do you solve it?**

```text
A3: The N+1 select problem occurs when an ORM executes 1 query to load N parent entities,
then N additional queries to load a related collection for each parent. This results in
N+1 total queries where 1 or 2 would suffice.

Example scenario:
- You have 100 Categories, each with Products.
- Query: "SELECT c FROM Category c" -> 1 SQL query returns 100 categories.
- Accessing c.getProducts() for each category -> 100 additional SQL queries.
- Total: 101 queries instead of 1 or 2.

Solutions:

1. JOIN FETCH (JPQL):
   "SELECT c FROM Category c JOIN FETCH c.products"
   One query with a JOIN. Caution: can produce duplicate parent rows (use DISTINCT).

2. @BatchSize (Hibernate):
   @BatchSize(size = 25) on the collection field.
   Loads products in batches: 4 queries for 100 categories (100/25).

3. @EntityGraph (JPA 2.1+):
   Define which associations to eagerly fetch per query.
   @EntityGraph(attributePaths = {"products"})

4. Subselect fetching (Hibernate):
   @Fetch(FetchMode.SUBSELECT) loads all products in 1 query using a subselect.

5. DTO projection:
   Don't load entities at all - project directly into DTOs with a single JOIN query.

Best approach depends on the use case: JOIN FETCH for single parent, @BatchSize for
collections, DTO projection for read-only reporting.
```

```java
// Problem: N+1 queries
List<Category> categories = em.createQuery(
    "SELECT c FROM Category c", Category.class).getResultList();
// 1 SQL: SELECT * FROM categories

for (Category c : categories) {
    c.getProducts().size();  // Each access triggers a query
    // N SQL: SELECT * FROM products WHERE category_id = ?
}

// Solution 1: JOIN FETCH
List<Category> categories = em.createQuery(
    "SELECT DISTINCT c FROM Category c JOIN FETCH c.products",
    Category.class).getResultList();
// 1 SQL: SELECT ... FROM categories c JOIN products p ON c.id = p.category_id

// Solution 2: EntityGraph
@NamedEntityGraph(name = "Category.withProducts",
    attributeNodes = @NamedAttributeNode("products"))
@Entity
public class Category { ... }

EntityGraph<?> graph = em.getEntityGraph("Category.withProducts");
List<Category> categories = em.createQuery("SELECT c FROM Category c", Category.class)
    .setHint("jakarta.persistence.fetchgraph", graph)
    .getResultList();
```

**Q4: What are the JPA inheritance mapping strategies and their trade-offs?**

```text
A4: JPA provides three strategies for mapping class hierarchies to database tables:

1. SINGLE_TABLE (default):
   - All classes in the hierarchy share ONE table.
   - A discriminator column (DTYPE) identifies the entity type.
   - Pros: Best query performance (no JOINs), simple schema.
   - Cons: Many NULL columns (subclass fields are null for other types),
     no NOT NULL constraints on subclass columns.
   - Best for: Hierarchies with few subclass-specific columns.

2. JOINED:
   - Each class gets its own table; subclass tables have a FK to the parent table.
   - Queries require JOINs across the hierarchy.
   - Pros: Normalized, supports NOT NULL on subclass columns, no wasted space.
   - Cons: Slower queries due to JOINs, especially for deep hierarchies.
   - Best for: Hierarchies with many subclass-specific columns.

3. TABLE_PER_CLASS:
   - Each concrete class gets a complete table (all fields including inherited ones).
   - Polymorphic queries require UNION ALL across all tables.
   - Pros: No JOINs for single-type queries, no discriminator needed.
   - Cons: Polymorphic queries are expensive (UNION), data duplication, poor for
     polymorphic associations.
   - Best for: Rare polymorphic queries, mostly querying concrete types.

Rule of thumb: Start with SINGLE_TABLE. Switch to JOINED if you need NOT NULL
constraints or have many subclass-specific columns. Avoid TABLE_PER_CLASS unless
you rarely query polymorphically.
```

```java
// SINGLE_TABLE strategy
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_type")
public abstract class Vehicle {
    @Id @GeneratedValue private Long id;
    private String manufacturer;
}

@Entity
@DiscriminatorValue("CAR")
public class Car extends Vehicle {
    private int numberOfDoors; // NULL for non-Car rows
}

@Entity
@DiscriminatorValue("TRUCK")
public class Truck extends Vehicle {
    private double payloadCapacity; // NULL for non-Truck rows
}

// JOINED strategy
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Payment {
    @Id @GeneratedValue private Long id;
    private double amount;
}

@Entity
public class CreditCardPayment extends Payment {
    private String cardNumber;  // In its own table with FK to Payment
}

@Entity
public class BankTransferPayment extends Payment {
    private String bankAccount; // In its own table with FK to Payment
}
```

**Q5: What is the difference between persist(), merge(), and save() in JPA/Hibernate?**

```text
A5: These methods handle entity persistence differently:

persist() (JPA standard):
- Makes a NEW (transient) entity MANAGED.
- The entity must not have an assigned ID (for auto-generated IDs).
- If called on a detached entity: throws EntityExistsException.
- INSERT is executed at flush time.
- Returns void; the original object becomes managed.

merge() (JPA standard):
- Copies the state of a detached entity into a new MANAGED instance.
- If the entity is new (no ID): works like persist (creates a new row).
- If the entity is detached: loads the existing row, copies state, returns managed copy.
- Returns a MANAGED copy (the original detached object is NOT managed).
- Can cause an extra SELECT before the UPDATE.

save() (Hibernate-specific, deprecated in favor of persist):
- Similar to persist() but returns the generated ID.
- Works with both transient and detached entities.
- Now deprecated - use persist() instead.

Key gotcha with merge():
  Employee detached = ...;
  Employee managed = em.merge(detached);
  // detached is still detached! Use 'managed' for further operations.
  detached.setName("X"); // NOT persisted
  managed.setName("X");  // WILL be persisted
```

```java
// persist() - for new entities
EntityManager em = emf.createEntityManager();
em.getTransaction().begin();

Employee newEmp = new Employee();
newEmp.setName("Alice");
em.persist(newEmp);  // newEmp is now managed
// newEmp.getId() is now available (after flush or at commit)

em.getTransaction().commit();

// merge() - for detached entities
em.close(); // newEmp is now detached
newEmp.setName("Alice Updated");

EntityManager em2 = emf.createEntityManager();
em2.getTransaction().begin();

Employee managedCopy = em2.merge(newEmp);
// managedCopy is managed, newEmp is still detached
// IMPORTANT: use managedCopy, not newEmp!

managedCopy.setSalary(100000); // This change WILL be persisted
newEmp.setSalary(200000);      // This change is LOST

em2.getTransaction().commit();
```

**Q6: How does JPQL differ from SQL?**

```text
A6: JPQL (Java Persistence Query Language) is an object-oriented query language that operates
on entities and their fields, while SQL operates on tables and columns.

Key differences:
1. Query target: JPQL uses entity class names (Employee), SQL uses table names (employees).
2. Field access: JPQL uses Java field names (e.department.name), SQL uses column names.
3. Relationships: JPQL navigates object relationships (e.department), SQL requires explicit JOINs.
4. Polymorphism: JPQL automatically includes subclasses in queries.
5. No SELECT *: JPQL requires specifying what to select.
6. Database-agnostic: JPQL is portable across databases; JPA translates to the native dialect.

JPQL features:
- Named parameters: WHERE e.name = :name (not positional ? like JDBC)
- Constructor expressions: SELECT NEW dto.EmployeeDTO(e.name, e.salary)
- Aggregate functions: COUNT, SUM, AVG, MIN, MAX
- Subqueries, CASE expressions, IS EMPTY, MEMBER OF
- Bulk update/delete: UPDATE Employee e SET e.salary = e.salary * 1.1

Limitations: No DDL, no database-specific functions (use native queries for those).
```

```java
// JPQL examples (conceptual - using JPA EntityManager)

// 1. Simple select with named parameter
String jpql = "SELECT e FROM Employee e WHERE e.department.name = :dept";
List<Employee> employees = em.createQuery(jpql, Employee.class)
    .setParameter("dept", "Engineering")
    .getResultList();

// 2. Constructor expression (DTO projection)
String jpql = """
    SELECT NEW com.example.EmployeeDTO(e.name, e.salary, e.department.name)
    FROM Employee e
    WHERE e.salary > :minSalary
    ORDER BY e.salary DESC
    """;
List<EmployeeDTO> dtos = em.createQuery(jpql, EmployeeDTO.class)
    .setParameter("minSalary", 80000.0)
    .getResultList();

// 3. Aggregate with GROUP BY
String jpql = """
    SELECT e.department.name, AVG(e.salary), COUNT(e)
    FROM Employee e
    GROUP BY e.department.name
    HAVING AVG(e.salary) > 70000
    """;
List<Object[]> results = em.createQuery(jpql, Object[].class).getResultList();

// 4. JOIN FETCH to avoid N+1
String jpql = "SELECT DISTINCT d FROM Department d JOIN FETCH d.employees WHERE d.active = true";

// 5. Bulk update
int updated = em.createQuery(
    "UPDATE Employee e SET e.salary = e.salary * 1.1 WHERE e.department.name = :dept")
    .setParameter("dept", "Engineering")
    .executeUpdate();
```

## Code Examples

- Implementation: [OrmPatterns.java](src/main/java/com/github/msorkhpar/claudejavatutor/javapersistence/OrmPatterns.java)
- Tests: [OrmPatternsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javapersistence/OrmPatternsTest.java)
