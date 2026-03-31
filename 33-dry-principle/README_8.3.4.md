# 8.3.4. Balancing DRY with Other Principles (e.g., SRP)

## Concept Explanation

DRY is a powerful principle, but applying it too aggressively can conflict with other important principles -- particularly SRP (Single Responsibility Principle), KISS (Keep It Simple, Stupid), and YAGNI (You Ain't Gonna Need It). Balancing DRY means knowing **when to extract** and **when to tolerate duplication**.

**Real-world analogy**: A Swiss Army knife combines a blade, screwdriver, corkscrew, and scissors into one tool. It's DRY -- one tool instead of four. But a surgeon, electrician, sommelier, and tailor each prefer their own specialized tool because their requirements diverge. Over-DRYing by forcing everyone to use the Swiss Army knife sacrifices effectiveness for generality.

The key tensions:
- **DRY vs. SRP**: Extracting shared logic into a utility can couple unrelated domains. If `calculateTax()` and `calculateCommission()` look identical today but serve different business rules, merging them violates SRP.
- **DRY vs. KISS**: An elaborate generic abstraction to eliminate two lines of duplication adds complexity that hurts readability.
- **DRY vs. YAGNI**: Abstracting after seeing only one instance is premature -- the Rule of Three says wait.

## Key Points to Remember

- **Incidental duplication** (code looks similar but represents different domain concepts) should NOT be eliminated.
- **Intentional duplication** (same business rule in multiple places) SHOULD be eliminated.
- The **Rule of Three** heuristic: tolerate duplication until you see it three times.
- Ask: "If one copy changes, must the other change too?" If yes, it's a DRY violation. If no, it's incidental.
- Shared validation utilities (null checks, range checks) are good DRY candidates -- they're truly cross-cutting.
- Domain-specific validators that USE those utilities respect both DRY and SRP.
- Over-DRY creates **wrong abstractions** -- harder to fix than duplication.

## Relevant Java 21 Features

- **Records**: Separate records for `EmployeeAddress` and `CustomerAddress` even if they look identical -- they represent different domains that may diverge.
- **Pattern matching for switch**: Reduces type-checking boilerplate without creating artificial shared abstractions.
- **Sealed classes**: Enable exhaustive matching, reducing the need for generic catch-all utilities.
- **Default methods in interfaces**: Allow shared behavior in a type-safe way that respects interface segregation.

## Common Pitfalls and How to Avoid Them

1. **Merging unrelated business rules into a "generic" method**
   ```java
   // OVER-DRY: Couples tax and discount logic
   double genericCalculation(double amount, double rate, boolean isTax) {
       return isTax ? amount * (1 + rate) : amount * (1 - rate);
   }
   ```
   **Solution**: Keep `calculateTax()` and `calculateDiscount()` separate -- they're different business rules.

2. **Creating deeply generic utilities for two use cases**
   ```java
   // OVER-DRY: GenericProcessor<T,R,S,U> after seeing two similar methods
   ```
   **Solution**: Wait for the Rule of Three before abstracting.

3. **Sharing validation logic across incompatible domains**
   ```java
   // Validator handles orders, employees, invoices -- too many responsibilities
   ```
   **Solution**: Use shared primitive utilities (`isNonBlank`, `isPositive`) but domain-specific validators.

4. **Premature abstraction that becomes a bottleneck**
   ```java
   // A "universal data accessor" that wraps every database call
   // Now every team depends on it and can't optimize for their use case
   ```
   **Solution**: Only abstract when the duplication is proven harmful and the abstraction is stable.

## Best Practices and Optimization Techniques

1. **Layer your abstractions**: Low-level utilities (`isNonBlank`, `isInRange`) are safe to share. Domain validators use them but encapsulate domain rules.
2. **Use the Rule of Three**: First and second occurrence -- note the similarity. Third occurrence -- extract.
3. **Ask the coupling question**: "Will these two uses always change together?" If yes, extract. If maybe, wait.
4. **Prefer composition over inheritance for DRY**: Compose behaviors with lambdas and utility methods rather than creating deep inheritance hierarchies.
5. **Review abstractions periodically**: An extraction that made sense a year ago may now be a wrong abstraction that should be inlined.

## Edge Cases and Their Handling

1. **Negative amounts in domain calculations**: Each domain validator handles its own edge cases -- don't force them into a shared validator.
2. **Boundary rates (0.0, 1.0)**: Tax rate of 0 and 1 are valid; discount rate of 0 and 1 are valid but have different semantics.
3. **Null inputs to shared utilities**: `ValidationUtils.isNonBlank(null)` returns `false` rather than throwing -- defensive design for cross-cutting utilities.
4. **Empty error lists from validators**: An empty list means validation passed -- no errors.
5. **Conflicting validation rules across domains**: `OrderValidator` allows quantity up to 10000; `InventoryValidator` might have a different limit -- separate validators prevent forced unification.

## Interview-specific Insights

Interviewers focus on:
- Your ability to distinguish incidental from intentional duplication
- Whether you can articulate when DRY harms rather than helps
- Knowledge of the Rule of Three and when to apply it
- How you balance DRY with SRP in real codebases
- Awareness of wrong abstractions and their cost

Tricky questions:
- "Two methods have identical code. Should you always extract?" (No -- if they serve different domains, they may diverge independently.)
- "When is duplication better than abstraction?" (When the code serves different purposes, when you've only seen it once or twice, when the abstraction would be more complex than the duplication.)
- "How do you decide when to refactor duplicated code?" (Rule of Three, coupling analysis, domain boundary analysis.)

## Interview Q&A Section

**Q1: When should you NOT eliminate code duplication?**

```text
A1: You should tolerate duplication when:

1. It's incidental (structural similarity, different business domains). Tax calculation
   and commission calculation may look identical but represent different rules.
2. You've seen it fewer than three times (Rule of Three). Premature abstraction is
   worse than small-scale duplication.
3. The abstraction would be more complex than the duplication. A 3-line duplicated
   snippet is better than a 30-line generic framework.
4. The duplicated code serves different stakeholders who may change it independently.
5. Coupling the two would violate SRP -- a shared utility serving unrelated domains
   becomes a maintenance burden.

The key question: "If one copy changes, must the other change too?" If the answer
is "not necessarily," keep them separate.
```

```java
// INCIDENTAL duplication -- keep separate
// Tax and discount may diverge: tax could add surcharges, discount could add tiers
double calculateTax(double amount, double rate) { return amount * (1 + rate); }
double calculateDiscount(double amount, double rate) { return amount * (1 - rate); }

// INTENTIONAL duplication -- extract
// Same bulk discount rule used in two contexts
double applyBulkDiscount(double price, int qty) {
    if (qty >= 100) return price * 0.80;
    if (qty >= 50)  return price * 0.90;
    if (qty >= 10)  return price * 0.95;
    return price;
}
```

**Q2: How do you balance DRY with SRP?**

```text
A2: DRY says "don't duplicate knowledge." SRP says "each class has one reason to change."
They conflict when extracting shared logic couples unrelated domains.

The solution is layered abstraction:
1. Low-level utilities (isNonBlank, isPositive, isInRange) are truly cross-cutting
   and safe to share. They're DRY without violating SRP because they have one
   responsibility: primitive validation.
2. Domain validators (OrderValidator, EmployeeValidator) USE the shared utilities
   but encapsulate domain-specific rules. Each has one responsibility: validating
   its domain.

This way, the validation primitives are DRY (written once), and the domain validators
respect SRP (each validates one domain).
```

```java
// Shared utilities -- DRY, single responsibility: primitive checks
class ValidationUtils {
    static boolean isNonBlank(String s) { return s != null && !s.isBlank(); }
    static boolean isPositive(Number n) { return n != null && n.doubleValue() > 0; }
}

// Domain validators -- SRP, each validates one domain
class OrderValidator {
    List<String> validate(String customerId, double amount, int quantity) {
        List<String> errors = new ArrayList<>();
        if (!ValidationUtils.isNonBlank(customerId)) errors.add("Customer ID required");
        if (!ValidationUtils.isPositive(amount)) errors.add("Amount must be positive");
        if (quantity > 10000) errors.add("Quantity exceeds max");
        return errors;
    }
}
```

**Q3: What is the Rule of Three and why does it matter?**

```text
A3: The Rule of Three says: don't abstract until you see duplication at least three times.

Rationale:
1. First time: Just write it. You don't know yet if it's a pattern.
2. Second time: Note the similarity but tolerate it. Two instances might be coincidence.
3. Third time: Now you have evidence of a real pattern. Extract with confidence.

Why it matters:
- Premature abstraction creates wrong abstractions that are hard to undo.
- Wrong abstractions couple unrelated code through a shared dependency.
- The Rule of Three balances DRY with KISS and YAGNI.
- By the third occurrence, you understand the pattern well enough to create
  an abstraction that actually fits all use cases.
```

```java
// First: order summary
String formatOrderSummary(String id, String desc, double total) {
    return String.format("[%s] %s - $%.2f", id, desc, total);
}
// Second: invoice summary -- looks similar, keep separate for now
String formatInvoiceSummary(String num, String client, double amount) {
    return String.format("[%s] %s - $%.2f", num, client, amount);
}
// Third: receipt summary -- NOW extract!
String formatSummary(String identifier, String description, double amount) {
    return String.format("[%s] %s - $%.2f", identifier, description, amount);
}
```

**Q4: What is a "wrong abstraction" and how do you fix it?**

```text
A4: A wrong abstraction is an extraction that doesn't fit all its use cases.
It accumulates boolean flags, type checks, and special cases to handle the
differences between consumers -- becoming more complex than the original duplication.

Signs of a wrong abstraction:
1. Boolean parameters controlling behavior: genericCalc(amount, rate, isTax)
2. Growing switch/if chains inside the shared method
3. Consumers working around the abstraction with pre/post-processing
4. Every change requires modifying the shared utility and all its callers

How to fix it:
1. Inline the abstraction back into the callers (undo the DRY)
2. Identify the true common parts vs. the varying parts
3. Use composition (lambdas, strategy pattern) instead of flags
4. Or accept that the code isn't truly duplicated and keep it separate

As Sandi Metz says: "Duplication is far cheaper than the wrong abstraction."
```

```java
// WRONG ABSTRACTION: boolean flag switches behavior
double genericCalc(double amount, double rate, boolean isTax) {
    return isTax ? amount * (1 + rate) : amount * (1 - rate);
}

// FIX: Separate methods -- cleaner, independent evolution
double calculateTax(double amount, double taxRate) {
    return amount * (1 + taxRate);
}
double calculateDiscount(double amount, double discountRate) {
    return amount * (1 - discountRate);
}
```

**Q5: How do records help distinguish incidental from intentional duplication?**

```text
A5: Java records make it explicit that two similar-looking data structures represent
different domain concepts. Even if EmployeeAddress and CustomerAddress have the same
fields (street, city, zipCode), making them separate records:

1. Signals they are different domain types that may diverge.
2. Enables type-safe method signatures: void ship(CustomerAddress a) won't accept
   an EmployeeAddress.
3. Each can evolve independently: CustomerAddress might add shippingPreference,
   EmployeeAddress might add department.
4. Pattern matching treats them as distinct types.

This is incidental duplication that SHOULD be preserved -- records make the intent
clear. If they truly represent the same concept, use one record for both.
```

```java
// Incidental duplication -- keep separate records
record EmployeeAddress(String street, String city, String zipCode) {
    String format() { return street + ", " + city + " " + zipCode; }
}
record CustomerAddress(String street, String city, String zipCode) {
    String format() { return street + ", " + city + " " + zipCode; }
}

// Type safety: can't accidentally pass employee address to shipping
void shipOrder(CustomerAddress address) { /* ... */ }
void sendPayslip(EmployeeAddress address) { /* ... */ }
```

## Code Examples

- Test: [DryBalancingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryBalancingTest.java)
- Source: [DryBalancing.java](src/main/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryBalancing.java)
