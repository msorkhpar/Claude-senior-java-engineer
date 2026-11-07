The forum's outline view lists every post of a discussion with how deep it sits in the thread.
One discussion has turned into a game: each new post answers the previous one, and it is now
hundreds of thousands of posts deep. Opening its outline kills the request with a
`StackOverflowError`.

Write `lines(...)` in `src/main/java/practice/Outline.java`. Its comment gives the format and the
order. When you're done, the outline of any discussion comes out in reading order with the right
depth on every line, however deep it goes, even on a thread with a small stack. About ten lines
in total.
