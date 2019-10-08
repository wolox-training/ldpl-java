package wolox.training.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import wolox.training.TestUtils;
import wolox.training.authentication.UserAndPasswordAuthenticationProvider;
import wolox.training.exceptions.ParseBookException;
import wolox.training.exceptions.RequestException;
import wolox.training.models.Book;
import wolox.training.models.dtos.BookDto;
import wolox.training.repositories.BookRepository;
import wolox.training.services.OpenLibraryService;

@RunWith(SpringRunner.class)
@WebMvcTest(BookController.class)
public class BookControllerTest {

    private final String KEY_PAGE = "page";
    private final String KEY_SIZE = "size";
    private final String KEY_SORT = "sort";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private OpenLibraryService openLibraryService;

    @MockBean
    private UserAndPasswordAuthenticationProvider userAndPasswordAuthenticationProvider;

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
    @WithMockUser(username = "username")
    public void givenNoBooks_whenGetBooksIsCalled_thenReturnEmptyList() throws Exception {
        List<Book> books = Collections.emptyList();
        Page<Book> bookPage = new PageImpl<>(books);

        given(bookRepository
            .findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .willReturn(bookPage);

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenBooksExist_whenGetBooksIsCalled_thenReturnNonEmptyList() throws Exception {
        List<Book> books = Collections.singletonList(testBook);
        Page<Book> booksPage = new PageImpl<>(books);

        given(bookRepository
            .findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .willReturn(booksPage);

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content[0].id", is(testBook.getId())))
            .andExpect(jsonPath("$.content[0].isbn", is(testBook.getIsbn())))
            .andExpect(jsonPath("$.content[0].author", is(testBook.getAuthor())))
            .andExpect(jsonPath("$.content[0].genre", is(testBook.getGenre())))
            .andExpect(jsonPath("$.content[0].image", is(testBook.getImage())))
            .andExpect(jsonPath("$.content[0].year", is(testBook.getYear())))
            .andExpect(jsonPath("$.content[0].pages", is(testBook.getPages())))
            .andExpect(jsonPath("$.content[0].publisher", is(testBook.getPublisher())))
            .andExpect(jsonPath("$.content[0].title", is(testBook.getTitle())))
            .andExpect(jsonPath("$.content[0].subtitle", is(testBook.getSubtitle())));
    }

    @Test
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
    public void givenAnIncorrectBookId_whenUpdateIsCalled_thenResponseMustBeBadRequest()
        throws Exception {
        mockMvc.perform(put(baseUrl + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.toStringJson(testBook)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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
    @WithMockUser(username = "username")
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

    @Test
    @WithMockUser(username = "username")
    public void givenBooksExist_whenGetBooksIsCalledPaginated_thenReturnElementsAsExpected()
        throws Exception {
        List<Book> books = new ArrayList<>();

        books.add(
            TestUtils.createBookWithData(2L, "isbn2", "author2", "image2", 2 * 100, "publisher2",
                "title2", "subtitle2", 2002));
        books.add(
            TestUtils.createBookWithData(3L, "isbn3", "author3", "image3", 3 * 100, "publisher3",
                "title3", "subtitle3", 2003));
        books.add(
            TestUtils.createBookWithData(1L, "isbn1", "author1", "image1", 100, "publisher1",
                "title1", "subtitle1", 2001));

        books.sort(Comparator.comparing(Book::getIsbn));

        Page<Book> booksPage = new PageImpl<>(books);

        String page = "0";
        String size = "10";

        PageRequest pageableRequest = PageRequest
            .of(Integer.parseInt(page), Integer.parseInt(size), Sort.by(Order.asc("isbn")));

        given(bookRepository
            .findAll(null, null, null, null, null, null, null, null, null, pageableRequest))
            .willReturn(booksPage);

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .param(KEY_SORT, "isbn,asc")
            .param(KEY_PAGE, page)
            .param(KEY_SIZE, size))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(3)))
            .andExpect(jsonPath("$.content[0].isbn", is("isbn1")))
            .andExpect(jsonPath("$.content[1].isbn", is("isbn2")));

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository)
            .findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                pageableArgumentCaptor.capture());
        Pageable pageable = pageableArgumentCaptor.getValue();

        Assertions.assertThat(pageable).isNotNull();
        Assertions.assertThat(pageable.getPageNumber()).isEqualTo(Integer.valueOf(page));
        Assertions.assertThat(pageable.getPageSize()).isEqualTo(Integer.valueOf(size));
        Assertions.assertThat(pageable.getSort()).isNotNull();
        Assertions.assertThat(pageable.getSort().getOrderFor("isbn")).isNotNull();
        Assertions.assertThat(pageable.getSort().getOrderFor("isbn").getDirection()).isNotNull();
        Assertions.assertThat(pageable.getSort().getOrderFor("isbn").getDirection())
            .isEqualTo(Direction.ASC);
    }
}