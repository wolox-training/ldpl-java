package wolox.training.controllers;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import wolox.training.TestUtils;
import wolox.training.authentication.UserAndPasswordAuthenticationProvider;
import wolox.training.models.Book;
import wolox.training.models.User;
import wolox.training.repositories.BookRepository;
import wolox.training.repositories.UserRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    private final String OLD_PASSWORD_KEY = "oldPassword";
    private final String NEW_PASSWORD_KEY = "newPassword";
    private final String NEW_PASSWORD_CONFIRMATION_KEY = "newPasswordConfirmation";
    private final String KEY_PAGE = "page";
    private final String KEY_SIZE = "size";
    private final String KEY_SORT = "sort";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserAndPasswordAuthenticationProvider userAndPasswordAuthenticationProvider;

    private String baseUrl = "/api/users/";
    private String selfUserUrl = baseUrl.concat("self");
    private User testUser;
    private User testUserWithId;
    private Book testBook;

    @Before
    public void setup() {
        testUser = new User();
        testUser.setUsername("my username");
        testUser.setName("my name");
        testUser.setBirthDate(LocalDate.now().minusDays(1));

        testUserWithId = TestUtils
            .createUserWithData(1L, "another username", "another name", "password");

        testBook = TestUtils
            .createBookWithData(1L, "an-isbn", "an author", "some image", 100, "a publisher",
                " a title", "a subtitle", 2019);
    }

    @Test
    public void givenValidUserInput_thenCreateNewOne() throws Exception {
        when(userRepository.save(testUser))
            .thenReturn(testUser);

        mockMvc.perform(post(baseUrl)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON)
            .content(TestUtils.toStringJson(testUser)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenInvalidUserInput_thenBadRequestResponse() throws Exception {
        User user = new User(); // Empty user;

        mockMvc.perform(post(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.toStringJson(user)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenNoUsers_thenGetUsersReturnEmptyList() throws Exception {
        String nameSort = "name,asc";
        String page = "0";
        String size = "10";

        Page<User> userPage = new PageImpl<>(Collections.emptyList());

        when(userRepository.findAll(ArgumentMatchers.any()))
            .thenReturn(userPage);

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .param(KEY_SORT, nameSort)
            .param(KEY_PAGE, page)
            .param(KEY_SIZE, size))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isEmpty());

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableArgumentCaptor.capture());
        Pageable pageable = pageableArgumentCaptor.getValue();

        Assertions.assertThat(pageable).isNotNull();
        Assertions.assertThat(pageable.getPageNumber()).isEqualTo(Integer.valueOf(page));
        Assertions.assertThat(pageable.getPageSize()).isEqualTo(Integer.valueOf(size));
        Assertions.assertThat(pageable.getSort()).isNotNull();
        Assertions.assertThat(pageable.getSort().getOrderFor("name")).isNotNull();
        Assertions.assertThat(pageable.getSort().getOrderFor("name").getDirection()).isNotNull();
        Assertions.assertThat(pageable.getSort().getOrderFor("name").getDirection())
            .isEqualTo(Direction.ASC);
    }

    @Test
    @WithMockUser(username = "username")
    public void givenUsersExist_thenGetUsersReturnNonEmptyList() throws Exception {
        String nameSort = "username,desc";
        String page = "0";
        String size = "10";

        List<User> users = Collections.singletonList(testUser);

        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any()))
            .thenReturn(userPage);

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .param(KEY_SORT, nameSort)
            .param(KEY_PAGE, page)
            .param(KEY_SIZE, size))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].name", is(testUser.getName())))
            .andExpect(jsonPath("$.content[0].username", is(testUser.getUsername())))
            .andExpect(jsonPath("$.content[0].birthDate", is(testUser.getBirthDate().toString())));

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableArgumentCaptor.capture());
        Pageable pageable = pageableArgumentCaptor.getValue();

        Assertions.assertThat(pageable).isNotNull();
        Assertions.assertThat(pageable.getPageNumber()).isEqualTo(Integer.valueOf(page));
        Assertions.assertThat(pageable.getPageSize()).isEqualTo(Integer.valueOf(size));
        Assertions.assertThat(pageable.getSort()).isNotNull();
        Assertions.assertThat(pageable.getSort().getOrderFor("username")).isNotNull();
        Assertions.assertThat(pageable.getSort().getOrderFor("username").getDirection())
            .isNotNull();
        Assertions.assertThat(pageable.getSort().getOrderFor("username").getDirection())
            .isEqualTo(Direction.DESC);
    }

    @Test
    @WithMockUser(username = "username")
    public void givenExistingUser_thenItMustBeReturned() throws Exception {
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get(baseUrl + id)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(testUser.getName())));
    }

    @Test
    @WithMockUser(username = "username")
    public void givenNonExistingUser_thenNotFoundMustBeReturned() throws Exception {
        Long id = 1L;

        when(userRepository.findById(id))
            .thenReturn(Optional.empty());

        mockMvc.perform(get(baseUrl + id)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenNonExistingUser_thenDeletedMustReturnNotFound() throws Exception {
        when(userRepository.findById(1L))
            .thenReturn(Optional.empty());

        mockMvc.perform(delete(baseUrl + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenExistingUser_thenDeletedMustReturnOk() throws Exception {
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(testUser));

        doNothing().when(userRepository).delete(testUser);

        mockMvc.perform(delete(baseUrl + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenAnIncorrectUserId_thenUpdateMustReturnConflict() throws Exception {
        mockMvc.perform(put(baseUrl + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.toStringJson(testUser)))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenNonExistingUser_thenUpdateMustReturnNotFound() throws Exception {
        when(userRepository.findById(testUserWithId.getId()))
            .thenReturn(Optional.empty());

        mockMvc.perform(put(baseUrl + testUserWithId.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtils.toStringJson(testUserWithId)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenValidUserInput_thenUpdateUser() throws Exception {
        when(userRepository.findById(testUserWithId.getId()))
            .thenReturn(Optional.of(testUserWithId));

        when(userRepository.save(testUserWithId))
            .thenReturn(testUserWithId);

        mockMvc.perform(put(baseUrl + testUserWithId.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtils.toStringJson(testUserWithId)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenNonExistingBook_whenAddBookIsCalled_then404MustBeReturned() throws Exception {
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(testUserWithId));

        when(bookRepository.findById(1L))
            .thenReturn(Optional.empty());

        mockMvc.perform(put(baseUrl + testUserWithId.getId() + "/books/" + 1))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenNonExistingUser_whenAddBookIsCalled_then404MustBeReturned() throws Exception {
        when(userRepository.findById(1L))
            .thenReturn(Optional.empty());

        when(bookRepository.findById(1L))
            .thenReturn(Optional.of(testBook));

        mockMvc.perform(put(baseUrl + testUserWithId.getId() + "/books/" + 1))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenABook_thenAddIdToUserCollection() throws Exception {
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(testUserWithId));

        when(bookRepository.findById(1L))
            .thenReturn(Optional.of(testBook));

        when(userRepository.save(testUserWithId)).thenReturn(testUser);

        mockMvc.perform(put(baseUrl + testUserWithId.getId() + "/books/" + testBook.getId()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenABookInUserList_whenAddBookIsCalled_thenReturnConflict() throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);
        user.setBooks(Collections.singletonList(testBook));

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
            .thenReturn(Optional.of(testBook));

        mockMvc.perform(put(baseUrl + user.getId() + "/books/" + testBook.getId()))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenNonExistingBook_whenRemoveBookIsCalled_thenReturn404() throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);
        user.setBooks(Collections.singletonList(testBook));

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
            .thenReturn(Optional.empty());

        mockMvc.perform(delete(baseUrl + user.getId() + "/books/" + testBook.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenBookInUserList_whenRemoveBookIsCalled_thenRemoveIt() throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);
        List<Book> books = new ArrayList<>();
        books.add(testBook);
        user.setBooks(books);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
            .thenReturn(Optional.of(testBook));

        mockMvc.perform(delete(baseUrl + user.getId() + "/books/" + testBook.getId()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenABookNotInUserCollection_whenRemoveBookIsCalled_thenReturn404()
        throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
            .thenReturn(Optional.of(testBook));

        mockMvc.perform(delete(baseUrl + user.getId() + "/books/" + testBook.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenEmptyPasswordInput_whenUpdatePasswordIsCalled_thenResponseMustBeBadRequest()
        throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);

        Map<String, String> body = new HashMap<>();

        mockMvc.perform(patch(baseUrl + user.getId() + "/password")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtils.toStringJson(body)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenInvalidNewAndConfirmPasswordsInput_whenUpdatePasswordIsCalled_thenResponseMustBeBadRequest()
        throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);

        Map<String, String> body = new HashMap<>();
        body.put(NEW_PASSWORD_KEY, "newPassword");
        body.put(NEW_PASSWORD_CONFIRMATION_KEY, "newPasswordNo");
        body.put(OLD_PASSWORD_KEY, "oldPassword");

        mockMvc.perform(patch(baseUrl + user.getId() + "/password")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtils.toStringJson(body)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenValidInputButNoUser_whenUpdatePasswordIsCalled_thenResponseMustBeUserNotFound()
        throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);

        when(userRepository.findById(1L))
            .thenReturn(Optional.empty());

        Map<String, String> body = new HashMap<>();
        body.put(NEW_PASSWORD_KEY, "newPassword");
        body.put(NEW_PASSWORD_CONFIRMATION_KEY, "newPassword");
        body.put(OLD_PASSWORD_KEY, "oldPassword");

        mockMvc.perform(patch(baseUrl + user.getId() + "/password")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtils.toStringJson(body)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenInvalidOldPasswordInput_whenUpdatePasswordIsCalled_thenResponseMustBeConflict()
        throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);
        user.setPassword("oldPassword");

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        Map<String, String> body = new HashMap<>();
        body.put(NEW_PASSWORD_KEY, "newPassword");
        body.put(NEW_PASSWORD_CONFIRMATION_KEY, "newPassword");
        body.put(OLD_PASSWORD_KEY, "a password");

        mockMvc.perform(patch(baseUrl + user.getId() + "/password")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtils.toStringJson(body)))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenValidInputAndUserFound_whenUpdatePasswordIsCalled_thenResponseMustBeOk()
        throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);
        user.setPassword("oldPassword");

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        Map<String, String> body = new HashMap<>();
        body.put(NEW_PASSWORD_KEY, "newPassword");
        body.put(NEW_PASSWORD_CONFIRMATION_KEY, "newPassword");
        body.put(OLD_PASSWORD_KEY, "oldPassword");

        mockMvc.perform(patch(baseUrl + user.getId() + "/password")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.name())
            .content(TestUtils.toStringJson(body)))
            .andExpect(status().isOk());
    }

    @Test
    public void givenNoAuthenticatedUser_whenSelfEndpointIsCalled_thenReturn401() throws Exception {
        mockMvc.perform(get(selfUserUrl)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "username")
    public void givenAuthenticatedUser_whenSelfEndpointIsCalled_thenReturnUserInfo()
        throws Exception {
        User user = TestUtils.cloneUser(testUserWithId);
        user.setUsername("username");

        when(userRepository.findFirstByUsername("username"))
            .thenReturn(Optional.of(user));

        mockMvc.perform(get(selfUserUrl)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("name", is(user.getName())))
            .andExpect(jsonPath("birthDate", is(user.getBirthDate().toString())))
            .andExpect(jsonPath("username", is(user.getUsername())))
        ;
    }
}
