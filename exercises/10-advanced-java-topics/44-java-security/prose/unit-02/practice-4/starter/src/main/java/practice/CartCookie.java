package practice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 * The shop keeps a visitor's cart in a cookie: the cart, serialized with Java serialization.
 * The browser sends the cookie back with every request, and anyone can edit it on the way.
 */
public class CartCookie {

    public record CartLine(String sku, int quantity) implements Serializable {}

    /** Serialization writes a Cart, the ArrayList of its lines, that list's Object[], and each CartLine. */
    public record Cart(String visitor, ArrayList<CartLine> lines) implements Serializable {}

    public static byte[] write(Cart cart) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(cart);
        }
        return out.toByteArray();
    }

    /**
     * The cart in a cookie the browser sent back. A cookie holding anything but a cart is refused
     * with an IOException, and no code of any other class runs while it is read.
     */
    public static Cart read(byte[] cookie) throws IOException, ClassNotFoundException {
        // TODO: The cookie's bytes are whatever the browser sent. Return the cart they hold.
        throw new IllegalStateException("TODO: read");
    }
}
