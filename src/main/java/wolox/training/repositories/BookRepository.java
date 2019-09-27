package wolox.training.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wolox.training.models.Book;

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {

    Optional<Book> findFirstByAuthor(String author);

    List<Book> findAll();

    Optional<Book> findByIsbn(String isbn);

    @Query(
        value =
            "SELECT * FROM book WHERE (:genre IS NULL OR genre = CAST(:genre AS VARCHAR)) AND "
                + "(:publisher IS NULL OR publisher = CAST(:publisher AS VARCHAR)) AND "
                + "(:year IS NULL OR year = CAST(:year AS VARCHAR))",
        nativeQuery = true)
    List<Book> findByGenreAndPublisherAndYear(@Param("genre") String genre,
        @Param("publisher") String publisher, @Param("year") String year);
}
