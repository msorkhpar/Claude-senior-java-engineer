A method declaration names its access modifier, return type, name, parameters, the
exceptions it may throw, and its body:

```java
accessModifier returnType methodName(parameterList) throws exceptionList {
    // method body
}
```

Dividing two `double`s never throws in Java: `10.0 / 0` is `Infinity` and `0.0 / 0` is
`NaN`. The course's `divide` refuses a zero denominator itself.

Write `public static double divide(double numerator, double denominator) throws
ArithmeticException` in `Divider`:

- it returns `numerator / denominator`: `divide(10, 2)` is `5.0` and `divide(1, 3)` is
  about `0.3333`;
- a denominator of `0` (or `-0.0`) throws `ArithmeticException` with the message
  `"Cannot divide by zero"`, whatever the numerator, `0` included.
