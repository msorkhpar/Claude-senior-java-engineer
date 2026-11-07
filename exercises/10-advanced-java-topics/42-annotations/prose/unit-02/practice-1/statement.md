The billing API marks each endpoint with the permissions that let a caller in:

```java
@Permission("invoices:write")
@Permission("billing:admin")
public void issueCredit() { ... }
```

Holding any one of them is enough. A method that lists none uses its class's permissions, and an
endpoint with none on the method or its class is public. The penetration test found that a caller
with no billing permissions at all can call some protected endpoints, while others refuse them as
they should.

`PermissionGuard.mayCall(...)` already applies those rules to whatever `required(...)` returns.
Finish `required(Method endpoint)` in `src/main/java/practice/PermissionGuard.java`: it returns
the permissions that let a caller into the endpoint, for an endpoint or a class that lists one
permission or several. About five lines in total.
