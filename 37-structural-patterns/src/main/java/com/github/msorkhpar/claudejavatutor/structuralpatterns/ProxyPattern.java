package com.github.msorkhpar.claudejavatutor.structuralpatterns;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demonstrates the Proxy Pattern — a structural design pattern that provides a substitute
 * or placeholder for another object. A proxy controls access to the original object,
 * allowing you to perform something either before or after the request gets through.
 */
public class ProxyPattern {

    // -----------------------------------------------------------------------
    // Subject interface — the common interface for Real Subject and Proxy
    // -----------------------------------------------------------------------

    /**
     * Service interface that both the real service and the proxy implement.
     */
    public interface ImageService {
        String display();
        String getFilename();
        long getSize();
    }

    // -----------------------------------------------------------------------
    // Real Subject
    // -----------------------------------------------------------------------

    /**
     * The real image service that loads and displays images.
     * Simulates an expensive loading operation.
     */
    public static class RealImageService implements ImageService {

        private final String filename;
        private boolean loaded = false;
        private long size;

        public RealImageService(String filename) {
            this.filename = Objects.requireNonNull(filename, "Filename must not be null");
            if (filename.isBlank()) {
                throw new IllegalArgumentException("Filename must not be blank");
            }
            loadFromDisk();
        }

        private void loadFromDisk() {
            // Simulate expensive loading
            this.size = filename.length() * 1024L;
            this.loaded = true;
        }

        public boolean isLoaded() {
            return loaded;
        }

        @Override
        public String display() {
            return "Displaying image: " + filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public long getSize() {
            return size;
        }
    }

    // -----------------------------------------------------------------------
    // Virtual Proxy — lazy initialization
    // -----------------------------------------------------------------------

    /**
     * Virtual proxy that delays creation of the real image service until it is actually needed.
     * This is useful when creating the real object is expensive.
     */
    public static class VirtualImageProxy implements ImageService {

        private final String filename;
        private RealImageService realService;

        public VirtualImageProxy(String filename) {
            this.filename = Objects.requireNonNull(filename, "Filename must not be null");
            if (filename.isBlank()) {
                throw new IllegalArgumentException("Filename must not be blank");
            }
        }

        private RealImageService getRealService() {
            if (realService == null) {
                realService = new RealImageService(filename);
            }
            return realService;
        }

        public boolean isLoaded() {
            return realService != null;
        }

        @Override
        public String display() {
            return getRealService().display();
        }

        @Override
        public String getFilename() {
            return filename; // No need to load the real service for this
        }

        @Override
        public long getSize() {
            return getRealService().getSize();
        }
    }

    // -----------------------------------------------------------------------
    // Protection Proxy — access control
    // -----------------------------------------------------------------------

    /**
     * Represents a user with a role for access control.
     */
    public record User(String name, Role role) {
        public enum Role { ADMIN, USER, GUEST }

        public User {
            Objects.requireNonNull(name, "Name must not be null");
            Objects.requireNonNull(role, "Role must not be null");
        }
    }

    /**
     * Service interface for a document repository.
     */
    public interface DocumentService {
        String read(String docId);
        String write(String docId, String content);
        String delete(String docId);
    }

    /**
     * Real document service implementation.
     */
    public static class RealDocumentService implements DocumentService {

        private final Map<String, String> documents = new HashMap<>();

        public RealDocumentService() {
            documents.put("doc1", "Public document content");
            documents.put("doc2", "Confidential report");
        }

        @Override
        public String read(String docId) {
            Objects.requireNonNull(docId, "Document ID must not be null");
            String content = documents.get(docId);
            if (content == null) {
                throw new NoSuchElementException("Document not found: " + docId);
            }
            return content;
        }

        @Override
        public String write(String docId, String content) {
            Objects.requireNonNull(docId, "Document ID must not be null");
            Objects.requireNonNull(content, "Content must not be null");
            documents.put(docId, content);
            return "Written to " + docId;
        }

        @Override
        public String delete(String docId) {
            Objects.requireNonNull(docId, "Document ID must not be null");
            if (documents.remove(docId) == null) {
                throw new NoSuchElementException("Document not found: " + docId);
            }
            return "Deleted " + docId;
        }
    }

    /**
     * Protection proxy that controls access based on user roles.
     * - GUEST: read only
     * - USER: read and write
     * - ADMIN: read, write, and delete
     */
    public static class ProtectionDocumentProxy implements DocumentService {

        private final DocumentService realService;
        private final User user;

        public ProtectionDocumentProxy(DocumentService realService, User user) {
            this.realService = Objects.requireNonNull(realService, "Real service must not be null");
            this.user = Objects.requireNonNull(user, "User must not be null");
        }

        @Override
        public String read(String docId) {
            // All roles can read
            return realService.read(docId);
        }

        @Override
        public String write(String docId, String content) {
            if (user.role() == User.Role.GUEST) {
                throw new SecurityException("User '%s' with role GUEST cannot write documents".formatted(user.name()));
            }
            return realService.write(docId, content);
        }

        @Override
        public String delete(String docId) {
            if (user.role() != User.Role.ADMIN) {
                throw new SecurityException(
                        "User '%s' with role %s cannot delete documents".formatted(user.name(), user.role()));
            }
            return realService.delete(docId);
        }
    }

    // -----------------------------------------------------------------------
    // Caching Proxy — smart reference / caching
    // -----------------------------------------------------------------------

    /**
     * Interface for a data lookup service.
     */
    public interface DataLookupService {
        String lookup(String key);
    }

    /**
     * Real data lookup that simulates an expensive operation.
     */
    public static class RealDataLookupService implements DataLookupService {

        private int lookupCount = 0;

        @Override
        public String lookup(String key) {
            Objects.requireNonNull(key, "Key must not be null");
            lookupCount++;
            return "Result for: " + key;
        }

        public int getLookupCount() {
            return lookupCount;
        }
    }

    /**
     * Caching proxy that stores results and returns cached values for repeated lookups.
     */
    public static class CachingDataLookupProxy implements DataLookupService {

        private final DataLookupService realService;
        private final Map<String, String> cache = new ConcurrentHashMap<>();

        public CachingDataLookupProxy(DataLookupService realService) {
            this.realService = Objects.requireNonNull(realService, "Real service must not be null");
        }

        @Override
        public String lookup(String key) {
            return cache.computeIfAbsent(key, realService::lookup);
        }

        public int getCacheSize() {
            return cache.size();
        }

        public void clearCache() {
            cache.clear();
        }

        public boolean isCached(String key) {
            return cache.containsKey(key);
        }
    }

    // -----------------------------------------------------------------------
    // Logging Proxy — smart reference / logging
    // -----------------------------------------------------------------------

    /**
     * Logging proxy that records all method invocations.
     */
    public static class LoggingDataLookupProxy implements DataLookupService {

        private final DataLookupService realService;
        private final List<String> accessLog = new ArrayList<>();

        public LoggingDataLookupProxy(DataLookupService realService) {
            this.realService = Objects.requireNonNull(realService, "Real service must not be null");
        }

        @Override
        public String lookup(String key) {
            accessLog.add("lookup(" + key + ")");
            return realService.lookup(key);
        }

        public List<String> getAccessLog() {
            return Collections.unmodifiableList(accessLog);
        }
    }

    // -----------------------------------------------------------------------
    // Dynamic Proxy — using java.lang.reflect.Proxy
    // -----------------------------------------------------------------------

    /**
     * A generic logging invocation handler that logs all method calls on any interface.
     */
    public static class LoggingInvocationHandler implements InvocationHandler {

        private final Object target;
        private final List<String> invocationLog = new ArrayList<>();

        public LoggingInvocationHandler(Object target) {
            this.target = Objects.requireNonNull(target, "Target must not be null");
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String argStr = args == null ? "" : Arrays.toString(args);
            invocationLog.add(method.getName() + "(" + argStr + ")");
            return method.invoke(target, args);
        }

        public List<String> getInvocationLog() {
            return Collections.unmodifiableList(invocationLog);
        }
    }

    /**
     * Factory method to create a dynamic logging proxy for any interface.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createLoggingProxy(T target, Class<T> interfaceType,
                                           LoggingInvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler
        );
    }
}
