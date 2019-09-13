package wolox.training.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import wolox.training.TestUtils;
import wolox.training.models.User;
import wolox.training.repositories.BookRepository;
import wolox.training.repositories.UserRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private UserRepository userRepository;

    private String baseUrl = "/api/users";
    private String usrString = "{\"username\":\"myusername\",\"name\":\"my full name\",\"birthDate\":\"1992-10-21\"}";

    @Test
    public void givenValidUserInput_thenCreateNewOne() throws Exception {
        User user = new User();
        user.setUsername("my username");
        user.setName("my name");
        user.setBirthDate(LocalDate.now().minusYears(1));

        Mockito.when(userRepository.save(user)).thenReturn(user);

        mockMvc.perform(post(baseUrl)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.toStringJson(user)))
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
        Mockito.when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    public void givenUsersExist_thenGetUsersReturnNonEmptyList() throws Exception {
        User user = new User();
        user.setUsername("my username");
        user.setName("my name");
        user.setBirthDate(LocalDate.now().minusDays(1));

        List<User> users = Collections.singletonList(user);

        Mockito.when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users/")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is(user.getName())))
            .andExpect(jsonPath("$[0].username", is(user.getUsername())))
            .andExpect(jsonPath("$[0].birthDate", is(user.getBirthDate().toString())));
    }

}
