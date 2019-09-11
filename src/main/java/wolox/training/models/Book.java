package wolox.training.models;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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

    public Book() {
        // Added to use with JPA;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
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
        this.image = image;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Book) && this.getId().equals(((Book) obj).getId());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(id, isbn, author, genre, image, pages, publisher, subtitle, title, year);
    }
}
