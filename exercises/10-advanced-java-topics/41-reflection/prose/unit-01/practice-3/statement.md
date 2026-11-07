The gateway reads its configuration into plain settings objects: a line such as
`listenPorts=8443,9443` fills the list field `listenPorts`. A release that bound a fresh
`GatewaySettings` started fine and then failed on the first connection with a
`ClassCastException` in code that reads `int port = settings.listenPorts.get(0)`.

`SettingsBinder` already finds the field, clears the list and converts each item. Finish
`elementType(...)` in `src/main/java/practice/SettingsBinder.java`: the type every item of that
field's list must have.

Settings classes declare their lists as `List<Something>`, or as a named class of their own such
as `class PortList extends ArrayList<Integer>`, and a list may be empty before binding. When
you're done, every item ends up as the declared element type in all of these cases. About four
lines in total.
