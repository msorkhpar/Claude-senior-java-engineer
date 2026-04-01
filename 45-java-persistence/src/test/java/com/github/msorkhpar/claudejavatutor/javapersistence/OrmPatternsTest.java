package com.github.msorkhpar.claudejavatutor.javapersistence;

import org.junit.jupiter.api.*;

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
            OrmPatterns.Product product = new OrmPatterns.Product(0, "Laptop", 999.99, catId);

            OrmPatterns.Product saved = repo.save(product);

            assertThat(saved.getId()).isGreaterThan(0);
            assertThat(saved.getName()).isEqualTo("Laptop");
        }

        @Test
        @DisplayName("Should update an existing product")
        void testUpdateProduct() throws SQLException {
            int catId = repo.saveCategory("Electronics");
            OrmPatterns.Product product = new OrmPatterns.Product(0, "Phone", 699.99, catId);
            OrmPatterns.Product saved = repo.save(product);

            saved.setPrice(749.99);
            saved.setName("Smartphone");
            repo.save(saved);

            Optional<OrmPatterns.Product> found = repo.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Smartphone");
            assertThat(found.get().getPrice()).isEqualTo(749.99);
        }

        @Test
        @DisplayName("Should find product by ID")
        void testFindById() throws SQLException {
            int catId = repo.saveCategory("Books");
            OrmPatterns.Product product = repo.save(new OrmPatterns.Product(0, "Java Book", 49.99, catId));

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
            repo.save(new OrmPatterns.Product(0, "Apple", 1.50, catId));
            repo.save(new OrmPatterns.Product(0, "Banana", 0.75, catId));

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
            OrmPatterns.Product product = repo.save(new OrmPatterns.Product(0, "Ball", 5.99, catId));

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
            repo.save(new OrmPatterns.Product(0, "Shirt", 29.99, catId));
            repo.save(new OrmPatterns.Product(0, "Pants", 49.99, catId));
            repo.save(new OrmPatterns.Product(0, "Hat", 14.99, catId));

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
            repo.save(new OrmPatterns.Product(0, "Cheap", 10.00, catId));
            repo.save(new OrmPatterns.Product(0, "Mid", 50.00, catId));
            repo.save(new OrmPatterns.Product(0, "Expensive", 200.00, catId));

            List<OrmPatterns.Product> result = repo.findByPriceRange(20.00, 100.00);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Mid");
        }

        @Test
        @DisplayName("Should return empty for price range with no matches")
        void testFindByPriceRangeNoMatch() throws SQLException {
            int catId = repo.saveCategory("Electronics");
            repo.save(new OrmPatterns.Product(0, "Item", 500.00, catId));

            List<OrmPatterns.Product> result = repo.findByPriceRange(1.00, 10.00);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all products with their category (join)")
        void testFindAllWithCategory() throws SQLException {
            int catId = repo.saveCategory("Sports");
            repo.save(new OrmPatterns.Product(0, "Tennis Ball", 3.99, catId));
            repo.save(new OrmPatterns.Product(0, "Racket", 89.99, catId));

            List<OrmPatterns.Product> result = repo.findAllWithCategory();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find product names by category name")
        void testFindProductNamesByCategoryName() throws SQLException {
            int electId = repo.saveCategory("Electronics");
            int bookId = repo.saveCategory("Books");
            repo.save(new OrmPatterns.Product(0, "Laptop", 999.99, electId));
            repo.save(new OrmPatterns.Product(0, "Mouse", 29.99, electId));
            repo.save(new OrmPatterns.Product(0, "Java Book", 49.99, bookId));

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
            repo.save(new OrmPatterns.Product(0, "Shovel", 19.99, catId));
            repo.save(new OrmPatterns.Product(0, "Seeds", 4.99, catId));

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
            OrmPatterns.Product p = new OrmPatterns.Product(1, "Widget", 9.99, 5);
            String str = p.toString();
            assertThat(str).contains("Widget", "9.99");
        }

        @Test
        @DisplayName("Category should manage product list")
        void testCategoryProducts() {
            OrmPatterns.Category cat = new OrmPatterns.Category(1, "Test");
            assertThat(cat.getProducts()).isEmpty();

            OrmPatterns.Product p = new OrmPatterns.Product(1, "P1", 10.0, 1);
            cat.setProducts(List.of(p));
            assertThat(cat.getProducts()).hasSize(1);
        }

        @Test
        @DisplayName("Product getters and setters should work")
        void testProductGettersSetters() {
            OrmPatterns.Product p = new OrmPatterns.Product();
            p.setId(42);
            p.setName("Gadget");
            p.setPrice(19.99);
            p.setCategoryId(3);

            assertThat(p.getId()).isEqualTo(42);
            assertThat(p.getName()).isEqualTo("Gadget");
            assertThat(p.getPrice()).isEqualTo(19.99);
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
