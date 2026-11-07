The shop's checkout applies price rules named in its config, such as
`practice.Seasonal#winterSale`: a class and one of its public methods that takes a price in
cents and returns the new price in cents. The checkout binds every rule once when it starts and
then applies it to every basket, so the binding is where a wrong entry should be caught. Last
month an entry named a method that returns an `int`, and it was accepted: baskets went through
with prices cut to whole euros until someone noticed.

`PriceRules.bind` already creates the rule's object and wraps the call. Finish the two `TODO`s in
`src/main/java/practice/PriceRules.java` with `java.lang.invoke` method handles:

- `lookup(type, methodName)`: the handle for the rule method;
- the call in `call(handle, target, cents)`.

When you're done, a bound rule returns what its method returns for any price, and an entry whose
method doesn't exist or doesn't take and return a price in cents is refused when it is bound,
with an `IllegalArgumentException` that names the method. About seven lines in total.
