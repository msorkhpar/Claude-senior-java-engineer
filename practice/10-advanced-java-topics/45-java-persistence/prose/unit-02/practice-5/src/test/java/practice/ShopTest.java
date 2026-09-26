package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import practice.Shop.Category;
import practice.Shop.Product;

import static org.assertj.core.api.Assertions.assertThat;

class ShopTest {

    @Test
    void addingSetsBothSides() {
        Category books = new Category("Books");
        Product dune = new Product("Dune");
        Product emma = new Product("Emma");

        books.addProduct(dune);
        books.addProduct(emma);

        assertThat(books.products()).containsExactly(dune, emma);
        assertThat(dune.category()).isSameAs(books);
        assertThat(emma.category()).isSameAs(books);
    }

    @Test
    void movingAProductLeavesTheOldCategory() {
        Category books = new Category("Books");
        Category pens = new Category("Pens");
        Product dune = new Product("Dune");
        books.addProduct(dune);

        pens.addProduct(dune);

        assertThat(books.products()).isEmpty();
        assertThat(pens.products()).containsExactly(dune);
        assertThat(dune.category()).isSameAs(pens);
    }

    @Test
    void addingTwiceKeepsOneEntry() {
        Category books = new Category("Books");
        Product dune = new Product("Dune");

        books.addProduct(dune);
        books.addProduct(dune);

        assertThat(books.products()).containsExactly(dune);
    }

    @Test
    void removingClearsTheBackReference() {
        Category books = new Category("Books");
        Product dune = new Product("Dune");
        Product emma = new Product("Emma");
        books.addProduct(dune);
        books.addProduct(emma);

        books.removeProduct(dune);

        assertThat(books.products()).containsExactly(emma);
        assertThat(dune.category()).isNull();

        Category pens = new Category("Pens");
        pens.removeProduct(emma);
        assertThat(emma.category()).isSameAs(books);
        assertThat(books.products()).containsExactly(emma);
    }

    @Test
    void callersCannotEditTheListDirectly() {
        Category books = new Category("Books");
        Product dune = new Product("Dune");
        Product stray = new Product("Stray");
        books.addProduct(dune);

        List<Product> handedOut = books.products();
        try {
            handedOut.add(stray);
        } catch (UnsupportedOperationException refused) {
            // a read-only view is one way to keep the sides in step
        }

        assertThat(books.products()).containsExactly(dune);
        assertThat(stray.category()).isNull();
    }
}
