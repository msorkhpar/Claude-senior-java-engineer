# 7.2. Enhanced Enums

Java enums cannot be generic. JEP 301 ("Enhanced Enums") proposed generic enums and sharper typing of enum constants,
but it was withdrawn and never shipped, so `enum Setting<T>` does not compile in Java 21. This module teaches the
workarounds Java actually supports: enums with constructor parameters and constant-specific bodies, generic methods on
enums, `Class<T>` tokens, and sealed interfaces with records.

## Contents

1. [7.2.1. Limitations of Traditional Enums](README_7.2.1.md)
2. [7.2.2. Declaring Generic Enums](README_7.2.2.md)
3. [7.2.3. Parameterized Enum Constants](README_7.2.3.md)
4. [7.2.4. Generic Methods in Enums](README_7.2.4.md)
5. [7.2.5. Use Cases and Examples](README_7.2.5.md)
