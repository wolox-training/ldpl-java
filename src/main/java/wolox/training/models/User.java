package wolox.training.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import wolox.training.exceptions.BookAlreadyOwned;
import wolox.training.exceptions.BookNotFoundException;

@ApiModel(description = "User model")
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Username of user", dataType = "String", example = "juanes")
    @Column(nullable = false)
    @NotEmpty(message = "Username is required")
    private String username;

    @ApiModelProperty(value = "Full name of the user", dataType = "String", example = "Juan Esteban Ximenez")
    @Column(nullable = false)
    @NotEmpty(message = "Name is required")
    private String name;

    @ApiModelProperty(value = "User's birth date", dataType = "String", example = "1992-11-26")
    @Column(nullable = false)
    @NotNull(message = "Birth date is required")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd", shape = Shape.STRING)
    private LocalDate birthDate;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "book_user",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> books = new ArrayList<>();

    public User() {
        // Added to use with JPA;
    }

    public User(Long id, String username, String name, LocalDate birthDate) {
        this.id = id;
        setUsername(username);
        setName(name);
        setBirthDate(birthDate);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        Preconditions.checkNotNull(username, "Username can't be null --->");
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Preconditions.checkNotNull(name, "Name can't be null");
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        Preconditions.checkArgument(birthDate != null && birthDate.isBefore(LocalDate.now()),
            "Birth date can't be null or be in the future");
        this.birthDate = birthDate;
    }

    public List<Book> getBooks() {
        return Collections.unmodifiableList(books);
    }

    public void setBooks(List<Book> books) {
        Preconditions.checkNotNull(books, "Books list can't be empty");
        this.books = books;
    }

    public void addBook(Book book) {
        Preconditions.checkNotNull(book, "A null book can't be added");
        if (books.contains(book)) {
            throw new BookAlreadyOwned();
        }

        books.add(book);
    }

    public boolean removeBook(Book book) {
        Preconditions.checkNotNull(book, "A null book can't be removed");
        if (!books.contains(book)) {
            throw new BookNotFoundException();
        }

        return books.remove(book);
    }
}
