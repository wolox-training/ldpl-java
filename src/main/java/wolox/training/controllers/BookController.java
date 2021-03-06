package wolox.training.controllers;

import java.io.IOException;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import wolox.training.exceptions.BookIdMismatchException;
import wolox.training.exceptions.BookNotFoundException;
import wolox.training.exceptions.ParseBookException;
import wolox.training.exceptions.RequestException;
import wolox.training.models.Book;
import wolox.training.models.dtos.BookDto;
import wolox.training.repositories.BookRepository;
import wolox.training.services.OpenLibraryService;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OpenLibraryService openLibraryService;

    @GetMapping("/greeting")
    public String greeting(
        @RequestParam(name = "name", required = false, defaultValue = "World") String name,
        Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@Valid @RequestBody Book book) {
        return bookRepository.save(book);
    }

    @GetMapping
    public Page<Book> findAll(
        @RequestParam(name = "isbn", required = false) String isbn,
        @RequestParam(name = "author", required = false) String author,
        @RequestParam(name = "genre", required = false) String genre,
        @RequestParam(name = "image", required = false) String image,
        @RequestParam(name = "pages", required = false) Integer pages,
        @RequestParam(name = "publisher", required = false) String publisher,
        @RequestParam(name = "subtitle", required = false) String subtitle,
        @RequestParam(name = "title", required = false) String title,
        @RequestParam(name = "year", required = false) String year,
        Pageable pageable
    ) {
        return bookRepository
            .findAll(isbn, author, genre, image, pages, publisher, subtitle, title, year, pageable);
    }

    @GetMapping("/{id}")
    public Book findOne(@PathVariable(value = "id") Long id) {
        return bookRepository
            .findById(id)
            .orElseThrow(BookNotFoundException::new);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable(value = "id") Long id) {
        Book book = bookRepository
            .findById(id)
            .orElseThrow(BookNotFoundException::new);

        bookRepository.delete(book);
    }

    @PutMapping("/{id}")
    public Book update(@PathVariable("id") Long id, @RequestBody Book book) {
        if (!id.equals(book.getId())) {
            throw new BookIdMismatchException("Ids mismatch");
        }

        bookRepository.findById(id)
            .orElseThrow(BookNotFoundException::new);

        return bookRepository.save(book);
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> findByIsbn(@PathVariable(name = "isbn") String isbn)
        throws ParseBookException, RequestException {
        Optional<Book> databaseBook = bookRepository.findByIsbn(isbn);

        if (databaseBook.isPresent()) {
            return new ResponseEntity<>(databaseBook.get(), HttpStatus.OK);
        }

        BookDto bookDto;

        try {
            bookDto = openLibraryService.bookInfo(isbn);
        } catch (IOException exception) {
            throw new ParseBookException();
        }

        if (bookDto != null) {
            Book newBook;

            try {
                newBook = Book.fromDto(bookDto);
            } catch (Exception e) {
                throw new ParseBookException();
            }

            Book book = create(newBook);
            return new ResponseEntity<>(book, HttpStatus.CREATED);
        }

        throw new BookNotFoundException();
    }
}
