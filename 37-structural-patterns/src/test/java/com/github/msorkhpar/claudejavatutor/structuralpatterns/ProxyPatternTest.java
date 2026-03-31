package com.github.msorkhpar.claudejavatutor.structuralpatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Proxy Pattern Tests")
class ProxyPatternTest {

    @Nested
    @DisplayName("RealImageService")
    class RealImageServiceTest {

        @Test
        @DisplayName("Should load and display image")
        void testDisplayImage() {
            var service = new ProxyPattern.RealImageService("photo.jpg");

            assertThat(service.display()).isEqualTo("Displaying image: photo.jpg");
        }

        @Test
        @DisplayName("Should return correct filename")
        void testGetFilename() {
            var service = new ProxyPattern.RealImageService("photo.jpg");

            assertThat(service.getFilename()).isEqualTo("photo.jpg");
        }

        @Test
        @DisplayName("Should calculate size based on filename")
        void testGetSize() {
            var service = new ProxyPattern.RealImageService("photo.jpg");

            assertThat(service.getSize()).isEqualTo("photo.jpg".length() * 1024L);
        }

        @Test
        @DisplayName("Should be loaded immediately after construction")
        void testIsLoadedImmediately() {
            var service = new ProxyPattern.RealImageService("photo.jpg");

            assertThat(service.isLoaded()).isTrue();
        }

        @Test
        @DisplayName("Should throw NullPointerException for null filename")
        void testNullFilename() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ProxyPattern.RealImageService(null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank filename")
        void testBlankFilename() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ProxyPattern.RealImageService("   "));
        }
    }

    @Nested
    @DisplayName("VirtualImageProxy - Lazy Loading")
    class VirtualImageProxyTest {

        @Test
        @DisplayName("Should not load real service until needed")
        void testLazyLoading() {
            var proxy = new ProxyPattern.VirtualImageProxy("heavy-image.png");

            assertThat(proxy.isLoaded()).isFalse();
        }

        @Test
        @DisplayName("Should load real service on display")
        void testLoadsOnDisplay() {
            var proxy = new ProxyPattern.VirtualImageProxy("heavy-image.png");

            proxy.display();

            assertThat(proxy.isLoaded()).isTrue();
        }

        @Test
        @DisplayName("Should return filename without loading real service")
        void testFilenameWithoutLoading() {
            var proxy = new ProxyPattern.VirtualImageProxy("heavy-image.png");

            String filename = proxy.getFilename();

            assertThat(filename).isEqualTo("heavy-image.png");
            assertThat(proxy.isLoaded()).isFalse();
        }

        @Test
        @DisplayName("Should return same result as real service")
        void testSameResultAsRealService() {
            var proxy = new ProxyPattern.VirtualImageProxy("photo.jpg");
            var real = new ProxyPattern.RealImageService("photo.jpg");

            assertThat(proxy.display()).isEqualTo(real.display());
            assertThat(proxy.getSize()).isEqualTo(real.getSize());
        }

        @Test
        @DisplayName("Should load real service only once")
        void testSingleLoading() {
            var proxy = new ProxyPattern.VirtualImageProxy("photo.jpg");

            proxy.display();
            proxy.display();
            proxy.getSize();

            assertThat(proxy.isLoaded()).isTrue();
        }

        @Test
        @DisplayName("Should throw NullPointerException for null filename")
        void testNullFilename() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ProxyPattern.VirtualImageProxy(null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank filename")
        void testBlankFilename() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ProxyPattern.VirtualImageProxy("  "));
        }
    }

    @Nested
    @DisplayName("ProtectionDocumentProxy - Access Control")
    class ProtectionDocumentProxyTest {

        @Test
        @DisplayName("GUEST should be able to read documents")
        void testGuestCanRead() {
            var realService = new ProxyPattern.RealDocumentService();
            var guest = new ProxyPattern.User("Guest", ProxyPattern.User.Role.GUEST);
            var proxy = new ProxyPattern.ProtectionDocumentProxy(realService, guest);

            assertThat(proxy.read("doc1")).isEqualTo("Public document content");
        }

        @Test
        @DisplayName("GUEST should NOT be able to write documents")
        void testGuestCannotWrite() {
            var realService = new ProxyPattern.RealDocumentService();
            var guest = new ProxyPattern.User("Guest", ProxyPattern.User.Role.GUEST);
            var proxy = new ProxyPattern.ProtectionDocumentProxy(realService, guest);

            assertThatExceptionOfType(SecurityException.class)
                    .isThrownBy(() -> proxy.write("doc1", "hacked"))
                    .withMessageContaining("GUEST")
                    .withMessageContaining("cannot write");
        }

        @Test
        @DisplayName("GUEST should NOT be able to delete documents")
        void testGuestCannotDelete() {
            var realService = new ProxyPattern.RealDocumentService();
            var guest = new ProxyPattern.User("Guest", ProxyPattern.User.Role.GUEST);
            var proxy = new ProxyPattern.ProtectionDocumentProxy(realService, guest);

            assertThatExceptionOfType(SecurityException.class)
                    .isThrownBy(() -> proxy.delete("doc1"))
                    .withMessageContaining("cannot delete");
        }

        @Test
        @DisplayName("USER should be able to read and write")
        void testUserCanReadAndWrite() {
            var realService = new ProxyPattern.RealDocumentService();
            var user = new ProxyPattern.User("Editor", ProxyPattern.User.Role.USER);
            var proxy = new ProxyPattern.ProtectionDocumentProxy(realService, user);

            assertThat(proxy.read("doc1")).isEqualTo("Public document content");
            assertThat(proxy.write("doc3", "New content")).isEqualTo("Written to doc3");
        }

        @Test
        @DisplayName("USER should NOT be able to delete")
        void testUserCannotDelete() {
            var realService = new ProxyPattern.RealDocumentService();
            var user = new ProxyPattern.User("Editor", ProxyPattern.User.Role.USER);
            var proxy = new ProxyPattern.ProtectionDocumentProxy(realService, user);

            assertThatExceptionOfType(SecurityException.class)
                    .isThrownBy(() -> proxy.delete("doc1"))
                    .withMessageContaining("USER")
                    .withMessageContaining("cannot delete");
        }

        @Test
        @DisplayName("ADMIN should be able to read, write, and delete")
        void testAdminFullAccess() {
            var realService = new ProxyPattern.RealDocumentService();
            var admin = new ProxyPattern.User("Admin", ProxyPattern.User.Role.ADMIN);
            var proxy = new ProxyPattern.ProtectionDocumentProxy(realService, admin);

            assertThat(proxy.read("doc1")).isEqualTo("Public document content");
            assertThat(proxy.write("doc3", "Admin content")).isEqualTo("Written to doc3");
            assertThat(proxy.delete("doc1")).isEqualTo("Deleted doc1");
        }

        @Test
        @DisplayName("Should throw NoSuchElementException for non-existent document")
        void testNonExistentDocument() {
            var realService = new ProxyPattern.RealDocumentService();
            var admin = new ProxyPattern.User("Admin", ProxyPattern.User.Role.ADMIN);
            var proxy = new ProxyPattern.ProtectionDocumentProxy(realService, admin);

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> proxy.read("nonexistent"));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null real service")
        void testNullRealService() {
            var admin = new ProxyPattern.User("Admin", ProxyPattern.User.Role.ADMIN);

            assertThatNullPointerException()
                    .isThrownBy(() -> new ProxyPattern.ProtectionDocumentProxy(null, admin));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null user")
        void testNullUser() {
            var realService = new ProxyPattern.RealDocumentService();

            assertThatNullPointerException()
                    .isThrownBy(() -> new ProxyPattern.ProtectionDocumentProxy(realService, null));
        }
    }

    @Nested
    @DisplayName("CachingDataLookupProxy - Smart Reference")
    class CachingDataLookupProxyTest {

        @Test
        @DisplayName("Should cache lookup results")
        void testCaching() {
            var realService = new ProxyPattern.RealDataLookupService();
            var proxy = new ProxyPattern.CachingDataLookupProxy(realService);

            String first = proxy.lookup("key1");
            String second = proxy.lookup("key1");

            assertThat(first).isEqualTo(second);
            assertThat(realService.getLookupCount()).isEqualTo(1); // Only one actual lookup
        }

        @Test
        @DisplayName("Should perform separate lookups for different keys")
        void testDifferentKeys() {
            var realService = new ProxyPattern.RealDataLookupService();
            var proxy = new ProxyPattern.CachingDataLookupProxy(realService);

            proxy.lookup("key1");
            proxy.lookup("key2");

            assertThat(realService.getLookupCount()).isEqualTo(2);
            assertThat(proxy.getCacheSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should report cache status correctly")
        void testIsCached() {
            var realService = new ProxyPattern.RealDataLookupService();
            var proxy = new ProxyPattern.CachingDataLookupProxy(realService);

            assertThat(proxy.isCached("key1")).isFalse();

            proxy.lookup("key1");

            assertThat(proxy.isCached("key1")).isTrue();
            assertThat(proxy.isCached("key2")).isFalse();
        }

        @Test
        @DisplayName("Should clear cache")
        void testClearCache() {
            var realService = new ProxyPattern.RealDataLookupService();
            var proxy = new ProxyPattern.CachingDataLookupProxy(realService);

            proxy.lookup("key1");
            proxy.clearCache();

            assertThat(proxy.getCacheSize()).isZero();
            assertThat(proxy.isCached("key1")).isFalse();
        }

        @Test
        @DisplayName("Should re-fetch after cache clear")
        void testReFetchAfterClear() {
            var realService = new ProxyPattern.RealDataLookupService();
            var proxy = new ProxyPattern.CachingDataLookupProxy(realService);

            proxy.lookup("key1");
            proxy.clearCache();
            proxy.lookup("key1");

            assertThat(realService.getLookupCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("LoggingDataLookupProxy")
    class LoggingDataLookupProxyTest {

        @Test
        @DisplayName("Should log all lookups")
        void testLogging() {
            var realService = new ProxyPattern.RealDataLookupService();
            var proxy = new ProxyPattern.LoggingDataLookupProxy(realService);

            proxy.lookup("key1");
            proxy.lookup("key2");

            assertThat(proxy.getAccessLog()).containsExactly(
                    "lookup(key1)",
                    "lookup(key2)"
            );
        }

        @Test
        @DisplayName("Should return unmodifiable log")
        void testUnmodifiableLog() {
            var proxy = new ProxyPattern.LoggingDataLookupProxy(new ProxyPattern.RealDataLookupService());

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> proxy.getAccessLog().add("hacked"));
        }

        @Test
        @DisplayName("Should still delegate to real service")
        void testDelegation() {
            var realService = new ProxyPattern.RealDataLookupService();
            var proxy = new ProxyPattern.LoggingDataLookupProxy(realService);

            String result = proxy.lookup("key1");

            assertThat(result).isEqualTo("Result for: key1");
        }
    }

    @Nested
    @DisplayName("Dynamic Proxy - java.lang.reflect.Proxy")
    class DynamicProxyTest {

        @Test
        @DisplayName("Should create dynamic logging proxy for DataLookupService")
        void testDynamicProxy() {
            var realService = new ProxyPattern.RealDataLookupService();
            var handler = new ProxyPattern.LoggingInvocationHandler(realService);
            var proxy = ProxyPattern.createLoggingProxy(realService,
                    ProxyPattern.DataLookupService.class, handler);

            String result = proxy.lookup("test-key");

            assertThat(result).isEqualTo("Result for: test-key");
            assertThat(handler.getInvocationLog()).hasSize(1);
            assertThat(handler.getInvocationLog().get(0)).contains("lookup");
        }

        @Test
        @DisplayName("Should log multiple invocations")
        void testMultipleInvocations() {
            var realService = new ProxyPattern.RealDataLookupService();
            var handler = new ProxyPattern.LoggingInvocationHandler(realService);
            var proxy = ProxyPattern.createLoggingProxy(realService,
                    ProxyPattern.DataLookupService.class, handler);

            proxy.lookup("key1");
            proxy.lookup("key2");
            proxy.lookup("key3");

            assertThat(handler.getInvocationLog()).hasSize(3);
        }

        @Test
        @DisplayName("Should return unmodifiable invocation log")
        void testUnmodifiableInvocationLog() {
            var handler = new ProxyPattern.LoggingInvocationHandler(new ProxyPattern.RealDataLookupService());

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> handler.getInvocationLog().add("hacked"));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null target")
        void testNullTarget() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ProxyPattern.LoggingInvocationHandler(null));
        }
    }

    @Nested
    @DisplayName("User Record")
    class UserRecordTest {

        @Test
        @DisplayName("Should create user with valid data")
        void testValidUser() {
            var user = new ProxyPattern.User("Alice", ProxyPattern.User.Role.ADMIN);

            assertThat(user.name()).isEqualTo("Alice");
            assertThat(user.role()).isEqualTo(ProxyPattern.User.Role.ADMIN);
        }

        @Test
        @DisplayName("Should throw NullPointerException for null name")
        void testNullName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ProxyPattern.User(null, ProxyPattern.User.Role.USER));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null role")
        void testNullRole() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ProxyPattern.User("Alice", null));
        }
    }
}
