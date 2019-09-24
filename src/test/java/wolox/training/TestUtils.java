package wolox.training;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import wolox.training.models.Book;
import wolox.training.models.User;

public class TestUtils {

    public static <T> String toStringJson(T object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

    public static <T> void persist(TestEntityManager entityManager, T entity) {
        entityManager.persist(entity);
        entityManager.flush();
    }

    public static Book createBookWithData(@Nullable Long id, String isbn, String bookAuthor,
        String image, int pages, String publisher, String title, String subtitle, int year) {
        return new Book(id, isbn, bookAuthor, null, image, pages, publisher, subtitle, title,
            String.valueOf(year));
    }

    public static User cloneUser(User oldUser) {
        return new User(oldUser.getId(), oldUser.getUsername(), oldUser.getName(),
            oldUser.getBirthDate());
    }

    public static User createUserWithData(@Nullable Long id, @Nonnull String username,
        @Nonnull String name) {
        return new User(id, username, name, LocalDate.now().minusYears(1));
    }
}
