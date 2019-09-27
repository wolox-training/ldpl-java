package wolox.training.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.time.LocalDate;
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

    @Test
    public void givenNoUsersInDB_whenFindByBirthDateAndName_thenReturnEmpty() {
        List<User> userList = userRepository
            .findByBirthDateAndName(LocalDate.now().minusYears(2), LocalDate.now().plusYears(2),
                "nam");

        Assertions.assertThat(userList).isEmpty();
    }

    @Test
    public void givenUsersInDB_whenFindByBirthDateAndNameIsCalledWithBothDatesAndName_thenReturnFoundResults() {
        persistUser();

        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now().plusYears(2);

        List<User> userList = userRepository
            .findByBirthDateAndName(startDate, endDate, "nam");

        Assertions
            .assertThat(userList)
            .isNotEmpty();

        User foundUser = userList.get(0);

        Assertions.assertThat(foundUser.getName()).contains("nam");
        Assertions.assertThat(foundUser.getBirthDate()).isBeforeOrEqualTo(endDate);
        Assertions.assertThat(foundUser.getBirthDate()).isAfterOrEqualTo(startDate);
        Assertions.assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    public void givenUsersInDB_whenFindByBirthDateAndNameIsCalledWithOneDate_thenReturnFoundResults() {
        persistUser();

        LocalDate startDate = LocalDate.now().minusYears(2);

        List<User> userList = userRepository
            .findByBirthDateAndName(startDate, null, "nam");

        Assertions
            .assertThat(userList)
            .isNotEmpty();

        User foundUser = userList.get(0);

        Assertions.assertThat(foundUser.getName()).contains("nam");
        Assertions.assertThat(foundUser.getBirthDate()).isAfterOrEqualTo(startDate);
        Assertions.assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    public void givenUsersInDB_whenFindByBirthDateAndNameIsCalledWithPartOfName_thenReturnFoundResults() {
        persistUser();

        List<User> userList = userRepository
            .findByBirthDateAndName(null, null, "nam");

        Assertions
            .assertThat(userList)
            .isNotEmpty();

        User foundUser = userList.get(0);

        Assertions.assertThat(foundUser.getName()).contains("nam");
        Assertions.assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
        Assertions.assertThat(foundUser.getBirthDate()).isEqualTo(testUser.getBirthDate());
    }
}
