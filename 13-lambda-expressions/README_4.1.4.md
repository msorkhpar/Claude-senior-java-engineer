# 4.1.4. Best Practices and Readability Considerations

## Concept Explanation

While lambda expressions make Java code more concise and expressive, they can also make code harder to understand if
used improperly. Writing readable, maintainable lambda expressions requires following established best practices and
considering the readability impact of your functional code.

The goal is to leverage lambda expressions to make code more declarative and intention-revealing, not just shorter. Good
lambda code reads like a description of what should happen, not a puzzle to be decoded.

**Real-world analogy**: Think of lambda expressions like spices in cooking. Used appropriately, they enhance the dish (
code). Used excessively or incorrectly, they overpower and ruin it. The best chefs know when to use bold flavors and
when simplicity is better.

## Key Points to Remember

1. Lambda expressions should make code more readable, not less.
2. Complex lambdas should be extracted to named methods.
3. Method references are often more readable than equivalent lambdas.
4. Descriptive variable names matter, even in lambdas.
5. Keep lambdas short - ideally one or two lines.
6. Avoid side effects in lambdas when possible.
7. Consider the audience - what's readable to you might not be to others.
8. Use lambda expressions to reveal intent, not hide implementation.

## Relevant Java 21 Features

Java 21 continues to improve lambda expression readability:

- **Better type inference**: Reduces the need for verbose type declarations.
- **Enhanced compiler errors**: More helpful messages when something goes wrong.
- **Pattern matching**: Can make lambda bodies more readable in some cases.
- **Improved tooling**: IDEs better support lambda refactoring and navigation.

## Common Pitfalls and How to Avoid Them

1. **Overly complex lambdas**:
   ```java
   // Bad: Too complex
   list.stream()
       .filter(item -> {
           if (item == null) return false;
           if (item.getValue() <= 0) return false;
           if (!item.isActive()) return false;
           if (item.getStatus() == Status.DELETED) return false;
           return item.getOwner() != null && item.getOwner().isVerified();
       })
       .forEach(this::process);

   // Good: Extract to named method
   list.stream()
       .filter(this::isValidItem)
       .forEach(this::process);

   private boolean isValidItem(Item item) {
       return item != null &&
              item.getValue() > 0 &&
              item.isActive() &&
              item.getStatus() != Status.DELETED &&
              item.getOwner() != null &&
              item.getOwner().isVerified();
   }
   ```

2. **Using lambdas when method references are clearer**:
   ```java
   // Less clear
   list.forEach(item -> System.out.println(item));
   list.stream().map(item -> item.getName()).collect(Collectors.toList());

   // More clear
   list.forEach(System.out::println);
   list.stream().map(Item::getName).collect(Collectors.toList());
   ```

3. **Poor variable naming**:
   ```java
   // Bad: Non-descriptive names
   users.stream()
        .filter(u -> u.getAge() > 18)
        .map(u -> u.getName())
        .forEach(n -> sendEmail(n));

   // Good: Descriptive names
   users.stream()
        .filter(user -> user.getAge() > 18)
        .map(User::getName)
        .forEach(this::sendEmail);
   ```

4. **Side effects in filter/map operations**:
   ```java
   // Bad: Side effects
   int[] count = {0};
   list.stream()
       .filter(item -> {
           count[0]++;  // Side effect!
           return item.isValid();
       })
       .collect(Collectors.toList());

   // Good: No side effects
   List<Item> valid = list.stream()
       .filter(Item::isValid)
       .collect(Collectors.toList());
   int count = valid.size();
   ```

## Best Practices and Optimization Techniques

1. **Prefer method references when possible**:
   ```java
   // Good progression from lambda to method reference
   Function<String, Integer> length1 = s -> s.length();
   Function<String, Integer> length2 = String::length;

   // Static method reference
   Function<String, Integer> parser1 = s -> Integer.parseInt(s);
   Function<String, Integer> parser2 = Integer::parseInt;

   // Constructor reference
   Supplier<List<String>> supplier1 = () -> new ArrayList<>();
   Supplier<List<String>> supplier2 = ArrayList::new;
   ```

2. **Keep lambdas short and focused**:
   ```java
   // Guideline: If lambda exceeds 3-5 lines, extract it

   // Good: Short and clear
   list.stream()
       .filter(item -> item.getPrice() > 100)
       .collect(Collectors.toList());

   // Extract complex logic
   list.stream()
       .filter(this::meetsBusinessRules)
       .collect(Collectors.toList());
   ```

3. **Use descriptive names even in short lambdas**:
   ```java
   // Less clear
   users.stream()
        .filter(u -> u.getOrders().stream()
                      .anyMatch(o -> o.getTotal() > 1000))
        .collect(Collectors.toList());

   // More clear
   users.stream()
        .filter(user -> user.getOrders().stream()
                           .anyMatch(order -> order.getTotal() > 1000))
        .collect(Collectors.toList());

   // Even better: extract predicate
   Predicate<User> hasLargeOrder = user ->
       user.getOrders().stream()
           .anyMatch(order -> order.getTotal() > 1000);

   users.stream()
        .filter(hasLargeOrder)
        .collect(Collectors.toList());
   ```

4. **Format multi-statement lambdas properly**:
   ```java
   // Good formatting
   items.stream()
        .map(item -> {
            double basePrice = item.getPrice();
            double discount = calculateDiscount(item);
            double tax = calculateTax(basePrice - discount);
            return basePrice - discount + tax;
        })
        .collect(Collectors.toList());
   ```

5. **Avoid deeply nested lambdas**:
   ```java
   // Bad: Hard to read
   users.stream()
        .map(user -> user.getOrders().stream()
                        .map(order -> order.getItems().stream()
                                          .map(item -> item.getPrice())
                                          .reduce(0.0, Double::sum))
                        .reduce(0.0, Double::sum))
        .collect(Collectors.toList());

   // Good: Extract to methods
   users.stream()
        .map(this::calculateUserTotal)
        .collect(Collectors.toList());

   private double calculateUserTotal(User user) {
       return user.getOrders().stream()
                  .mapToDouble(this::calculateOrderTotal)
                  .sum();
   }

   private double calculateOrderTotal(Order order) {
       return order.getItems().stream()
                   .mapToDouble(Item::getPrice)
                   .sum();
   }
   ```

6. **Use explicit types when they improve clarity**:
   ```java
   // Sometimes explicit types help
   BiFunction<Map<String, List<String>>, String, List<String>> extractor =
       (Map<String, List<String>> map, String key) ->
           map.getOrDefault(key, Collections.emptyList());

   // But often inference is fine
   users.stream()
        .filter(user -> user.getAge() > 18)  // Type is obvious from context
        .collect(Collectors.toList());
   ```

7. **Extract lambdas to constants for reuse**:
   ```java
   // Good: Reusable predicates
   public class UserFilters {
       public static final Predicate<User> IS_ADULT =
           user -> user.getAge() >= 18;

       public static final Predicate<User> IS_ACTIVE =
           User::isActive;

       public static final Predicate<User> HAS_VERIFIED_EMAIL =
           user -> user.getEmail() != null && user.isEmailVerified();

       public static final Predicate<User> IS_ELIGIBLE =
           IS_ADULT.and(IS_ACTIVE).and(HAS_VERIFIED_EMAIL);
   }

   // Usage
   users.stream()
        .filter(UserFilters.IS_ELIGIBLE)
        .collect(Collectors.toList());
   ```

## Edge Cases and Their Handling

1. **Null handling in lambda chains**:
   ```java
   // Proper null handling
   list.stream()
       .filter(Objects::nonNull)
       .map(item -> Optional.ofNullable(item.getOwner())
                           .map(Owner::getName)
                           .orElse("Unknown"))
       .collect(Collectors.toList());
   ```

2. **Exception handling without compromising readability**:
   ```java
   // Clean exception handling
   public class ExceptionHandlers {
       public static <T, R> Function<T, Optional<R>> lift(
           CheckedFunction<T, R> function) {
           return t -> {
               try {
                   return Optional.ofNullable(function.apply(t));
               } catch (Exception e) {
                   return Optional.empty();
               }
           };
       }
   }

   // Usage
   List<Integer> parsed = strings.stream()
       .map(ExceptionHandlers.lift(Integer::parseInt))
       .filter(Optional::isPresent)
       .map(Optional::get)
       .collect(Collectors.toList());
   ```

3. **Parallel stream considerations**:
   ```java
   // Ensure thread-safe operations
   list.parallelStream()
       .map(this::expensiveOperation)  // Must be thread-safe
       .collect(Collectors.toList());
   ```

## Interview-specific Insights

Interviewers often focus on:

- Understanding of lambda readability principles
- Ability to refactor complex lambdas into readable code
- Knowledge of when to extract lambdas to named methods
- Understanding the trade-offs between conciseness and clarity
- Awareness of performance implications
- Ability to identify and fix poorly written lambda code

Common tricky questions:

- "How do you decide when a lambda is too complex?"
- "What makes one lambda more readable than another?"
- "When should you use method references vs. lambda expressions?"
- "How do you handle exceptions in lambdas while maintaining readability?"

## Interview Q&A Section

**Q1: What are the key principles for writing readable lambda expressions?**

```text
A1: The key principles for readable lambda expressions are:

1. **Simplicity**: Keep lambdas short and focused on a single operation
   - Ideal: 1-2 lines
   - Maximum: 3-5 lines
   - Beyond that: extract to a named method

2. **Clarity over brevity**: Don't sacrifice readability for shorter code
   - Use descriptive parameter names
   - Prefer method references when they're clearer
   - Add explicit types if they improve understanding

3. **Intention-revealing**: Code should express what, not how
   - Use predicates with meaningful names
   - Extract complex conditions to methods
   - Chain operations declaratively

4. **Avoid side effects**: Lambdas should be pure functions when possible
   - No modifying external state
   - No I/O operations in map/filter
   - Use forEach or peek for intentional side effects

5. **Proper formatting**: Format code consistently
   - Break long chains into multiple lines
   - Align operations vertically
   - Use proper indentation for multi-statement lambdas

6. **Context awareness**: Consider your audience
   - Team familiarity with functional programming
   - Complexity of the domain
   - Maintenance requirements
```

```java
// Examples demonstrating these principles
public class ReadableLambdas {

    // 1. Simplicity - extract complex logic
    public List<User> getEligibleUsers(List<User> users) {
        // Bad: Complex inline lambda
        return users.stream()
                .filter(u -> u.getAge() >= 18 &&
                        u.isActive() &&
                        u.getEmail() != null &&
                        u.isEmailVerified() &&
                        u.getRegistrationDate().isBefore(LocalDate.now().minusMonths(3)))
                .collect(Collectors.toList());

        // Good: Extracted method
        return users.stream()
                .filter(this::isEligibleUser)
                .collect(Collectors.toList());
    }

    private boolean isEligibleUser(User user) {
        return user.getAge() >= 18 &&
                user.isActive() &&
                hasVerifiedEmail(user) &&
                isRegisteredLongEnough(user);
    }

    // 2. Clarity over brevity
    public void processOrders(List<Order> orders) {
        // Less clear
        orders.stream()
                .filter(o -> o.getTotal() > 100 && o.getStatus() == Status.PENDING)
                .forEach(o -> process(o));

        // More clear
        Predicate<Order> isLargeOrder = order -> order.getTotal() > 100;
        Predicate<Order> isPending = order -> order.getStatus() == Status.PENDING;

        orders.stream()
                .filter(isLargeOrder.and(isPending))
                .forEach(this::process);
    }

    // 3. Intention-revealing names
    public Map<String, List<User>> groupUsersByDepartment(List<User> users) {
        // Clear intent
        return users.stream()
                .collect(Collectors.groupingBy(User::getDepartment));
    }

    // 4. Avoid side effects
    public long countValidItems(List<Item> items) {
        // Bad: Side effect in filter
        List<String> processedIds = new ArrayList<>();
        long count = items.stream()
                .filter(item -> {
                    processedIds.add(item.getId());  // Side effect!
                    return item.isValid();
                })
                .count();

        // Good: Separate concerns
        long count2 = items.stream()
                .filter(Item::isValid)
                .count();

        List<String> processedIds2 = items.stream()
                .filter(Item::isValid)
                .map(Item::getId)
                .collect(Collectors.toList());

        return count2;
    }

    // 5. Proper formatting
    public List<OrderSummary> createOrderSummaries(List<Order> orders) {
        return orders.stream()
                .filter(Order::isCompleted)
                .map(order -> new OrderSummary(
                        order.getId(),
                        order.getCustomer().getName(),
                        order.getTotal(),
                        order.getCompletedDate()
                ))
                .sorted(Comparator.comparing(OrderSummary::total).reversed())
                .collect(Collectors.toList());
    }

    // Supporting classes
    record User(int age, boolean active, String email, boolean emailVerified,
                LocalDate registrationDate, String department) {
        int getAge() {
            return age;
        }

        boolean isActive() {
            return active;
        }

        String getEmail() {
            return email;
        }

        boolean isEmailVerified() {
            return emailVerified;
        }

        LocalDate getRegistrationDate() {
            return registrationDate;
        }

        String getDepartment() {
            return department;
        }
    }

    record Order(String id, double total, Status status, Customer customer,
                 boolean completed, LocalDate completedDate) {
        String getId() {
            return id;
        }

        double getTotal() {
            return total;
        }

        Status getStatus() {
            return status;
        }

        Customer getCustomer() {
            return customer;
        }

        boolean isCompleted() {
            return completed;
        }

        LocalDate getCompletedDate() {
            return completedDate;
        }
    }

    record Customer(String name) {
        String getName() {
            return name;
        }
    }

    record Item(String id, boolean valid) {
        String getId() {
            return id;
        }

        boolean isValid() {
            return valid;
        }
    }

    record OrderSummary(String id, String customerName, double total,
                        LocalDate completedDate) {
        double total() {
            return total;
        }
    }

    enum Status {PENDING, COMPLETED}

    private boolean hasVerifiedEmail(User user) {
        return user.getEmail() != null && user.isEmailVerified();
    }

    private boolean isRegisteredLongEnough(User user) {
        return user.getRegistrationDate()
                .isBefore(LocalDate.now().minusMonths(3));
    }

    private void process(Order order) {
    }
}
```

**Q2: When should you extract a lambda to a named method?**

```java
public class LambdaExtractionGuidelines {

    // Guideline 1: Extract when lambda exceeds 3-5 lines
    public void guideline1_Length() {
        List<User> users = getUsers();

        // Too long inline
        List<User> eligible = users.stream()
                .filter(user -> {
                    if (user.getAge() < 18) return false;
                    if (!user.isActive()) return false;
                    if (user.getEmail() == null) return false;
                    if (!user.isEmailVerified()) return false;
                    if (user.getBannedUntil() != null &&
                            user.getBannedUntil().isAfter(LocalDate.now())) return false;
                    return true;
                })
                .collect(Collectors.toList());

        // Better: extracted method
        List<User> eligible2 = users.stream()
                .filter(this::isEligibleUser)
                .collect(Collectors.toList());
    }

    // Guideline 2: Extract when lambda is used multiple times
    public void guideline2_Reuse() {
        List<User> users = getUsers();

        // Duplicated lambda - bad
        long adultCount = users.stream()
                .filter(user -> user.getAge() >= 18)
                .count();

        List<User> adults = users.stream()
                .filter(user -> user.getAge() >= 18)
                .collect(Collectors.toList());

        // Better: extract to constant or method
        Predicate<User> IS_ADULT = user -> user.getAge() >= 18;

        long adultCount2 = users.stream().filter(IS_ADULT).count();
        List<User> adults2 = users.stream().filter(IS_ADULT).collect(Collectors.toList());
    }

    // Guideline 3: Extract when lambda has complex business logic
    public void guideline3_BusinessLogic() {
        List<Order> orders = getOrders();

        // Complex business logic inline - hard to understand
        List<Order> priorityOrders = orders.stream()
                .filter(order -> {
                    if (order.getCustomer().isPremium()) return true;
                    if (order.getTotal() > 1000) return true;
                    if (order.getItems().stream()
                            .anyMatch(item -> item.isUrgent())) return true;
                    return false;
                })
                .collect(Collectors.toList());

        // Better: named method reveals intent
        List<Order> priorityOrders2 = orders.stream()
                .filter(this::isPriorityOrder)
                .collect(Collectors.toList());
    }

    // Guideline 4: Extract when lambda contains domain knowledge
    public void guideline4_DomainKnowledge() {
        List<Product> products = getProducts();

        // Domain-specific calculation inline
        List<Product> discounted = products.stream()
                .filter(product -> {
                    // Complex discount eligibility rules
                    return product.getCategory().equals("Electronics") &&
                            product.getStock() > 50 &&
                            product.getDaysSinceRelease() > 90 &&
                            !product.isOnSale();
                })
                .collect(Collectors.toList());

        // Better: encapsulate domain knowledge
        List<Product> discounted2 = products.stream()
                .filter(this::isEligibleForClearanceDiscount)
                .collect(Collectors.toList());
    }

    // Guideline 5: Extract when testability is important
    public void guideline5_Testability() {
        // Inline lambda - hard to test independently
        List<Transaction> suspicious = transactions.stream()
                .filter(t -> t.getAmount() > 10000 &&
                        t.getLocation().getCountry().equals("Foreign") &&
                        !t.getUser().isVerified())
                .collect(Collectors.toList());

        // Extracted method - easy to test
        List<Transaction> suspicious2 = transactions.stream()
                .filter(this::isSuspiciousTransaction)
                .collect(Collectors.toList());
    }

    // When NOT to extract: Simple, self-evident lambdas
    public void whenNotToExtract() {
        List<String> names = List.of("Alice", "Bob", "Charlie");

        // These are fine inline - simple and clear
        List<String> upperNames = names.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        List<String> shortNames = names.stream()
                .filter(name -> name.length() < 5)
                .collect(Collectors.toList());

        names.forEach(System.out::println);

        // Simple transformations don't need extraction
        List<Integer> lengths = names.stream()
                .map(String::length)
                .collect(Collectors.toList());
    }

    // Helper methods
    private boolean isEligibleUser(User user) {
        return user.getAge() >= 18 &&
                user.isActive() &&
                user.getEmail() != null &&
                user.isEmailVerified() &&
                (user.getBannedUntil() == null ||
                        user.getBannedUntil().isBefore(LocalDate.now()));
    }

    private boolean isPriorityOrder(Order order) {
        return order.getCustomer().isPremium() ||
                order.getTotal() > 1000 ||
                containsUrgentItems(order);
    }

    private boolean containsUrgentItems(Order order) {
        return order.getItems().stream().anyMatch(Item::isUrgent);
    }

    private boolean isEligibleForClearanceDiscount(Product product) {
        return product.getCategory().equals("Electronics") &&
                product.getStock() > 50 &&
                product.getDaysSinceRelease() > 90 &&
                !product.isOnSale();
    }

    private boolean isSuspiciousTransaction(Transaction transaction) {
        return transaction.getAmount() > 10000 &&
                isForeignTransaction(transaction) &&
                !transaction.getUser().isVerified();
    }

    private boolean isForeignTransaction(Transaction transaction) {
        return transaction.getLocation().getCountry().equals("Foreign");
    }

    // Supporting classes
    private List<User> getUsers() {
        return List.of();
    }

    private List<Order> getOrders() {
        return List.of();
    }

    private List<Product> getProducts() {
        return List.of();
    }

    private List<Transaction> transactions = List.of();

    record User(int age, boolean active, String email, boolean emailVerified,
                LocalDate bannedUntil) {
        int getAge() {
            return age;
        }

        boolean isActive() {
            return active;
        }

        String getEmail() {
            return email;
        }

        boolean isEmailVerified() {
            return emailVerified;
        }

        LocalDate getBannedUntil() {
            return bannedUntil;
        }
    }

    record Order(double total, Customer customer, List<Item> items) {
        double getTotal() {
            return total;
        }

        Customer getCustomer() {
            return customer;
        }

        List<Item> getItems() {
            return items;
        }
    }

    record Customer(boolean premium) {
        boolean isPremium() {
            return premium;
        }
    }

    record Item(boolean urgent) {
        boolean isUrgent() {
            return urgent;
        }
    }

    record Product(String category, int stock, int daysSinceRelease,
                   boolean onSale) {
        String getCategory() {
            return category;
        }

        int getStock() {
            return stock;
        }

        int getDaysSinceRelease() {
            return daysSinceRelease;
        }

        boolean isOnSale() {
            return onSale;
        }
    }

    record Transaction(double amount, Location location, User user) {
        double getAmount() {
            return amount;
        }

        Location getLocation() {
            return location;
        }

        User getUser() {
            return user;
        }
    }

    record Location(String country) {
        String getCountry() {
            return country;
        }
    }
}
```

```text
A2 (continued): Extract lambdas when:
1. **Length**: Exceeds 3-5 lines
2. **Reuse**: Used in multiple places
3. **Complexity**: Contains complex business logic
4. **Domain knowledge**: Encapsulates domain-specific rules
5. **Testability**: Needs independent testing
6. **Documentation**: Requires explanation

Keep inline when:
1. **Simplicity**: One-liner with obvious intent
2. **Uniqueness**: Used only once and simple
3. **Standard operations**: map, filter with simple predicates
4. **Method references**: Can use method reference instead

The goal: Balance between conciseness and clarity. If someone needs to pause and figure out what your lambda does, extract it!
```

**Q3: How do you balance functional programming style with code readability?**

```java
public class BalancingFunctionalAndReadable {

    // Example 1: Finding the balance
    public class OrderProcessor {
        // Too imperative - not leveraging functional style
        public double calculateTotalImperative(List<Order> orders) {
            double total = 0;
            for (Order order : orders) {
                if (order.getStatus() == Status.COMPLETED) {
                    for (Item item : order.getItems()) {
                        if (item.getPrice() > 0) {
                            total += item.getPrice();
                        }
                    }
                }
            }
            return total;
        }

        // Too functional - hard to understand
        public double calculateTotalOverlyFunctional(List<Order> orders) {
            return orders.stream()
                    .filter(o -> o.getStatus() == Status.COMPLETED)
                    .flatMap(o -> o.getItems().stream())
                    .map(Item::getPrice)
                    .filter(p -> p > 0)
                    .reduce(0.0, (a, b) -> a + b, (a, b) -> a + b);
        }

        // Balanced - clear and functional
        public double calculateTotalBalanced(List<Order> orders) {
            return orders.stream()
                    .filter(this::isCompleted)
                    .flatMap(order -> order.getItems().stream())
                    .mapToDouble(Item::getPrice)
                    .filter(price -> price > 0)
                    .sum();
        }

        private boolean isCompleted(Order order) {
            return order.getStatus() == Status.COMPLETED;
        }
    }

    // Example 2: When to use streams vs loops
    public class CollectionProcessing {
        // Use loops for: Simple iterations, debugging, step-through logic
        public void sendNotificationsLoop(List<User> users) {
            for (User user : users) {
                if (user.wantsNotifications()) {
                    sendEmail(user);
                }
            }
        }

        // Use streams for: Transformations, filtering, collecting
        public List<String> getActiveUserNamesStream(List<User> users) {
            return users.stream()
                    .filter(User::isActive)
                    .map(User::getName)
                    .sorted()
                    .collect(Collectors.toList());
        }

        // Hybrid approach: Use what makes sense
        public Map<String, List<Order>> groupOrdersByStatus(List<Order> orders) {
            // Stream for grouping - natural fit
            Map<String, List<Order>> grouped = orders.stream()
                    .collect(Collectors.groupingBy(
                            order -> order.getStatus().name()
                    ));

            // Loop for post-processing if needed
            for (Map.Entry<String, List<Order>> entry : grouped.entrySet()) {
                processGroupStatistics(entry.getKey(), entry.getValue());
            }

            return grouped;
        }

        private void sendEmail(User user) {
        }

        private void processGroupStatistics(String status, List<Order> orders) {
        }
    }

    // Example 3: Breaking down complex operations
    public class ComplexOperations {
        // Bad: Everything in one chain
        public List<OrderSummary> processOrdersBad(List<Order> orders) {
            return orders.stream()
                    .filter(o -> o.getStatus() == Status.COMPLETED &&
                            o.getCompletedDate().isAfter(LocalDate.now().minusDays(30)) &&
                            o.getTotal() > 100)
                    .map(o -> new OrderSummary(
                            o.getId(),
                            o.getCustomer().getName(),
                            o.getItems().stream()
                                    .mapToDouble(i -> i.getPrice() * (1 + i.getTaxRate()))
                                    .sum(),
                            o.getCompletedDate()
                    ))
                    .sorted((a, b) -> Double.compare(b.total(), a.total()))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        // Good: Broken into readable steps
        public List<OrderSummary> processOrdersGood(List<Order> orders) {
            return orders.stream()
                    .filter(this::isRecentAndSignificant)
                    .map(this::createOrderSummary)
                    .sorted(this::byTotalDescending)
                    .limit(10)
                    .collect(Collectors.toList());
        }

        private boolean isRecentAndSignificant(Order order) {
            return order.getStatus() == Status.COMPLETED &&
                    order.getCompletedDate().isAfter(LocalDate.now().minusDays(30)) &&
                    order.getTotal() > 100;
        }

        private OrderSummary createOrderSummary(Order order) {
            return new OrderSummary(
                    order.getId(),
                    order.getCustomer().getName(),
                    calculateTotalWithTax(order),
                    order.getCompletedDate()
            );
        }

        private double calculateTotalWithTax(Order order) {
            return order.getItems().stream()
                    .mapToDouble(item -> item.getPrice() * (1 + item.getTaxRate()))
                    .sum();
        }

        private int byTotalDescending(OrderSummary a, OrderSummary b) {
            return Double.compare(b.total(), a.total());
        }
    }

    // Example 4: Documentation and comments
    public class DocumentingFunctionalCode {
        // Good: Self-documenting with clear names
        public List<User> getEligibleVoters(List<User> users) {
            return users.stream()
                    .filter(this::isOfVotingAge)
                    .filter(this::isCitizen)
                    .filter(this::isRegistered)
                    .collect(Collectors.toList());
        }

        // When complex, add javadoc

        /**
         * Calculates risk score for loan application.
         * Score ranges from 0 (low risk) to 100 (high risk).
         *
         * Factors:
         * - Credit history (40% weight)
         * - Income to debt ratio (30% weight)
         * - Employment stability (20% weight)
         * - Savings (10% weight)
         *
         * @param application The loan application to evaluate
         * @return Risk score between 0 and 100
         */
        public double calculateRiskScore(LoanApplication application) {
            return Stream.of(
                            evaluateCreditHistory(application) * 0.4,
                            evaluateDebtRatio(application) * 0.3,
                            evaluateEmployment(application) * 0.2,
                            evaluateSavings(application) * 0.1
                    )
                    .mapToDouble(Double::doubleValue)
                    .sum();
        }

        private boolean isOfVotingAge(User user) {
            return user.getAge() >= 18;
        }

        private boolean isCitizen(User user) {
            return user.isCitizen();
        }

        private boolean isRegistered(User user) {
            return user.isRegistered();
        }

        private double evaluateCreditHistory(LoanApplication app) {
            return 0.0;
        }

        private double evaluateDebtRatio(LoanApplication app) {
            return 0.0;
        }

        private double evaluateEmployment(LoanApplication app) {
            return 0.0;
        }

        private double evaluateSavings(LoanApplication app) {
            return 0.0;
        }
    }

    // Supporting classes
    record Order(String id, Status status, List<Item> items, double total,
                 LocalDate completedDate, Customer customer) {
        String getId() {
            return id;
        }

        Status getStatus() {
            return status;
        }

        List<Item> getItems() {
            return items;
        }

        double getTotal() {
            return total;
        }

        LocalDate getCompletedDate() {
            return completedDate;
        }

        Customer getCustomer() {
            return customer;
        }
    }

    record Item(double price, double taxRate) {
        double getPrice() {
            return price;
        }

        double getTaxRate() {
            return taxRate;
        }
    }

    record Customer(String name) {
        String getName() {
            return name;
        }
    }

    record OrderSummary(String id, String customerName, double total,
                        LocalDate completedDate) {
        double total() {
            return total;
        }
    }

    record User(String name, boolean active, boolean wantsNotifications,
                int age, boolean citizen, boolean registered) {
        String getName() {
            return name;
        }

        boolean isActive() {
            return active;
        }

        boolean wantsNotifications() {
            return wantsNotifications;
        }

        int getAge() {
            return age;
        }

        boolean isCitizen() {
            return citizen;
        }

        boolean isRegistered() {
            return registered;
        }
    }

    enum Status {PENDING, COMPLETED}

    record LoanApplication() {
    }
}
```

```text
A3: Guidelines for balancing functional and readable:

1. **Progressive enhancement**: Start simple, add complexity only when needed
2. **Meaningful names**: Extract lambdas to methods with descriptive names
3. **Right tool for the job**: Use streams for transformations, loops for debugging
4. **Break complex chains**: Split long chains into intermediate steps or methods
5. **Document intent**: Use javadoc for complex algorithms
6. **Consider audience**: Team experience with functional programming matters
7. **Test thoroughly**: Functional code can hide bugs - test edge cases
8. **Performance awareness**: Don't sacrifice performance for style

Signs you've gone too functional:
- Teammates struggle to understand code
- Debugging is difficult
- Simple operations become complex
- Performance suffers unnecessarily

Signs you're not functional enough:
- Lots of mutable state
- Nested loops everywhere
- Code that's hard to parallelize
- Repetitive transformation logic

The sweet spot:
- Use functional style for data transformations
- Keep lambdas simple and focused
- Extract complex logic to named methods
- Mix paradigms when it makes sense
- Prioritize readability over cleverness
```

## Code Examples

-
Test: [LambdaBestPracticesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaBestPracticesTest.java)
-
Source: [LambdaBestPractices.java](src/main/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaBestPractices.java)