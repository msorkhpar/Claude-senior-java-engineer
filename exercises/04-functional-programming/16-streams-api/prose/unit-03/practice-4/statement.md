Operations staff page through the company's warehouse codes in the admin console, a few codes per
page, in alphabetical order. They report that a page sometimes lists codes in no recognisable
order, that the same page number shows different codes from one visit to the next, and that some
warehouses never appear on any page.

The console runs on several instances behind a load balancer, and each one gets the codes from the
inventory service as a `Set` that promises no order. Finish the `TODO` in
`src/main/java/practice/WarehouseDirectory.java`: `page(page, size)` returns page `page` (counted
from 0) of `size` codes in alphabetical order, so that paging from the first page to the last
shows every code exactly once, whichever instance serves each page.

About three lines.
