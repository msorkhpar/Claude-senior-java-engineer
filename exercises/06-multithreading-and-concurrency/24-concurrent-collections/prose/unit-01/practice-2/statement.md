Tenant settings are read by every request thread and changed from the admin page, so they live in
a `ConcurrentHashMap`. Tenants can now **clear** a setting, and that is not the same as never
setting it: a tenant who clears the invoice footer wants no footer, while a tenant who never set
one gets the platform's default footer.

Finish the three `TODO` methods in `src/main/java/practice/TenantSettings.java`:

- `encode(value)`: what to store for a value the tenant set, `null` included;
- `decode(stored)`: the tenant's value back from what `encode` stored;
- `isSet(name)`: whether the tenant ever set the setting, cleared or not.

Any text a tenant types must read back exactly as typed, whatever it is. About five lines in total.
The tests use one thread.
