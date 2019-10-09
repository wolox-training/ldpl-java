package wolox.training.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import wolox.training.TestUtils;
import wolox.training.models.User;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String userUsername = "some-username";

    private void persistUser() {
        testUser = TestUtils
            .createUserWithData(null, userUsername, "a name", "password");

        TestUtils.persist(testEntityManager, testUser);
    }

    @Test
    public void givenAnUsername_whenFindByUsernameIsCalled_thenReturnUser() {
        persistUser();

        User userFound = userRepository.findFirstByUsername(userUsername)
            .orElse(new User());

        assertThat(userFound.getId(), is(testUser.getId()));
        assertThat(userFound.getUsername(), is(testUser.getUsername()));
        assertThat(userFound.getName(), is(testUser.getName()));
        assertThat(userFound.getBirthDate(), is(testUser.getBirthDate()));
    }

    @Test
    public void givenUnknownUsername_whenFinByUsernameIsCalled_thenReturnNoResults() {
        Assertions.assertThat(userRepository.findFirstByUsername("some username").isPresent())
            .isFalse();
    }

    @Test
    public void givenUsersInDatabase_whenFindAll_thenReturnUserList() {
        persistUser();

        List<User> userList = userRepository.findAll();

        Assertions.assertThat(userList).hasSize(1);
        Assertions.assertThat(userList.get(0)).isEqualTo(testUser);
    }

    @Test
    public void givenNoUsersInDatabase_whenFindAll_thenReturnEmptyList() {
        List<User> userList = userRepository.findAll();

        Assertions.assertThat(userList).isEmpty();
    }

}
