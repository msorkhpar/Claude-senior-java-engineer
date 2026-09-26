The page's `BankAccount` follows the best practices: exception names that say
what went wrong, context carried in fields, and checks that run before any
state changes. Write it.

1. Complete the checked `InsufficientFundsException(String message, double requestedAmount, double accountBalance)`
   with `getRequestedAmount()` and `getAccountBalance()`.
2. Write `BankAccount(String accountNumber, double initialBalance)`,
   `getBalance()`, and:
   - `deposit(double amount)`: an amount that is not positive (zero, negative,
     or `NaN`) throws
     `InvalidAmountException("Deposit amount must be positive")`;
   - `withdraw(double amount)`: an amount that is not positive throws
     `InvalidAmountException("Withdrawal amount must be positive")`; more than
     the balance throws
     `InsufficientFundsException("Insufficient funds for withdrawal", amount, balance)`.

| on an account opened with `1000.0` | result |
|---|---|
| `deposit(500.0)` | balance `1500.0` |
| `withdraw(500.0)` | balance `500.0` |
| `withdraw(1500.0)` | throws `InsufficientFundsException`, requested `1500.0`, balance `1000.0` |
| `deposit(-100.0)` | throws `InvalidAmountException("Deposit amount must be positive")` |

Opening balances are never negative, so the two checks of `withdraw` never
both apply; their order is up to you. How the exception stores its numbers is
up to you as long as the getters return them.

A caller should be able to build a precise reply from the exception without
parsing its message, and an account that refused an operation must be exactly
as it was. Mind both ends of every range.
