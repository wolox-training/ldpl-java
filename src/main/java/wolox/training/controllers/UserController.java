package wolox.training.controllers;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import wolox.training.exceptions.BookNotFoundException;
import wolox.training.exceptions.UserIdMismatchException;
import wolox.training.exceptions.UserNotFoundException;
import wolox.training.models.Book;
import wolox.training.models.User;
import wolox.training.repositories.BookRepository;
import wolox.training.repositories.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User findOne(@PathVariable(name = "id") Long id) {
        return userRepository
            .findById(id)
            .orElseThrow(UserNotFoundException::new);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable(value = "id") Long id) {
        User user = userRepository
            .findById(id)
            .orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable("id") Long id, @Valid @RequestBody User user) {
        if (!id.equals(user.getId())) {
            throw new UserIdMismatchException();
        }

        userRepository.findById(id)
            .orElseThrow(UserNotFoundException::new);

        return userRepository.save(user);
    }

    @PutMapping("/{userId}/books/{bookId}")
    public User addBook(@PathVariable(name = "userId") Long userId,
        @PathVariable(name = "bookId") Long bookId) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(UserNotFoundException::new);

        Book book = bookRepository
            .findById(bookId)
            .orElseThrow(BookNotFoundException::new);

        user.addBook(book);

        return userRepository.save(user);
    }

    @DeleteMapping("/{userId}/books/{bookId}")
    public User removeBook(@PathVariable(name = "userId") Long userId,
        @PathVariable(name = "bookId") Long bookId) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(UserNotFoundException::new);

        Book book = bookRepository
            .findById(bookId)
            .orElseThrow(BookNotFoundException::new);

        user.removeBook(book);

        return userRepository.save(user);
    }
}
