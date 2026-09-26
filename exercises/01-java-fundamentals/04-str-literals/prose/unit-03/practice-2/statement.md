`charAt` reads one character in constant time, from index `0` to `length() - 1`. Two indices
walking toward each other, one from each end, check a palindrome without building any new
String.

Write `isPalindrome(String s)` in `Palindrome`. It says whether `s` reads the same forwards
and backwards, ignoring upper and lower case:

- `isPalindrome("level")` and `isPalindrome("abba")` are `true`; `isPalindrome("hello")` is
  `false`;
- case does not matter: `isPalindrome("Level")` is `true`;
- `isPalindrome("")` and `isPalindrome("x")` are `true`.
