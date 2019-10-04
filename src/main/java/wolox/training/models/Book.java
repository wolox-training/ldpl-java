package wolox.training.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @NotEmpty(message = "ISBN is required")
    private String isbn;

    @Column(nullable = false)
    @NotEmpty(message = "Author is required")
    private String author;

    @Column
    private String genre;

    @Column(nullable = false)
    @NotEmpty(message = "Image is required")
    private String image;

    @Column(nullable = false)
    @Min(1)
    @NotNull
    private Integer pages;

    @Column(nullable = false)
    @NotEmpty(message = "Publisher is required")
    private String publisher;

    @Column(nullable = false)
    @NotEmpty(message = "Subtitle is required")
    private String subtitle;

    @Column(nullable = false)
    @NotEmpty(message = "Title is required")
    private String title;

    @Column(nullable = false)
    @NotEmpty
    private String year;

    @ManyToMany(mappedBy = "books", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<User> users = new ArrayList<>();

    public Book() {
        // Added to use with JPA;
    }

    public Book(@Nullable Long id, @NotNull String isbn, @NotNull String author, String genre,
        @NotNull String image, @Min(1) Integer pages, @NotNull String publisher,
        @NotNull String subtitle, @NotNull String title, @Size(min = 4, max = 4) String year) {
        this.id = id;
        this.isbn = isbn;
        this.author = author;
        this.genre = genre;
        this.image = image;
        this.pages = pages;
        this.publisher = publisher;
        this.subtitle = subtitle;
        this.title = title;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        Preconditions.checkNotNull(isbn, "Isbn can't be null");
        this.isbn = isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        Preconditions.checkNotNull(author, "Author can't be null");
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        Preconditions.checkNotNull(image, "Image can't be null");
        this.image = image;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        Preconditions.checkArgument(pages != null && pages > 0,
            "Invalid pages argument, must be a number, at least 1");
        this.pages = pages;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        Preconditions.checkNotNull(publisher, "Publisher can't be null");
        this.publisher = publisher;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        Preconditions.checkNotNull(subtitle, "Subtitle can't be null");
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        Preconditions.checkNotNull(title, "Title can't be null");
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        Preconditions
            .checkArgument(year != null && year.length() == 4 && Integer.parseInt(year) > 0,
                "Type a valid year, i.e 1992");
        this.year = year;
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public void setUsers(List<User> users) {
        Preconditions.checkNotNull(users, "Users must not be null");
        this.users = users;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Book) && this.getId() != null && this.getId()
            .equals(((Book) obj).getId());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(id, isbn, author, genre, image, pages, publisher, subtitle, title, year);
    }
}
