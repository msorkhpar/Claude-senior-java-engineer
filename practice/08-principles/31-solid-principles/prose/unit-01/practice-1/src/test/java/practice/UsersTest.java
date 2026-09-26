package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UsersTest {

    @Test
    void createsAndAnnouncesAUser() {
        var repository = new Users.UserRepository();
        var notifications = new Users.NotificationService();
        var service = new Users.UserService(repository, notifications);
        service.createUser("u1", "Alice");
        service.createUser("u2", "Bob");
        assertThat(service.findUser("u1")).contains("Alice");
        assertThat(service.findUser("u2")).contains("Bob");
        assertThat(service.findUser("nobody")).isEmpty();
        assertThat(notifications.sent()).containsExactly("User created: Alice", "User created: Bob");
        assertThatIllegalArgumentException().isThrownBy(() -> notifications.send("  "));
        assertThatIllegalArgumentException().isThrownBy(() -> notifications.send(null));
        assertThat(notifications.sent()).hasSize(2);
        var kept = new Users.UserRepository();
        kept.save("u7", "Old");
        kept.save("u7", "New");
        assertThat(kept.count()).isEqualTo(1);
        assertThat(kept.findById("u7")).contains("New");
    }

    @Test
    void anInvalidUserIsNeitherSavedNorAnnounced() {
        var repository = new Users.UserRepository();
        var notifications = new Users.NotificationService();
        var service = new Users.UserService(repository, notifications);
        assertThatIllegalArgumentException().isThrownBy(() -> service.createUser("u2", "   "));
        assertThatIllegalArgumentException().isThrownBy(() -> service.createUser(null, "Carol"));
        assertThatIllegalArgumentException().isThrownBy(() -> service.createUser("  ", "Carol"));
        assertThatIllegalArgumentException().isThrownBy(() -> service.createUser("u3", null));
        assertThatIllegalArgumentException().isThrownBy(() -> repository.save("u4", null));
        assertThatIllegalArgumentException().isThrownBy(() -> repository.save("", "Dan"));
        assertThat(repository.count()).isZero();
        assertThat(notifications.sent()).isEmpty();
    }

    @Test
    void theServiceUsesTheRepositoryItIsGiven() {
        var repository = new Users.UserRepository();
        repository.save("u9", "Dana");
        var service = new Users.UserService(repository, new Users.NotificationService());
        assertThat(service.findUser("u9")).contains("Dana");
        service.createUser("u3", "Eve");
        assertThat(repository.findById("u3")).contains("Eve");
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void aMissingCollaboratorIsRejected() {
        var repository = new Users.UserRepository();
        var notifications = new Users.NotificationService();
        assertThatNullPointerException().isThrownBy(() -> new Users.UserService(null, notifications));
        assertThatNullPointerException().isThrownBy(() -> new Users.UserService(repository, null));
    }
}
