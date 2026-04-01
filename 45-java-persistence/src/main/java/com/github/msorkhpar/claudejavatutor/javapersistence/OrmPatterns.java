package com.github.msorkhpar.claudejavatutor.javapersistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Demonstrates Object-Relational Mapping (ORM) patterns using plain JDBC.
 * Illustrates the concepts behind JPA/Hibernate: entity mapping, repositories,
 * query abstraction, and relationship modeling without requiring a full JPA provider.
 */
public class OrmPatterns {

    // --- Entity classes modeling JPA-style entities ---

    /**
     * Simulates a JPA @Entity - a Product mapped to a database table.
     */
    public static class Product {
        private int id;
        private String name;
        private double price;
        private int categoryId;

        public Product() {
        }

        public Product(int id, String name, double price, int categoryId) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.categoryId = categoryId;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getCategoryId() { return categoryId; }
        public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

        @Override
        public String toString() {
            return "Product{id=%d, name='%s', price=%.2f, categoryId=%d}"
                    .formatted(id, name, price, categoryId);
        }
    }

    /**
     * Simulates a JPA @Entity - a Category with a one-to-many relationship to Products.
     */
    public static class Category {
        private int id;
        private String name;
        private List<Product> products = new ArrayList<>();

        public Category() {
        }

        public Category(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Product> getProducts() { return products; }
        public void setProducts(List<Product> products) { this.products = products; }
    }

    // --- Generic Repository interface (mirrors Spring Data JPA pattern) ---

    /**
     * A generic repository interface simulating JPA's CrudRepository.
     */
    public interface Repository<T, ID> {
        T save(T entity) throws SQLException;
        Optional<T> findById(ID id) throws SQLException;
        List<T> findAll() throws SQLException;
        boolean deleteById(ID id) throws SQLException;
        long count() throws SQLException;
    }

    // --- Product Repository implementation ---

    /**
     * Concrete repository for Product entities, demonstrating how an ORM
     * maps between objects and relational tables.
     */
    public static class ProductRepository implements Repository<Product, Integer> {

        private final String url;
        private final String user;
        private final String password;

        public ProductRepository(String url, String user, String password) {
            this.url = url;
            this.user = user;
            this.password = password;
        }

        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url, user, password);
        }

        public void createTables() throws SQLException {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL UNIQUE
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        price DECIMAL(10,2) NOT NULL,
                        category_id INT,
                        FOREIGN KEY (category_id) REFERENCES categories(id)
                    )
                """);
            }
        }

        public void dropTables() throws SQLException {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS products");
                stmt.execute("DROP TABLE IF EXISTS categories");
            }
        }

        /**
         * Persists or updates a Product (simulates JPA merge/persist).
         */
        @Override
        public Product save(Product entity) throws SQLException {
            if (entity.getId() == 0) {
                return insert(entity);
            } else {
                update(entity);
                return entity;
            }
        }

        private Product insert(Product entity) throws SQLException {
            String sql = "INSERT INTO products (name, price, category_id) VALUES (?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, entity.getName());
                pstmt.setDouble(2, entity.getPrice());
                pstmt.setInt(3, entity.getCategoryId());
                pstmt.executeUpdate();
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        entity.setId(keys.getInt(1));
                    }
                }
            }
            return entity;
        }

        private void update(Product entity) throws SQLException {
            String sql = "UPDATE products SET name = ?, price = ?, category_id = ? WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, entity.getName());
                pstmt.setDouble(2, entity.getPrice());
                pstmt.setInt(3, entity.getCategoryId());
                pstmt.setInt(4, entity.getId());
                pstmt.executeUpdate();
            }
        }

        @Override
        public Optional<Product> findById(Integer id) throws SQLException {
            String sql = "SELECT id, name, price, category_id FROM products WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapProduct(rs));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public List<Product> findAll() throws SQLException {
            String sql = "SELECT id, name, price, category_id FROM products";
            List<Product> products = new ArrayList<>();
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
            return products;
        }

        @Override
        public boolean deleteById(Integer id) throws SQLException {
            String sql = "DELETE FROM products WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        }

        @Override
        public long count() throws SQLException {
            String sql = "SELECT COUNT(*) FROM products";
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                rs.next();
                return rs.getLong(1);
            }
        }

        /**
         * Named query simulating JPQL: SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max
         */
        public List<Product> findByPriceRange(double minPrice, double maxPrice) throws SQLException {
            String sql = "SELECT id, name, price, category_id FROM products WHERE price BETWEEN ? AND ?";
            List<Product> products = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, minPrice);
                pstmt.setDouble(2, maxPrice);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        products.add(mapProduct(rs));
                    }
                }
            }
            return products;
        }

        /**
         * Simulates JPQL join fetch: SELECT p FROM Product p JOIN FETCH p.category
         */
        public List<Product> findAllWithCategory() throws SQLException {
            String sql = """
                SELECT p.id, p.name, p.price, p.category_id
                FROM products p
                JOIN categories c ON p.category_id = c.id
                """;
            List<Product> products = new ArrayList<>();
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
            return products;
        }

        /**
         * Simulates JPQL: SELECT p.name FROM Product p WHERE p.category.name = :categoryName
         */
        public List<String> findProductNamesByCategoryName(String categoryName) throws SQLException {
            String sql = """
                SELECT p.name
                FROM products p
                JOIN categories c ON p.category_id = c.id
                WHERE c.name = ?
                """;
            List<String> names = new ArrayList<>();
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, categoryName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        names.add(rs.getString("name"));
                    }
                }
            }
            return names;
        }

        /**
         * Inserts a category and returns its generated ID.
         */
        public int saveCategory(String name) throws SQLException {
            String sql = "INSERT INTO categories (name) VALUES (?)";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
            return -1;
        }

        /**
         * Eager loading: fetches a Category with all its Products (simulates JOIN FETCH).
         */
        public Optional<Category> findCategoryWithProducts(int categoryId) throws SQLException {
            String categorySql = "SELECT id, name FROM categories WHERE id = ?";
            String productsSql = "SELECT id, name, price, category_id FROM products WHERE category_id = ?";
            try (Connection conn = getConnection()) {
                Category category = null;
                try (PreparedStatement pstmt = conn.prepareStatement(categorySql)) {
                    pstmt.setInt(1, categoryId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            category = new Category(rs.getInt("id"), rs.getString("name"));
                        }
                    }
                }
                if (category == null) {
                    return Optional.empty();
                }
                try (PreparedStatement pstmt = conn.prepareStatement(productsSql)) {
                    pstmt.setInt(1, categoryId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        List<Product> products = new ArrayList<>();
                        while (rs.next()) {
                            products.add(mapProduct(rs));
                        }
                        category.setProducts(products);
                    }
                }
                return Optional.of(category);
            }
        }

        private Product mapProduct(ResultSet rs) throws SQLException {
            return new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("category_id")
            );
        }
    }
}
