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

    public static Book createBookWithData(String isbn, String bookAuthor, String image,
        Integer pages, String publisher, String title, String subtitle, int year) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setAuthor(bookAuthor);
        book.setImage(image);
        book.setPages(pages);
        book.setPublisher(publisher);
        book.setSubtitle(subtitle);
        book.setTitle(title);
        book.setYear(String.valueOf(year));
        return book;
    }

    public static User cloneUser(User oldUser) {
        User user = new User();
        user.setId(oldUser.getId());
        user.setBirthDate(oldUser.getBirthDate());
        user.setName(oldUser.getName());
        user.setUsername(oldUser.getUsername());
        return user;
    }

    public static User createUserWithData(@Nullable Long id, @Nonnull String username,
        @Nonnull String name) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setName(name);
        user.setBirthDate(LocalDate.now().minusYears(1));

        return user;
    }
}
