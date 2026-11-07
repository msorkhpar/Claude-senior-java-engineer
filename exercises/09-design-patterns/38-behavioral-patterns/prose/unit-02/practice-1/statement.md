The CI server tells its plug-ins about every finished build through `BuildEvents`. The
release-notes plug-in only wants the first green build after a release branch is cut, so it
unsubscribes itself from inside its callback. Since it shipped, two things happened: on the day
it was installed first, the build event failed and no plug-in after it heard that build; on a
server where it was installed second, the dashboard silently lost one build.

`subscribe`, `unsubscribe` and `listenerCount` work. Finish `publish(String buildId)` in
`src/main/java/practice/BuildEvents.java` so that every plug-in subscribed when the event starts
hears it, wherever a plug-in that leaves sits in the list, and
`publish` never fails because of it. About three lines in total.
