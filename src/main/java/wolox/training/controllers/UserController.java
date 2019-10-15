package wolox.training.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import wolox.training.exceptions.BookNotFoundException;
import wolox.training.exceptions.NewPasswordsNotMatchException;
import wolox.training.exceptions.NoPasswordsProvidedException;
import wolox.training.exceptions.UserIdMismatchException;
import wolox.training.exceptions.UserNotAuthenticatedException;
import wolox.training.exceptions.UserNotFoundException;
import wolox.training.exceptions.UserPasswordMismatch;
import wolox.training.models.Book;
import wolox.training.models.User;
import wolox.training.repositories.BookRepository;
import wolox.training.repositories.UserRepository;

@Api(value = "CRUD User")
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
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @ApiOperation(value = "Find an user by id", response = User.class, authorizations = {
        @Authorization("none")})
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = User.class, message = "Successfully retrieved user"),
        @ApiResponse(code = 404, response = String.class, message = "User not found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @GetMapping("/{id}")
    public User findOne(
        @ApiParam(required = true, value = "User's id") @PathVariable(name = "id") Long id) {
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

        User newUser = new User(user.getId(), user.getUsername(), user.getName(),
            user.getBirthDate());

        return userRepository.save(newUser);
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

    @PatchMapping("/{userId}/password")
    @ResponseStatus(HttpStatus.OK)
    public User updatePassword(@PathVariable(name = "userId") Long userId,
        @RequestBody Map<String, String> data) {
        String oldPassword = Optional.ofNullable(data.get("oldPassword")).orElse("");
        String newPassword = Optional.ofNullable(data.get("newPassword")).orElse("");
        String newPasswordConfirmation = Optional
            .ofNullable(data.get("newPasswordConfirmation"))
            .orElse("");

        if (oldPassword.isEmpty() || newPassword.isEmpty() || newPasswordConfirmation.isEmpty()) {
            throw new NoPasswordsProvidedException();
        }

        if (!newPassword.equals(newPasswordConfirmation)) {
            throw new NewPasswordsNotMatchException();
        }

        User user = userRepository
            .findById(userId)
            .orElseThrow(UserNotFoundException::new);

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new UserPasswordMismatch();
        }

        user.setPassword(newPassword);

        return userRepository.save(user);
    }

    @GetMapping("/self")
    public User selfUser(Authentication authentication) {
        if (authentication.isAuthenticated()) {
            String username = authentication.getName();
            return userRepository
                .findFirstByUsername(username)
                .orElseThrow(UserNotFoundException::new);
        }

        throw new UserNotAuthenticatedException();
    }
}
