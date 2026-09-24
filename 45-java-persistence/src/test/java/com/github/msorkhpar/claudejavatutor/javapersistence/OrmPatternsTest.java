package com.github.msorkhpar.claudejavatutor.javapersistence;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ORM Patterns Tests")
class OrmPatternsTest {

    private static final String URL = "jdbc:h2:mem:ormtest;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private OrmPatterns.ProductRepository repo;

    @BeforeEach
    void setUp() throws SQLException {
        repo = new OrmPatterns.ProductRepository(URL, USER, PASSWORD);
        repo.dropTables();
        repo.createTables();
    }

    @AfterEach
    void tearDown() throws SQLException {
        repo.dropTables();
    }

    @Nested
    @DisplayName("Entity CRUD Operations")
    class CrudTests {

        @Test
        @DisplayName("Should save a new product and assign an ID")
        void testSaveNewProduct() throws SQLException {
            int catId = repo.saveCategory("Electronics");
            OrmPatterns.Product product = new OrmPatterns.Product(0, "Laptop", new BigDecimal("999.99"), catId);

            OrmPatterns.Product saved = repo.save(product);

            assertThat(saved.getId()).isGreaterThan(0);
            assertThat(saved.getName()).isEqualTo("Laptop");
        }

        @Test
        @DisplayName("Should save and update a product without a category (categoryId 0 = NULL)")
        void testSaveProductWithoutCategory() throws SQLException {
            OrmPatterns.Product saved = repo.save(new OrmPatterns.Product(0, "Gift Card", new BigDecimal("25.00"), 0));
            assertThat(saved.getId()).isGreaterThan(0);
            assertThat(repo.findById(saved.getId())).get()
                    .extracting(OrmPatterns.Product::getCategoryId).isEqualTo(0);

            saved.setPrice(new BigDecimal("30.00"));
            repo.save(saved);
            assertThat(repo.findById(saved.getId()).get().getPrice()).isEqualByComparingTo("30.00");
            assertThat(repo.findAllWithCategory()).isEmpty(); // inner join leaves it out
        }

        @Test
        @DisplayName("Should update an existing product")
        void testUpdateProduct() throws SQLException {
            int catId = repo.saveCategory("Electronics");
            OrmPatterns.Product product = new OrmPatterns.Product(0, "Phone", new BigDecimal("699.99"), catId);
            OrmPatterns.Product saved = repo.save(product);

            saved.setPrice(new BigDecimal("749.99"));
            saved.setName("Smartphone");
            repo.save(saved);

            Optional<OrmPatterns.Product> found = repo.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Smartphone");
            assertThat(found.get().getPrice()).isEqualByComparingTo("749.99");
        }

        @Test
        @DisplayName("Should find product by ID")
        void testFindById() throws SQLException {
            int catId = repo.saveCategory("Books");
            OrmPatterns.Product product = repo.save(new OrmPatterns.Product(0, "Java Book", new BigDecimal("49.99"), catId));

            Optional<OrmPatterns.Product> found = repo.findById(product.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Java Book");
        }

        @Test
        @DisplayName("Should return empty for non-existent product ID")
        void testFindByIdNotFound() throws SQLException {
            Optional<OrmPatterns.Product> found = repo.findById(9999);
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should find all products")
        void testFindAll() throws SQLException {
            int catId = repo.saveCategory("Food");
            repo.save(new OrmPatterns.Product(0, "Apple", new BigDecimal("1.50"), catId));
            repo.save(new OrmPatterns.Product(0, "Banana", new BigDecimal("0.75"), catId));

            List<OrmPatterns.Product> all = repo.findAll();
            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no products exist")
        void testFindAllEmpty() throws SQLException {
            List<OrmPatterns.Product> all = repo.findAll();
            assertThat(all).isEmpty();
        }

        @Test
        @DisplayName("Should delete product by ID")
        void testDeleteById() throws SQLException {
            int catId = repo.saveCategory("Toys");
            OrmPatterns.Product product = repo.save(new OrmPatterns.Product(0, "Ball", new BigDecimal("5.99"), catId));

            boolean deleted = repo.deleteById(product.getId());
            assertThat(deleted).isTrue();
            assertThat(repo.findById(product.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should return false when deleting non-existent product")
        void testDeleteNonExistent() throws SQLException {
            boolean deleted = repo.deleteById(9999);
            assertThat(deleted).isFalse();
        }

        @Test
        @DisplayName("Should count products correctly")
        void testCount() throws SQLException {
            int catId = repo.saveCategory("Clothing");
            repo.save(new OrmPatterns.Product(0, "Shirt", new BigDecimal("29.99"), catId));
            repo.save(new OrmPatterns.Product(0, "Pants", new BigDecimal("49.99"), catId));
            repo.save(new OrmPatterns.Product(0, "Hat", new BigDecimal("14.99"), catId));

            long count = repo.count();
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return zero count when no products")
        void testCountEmpty() throws SQLException {
            long count = repo.count();
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Query Methods (simulating JPQL)")
    class QueryTests {

        @Test
        @DisplayName("Should find products by price range")
        void testFindByPriceRange() throws SQLException {
            int catId = repo.saveCategory("Electronics");
            repo.save(new OrmPatterns.Product(0, "Cheap", new BigDecimal("10.00"), catId));
            repo.save(new OrmPatterns.Product(0, "Mid", new BigDecimal("50.00"), catId));
            repo.save(new OrmPatterns.Product(0, "Expensive", new BigDecimal("200.00"), catId));

            List<OrmPatterns.Product> result = repo.findByPriceRange(new BigDecimal("20.00"), new BigDecimal("100.00"));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Mid");
        }

        @Test
        @DisplayName("Should return empty for price range with no matches")
        void testFindByPriceRangeNoMatch() throws SQLException {
            int catId = repo.saveCategory("Electronics");
            repo.save(new OrmPatterns.Product(0, "Item", new BigDecimal("500.00"), catId));

            List<OrmPatterns.Product> result = repo.findByPriceRange(new BigDecimal("1.00"), new BigDecimal("10.00"));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all products with their category (join)")
        void testFindAllWithCategory() throws SQLException {
            int catId = repo.saveCategory("Sports");
            repo.save(new OrmPatterns.Product(0, "Tennis Ball", new BigDecimal("3.99"), catId));
            repo.save(new OrmPatterns.Product(0, "Racket", new BigDecimal("89.99"), catId));

            List<OrmPatterns.Product> result = repo.findAllWithCategory();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find product names by category name")
        void testFindProductNamesByCategoryName() throws SQLException {
            int electId = repo.saveCategory("Electronics");
            int bookId = repo.saveCategory("Books");
            repo.save(new OrmPatterns.Product(0, "Laptop", new BigDecimal("999.99"), electId));
            repo.save(new OrmPatterns.Product(0, "Mouse", new BigDecimal("29.99"), electId));
            repo.save(new OrmPatterns.Product(0, "Java Book", new BigDecimal("49.99"), bookId));

            List<String> names = repo.findProductNamesByCategoryName("Electronics");
            assertThat(names).containsExactlyInAnyOrder("Laptop", "Mouse");
        }

        @Test
        @DisplayName("Should return empty for non-existent category name")
        void testFindProductNamesByInvalidCategory() throws SQLException {
            List<String> names = repo.findProductNamesByCategoryName("NonExistent");
            assertThat(names).isEmpty();
        }
    }

    @Nested
    @DisplayName("Relationship Loading (Eager)")
    class RelationshipTests {

        @Test
        @DisplayName("Should load category with all its products (eager)")
        void testFindCategoryWithProducts() throws SQLException {
            int catId = repo.saveCategory("Garden");
            repo.save(new OrmPatterns.Product(0, "Shovel", new BigDecimal("19.99"), catId));
            repo.save(new OrmPatterns.Product(0, "Seeds", new BigDecimal("4.99"), catId));

            Optional<OrmPatterns.Category> result = repo.findCategoryWithProducts(catId);

            assertThat(result).isPresent();
            OrmPatterns.Category cat = result.get();
            assertThat(cat.getName()).isEqualTo("Garden");
            assertThat(cat.getProducts()).hasSize(2);
            assertThat(cat.getProducts()).extracting(OrmPatterns.Product::getName)
                    .containsExactlyInAnyOrder("Shovel", "Seeds");
        }

        @Test
        @DisplayName("Should return empty for non-existent category")
        void testFindCategoryWithProductsNotFound() throws SQLException {
            Optional<OrmPatterns.Category> result = repo.findCategoryWithProducts(9999);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should load category with empty product list")
        void testFindCategoryWithNoProducts() throws SQLException {
            int catId = repo.saveCategory("EmptyCategory");

            Optional<OrmPatterns.Category> result = repo.findCategoryWithProducts(catId);
            assertThat(result).isPresent();
            assertThat(result.get().getProducts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Entity Class Tests")
    class EntityTests {

        @Test
        @DisplayName("Product toString should contain all fields")
        void testProductToString() {
            OrmPatterns.Product p = new OrmPatterns.Product(1, "Widget", new BigDecimal("9.99"), 5);
            String str = p.toString();
            assertThat(str).contains("Widget", "9.99");
        }

        @Test
        @DisplayName("Category should manage product list")
        void testCategoryProducts() {
            OrmPatterns.Category cat = new OrmPatterns.Category(1, "Test");
            assertThat(cat.getProducts()).isEmpty();

            OrmPatterns.Product p = new OrmPatterns.Product(1, "P1", new BigDecimal("10.0"), 1);
            cat.setProducts(List.of(p));
            assertThat(cat.getProducts()).hasSize(1);
        }

        @Test
        @DisplayName("Product getters and setters should work")
        void testProductGettersSetters() {
            OrmPatterns.Product p = new OrmPatterns.Product();
            p.setId(42);
            p.setName("Gadget");
            p.setPrice(new BigDecimal("19.99"));
            p.setCategoryId(3);

            assertThat(p.getId()).isEqualTo(42);
            assertThat(p.getName()).isEqualTo("Gadget");
            assertThat(p.getPrice()).isEqualByComparingTo("19.99");
            assertThat(p.getCategoryId()).isEqualTo(3);
        }

        @Test
        @DisplayName("Category getters and setters should work")
        void testCategoryGettersSetters() {
            OrmPatterns.Category c = new OrmPatterns.Category();
            c.setId(7);
            c.setName("Hardware");

            assertThat(c.getId()).isEqualTo(7);
            assertThat(c.getName()).isEqualTo("Hardware");
        }
    }
}
