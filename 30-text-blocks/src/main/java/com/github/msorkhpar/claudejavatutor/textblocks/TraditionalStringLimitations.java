package com.github.msorkhpar.claudejavatutor.textblocks;

/**
 * Demonstrates the limitations of traditional string literals in Java
 * and motivates the introduction of text blocks (JEP 378).
 */
public class TraditionalStringLimitations {

    /**
     * Builds a JSON string using traditional string concatenation.
     * Demonstrates verbosity with escape sequences and concatenation.
     */
    public static String buildJsonTraditional(String name, int age) {
        return "{\n" +
                "    \"name\": \"" + name + "\",\n" +
                "    \"age\": " + age + "\n" +
                "}";
    }

    /**
     * Builds a JSON string using a text block for comparison.
     */
    public static String buildJsonTextBlock(String name, int age) {
        return """
                {
                    "name": "%s",
                    "age": %d
                }""".formatted(name, age);
    }

    /**
     * Builds an HTML snippet using traditional string literals.
     */
    public static String buildHtmlTraditional(String title, String body) {
        return "<html>\n" +
                "    <head>\n" +
                "        <title>" + title + "</title>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <p>" + body + "</p>\n" +
                "    </body>\n" +
                "</html>";
    }

    /**
     * Builds an HTML snippet using a text block.
     */
    public static String buildHtmlTextBlock(String title, String body) {
        return """
                <html>
                    <head>
                        <title>%s</title>
                    </head>
                    <body>
                        <p>%s</p>
                    </body>
                </html>""".formatted(title, body);
    }

    /**
     * Builds a SQL query using traditional concatenation.
     */
    public static String buildSqlTraditional(String table, String column, String value) {
        return "SELECT *\n" +
                "FROM " + table + "\n" +
                "WHERE " + column + " = '" + value + "'\n" +
                "ORDER BY id;";
    }

    /**
     * Builds a SQL query using a text block.
     */
    public static String buildSqlTextBlock(String table, String column, String value) {
        return """
                SELECT *
                FROM %s
                WHERE %s = '%s'
                ORDER BY id;""".formatted(table, column, value);
    }

    /**
     * Demonstrates a string with many escape characters (traditional).
     */
    public static String buildEscapeHeavyTraditional() {
        return "He said, \"Hello, World!\" and then typed:\n" +
                "\tSystem.out.println(\"test\");\n" +
                "\tPath: C:\\Users\\admin\\docs";
    }

    /**
     * Demonstrates readability improvement for regex using traditional literals.
     * Regex patterns require double-escaping in traditional strings.
     */
    public static String getEmailRegexTraditional() {
        return "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    }

    /**
     * Returns the same regex pattern. Note: text blocks don't reduce regex escaping
     * since backslash is still an escape character inside text blocks.
     */
    public static String getEmailRegexTextBlock() {
        return """
                ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$""";
    }

    /**
     * Demonstrates multiline string that requires counting escape characters.
     */
    public static String buildMultilineWithTabsTraditional() {
        return "Line 1\n\tIndented Line 2\n\t\tDouble Indented Line 3";
    }
}
