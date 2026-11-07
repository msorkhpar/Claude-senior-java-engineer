Branch offices upload a plain-text file of shipment statuses, one per line, and
`StatusUpload.statuses(...)` turns it into a list. An unknown status rejects the whole upload, so
a typo is caught before anything is booked.

The parser was written and tested on Linux. Since the offices started uploading, support keeps
forwarding rejections that quote a status that looks right: `unknown status 'SHIPPED'`. Other
uploads are rejected at a line that looks empty in the office's editor. The office in Osaka,
whose files come from a Japanese spreadsheet export, has never had an upload accepted.

Finish the two `TODO`s in `src/main/java/practice/StatusUpload.java`: `lines(...)` and
`carriesNoStatus(...)`. Any upload that holds only known statuses and lines without a status
must be accepted, whichever system wrote it. About two lines in total.
