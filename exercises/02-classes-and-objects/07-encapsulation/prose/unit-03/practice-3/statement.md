The web tier keeps a guest's `Reservation` in the session, and the session store writes sessions
out as serialized objects and reads them back on whichever node takes the next request. Since
last month's release the price page sometimes shows a stay for 0 nights or for 9 guests, values
the constructor has always refused. The session store logs a stream it can't read as an
`IOException` and starts a fresh session; anything else it lets through to the error page.

Finish `readObject` in `src/main/java/practice/Reservation.java`, so that a reservation read
back from a stream holds to the same rules as one built with the constructor, and a stream that
breaks them reaches the session store as a stream it can't read.

About five lines in total.
