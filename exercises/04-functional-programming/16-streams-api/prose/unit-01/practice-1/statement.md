The dispatcher decides which courier gets each delivery job, and the shop page asks it whether a
zone can be promised delivery within the hour. Both questions go to the courier service, which
allows a fixed number of requests per minute. Since the dispatcher was rewritten, the service
has started refusing requests at lunchtime, when the zone lists are longest.

`Dispatcher` already assigns jobs and answers the shop page. Finish the two `TODO`s in
`src/main/java/practice/Dispatcher.java`:

- `nearestFree(...)`: the first free courier of a list sorted nearest first;
- `anyFree(...)`: whether anyone in a list is free.

When you're done, both give the same answers as before, and neither asks the service about a
courier whose answer can no longer change the result. About two lines in total.
