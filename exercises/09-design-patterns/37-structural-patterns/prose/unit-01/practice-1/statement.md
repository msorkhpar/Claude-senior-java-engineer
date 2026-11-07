A vendor's sensor library samples a production line and writes each reading into an `int[]`
buffer that you allocate and hand to it; it keeps writing into the same buffer for as long as the
line runs. The analytics code takes a `List<Integer>`: it reads the latest samples, smooths
outliers by writing corrected values back where the library will pick them up, and sorts or
reverses the readings in place with `Collections`. Today the service copies the buffer into a new
list every second, and the corrections never reach the library.

Finish `src/main/java/practice/IntArrayList.java`, a `List<Integer>` over the buffer: the
constructor, `get`, `set` and `size`. `AbstractList` supplies the rest, and refuses `add` and
`remove`, since the buffer can't grow. When you're done, the list and the buffer stay the same
data in both directions, and every `java.util.Collections` algorithm that changes a list works on
it. About six lines in total.
