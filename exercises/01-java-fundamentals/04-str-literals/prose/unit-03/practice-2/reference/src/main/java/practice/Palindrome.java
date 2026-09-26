package practice;

public final class Palindrome {

    private Palindrome() {
    }

    /** Says whether s is a palindrome, ignoring case. */
    public static boolean isPalindrome(String s) {
        for (int i = 0, j = s.length() - 1; i < j; i++, j--) {
            if (Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(s.charAt(j))) {
                return false;
            }
        }
        return true;
    }
}
