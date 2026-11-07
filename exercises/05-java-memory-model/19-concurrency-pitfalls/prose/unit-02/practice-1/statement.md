The app's launch gives a voucher to the first sign-ups, as many as the campaign's `Quota` allows.
Vouchers are numbered in the order they are granted, and each one is emailed to its customer.
After launch morning, support had three kinds of tickets: customers with two voucher emails,
two customers holding the same voucher number, and more vouchers redeemed than the campaign
allowed. On a quiet staging server none of it happens.

Finish `claim(...)` in `src/main/java/practice/LaunchVouchers.java`. Sign-ups arrive on many request
threads at once. The quota check sometimes calls the campaign service and takes a while, and one
slow check must not hold up other sign-ups. When you're done, every granted voucher has its own
number and exactly one email, and no more are granted than the quota allows. About seven lines in
total.

The tests make one customer's quota check slow and let another customer sign up while it waits.
