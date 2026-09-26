package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServerTest {

    @Test
    void updatesTheConfig() {
        Server server = new Server(30, "localhost");
        assertThat(server.config().timeout()).isEqualTo(30);
        assertThat(server.config().host()).isEqualTo("localhost");
        server.withTimeout(60);
        assertThat(server.config().timeout()).isEqualTo(60);
        assertThat(server.config().host()).isEqualTo("localhost");
        server.withHost("newhost");
        assertThat(server.config().timeout()).isEqualTo(60);
        assertThat(server.config().host()).isEqualTo("newhost");
    }

    @Test
    void anOldSnapshotNeverChanges() {
        Server server = new Server(30, "localhost");
        Server.Config before = server.config();
        server.withTimeout(60);
        server.withHost("newhost");
        assertThat(before.timeout()).isEqualTo(30);
        assertThat(before.host()).isEqualTo("localhost");
        assertThat(server.config()).isNotSameAs(before);
    }

    @Test
    void configFieldsAreFinal() {
        List<Field> fields = Arrays.stream(Server.Config.class.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toList();
        assertThat(fields).as("Config holds its settings in fields").hasSizeGreaterThanOrEqualTo(2);
        assertThat(fields).allMatch(f -> Modifier.isFinal(f.getModifiers()), "is final");
    }

    @Test
    void theReferenceIsVolatile() {
        new Server(30, "localhost").config().timeout();
        List<Field> holders = Arrays.stream(Server.class.getDeclaredFields())
                .filter(f -> f.getType() == Server.Config.class)
                .toList();
        assertThat(holders).as("the server keeps its Config in a field").isNotEmpty();
        assertThat(holders).allMatch(f -> Modifier.isVolatile(f.getModifiers()), "is volatile");
    }
}
