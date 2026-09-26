The page's `BankAccount` follows the best practices: exception names that say
what went wrong, context carried in fields, and checks that run before any
state changes. Write it.

1. Complete the checked `InsufficientFundsException(String message, double requestedAmount, double accountBalance)`
   with `getRequestedAmount()` and `getAccountBalance()`.
2. Write `BankAccount(String accountNumber, double initialBalance)`,
   `getBalance()`, and:
   - `deposit(double amount)`: a non-positive amount throws
     `InvalidAmountException("Deposit amount must be positive")`;
   - `withdraw(double amount)`: a non-positive amount throws
     `InvalidAmountException("Withdrawal amount must be positive")`; more than
     the balance throws
     `InsufficientFundsException("Insufficient funds for withdrawal", amount, balance)`.

| on an account opened with `1000.0` | result |
|---|---|
| `deposit(500.0)` | balance `1500.0` |
| `withdraw(500.0)` | balance `500.0` |
| `withdraw(1500.0)` | throws `InsufficientFundsException`, requested `1500.0`, balance `1000.0` |
| `deposit(-100.0)` | throws `InvalidAmountException("Deposit amount must be positive")` |

A caller should be able to build a precise reply from the exception without
parsing its message, and an account that refused an operation must be exactly
as it was. Mind both ends of every range.
