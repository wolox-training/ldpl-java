package wolox.training.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserAndPasswordAuthenticationProvider userAndPasswordAuthenticationProvider;

    private String baseUrl = "/api/users/";
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
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(username = "username")
    public void givenUsersExist_thenGetUsersReturnNonEmptyList() throws Exception {
        List<User> users = Collections.singletonList(testUser);

        when(userRepository.findAll())
            .thenReturn(users);

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is(testUser.getName())))
            .andExpect(jsonPath("$[0].username", is(testUser.getUsername())))
            .andExpect(jsonPath("$[0].birthDate", is(testUser.getBirthDate().toString())));
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
}
