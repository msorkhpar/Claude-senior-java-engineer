The league site shows standings in three places. The club pages pass the teams in fixture order
and draw the fixture list from that same list afterwards. The results importer passes a list it
keeps adding teams to as results come in. The public API passes a list that nobody may change.

Finish `table(...)` in `src/main/java/practice/Standings.java`: the teams by points, most first,
with teams on the same points in the order they were given. Every caller must get its table and
find its own list as it left it, and a table it already holds must not change when its list does
later. About three lines in total.
