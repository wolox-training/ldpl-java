package wolox.training.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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
    private String isbn = "978-3-16-148410-0";

    private void persistBook() {
        testBook = TestUtils
            .createBookWithData(null, isbn, bookAuthor, "http://my-image.net/book",
                33, "El planeta", "The raven", "Narrative Poem", 1845);

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
        for (int i = 0; i < 10; i++) {
            Book tmpBook = TestUtils
                .createBookWithData(null, isbn, bookAuthor, "http://my-image.net/book",
                    33, "El planeta", "The raven", "Narrative Poem", 1845);

            TestUtils.persist(testEntityManager, tmpBook);
        }

        Page<Book> bookList = bookRepository
            .findAll(null, null, null, null, null, null, null, null, null,
                PageRequest.of(0, 2, Sort.by(Order.desc("id"))));

        List<Book> contentList = bookList.getContent();

        Assertions.assertThat(bookList).hasSize(2);
        Assertions.assertThat(bookList.getTotalElements()).isEqualTo(10);
        Assertions.assertThat(bookList.getTotalPages()).isEqualTo(5);
        Assertions.assertThat(bookList.getNumber()).isEqualTo(0);

        Assertions
            .assertThat(contentList.get(0).getId())
            .isGreaterThan(contentList.get(1).getId());
    }

    @Test
    public void givenNoBooksInDatabase_whenFindAll_thenReturnEmptyList() {
        Page<Book> bookList = bookRepository
            .findAll(null, null, null, null, null, null, null, null, null, PageRequest.of(0, 1));

        Assertions.assertThat(bookList.getContent()).isEmpty();
    }

    @Test
    public void givenNoBooksInDB_whenFindByIsbnIsCalled_thenNoReturnBook() {
        Optional<Book> foundBook = bookRepository.findByIsbn("an isbn");
        Assertions.assertThat(foundBook.isPresent()).isFalse();
    }

    @Test
    public void givenBooksInDB_whenFindByIsbnIsCalledWithValidValue_thenReturnMatchingBook() {
        persistBook();
        Optional<Book> foundBook = bookRepository.findByIsbn(isbn);
        Assertions.assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    public void givenNoBooksOnDB_whenFindByGenreAndPublisherAndYear_thenReturnEmpty() {
        Page<Book> filteredBooks = bookRepository
            .findByGenreAndPublisherAndYear("a genre", null, "1992", PageRequest.of(0, 1));

        Assertions.assertThat(filteredBooks.getContent()).isEmpty();
    }

    @Test
    public void givenBooksOnDB_whenFindByGenreAndPublisherAndYear_thenReturnListWithFoundBooks() {
        persistBook();

        String publisher = testBook.getPublisher();
        String year = testBook.getYear();

        Page<Book> filteredBooks = bookRepository
            .findByGenreAndPublisherAndYear(null, publisher, year, PageRequest.of(0, 1));

        Assertions.assertThat(filteredBooks.getContent()).isNotEmpty();

        Book foundBook = filteredBooks.getContent().get(0);
        Assertions.assertThat(foundBook.getAuthor()).isEqualTo(testBook.getAuthor());
        Assertions.assertThat(foundBook.getIsbn()).isEqualTo(testBook.getIsbn());
        Assertions.assertThat(foundBook.getPublisher()).isEqualTo(publisher);
        Assertions.assertThat(foundBook.getYear()).isEqualTo(year);
    }

    @Test
    public void givenBooksInDatabase_whenFindAllWithSomeFilterFields_thenReturnBooksList() {
        persistBook();

        Page<Book> bookList = bookRepository
            .findAll(testBook.getIsbn(), null, null, null, testBook.getPages(), null,
                testBook.getSubtitle().substring(0, 5), testBook.getTitle().substring(3), null,
                PageRequest.of(0, 1));

        Assertions.assertThat(bookList.getContent()).hasSize(1);
        Assertions.assertThat(bookList.getContent().get(0)).isEqualTo(testBook);
    }
}