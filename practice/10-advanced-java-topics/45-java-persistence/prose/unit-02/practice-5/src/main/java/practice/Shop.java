package practice;

import java.util.List;

public final class Shop {

    private Shop() {
    }

    public static final class Category {

        public Category(String name) {
            throw new UnsupportedOperationException("TODO");
        }

        public String name() {
            throw new UnsupportedOperationException("TODO");
        }

        /** The products, in the order they were added. */
        public List<Product> products() {
            throw new UnsupportedOperationException("TODO");
        }

        /** Links both sides: the product joins this category and leaves its old one. */
        public void addProduct(Product product) {
            throw new UnsupportedOperationException("TODO");
        }

        /** Unlinks both sides. */
        public void removeProduct(Product product) {
            throw new UnsupportedOperationException("TODO");
        }
    }

    public static final class Product {

        public Product(String name) {
            throw new UnsupportedOperationException("TODO");
        }

        public String name() {
            throw new UnsupportedOperationException("TODO");
        }

        /** The category this product belongs to, or null. */
        public Category category() {
            throw new UnsupportedOperationException("TODO");
        }
    }
}
