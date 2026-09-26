package practice;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JavaClassTest {

    /** The expected file, joined at run time from its lines; the file ends with a newline. */
    private static String file(String... lines) {
        return String.join("\n", lines) + "\n";
    }

    private static Map<String, String> fields(String... nameThenType) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (int i = 0; i < nameThenType.length; i += 2) {
            fields.put(nameThenType[i], nameThenType[i + 1]);
        }
        return fields;
    }

    @Test
    void generatesTheClass() {
        assertThat(JavaClass.source(new String("com.example"), new String("Point"), fields("x", "int", "y", "int")))
                .doesNotContain(" \n");
        assertThat(JavaClass.source(new String("com.example"), new String("User"), fields("age", "int", "name", "String")))
                .isEqualTo(file("package com.example;", "", "public class User {", "", "    private int age;",
                        "    private String name;", "", "    public User() {", "    }", "}"));
        assertThat(JavaClass.source(new String("org.example.model"), new String("Id"), fields("value", "long")))
                .isEqualTo(file("package org.example.model;", "", "public class Id {", "", "    private long value;",
                        "", "    public Id() {", "    }", "}"));
    }

    @Test
    void fieldsKeepTheGivenOrder() {
        assertThat(JavaClass.source(new String("com.example"), new String("User"), fields("name", "String", "age", "int")))
                .isEqualTo(file("package com.example;", "", "public class User {", "", "    private String name;",
                        "    private int age;", "", "    public User() {", "    }", "}"));
    }

    @Test
    void noFieldsLeavesNoEmptyBlock() {
        assertThat(JavaClass.source(new String("com.example"), new String("Empty"), fields()))
                .isEqualTo(file("package com.example;", "", "public class Empty {", "", "    public Empty() {", "    }", "}"));
    }
}
