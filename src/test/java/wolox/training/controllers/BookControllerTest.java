package wolox.training.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import wolox.training.TestUtils;
import wolox.training.exceptions.ParseBookException;
import wolox.training.exceptions.RequestException;
import wolox.training.models.Book;
import wolox.training.models.dtos.BookDto;
import wolox.training.repositories.BookRepository;
import wolox.training.services.OpenLibraryService;

@RunWith(SpringRunner.class)
@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private OpenLibraryService openLibraryService;

    private String baseUrl = "/api/books/";
    private Book testBook;
    private Book testBookWithId;

    @Before
    public void setup() {
        testBook = TestUtils
            .createBookWithData(null, "an-isbn", "an author", "some image", 100, "a publisher",
                "a title", "a subtitle", 2019);

        testBookWithId = TestUtils
            .createBookWithData(1L, "an-isbn", "an author", "some image", 100, "a publisher",
                "a title", "a subtitle", 2019);
    }

    @Test
    public void givenValidBookInput_whenCreateIsCalled_thenCreateNewBook() throws Exception {
        when(bookRepository.save(testBook))
            .thenReturn(testBook);

        mockMvc.perform(post(baseUrl)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON)
            .content(TestUtils.toStringJson(testBook)))
            .andExpect(status().isCreated());
    }


    @Test
    public void givenInvalidBookInput_whenCreateIsCalled_thenBadRequestResponse() throws Exception {
        Book book = new Book();

        mockMvc.perform(post(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.toStringJson(book)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoBooks_whenGetBooksIsCalled_thenReturnEmptyList() throws Exception {
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    public void givenBooksExist_whenGetBooksIsCalled_thenReturnNonEmptyList() throws Exception {
        List<Book> books = Collections.singletonList(testBook);

        when(bookRepository.findAll())
            .thenReturn(books);

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(testBook.getId())))
            .andExpect(jsonPath("$[0].isbn", is(testBook.getIsbn())))
            .andExpect(jsonPath("$[0].author", is(testBook.getAuthor())))
            .andExpect(jsonPath("$[0].genre", is(testBook.getGenre())))
            .andExpect(jsonPath("$[0].image", is(testBook.getImage())))
            .andExpect(jsonPath("$[0].year", is(testBook.getYear())))
            .andExpect(jsonPath("$[0].pages", is(testBook.getPages())))
            .andExpect(jsonPath("$[0].publisher", is(testBook.getPublisher())))
            .andExpect(jsonPath("$[0].title", is(testBook.getTitle())))
            .andExpect(jsonPath("$[0].subtitle", is(testBook.getSubtitle())));
    }

    @Test
    public void givenABook_whenGetBookByIdIsCalled_thenItMustBeReturned() throws Exception {
        when(bookRepository.findById(1L))
            .thenReturn(Optional.of(testBook));

        mockMvc.perform(get(baseUrl + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(testBook.getId())))
            .andExpect(jsonPath("$.isbn", is(testBook.getIsbn())))
            .andExpect(jsonPath("$.author", is(testBook.getAuthor())))
            .andExpect(jsonPath("$.genre", is(testBook.getGenre())))
            .andExpect(jsonPath("$.image", is(testBook.getImage())))
            .andExpect(jsonPath("$.year", is(testBook.getYear())))
            .andExpect(jsonPath("$.pages", is(testBook.getPages())))
            .andExpect(jsonPath("$.publisher", is(testBook.getPublisher())))
            .andExpect(jsonPath("$.title", is(testBook.getTitle())))
            .andExpect(jsonPath("$.subtitle", is(testBook.getSubtitle())));
    }

    @Test
    public void givenNonExistingBook_whenGetBookIsCalled_thenReturn404() throws Exception {
        Long id = 1L;

        when(bookRepository.findById(id))
            .thenReturn(Optional.empty());

        mockMvc.perform(get(baseUrl + id)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenNonExistingBook_whenDeleteIsCalled_thenDeletedMustReturn404()
        throws Exception {
        when(bookRepository.findById(1L))
            .thenReturn(Optional.empty());

        mockMvc.perform(delete(baseUrl + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenExistingBook_thenDeletedMustReturnOk() throws Exception {
        when(bookRepository.findById(1L))
            .thenReturn(Optional.of(testBook));

        doNothing().when(bookRepository).delete(testBook);

        mockMvc.perform(delete(baseUrl + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void givenAnIncorrectBookId_whenUpdateIsCalled_thenResponseMustBeBadRequest()
        throws Exception {
        mockMvc.perform(put(baseUrl + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.toStringJson(testBook)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNonExistingBook_thenUpdateMustReturnNotFound() throws Exception {
        when(bookRepository.findById(testBookWithId.getId()))
            .thenReturn(Optional.empty());

        mockMvc.perform(put(baseUrl + testBookWithId.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtils.toStringJson(testBookWithId)))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenValidBookInput_whenUpdateIsCalled_thenUpdateBook() throws Exception {
        when(bookRepository.findById(testBookWithId.getId()))
            .thenReturn(Optional.of(testBookWithId));

        when(bookRepository.save(testBookWithId))
            .thenReturn(testBookWithId);

        mockMvc.perform(put(baseUrl + testBookWithId.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtils.toStringJson(testBookWithId)))
            .andExpect(status().isOk());
    }

    @Test
    public void givenAIsbnInDB_whenFindByIsbnIsCalled_thenReturnSuccessfulResponse()
        throws Exception {
        String isbn = "an-isbn";

        when(bookRepository.findByIsbn(isbn))
            .thenReturn(Optional.of(testBook));

        mockMvc.perform(get(baseUrl.concat("/isbn/{isbn}"), isbn)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isbn", is(testBook.getIsbn())))
            .andExpect(jsonPath("$.author", is(testBook.getAuthor())))
            .andExpect(jsonPath("$.genre", is(testBook.getGenre())))
            .andExpect(jsonPath("$.image", is(testBook.getImage())))
            .andExpect(jsonPath("$.year", is(testBook.getYear())))
            .andExpect(jsonPath("$.pages", is(testBook.getPages())))
            .andExpect(jsonPath("$.publisher", is(testBook.getPublisher())))
            .andExpect(jsonPath("$.title", is(testBook.getTitle())))
            .andExpect(jsonPath("$.subtitle", is(testBook.getSubtitle())));
    }

    @Test
    public void givenIsbnNoInDB_whenFindByIsbnIsCalledAndServiceResponseIsNotOk_thenSendFailedDependencyResponse()
        throws Exception {
        String isbn = "an-isbn";

        when(bookRepository.findByIsbn(isbn))
            .thenReturn(Optional.empty());

        when(openLibraryService.bookInfo(isbn))
            .thenThrow(new RequestException());

        mockMvc.perform(get(baseUrl.concat("/isbn/{isbn}"), isbn)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isFailedDependency());
    }

    @Test
    public void givenIsbnNoInDB_whenFindByIsbnIsCalledAndServiceResponseIsOkButIncompleteJson_thenSendParseExceptionResponse()
        throws Exception {
        String isbn = "an-isbn";

        when(bookRepository.findByIsbn(isbn))
            .thenReturn(Optional.empty());

        when(openLibraryService.bookInfo(isbn))
            .thenThrow(new ParseBookException());

        mockMvc.perform(get(baseUrl.concat("/isbn/{isbn}"), isbn)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    public void givenIsbnNoInDB_whenFindByIsbnIsCalledAndServiceResponseIsIOException_thenSendParseExceptionResponse()
        throws Exception {
        String isbn = "an-isbn";

        when(bookRepository.findByIsbn(isbn))
            .thenReturn(Optional.empty());

        when(openLibraryService.bookInfo(isbn))
            .thenThrow(new IOException());

        mockMvc.perform(get(baseUrl.concat("/isbn/{isbn}"), isbn)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    public void givenIsbnIsNotInDB_whenFindByIsbnIsCalledAndServiceResponseIsInvalid_thenFailToCreateBook()
        throws Exception {
        String isbn = "an-isbn";

        when(bookRepository.findByIsbn(isbn))
            .thenReturn(Optional.empty());

        when(openLibraryService.bookInfo(isbn)).thenReturn(new BookDto());

        mockMvc.perform(get(baseUrl.concat("/isbn/{isbn}"), isbn)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    public void givenIsbnIsNotInDB_whenFindByIsbnIsCalledAndServiceResponseIsValid_thenCreateBook()
        throws Exception {
        String isbn = "an-isbn";
        BookDto bookDto = new BookDto(isbn, "a title", "a subtitle", Collections.emptyList(),
            "2014", 150, Collections.emptyList(), "an image");

        when(bookRepository.findByIsbn(isbn))
            .thenReturn(Optional.empty());

        when(openLibraryService.bookInfo(isbn))
            .thenReturn(bookDto);

        mockMvc.perform(get(baseUrl.concat("/isbn/{isbn}"), isbn)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isCreated());
    }

    @Test
    public void givenIsbnIsNotInDB_whenFindByIsbnIsCalledAndServiceResponseIsEmpty_thenSend404()
        throws Exception {
        String isbn = "an-isbn";

        when(bookRepository.findByIsbn(isbn))
            .thenReturn(Optional.empty());

        when(openLibraryService.bookInfo(isbn))
            .thenReturn(null);

        mockMvc.perform(get(baseUrl.concat("/isbn/{isbn}"), isbn)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isNotFound());
    }
}