The invoicing service runs one router for many tenants. Each tenant gets its own handler,
`new InvoiceHandler("northwind")`, whose `paths()` builds `/northwind/invoices` and
`/northwind/invoices/{id}` from the tenant it was given. The first version of the new `Handler`
base class built each handler's routes as the handler was created, and every tenant's handler
registered `/null/invoices`: every invoice request got a 404. The router asks each handler for
its `routes()` when it registers it, and again on every configuration reload, a few times a
minute.

Finish `routes()` in `src/main/java/practice/Handler.java`. When you're done, every handler
serves the paths its own `paths()` builds from its own fields, and a reload hands back the same
routes without making the handler build its paths again. Subclasses of `Handler` stay as they
are. About four lines in total.
