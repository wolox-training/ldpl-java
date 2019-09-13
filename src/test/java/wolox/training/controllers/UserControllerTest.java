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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import wolox.training.TestUtils;
import wolox.training.models.Book;
import wolox.training.models.User;
import wolox.training.repositories.BookRepository;
import wolox.training.repositories.UserRepository;

public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private UserRepository userRepository;

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

        testUserWithId = new User();
        testUserWithId.setId(1L);
        testUserWithId.setUsername("another username");
        testUserWithId.setName("another name");
        testUserWithId.setBirthDate(LocalDate.now().minusYears(1));

        testBook = new Book();
        testBook.setId(1L);
        testBook.setIsbn("an-isbn");
        testBook.setYear("2019");
        testBook.setAuthor("an author");
        testBook.setGenre("a genre");
        testBook.setImage("some image");
        testBook.setPages(100);
        testBook.setPublisher("a publisher");
        testBook.setTitle("a title");
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

        // TODO, how we can check the response has some attributes, the .property & $.property seems not working
    }

    @Test
    public void givenInvalidUserInput_thenBadRequestResponse() throws Exception {
        User user = new User(); // Empty user;

        mockMvc.perform(post(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.toStringJson(user)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoUsers_thenGetUsersReturnEmptyList() throws Exception {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(baseUrl)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
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
    public void givenAnIncorrectUserId_thenUpdateMustReturnConflict() throws Exception {
        mockMvc.perform(put(baseUrl + 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.toStringJson(testUser)))
            .andExpect(status().isConflict());
    }

    @Test
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
    public void givenABook_thenAddIdToUserCollection() throws Exception {
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(testUserWithId));

        when(bookRepository.findById(1L))
            .thenReturn(Optional.of(testBook));

        when(userRepository.save(testUserWithId)).thenReturn(testUser);

        mockMvc.perform(put(baseUrl + testUserWithId.getId() + "/books/" + testBook.getId()))
            .andExpect(status().isOk());
    }


}
