This continues the parking permits of the previous practice: `Permits` now starts from its
solution, and every permit also has a code printed on it. Residents read the code out to support on
the phone, and support finds the car with `carWithPermit(...)`. Until now a code was compared the way
a plate is, so `codeKey` simply returned `plateKey(code)`.

Support has a request. Codes are printed in a font where O and 0, and I and 1, look alike, and
residents read them either way. Codes are never issued with the letters O or I, so in a code, an O
always means 0 and an I always means 1. Plates are different: `LO-404-D` and `L0-404-D` are two
cars, and both exist.

Finish the `TODO` in `src/main/java/practice/Permits.java`, `codeKey(...)`, and change anything else
in the file you need to. When you're done, support finds the car from a code however the resident
reads it out, and every car keeps its own permit. About two lines in total.
