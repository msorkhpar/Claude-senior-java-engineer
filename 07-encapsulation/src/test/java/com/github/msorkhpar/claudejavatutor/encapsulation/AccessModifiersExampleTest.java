package com.github.msorkhpar.claudejavatutor.encapsulation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AccessModifiersExampleTest {

    @Test
    void testPublicAccess() {
        AccessModifiersExample example = new AccessModifiersExample();
        assertThat(example.publicField).isEqualTo(1);
        example.publicMethod(); // This should compile and run without issues
    }

    @Test
    void testProtectedAccess() {
        AccessModifiersExample example = new AccessModifiersExample();
        assertThat(example.protectedField).isEqualTo(2);
        example.protectedMethod(); // This should compile and run without issues as we're in the same package
    }

    @Test
    void testDefaultAccess() {
        AccessModifiersExample example = new AccessModifiersExample();
        assertThat(example.defaultField).isEqualTo(3);
        example.defaultMethod(); // This should compile and run without issues as we're in the same package
    }

    @Test
    void testPrivateAccess() {
        AccessModifiersExample example = new AccessModifiersExample();
        // The line below would not compile if uncommented
        // example.privateField = 5;
        
        // The line below would not compile if uncommented
        // example.privateMethod();
        
        // We can indirectly test private method by calling a public method that uses it
        example.callPrivateMethod(); // This should run without issues
    }

    @Test
    void testDefaultAccessClass() {
        DefaultAccessClass defaultClass = new DefaultAccessClass();
        AccessModifiersExample example = new AccessModifiersExample();
        defaultClass.testDefaultAccess(example); // This should compile and run without issues
    }

    @Test
    void testInnerClasses() {
        AccessModifiersExample example = new AccessModifiersExample();
        
        AccessModifiersExample.PublicInnerClass publicInner = example.new PublicInnerClass();
        AccessModifiersExample.ProtectedInnerClass protectedInner = example.new ProtectedInnerClass();
        AccessModifiersExample.DefaultInnerClass defaultInner = example.new DefaultInnerClass();
        
        // The line below would not compile if uncommented
        // AccessModifiersExample.PrivateInnerClass privateInner = example.new PrivateInnerClass();
        
        assertThat(publicInner).isNotNull();
        assertThat(protectedInner).isNotNull();
        assertThat(defaultInner).isNotNull();
    }
}