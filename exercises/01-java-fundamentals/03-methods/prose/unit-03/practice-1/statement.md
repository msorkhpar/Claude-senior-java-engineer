A method cannot swap two `int` variables of its caller: it receives copies of their values.
It can swap two elements of an array, because it receives a copy of the reference to the
caller's own array, and both references point to the same elements. Assigning a new array
to the parameter, on the other hand, only changes the method's copy:

```java
static void changeArray(int[] a) {
    a[0] = 10;                // the caller sees this
    a = new int[]{4, 5, 6};   // the caller does not
}
```

Write `swap(int[] values, int i, int j)` in `Swapper`. It swaps `values[i]` and `values[j]`
in the caller's array and returns nothing:

- with `values = {1, 2, 3}`, `swap(values, 0, 2)` leaves `values` as `{3, 2, 1}`;
- swapping an element with itself changes nothing: `swap(values, 1, 1)` keeps `{3, 2, 1}`.
