package wolox.training.repositories;

import static org.hamcrest.core.Is.is;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import wolox.training.models.Book;

@RunWith(SpringRunner.class)
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void whenFindByAuthor_thenReturnBook() {
        Book book = new Book();
        book.setIsbn("978-3-16-148410-0");
        book.setAuthor("Edgar Alan Poe");
        book.setImage("http://my-image.net/book");
        book.setPages(33);
        book.setPublisher("El planeta");
        book.setTitle("The raven");
        book.setSubtitle("Narrative poem");
        book.setYear(String.valueOf(1845));

        testEntityManager.persist(book);
        testEntityManager.flush();

        Book bookFound = bookRepository.findFirstByAuthor("Edgar Alan Poe").orElse(new Book());

        MatcherAssert.assertThat(bookFound.getId(), is(book.getId()));
    }

    @Test
    public void when() {

    }
}