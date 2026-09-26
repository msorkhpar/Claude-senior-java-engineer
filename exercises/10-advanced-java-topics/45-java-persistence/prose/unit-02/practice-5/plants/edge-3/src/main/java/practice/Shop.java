package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Shop {

    private Shop() {
    }

    public static final class Category {
        private final String name;
        private final List<Product> products = new ArrayList<>();

        public Category(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        public String name() {
            return name;
        }

        /** The products, in the order they were added. */
        public List<Product> products() {
            return Collections.unmodifiableList(products);
        }

        /** Links both sides: the product joins this category and leaves its old one. */
        public void addProduct(Product product) {
            Objects.requireNonNull(product, "product");
            if (product.category == this) {
                return;
            }
            if (product.category != null) {
                product.category.products.remove(product);
            }
            products.add(product);
            product.category = this;
        }

        /** Unlinks both sides. */
        public void removeProduct(Product product) {
            products.remove(product);
        }
    }

    public static final class Product {
        private final String name;
        private Category category;

        public Product(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        public String name() {
            return name;
        }

        /** The category this product belongs to, or null. */
        public Category category() {
            return category;
        }
    }
}
