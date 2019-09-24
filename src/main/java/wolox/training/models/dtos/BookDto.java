package wolox.training.models.dtos;

import java.util.List;

public class BookDto {

    private String ISBN;
    private String title;
    private String subtitle;
    private List<String> publishers;
    private String publishDate;
    private Integer pages;
    private List<String> authors;
    private String image;

    public BookDto() {
    }

    public BookDto(String ISBN, String title, String subtitle, List<String> publishers,
        String publishDate, Integer pages, List<String> authors, String image) {
        this.ISBN = ISBN;
        this.title = title;
        this.subtitle = subtitle;
        this.publishers = publishers;
        this.publishDate = publishDate;
        this.pages = pages;
        this.authors = authors;
        this.image = image;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public List<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
