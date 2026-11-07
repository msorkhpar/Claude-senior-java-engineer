Ticket attachments are downloaded with `GET /attachments?name=T-118/screenshot.txt`, and the name is
used to find the file under the store's directory. Next to that directory, on the same disk, are
the service's configuration, an export area, and an older store. Someone has also put a link
inside the store that leads to the export area. On some hosts the store's directory is configured
through a link of its own.

Write `locate(name)` in `src/main/java/practice/AttachmentStore.java`. A name that leads to one of
the store's files returns that file, however the name is spelled. A name that would lead to any
other file is refused with a `SecurityException`. The store must keep working on the hosts that
configure it through a link.

About four lines in total. The tests build the directories and links in a temporary directory.
