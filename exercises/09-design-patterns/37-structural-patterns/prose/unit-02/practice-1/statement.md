The warehouse service sends stock reports to its branch servers over long-lived connections, one
gzip-compressed message after another on the same stream. When `send(...)` returns, the server
writes the next message on that connection. Since the reports moved to gzip, some branches see
the next message fail on a closed connection, and others get a report that is cut short.

`ReportResponder.send(...)` already builds the layers and writes the rows. Finish `complete(...)`
in `src/main/java/practice/ReportResponder.java`, so that the branch receives every row of a
short or a long report as one complete gzip body, and the connection is still usable afterwards.
About two lines in total.
