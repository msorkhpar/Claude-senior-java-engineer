The page's interview answer hides a bank account's number and balance in private
fields. The only way to change the balance is through methods that check every
request first. Write the course's `BankAccount`:

- `BankAccount(String accountNumber, double initialBalance)`:
  - a `null`, empty or blank number is refused with
    `IllegalArgumentException("Account number cannot be null or empty")`;
  - a negative opening balance is refused with
    `IllegalArgumentException("Initial balance cannot be negative")`.
- `deposit(double amount)`: the amount must be positive, otherwise
  `IllegalArgumentException("Deposit amount must be positive")`.
- `withdraw(double amount)`: the amount must be positive, otherwise
  `IllegalArgumentException("Withdrawal amount must be positive")`. An amount
  larger than the balance is a different failure: the request is well formed, but
  the account cannot serve it. Refuse it with
  `IllegalStateException("Insufficient funds")`.
- `getAccountNumber()` and `getBalance()`.

A refused request changes nothing.

Examples:

```
BankAccount a = new BankAccount("123456", 1000.0);
a.deposit(500.0);   a.getBalance()   -> 1500.0
a.withdraw(300.0);  a.getBalance()   -> 1200.0
a.withdraw(5000.0)                   -> IllegalStateException("Insufficient funds")
new BankAccount("  ", 10.0)          -> IllegalArgumentException
```
