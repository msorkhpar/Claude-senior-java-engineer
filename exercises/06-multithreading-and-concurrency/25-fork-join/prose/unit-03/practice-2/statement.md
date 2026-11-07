The shop's morning e-mail names yesterday's best-selling product: the one with the most units over
all of the day's order lines. A product can appear on any number of lines, anywhere in the day. A
busy day has tens of millions of lines, so the count has been given a `ForkJoinPool` and a
threshold.

Write `top` in `src/main/java/practice/BestSeller.java`, with the `RecursiveTask` it runs as a
nested class. When you're done:

- `top` returns the product with the most units, and how many it sold, for any threshold;
- when two products sold the same number of units, the smaller product id wins.

About twenty-five lines in total, most of them the task class. The tests run `BestSeller` on a
pool of three workers, with days of 1 to about 3,000 order lines.
