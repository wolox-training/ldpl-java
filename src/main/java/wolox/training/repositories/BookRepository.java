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

    @Query(
        value = "SELECT b FROM Book b WHERE "
            + "(:isbn IS NULL OR b.isbn = :isbn) AND "
            + "(:author IS NULL OR b.author = :author) AND "
            + "(:genre IS NULL OR b.genre = :genre) AND "
            + "(:image IS NULL OR b.image = :image) AND "
            + "(:pages IS NULL OR pages = :pages) AND "
            + "(:publisher IS NULL OR b.publisher = :publisher) AND "
            + "(:subtitle IS NULL OR b.subtitle LIKE %:subtitle%) AND "
            + "(:title IS NULL OR b.title LIKE %:title%) AND "
            + "(:year IS NULL OR b.year = :year)"
    )
    List<Book> findAll(@Param("isbn") String isbn, @Param("author") String author,
        @Param("genre") String genre, @Param("image") String image, @Param("pages") Integer pages,
        @Param("publisher") String publisher, @Param("subtitle") String subtitle,
        @Param("title") String title, @Param("year") String year);

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
