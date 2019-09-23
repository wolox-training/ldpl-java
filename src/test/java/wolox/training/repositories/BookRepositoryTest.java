package wolox.training.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import wolox.training.TestUtils;
import wolox.training.models.Book;

@RunWith(SpringRunner.class)
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book testBook;
    private String bookAuthor = "Edgar Alan Poe";

    private void persistBook() {
        testBook = TestUtils
            .createBookWithData("978-3-16-148410-0", bookAuthor, "http://my-image.net/book", 33,
                "El planeta", "The raven", "Narrative Poem", 1845);

        TestUtils.persist(testEntityManager, testBook);
    }

    @Test
    public void givenAnAuthor_whenFindByAuthorIsCalled_thenReturnBook() {
        persistBook();

        Book bookFound = bookRepository.findFirstByAuthor(bookAuthor).orElse(new Book());

        assertThat(bookFound.getId(), is(testBook.getId()));
        assertThat(bookFound.getIsbn(), is(testBook.getIsbn()));
        assertThat(bookFound.getAuthor(), is(testBook.getAuthor()));
        assertThat(bookFound.getImage(), is(testBook.getImage()));
        assertThat(bookFound.getPages(), is(testBook.getPages()));
        assertThat(bookFound.getPublisher(), is(testBook.getPublisher()));
        assertThat(bookFound.getTitle(), is(testBook.getTitle()));
        assertThat(bookFound.getSubtitle(), is(testBook.getSubtitle()));
        assertThat(bookFound.getYear(), is(testBook.getYear()));
    }

    @Test
    public void givenUnknownAuthor_whenFinByAuthorIsCalled_thenReturnNoResults() {
        Assertions.assertThat(bookRepository.findFirstByAuthor("Some string").isPresent())
            .isFalse();
    }

    @Test
    public void givenBooksInDatabase_whenFindAll_thenReturnBooksList() {
        persistBook();

        List<Book> bookList = bookRepository.findAll();

        Assertions.assertThat(bookList).hasSize(1);
        Assertions.assertThat(bookList.get(0)).isEqualTo(testBook);
    }

    @Test
    public void givenNoBooksInDatabase_whenFindAll_thenReturnEmptyList() {
        List<Book> bookList = bookRepository.findAll();

        Assertions.assertThat(bookList).isEmpty();
    }
}